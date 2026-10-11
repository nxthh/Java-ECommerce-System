package org.marketplace.fx;

import org.marketplace.model.Product;
import org.marketplace.repository.*;
import org.marketplace.repository.impl.*;
import org.marketplace.service.*;
import org.marketplace.util.JdbcUtil;

import java.sql.Connection;
import java.sql.SQLException;

public final class FxServices {

    final UserRepository userRepository =
            new PSQLUserRepository();

    final ProductRepository productRepository =
            new PSQLProductRepository();

    final CartRepository cartRepository =
            new PSQLCartRepository();

    final AuthService auth =
            new AuthService(userRepository);

    final UserService users =
            new UserService(userRepository);

    final CategoryService categories =
            new CategoryService(new PSQLCategoryRepository());

    final ProductService products =
            new ProductService(productRepository);

    final CartService cart = new CartService(
            cartRepository,
            productRepository,
            userRepository
    );

    final WishlistService wishlist =
            new WishlistService(new PSQLWishlistRepository());

    final PaymentService payments =
            new PaymentService(new PSQLPaymentRepository());

    final OrderService orders = new OrderService(
            new PSQLOrderRepository(),
            cartRepository,
            productRepository,
            userRepository,
            payments
    );

    final ReviewService reviews =
            new ReviewService(new PSQLReviewRepository());

    final ReportService reports =
            new ReportService(new PSQLReportRepository());

    final ImportExportService files = new ImportExportService(
            productRepository,
            products,
            reports
    );

    Product findProduct(long id) throws SQLException {
        try (Connection connection = JdbcUtil.open()) {
            Product product = productRepository.findById(
                    connection, id, false
            );

            if (product == null) {
                throw new IllegalArgumentException(
                        "Product not found."
                );
            }

            return product;
        }
    }
}