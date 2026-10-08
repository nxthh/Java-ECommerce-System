package org.marketplace.controller;

import org.marketplace.service.ReportService;

import java.util.List;
import java.util.Map;

public class ReportController {
    private final ReportService reportService;

    public ReportController() {
        this.reportService = new ReportService();
    }

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    public List<Map<String, String>> getRevenueByCategory() {
        return reportService.getRevenueByCategory();
    }

    public List<Map<String, String>> getTopSellingProducts(int limit) {
        return reportService.getTopSellingProducts(limit);
    }

    public List<Map<String, String>> getRevenuePerSeller() {
        return reportService.getRevenuePerSeller();
    }

    public List<Map<String, String>> getDailySales(int days) {
        return reportService.getDailySales(days);
    }
}
