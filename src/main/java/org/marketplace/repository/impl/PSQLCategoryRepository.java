package org.marketplace.repository.impl;

import org.marketplace.model.TableData;
import org.marketplace.repository.CategoryRepository;
import org.marketplace.util.JdbcUtil;

import java.sql.SQLException;

public class PSQLCategoryRepository implements CategoryRepository {

    @Override
    public TableData findAll() throws SQLException {
        return JdbcUtil.query(
                "SELECT id, name FROM categories ORDER BY id"
        );
    }

    @Override
    public int save(long id, String name) throws SQLException {
        if (id == 0) {
            return JdbcUtil.update(
                    "INSERT INTO categories(name) VALUES (?)", name
            );
        }

        return JdbcUtil.update(
                "UPDATE categories SET name = ? WHERE id = ?",
                name, id
        );
    }

    @Override
    public int delete(long id) throws SQLException {
        return JdbcUtil.update(
                "DELETE FROM categories WHERE id = ?", id
        );
    }
}