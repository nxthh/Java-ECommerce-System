package org.marketplace.repository;

import org.marketplace.model.TableData;
import java.sql.SQLException;

public interface ReviewRepository {
    TableData findByProduct(long productId) throws SQLException;

    int save(long userId, long productId, int rating, String comment)
            throws SQLException;
}