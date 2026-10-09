package org.marketplace.repository;

import org.marketplace.model.Order;

import java.util.List;
import java.util.Optional;

/**
 * Data-access contract for {@link Order} entities.
 * Implemented by Phanit.
 */
public interface OrderRepository {

    /** Returns all orders for the given customer, most recent first. */
    List<Order> findByCustomer(long customerId);

    /** Returns every order in the system (admin view). */
    List<Order> findAll();

    /** Looks up a single order by its primary key, including its items. */
    Optional<Order> findById(long id);

    /**
     * Creates a new order (with items) inside a single transaction.
     *
     * @return the persisted order with its generated id.
     */
    Order save(Order order);

    /**
     * Updates the status of an order.
     *
     * @return {@code true} if a row was updated.
     */
    boolean updateStatus(long orderId, Order.Status status);
}
