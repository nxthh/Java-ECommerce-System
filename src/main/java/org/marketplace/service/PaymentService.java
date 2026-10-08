package org.marketplace.service;

import org.marketplace.model.Payment;
import org.marketplace.repository.PaymentRepository;
import org.marketplace.repository.impl.PSQLPaymentRepository;

import java.util.List;
import java.util.Optional;

public class PaymentService {
    private final PaymentRepository paymentRepo;

    public PaymentService() {
        this(new PSQLPaymentRepository());
    }

    public PaymentService(PaymentRepository paymentRepo) {
        this.paymentRepo = paymentRepo;
    }

    public List<Payment> getPaymentsForOrder(long orderId) {
        return paymentRepo.findByOrder(orderId);
    }

    public Optional<Payment> getPaymentById(long id) {
        return paymentRepo.findById(id);
    }

    public Payment processPayment(Payment payment) {
        return paymentRepo.save(payment);
    }
}
