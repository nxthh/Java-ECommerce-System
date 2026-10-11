package org.marketplace.repository;

import org.marketplace.model.TableData;
import java.sql.SQLException;

public interface WishlistRepository {
    TableData findByUser(long userId) throws SQLException;

    int add(long userId, long productId) throws SQLException;

    int remove(long userId, long productId) throws SQLException;
}