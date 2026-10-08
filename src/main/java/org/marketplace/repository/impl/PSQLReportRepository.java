package org.marketplace.repository.impl;

import org.marketplace.config.DBConfig;
import org.marketplace.exception.DataAccessException;
import org.marketplace.repository.ReportRepository;

import java.sql.*;
import java.util.*;

/**
 * PostgreSQL implementation of {@link ReportRepository}.
 * Executes analytic SQL queries for admin reports.
 */
public class PSQLReportRepository implements ReportRepository {

    private final DBConfig db = DBConfig.getInstance();

    private static final String REVENUE_BY_CATEGORY =
            "SELECT c.name AS category_name, COALESCE(SUM(oi.quantity * oi.unit_price), 0) AS revenue " +
            "FROM   categories c " +
            "LEFT JOIN products p ON p.category_id = c.id " +
            "LEFT JOIN order_items oi ON oi.product_id = p.id " +
            "LEFT JOIN orders o ON o.id = oi.order_id AND o.status = 'PAID' " +
            "GROUP BY c.name ORDER BY revenue DESC";

    private static final String TOP_SELLING =
            "SELECT p.id AS product_id, p.name AS product_name, " +
            "       COALESCE(SUM(oi.quantity), 0) AS total_sold " +
            "FROM   products p " +
            "LEFT JOIN order_items oi ON oi.product_id = p.id " +
            "LEFT JOIN orders o ON o.id = oi.order_id AND o.status IN ('PAID','COMPLETED') " +
            "GROUP BY p.id, p.name ORDER BY total_sold DESC LIMIT ?";

    private static final String REVENUE_PER_SELLER =
            "SELECT u.id AS seller_id, u.username AS seller_username, " +
            "       COALESCE(SUM(oi.quantity * oi.unit_price), 0) AS total_revenue " +
            "FROM   users u " +
            "LEFT JOIN products p ON p.seller_id = u.id " +
            "LEFT JOIN order_items oi ON oi.product_id = p.id " +
            "LEFT JOIN orders o ON o.id = oi.order_id AND o.status = 'PAID' " +
            "WHERE  u.role = 'SELLER' " +
            "GROUP BY u.id, u.username ORDER BY total_revenue DESC";

    private static final String DAILY_SALES =
            "SELECT DATE(created_at) AS sale_date, COUNT(*) AS order_count, " +
            "       COALESCE(SUM(total_amount), 0) AS total_revenue " +
            "FROM   orders " +
            "WHERE  status = 'PAID' AND created_at >= CURRENT_DATE - CAST(? AS INTEGER) * INTERVAL '1 day' " +
            "GROUP BY DATE(created_at) ORDER BY sale_date DESC";

    @Override
    public List<Map<String, String>> revenueByCategory() {
        return runReport(REVENUE_BY_CATEGORY);
    }

    @Override
    public List<Map<String, String>> topSellingProducts(int limit) {
        return runReport(TOP_SELLING, limit);
    }

    @Override
    public List<Map<String, String>> revenuePerSeller() {
        return runReport(REVENUE_PER_SELLER);
    }

    @Override
    public List<Map<String, String>> dailySales(int days) {
        return runReport(DAILY_SALES, days);
    }

    // -----------------------------------------------------------------------

    private List<Map<String, String>> runReport(String sql, Object... params) {
        List<Map<String, String>> result = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = prepareStmt(conn, sql, params);
             ResultSet rs = stmt.executeQuery()) {
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            while (rs.next()) {
                Map<String, String> row = new LinkedHashMap<>();
                for (int i = 1; i <= colCount; i++) {
                    String key = meta.getColumnLabel(i);
                    String val = rs.getString(i);
                    row.put(key, val == null ? "" : val);
                }
                result.add(row);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Report query failed: " + sql, e);
        }
        return result;
    }

    private PreparedStatement prepareStmt(Connection conn, String sql, Object[] params)
            throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
        return stmt;
    }
}
