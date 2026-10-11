package org.marketplace.service;

import lombok.RequiredArgsConstructor;
import org.marketplace.exception.ValidationException;
import org.marketplace.model.*;
import org.marketplace.repository.PaymentRepository;

import java.math.BigDecimal;
import java.sql.*;

@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository repository;

    public void validateMethod(String method) {
        if (!"CASH".equals(method)
                && !"CARD".equals(method)
                && !"QR".equals(method)) {
            throw new ValidationException("Invalid payment method.");
        }
    }

    public void record(
            Connection connection, long orderId,
            BigDecimal amount, String method, boolean success
    ) throws SQLException {

        validateMethod(method);

        if (amount == null || amount.signum() <= 0) {
            throw new ValidationException("Payment amount must be positive.");
        }

        String status = success ? "SUCCESS" : "FAILED";

        repository.save(connection, orderId, amount, method, status);
    }

    public TableData history(User user) throws SQLException {
        UserService.requireRole(user, "ADMIN", "CUSTOMER");
        return repository.findByUser(user);
    }
}