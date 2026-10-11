package org.marketplace.repository.impl;

import org.marketplace.model.*;
import org.marketplace.repository.ReportRepository;
import org.marketplace.util.JdbcUtil;

import java.sql.SQLException;

public class PSQLReportRepository implements ReportRepository {

    @Override
    public TableData generate(User user, String type)
            throws SQLException {

        String columns;
        String grouping;

        if ("DAILY".equals(type)) {
            columns = """
                    CAST(o.created_at AS DATE) AS sale_date,
                    COUNT(DISTINCT o.id) AS orders,
                    SUM(oi.quantity) AS units,
                    SUM(oi.quantity * oi.unit_price) AS revenue
                    """;

            grouping = """
                    GROUP BY CAST(o.created_at AS DATE)
                    ORDER BY sale_date DESC
                    """;

        } else if ("TOP".equals(type)) {
            columns = """
                    p.id, p.name,
                    SUM(oi.quantity) AS units,
                    SUM(oi.quantity * oi.unit_price) AS revenue
                    """;

            grouping = """
                    GROUP BY p.id, p.name
                    ORDER BY units DESC, p.id
                    LIMIT 10
                    """;

        } else {
            columns = """
                    COUNT(DISTINCT o.id) AS orders,
                    COALESCE(SUM(oi.quantity), 0) AS units,
                    COALESCE(SUM(oi.quantity * oi.unit_price), 0) AS revenue
                    """;

            grouping = "";
        }

        String sql = "SELECT " + columns + """
                 FROM orders o
                 JOIN order_items oi ON oi.order_id = o.id
                 JOIN products p ON p.id = oi.product_id
                 WHERE o.status IN ('PAID', 'COMPLETED')
                   AND (? = 'ADMIN' OR p.seller_id = ?)
                """ + grouping;

        return JdbcUtil.query(sql, user.getRole(), user.getId());
    }
}