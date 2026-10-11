package org.marketplace.controller;

import lombok.RequiredArgsConstructor;
import org.marketplace.model.User;
import org.marketplace.service.CategoryService;
import org.marketplace.util.InputUtil;

import java.sql.SQLException;

@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService service;

    public void list() throws SQLException {
        InputUtil.table(service.list());
    }

    public void add(User user) throws SQLException {
        service.save(
                user, 0,
                InputUtil.required("Category name: ", 100)
        );

        InputUtil.message("Category added.");
        list();
    }

    public void rename(User user) throws SQLException {
        list();

        service.save(
                user,
                InputUtil.id("Category ID: "),
                InputUtil.required("New category name: ", 100)
        );

        InputUtil.message("Category updated.");
    }

    public void delete(User user) throws SQLException {
        list();
        long id = InputUtil.id("Category ID: ");

        if (InputUtil.yes("Delete this unused category?")) {
            service.delete(user, id);
            InputUtil.message("Category deleted.");
        }
    }
}