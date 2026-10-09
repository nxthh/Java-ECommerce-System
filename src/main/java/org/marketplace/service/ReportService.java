package org.marketplace.service;

import org.marketplace.repository.ReportRepository;
import org.marketplace.repository.impl.PSQLReportRepository;

import java.util.List;
import java.util.Map;

public class ReportService {
    private final ReportRepository reportRepo;

    public ReportService() {
        this(new PSQLReportRepository());
    }

    public ReportService(ReportRepository reportRepo) {
        this.reportRepo = reportRepo;
    }

    public List<Map<String, String>> getRevenueByCategory() {
        return reportRepo.revenueByCategory();
    }

    public List<Map<String, String>> getTopSellingProducts(int limit) {
        return reportRepo.topSellingProducts(limit);
    }

    public List<Map<String, String>> getRevenuePerSeller() {
        return reportRepo.revenuePerSeller();
    }

    public List<Map<String, String>> getDailySales(int days) {
        return reportRepo.dailySales(days);
    }
}
