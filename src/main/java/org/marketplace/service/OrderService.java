package org.marketplace.service;

import lombok.RequiredArgsConstructor;
import org.marketplace.exception.ValidationException;
import org.marketplace.model.*;
import org.marketplace.repository.*;
import org.marketplace.util.JdbcUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PaymentService paymentService;

    public long checkout(
            User user,
            String method,
            boolean success,
            BigDecimal displayedTotal
    ) throws SQLException {

        UserService.requireRole(user, "CUSTOMER");
        paymentService.validateMethod(method);

        try (Connection connection = JdbcUtil.open()) {
            connection.setAutoCommit(false);

            try {
                userRepository.lock(connection, user.getId());

                List<CartItem> items = cartRepository.findItems(
                        connection, user.getId()
                );

                if (items.isEmpty()) {
                    throw new ValidationException("Your cart is empty.");
                }

                List<Product> products = new ArrayList<>();
                BigDecimal total = BigDecimal.ZERO;

                for (CartItem item : items) {
                    Product product = productRepository.findById(
                            connection, item.getProductId(), true
                    );

                    if (product == null || !product.isActive()
                            || item.getQuantity() <= 0
                            || product.getStock() < item.getQuantity()) {
                        throw new ValidationException(
                                "Unavailable or insufficient stock: product "
                                        + item.getProductId()
                        );
                    }

                    products.add(product);

                    total = total.add(
                            product.getPrice().multiply(
                                    BigDecimal.valueOf(item.getQuantity())
                            )
                    );
                }

                if (total.compareTo(displayedTotal) != 0) {
                    throw new ValidationException(
                            "Cart total changed. Review the cart and try again."
                    );
                }

                if (total.compareTo(
                        new BigDecimal("9999999999.99")) > 0) {
                    throw new ValidationException("Order total is too large.");
                }

                long orderId = orderRepository.create(
                        connection, user.getId(), total
                );

                for (int i = 0; i < items.size(); i++) {
                    CartItem item = items.get(i);
                    Product product = products.get(i);

                    orderRepository.addItem(
                            connection, orderId, product, item.getQuantity()
                    );

                    if (success) {
                        UserService.changed(productRepository.reduceStock(
                                connection,
                                product.getId(),
                                item.getQuantity()
                        ));

                        cartRepository.remove(
                                connection, user.getId(), product.getId()
                        );
                    }
                }

                paymentService.record(
                        connection, orderId, total, method, success
                );

                orderRepository.updateStatus(
                        connection,
                        orderId,
                        success ? "PAID" : "CANCELLED"
                );

                connection.commit();
                return orderId;

            } catch (SQLException | RuntimeException exception) {
                JdbcUtil.rollback(connection, exception);
                throw exception;
            }
        }
    }

    public TableData history(User user) throws SQLException {
        UserService.requireRole(user, "ADMIN", "SELLER", "CUSTOMER");
        return orderRepository.findByUser(user);
    }

    public TableData details(User user, long orderId)
            throws SQLException {

        UserService.requireRole(user, "ADMIN", "SELLER", "CUSTOMER");
        return orderRepository.details(user, orderId);
    }

    public void complete(User user, long orderId) throws SQLException {
        UserService.requireRole(user, "ADMIN");
        UserService.changed(orderRepository.complete(orderId));
    }
}