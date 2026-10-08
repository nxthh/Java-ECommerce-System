package org.marketplace.service;

import org.marketplace.exception.ValidationException;
import org.marketplace.model.Category;
import org.marketplace.model.Product;
import org.marketplace.model.User;
import org.marketplace.repository.CategoryRepository;
import org.marketplace.repository.ProductRepository;
import org.marketplace.repository.impl.PSQLCategoryRepository;
import org.marketplace.repository.impl.PSQLProductRepository;
import org.marketplace.util.CsvUtil;
import org.marketplace.util.ExcelUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Handles bulk CSV / Excel import and export of product data.
 * <p>
 * Expected column order for both formats:
 * <pre>Name | Price | Stock | Category</pre>
 * </p>
 */
public class ImportExportService {

    static final List<String> PRODUCT_HEADERS =
            List.of("Name", "Price", "Stock", "Category");

    private final ProductRepository  productRepo;
    private final CategoryRepository categoryRepo;
    private final ProductService     productService;

    /** Default constructor wires PostgreSQL implementations. */
    public ImportExportService() {
        this.productRepo    = new PSQLProductRepository();
        this.categoryRepo   = new PSQLCategoryRepository();
        this.productService = new ProductService(productRepo, categoryRepo);
    }

    /** Injection constructor for testing. */
    public ImportExportService(ProductRepository productRepo,
                               CategoryRepository categoryRepo,
                               ProductService productService) {
        this.productRepo    = productRepo;
        this.categoryRepo   = categoryRepo;
        this.productService = productService;
    }

    // -----------------------------------------------------------------------
    // Export
    // -----------------------------------------------------------------------

    /**
     * Exports the seller's products to a CSV file.
     *
     * @param actor    the logged-in seller.
     * @param filePath target CSV path.
     */
    public void exportProductsToCsv(User actor, String filePath) {
        List<List<String>> rows = buildExportRows(actor);
        CsvUtil.write(filePath, PRODUCT_HEADERS, rows);
    }

    /**
     * Exports the seller's products to an Excel (.xlsx) file.
     *
     * @param actor    the logged-in seller.
     * @param filePath target Excel path.
     */
    public void exportProductsToExcel(User actor, String filePath) {
        List<List<String>> rows = buildExportRows(actor);
        ExcelUtil.write(filePath, PRODUCT_HEADERS, rows);
    }

    // -----------------------------------------------------------------------
    // Import
    // -----------------------------------------------------------------------

    /**
     * Imports products from a CSV file for the given seller.
     * Rows with validation errors are skipped and reported.
     *
     * @param actor    the logged-in seller.
     * @param filePath source CSV path.
     * @return import result summary.
     */
    public ImportResult importProductsFromCsv(User actor, String filePath) {
        List<List<String>> rows = CsvUtil.read(filePath);
        return processImportRows(actor, rows);
    }

    /**
     * Imports products from an Excel (.xlsx) file for the given seller.
     *
     * @param actor    the logged-in seller.
     * @param filePath source Excel path.
     * @return import result summary.
     */
    public ImportResult importProductsFromExcel(User actor, String filePath) {
        List<List<String>> rows = ExcelUtil.read(filePath);
        return processImportRows(actor, rows);
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private List<List<String>> buildExportRows(User actor) {
        List<Product> products = productRepo.findBySeller(actor.getId());
        List<List<String>> rows = new ArrayList<>();
        for (Product p : products) {
            rows.add(List.of(
                    p.getName(),
                    p.getPrice().toPlainString(),
                    String.valueOf(p.getStock()),
                    p.getCategoryName() != null ? p.getCategoryName() : ""
            ));
        }
        return rows;
    }

    private ImportResult processImportRows(User actor, List<List<String>> rows) {
        int imported = 0;
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            int lineNum = i + 2; // 1-based, account for header

            try {
                if (row.size() < 4) {
                    errors.add("Line " + lineNum + ": expected 4 columns, got " + row.size());
                    continue;
                }

                String       name     = row.get(0).trim();
                BigDecimal   price    = new BigDecimal(row.get(1).trim());
                int          stock    = Integer.parseInt(row.get(2).trim());
                String       catName  = row.get(3).trim();

                Optional<Category> cat = categoryRepo.findByName(catName);
                if (cat.isEmpty()) {
                    errors.add("Line " + lineNum + ": unknown category \"" + catName + "\"");
                    continue;
                }

                productService.createProduct(actor, name, price, stock, cat.get().getId());
                imported++;

            } catch (NumberFormatException e) {
                errors.add("Line " + lineNum + ": invalid number format – " + e.getMessage());
            } catch (ValidationException e) {
                errors.add("Line " + lineNum + ": " + e.getMessage());
            }
        }

        return new ImportResult(imported, errors);
    }

    // -----------------------------------------------------------------------
    // Result record
    // -----------------------------------------------------------------------

    /** Summary returned after a bulk import. */
    public static class ImportResult {
        public final int          imported;
        public final List<String> errors;

        public ImportResult(int imported, List<String> errors) {
            this.imported = imported;
            this.errors   = errors;
        }

        @Override
        public String toString() {
            return "Imported: " + imported + " product(s). Errors: " + errors.size();
        }
    }
}
