package org.marketplace.repository;

import java.util.List;
import java.util.Map;

/**
 * Data-access contract for admin reports.
 * Returns raw aggregated data as maps (flexible for different report types).
 */
public interface ReportRepository {

    /**
     * Returns total revenue grouped by category.
     * Map key = category name, value = total revenue (BigDecimal as String).
     */
    List<Map<String, String>> revenueByCategory();

    /**
     * Returns the top N best-selling products by total quantity sold.
     * Each map has keys: productId, productName, totalSold.
     */
    List<Map<String, String>> topSellingProducts(int limit);

    /**
     * Returns total revenue grouped by seller.
     * Each map has keys: sellerId, sellerUsername, totalRevenue.
     */
    List<Map<String, String>> revenuePerSeller();

    /**
     * Returns daily order counts and revenue for the given number of past days.
     * Each map has keys: date, orderCount, totalRevenue.
     */
    List<Map<String, String>> dailySales(int days);
}
