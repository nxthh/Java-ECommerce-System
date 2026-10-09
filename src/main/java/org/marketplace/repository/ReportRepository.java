package org.marketplace.repository;

import org.marketplace.model.*;
import java.sql.SQLException;

public interface ReportRepository {
    TableData generate(User user, String type) throws SQLException;
}