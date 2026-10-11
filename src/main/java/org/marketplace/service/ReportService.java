package org.marketplace.service;

import lombok.RequiredArgsConstructor;
import org.marketplace.exception.ValidationException;
import org.marketplace.model.*;
import org.marketplace.repository.ReportRepository;

import java.sql.SQLException;

@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository repository;

    public TableData generate(User user, String type)
            throws SQLException {

        UserService.requireRole(user, "ADMIN", "SELLER");

        if (!"SUMMARY".equals(type)
                && !"DAILY".equals(type)
                && !"TOP".equals(type)) {
            throw new ValidationException("Invalid report type.");
        }

        return repository.generate(user, type);
    }
}