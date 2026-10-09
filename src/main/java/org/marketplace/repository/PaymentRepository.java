package org.marketplace.repository;

import org.marketplace.model.Payment;

import java.util.List;
import java.util.Optional;

/**
 * Data-access contract for {@link Payment} entities.
 * Implemented by Phanit.
 */
public interface PaymentRepository {

    /** Returns all payments for the given order. */
    List<Payment> findByOrder(long orderId);

    /** Looks up a payment by its primary key. */
    Optional<Payment> findById(long id);

    /**
     * Persists a new payment record.
     *
     * @return the saved payment with its generated id.
     */
    Payment save(Payment payment);
}
