package org.marketplace.repository;

import org.marketplace.model.TableData;
import java.sql.SQLException;

public interface CategoryRepository {
    TableData findAll() throws SQLException;

    int save(long id, String name) throws SQLException;

    int delete(long id) throws SQLException;
}