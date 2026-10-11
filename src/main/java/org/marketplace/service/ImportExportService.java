package org.marketplace.service;

import lombok.RequiredArgsConstructor;
import org.marketplace.exception.ValidationException;
import org.marketplace.model.*;
import org.marketplace.repository.ProductRepository;
import org.marketplace.util.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
public class ImportExportService {

    private final ProductRepository productRepository;
    private final ProductService productService;
    private final ReportService reportService;

    private Path outputPath(String filename) {
        Path path = Path.of(filename).toAbsolutePath().normalize();
        String name = path.getFileName().toString();
        String lower = name.toLowerCase(Locale.ROOT);

        boolean csv = lower.endsWith(".csv") && name.length() > 4;
        boolean excel = lower.endsWith(".xlsx") && name.length() > 5;

        if (!csv && !excel) {
            throw new ValidationException(
                    "Use a filename such as products.csv or report.xlsx."
            );
        }

        return path;
    }

    private Path write(String filename, TableData data) throws IOException {
        Path path = outputPath(filename);
        String lower = path.toString().toLowerCase(Locale.ROOT);

        if (lower.endsWith(".csv")) {
            CsvUtil.write(path, data);
        } else {
            ExcelUtil.write(path, data);
        }

        return path;
    }

    public Path exportProducts(User user, String filename)
            throws SQLException, IOException {

        UserService.requireRole(user, "SELLER");

        return write(
                filename,
                productRepository.exportBySeller(user.getId())
        );
    }

    public Path exportReport(User user, String type, String filename)
            throws SQLException, IOException {

        return write(filename, reportService.generate(user, type));
    }

    public int importProducts(User user, String filename)
            throws SQLException, IOException {

        UserService.requireRole(user, "SELLER");

        String lower = filename.toLowerCase(Locale.ROOT);
        TableData data;

        if (lower.endsWith(".csv")) {
            data = CsvUtil.read(Path.of(filename));
        } else if (lower.endsWith(".xlsx")) {
            data = ExcelUtil.read(Path.of(filename));
        } else {
            throw new ValidationException("Choose a CSV or XLSX file.");
        }

        String[] expected = {
                "category_id", "name", "price", "stock", "is_active"
        };

        if (data.getHeader().length != expected.length) {
            throw new ValidationException("Expected exactly 5 columns.");
        }

        for (int i = 0; i < expected.length; i++) {
            if (!expected[i].equals(data.getHeader()[i].trim())) {
                throw new ValidationException(
                        "Expected column " + (i + 1) + ": " + expected[i]
                );
            }
        }

        if (data.getRow().isEmpty()) {
            throw new ValidationException("No products to import.");
        }

        List<Product> products = new ArrayList<>();
        int rowNumber = 1;

        for (String[] row : data.getRow()) {
            rowNumber++;

            try {
                if (row.length != 5) {
                    throw new ValidationException("Expected 5 values.");
                }

                Product product = new Product();
                product.setSellerId(user.getId());
                product.setCategoryId(Long.parseLong(row[0].trim()));
                product.setName(row[1]);
                product.setPrice(new BigDecimal(row[2].trim()));
                product.setStock(Integer.parseInt(row[3].trim()));

                String active = row[4].trim();

                if (!"true".equalsIgnoreCase(active)
                        && !"false".equalsIgnoreCase(active)) {
                    throw new ValidationException(
                            "is_active must be true or false."
                    );
                }

                product.setActive(Boolean.parseBoolean(active));
                productService.validate(product);
                products.add(product);

            } catch (IllegalArgumentException | ValidationException exception) {
                throw new ValidationException(
                        "Row " + rowNumber + ": " + exception.getMessage()
                );
            }
        }

        try (Connection connection = JdbcUtil.open()) {
            connection.setAutoCommit(false);

            try {
                for (Product product : products) {
                    productRepository.save(connection, product);
                }

                connection.commit();
                return products.size();

            } catch (SQLException | RuntimeException exception) {
                JdbcUtil.rollback(connection, exception);
                throw exception;
            }
        }
    }
}