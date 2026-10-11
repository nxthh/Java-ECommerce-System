package org.marketplace.service;

import lombok.RequiredArgsConstructor;
import org.marketplace.exception.ValidationException;
import org.marketplace.model.*;
import org.marketplace.repository.CategoryRepository;

import java.sql.SQLException;

@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository repository;

    public TableData list() throws SQLException {
        return repository.findAll();
    }

    public void save(User user, long id, String name)
            throws SQLException {

        UserService.requireRole(user, "ADMIN");
        name = name.trim();

        if (id < 0 || name.isEmpty() || name.length() > 100) {
            throw new ValidationException("Invalid category.");
        }

        UserService.changed(repository.save(id, name));
    }

    public void delete(User user, long id) throws SQLException {
        UserService.requireRole(user, "ADMIN");
        UserService.changed(repository.delete(id));
    }
}