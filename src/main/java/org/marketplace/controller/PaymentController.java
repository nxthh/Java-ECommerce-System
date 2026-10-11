package org.marketplace.controller;

import lombok.RequiredArgsConstructor;
import org.marketplace.model.User;
import org.marketplace.service.PaymentService;
import org.marketplace.util.InputUtil;

import java.sql.SQLException;

@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService service;

    public void history(User user) throws SQLException {
        InputUtil.table(service.history(user));
    }
}