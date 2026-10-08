package org.marketplace.controller;

import org.marketplace.model.Order;
import org.marketplace.service.OrderService;

import java.util.List;
import java.util.Optional;

public class OrderController {
    private final OrderService orderService;

    public OrderController() {
        this.orderService = new OrderService();
    }

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    public List<Order> getCustomerOrders(long customerId) {
        return orderService.getCustomerOrders(customerId);
    }

    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    public Optional<Order> getOrderById(long orderId) {
        return orderService.getOrderById(orderId);
    }

    public Order placeOrder(Order order) {
        return orderService.placeOrder(order);
    }

    public boolean updateStatus(long orderId, Order.Status status) {
        return orderService.updateStatus(orderId, status);
    }
}
