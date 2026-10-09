package org.marketplace.service;

import org.marketplace.model.Order;
import org.marketplace.repository.OrderRepository;
import org.marketplace.repository.impl.PSQLOrderRepository;

import java.util.List;
import java.util.Optional;

public class OrderService {
    private final OrderRepository orderRepo;

    public OrderService() {
        this(new PSQLOrderRepository());
    }

    public OrderService(OrderRepository orderRepo) {
        this.orderRepo = orderRepo;
    }

    public List<Order> getCustomerOrders(long customerId) {
        return orderRepo.findByCustomer(customerId);
    }

    public List<Order> getAllOrders() {
        return orderRepo.findAll();
    }

    public Optional<Order> getOrderById(long orderId) {
        return orderRepo.findById(orderId);
    }

    public Order placeOrder(Order order) {
        return orderRepo.save(order);
    }

    public boolean updateStatus(long orderId, Order.Status status) {
        return orderRepo.updateStatus(orderId, status);
    }
}
