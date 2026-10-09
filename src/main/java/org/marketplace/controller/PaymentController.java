package org.marketplace.controller;

import org.marketplace.model.Payment;
import org.marketplace.service.PaymentService;

import java.util.List;
import java.util.Optional;

public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController() {
        this.paymentService = new PaymentService();
    }

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    public List<Payment> getPaymentsForOrder(long orderId) {
        return paymentService.getPaymentsForOrder(orderId);
    }

    public Optional<Payment> getPaymentById(long id) {
        return paymentService.getPaymentById(id);
    }

    public Payment processPayment(Payment payment) {
        return paymentService.processPayment(payment);
    }
}
