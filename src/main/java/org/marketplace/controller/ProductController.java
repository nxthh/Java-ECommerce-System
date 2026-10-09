package org.marketplace.controller;

import org.marketplace.model.Product;
import org.marketplace.model.User;
import org.marketplace.service.CategoryService;
import org.marketplace.service.ImportExportService;
import org.marketplace.service.ImportExportService.ImportResult;
import org.marketplace.service.ProductService;

import java.math.BigDecimal;
import java.util.List;

/**
 * Thin controller that mediates between the view layer and the
 * {@link ProductService}, {@link CategoryService}, and {@link ImportExportService}.
 *
 * <p>No business logic lives here – validation and authorization are in the
 * service layer. Controllers only delegate calls and return results.</p>
 */
public class ProductController {

    private final ProductService      productService;
    private final CategoryService     categoryService;
    private final ImportExportService importExportService;

    /** Default constructor wires default service implementations. */
    public ProductController() {
        this.productService      = new ProductService();
        this.categoryService     = new CategoryService();
        this.importExportService = new ImportExportService();
    }

    /** Injection constructor for testing. */
    public ProductController(ProductService productService,
                             CategoryService categoryService,
                             ImportExportService importExportService) {
        this.productService      = productService;
        this.categoryService     = categoryService;
        this.importExportService = importExportService;
    }

    // -----------------------------------------------------------------------
    // Browse / search
    // -----------------------------------------------------------------------

    /** @return all active products for customer/guest browsing. */
    public List<Product> listActiveProducts() {
        return productService.getAllActiveProducts();
    }

    /** @return active products matching the search keyword. */
    public List<Product> searchProducts(String keyword) {
        return productService.searchProducts(keyword);
    }

    /** @return active products in the given category. */
    public List<Product> listByCategory(long categoryId) {
        return productService.getProductsByCategory(categoryId);
    }

    /** @return a single product by id. */
    public Product getProduct(long productId) {
        return productService.getProductById(productId);
    }

    // -----------------------------------------------------------------------
    // Seller operations
    // -----------------------------------------------------------------------

    /** @return all products (active and inactive) owned by the logged-in seller. */
    public List<Product> listMyProducts(User seller) {
        return productService.getSellerProducts(seller.getId());
    }

    /**
     * Creates a new product for the logged-in seller.
     *
     * @return the persisted product with its generated id.
     */
    public Product createProduct(User seller, String name, BigDecimal price,
                                 int stock, long categoryId) {
        return productService.createProduct(seller, name, price, stock, categoryId);
    }

    /** Updates an existing product owned by the logged-in seller. */
    public void updateProduct(User seller, long productId, String name,
                              BigDecimal price, int stock, long categoryId) {
        productService.updateProduct(seller, productId, name, price, stock, categoryId);
    }

    /** Soft-deletes (deactivates) a product owned by the logged-in seller. */
    public void deactivateProduct(User seller, long productId) {
        productService.deactivateProduct(seller, productId);
    }

    /** Re-activates a previously deactivated product. */
    public void reactivateProduct(User seller, long productId) {
        productService.reactivateProduct(seller, productId);
    }

    // -----------------------------------------------------------------------
    // Import / Export
    // -----------------------------------------------------------------------

    /**
     * Imports products from a CSV file.
     *
     * @return import result with count and any per-row error messages.
     */
    public ImportResult importFromCsv(User seller, String filePath) {
        return importExportService.importProductsFromCsv(seller, filePath);
    }

    /**
     * Imports products from an Excel (.xlsx) file.
     *
     * @return import result with count and any per-row error messages.
     */
    public ImportResult importFromExcel(User seller, String filePath) {
        return importExportService.importProductsFromExcel(seller, filePath);
    }

    /** Exports the seller's products to a CSV file. */
    public void exportToCsv(User seller, String filePath) {
        importExportService.exportProductsToCsv(seller, filePath);
    }

    /** Exports the seller's products to an Excel (.xlsx) file. */
    public void exportToExcel(User seller, String filePath) {
        importExportService.exportProductsToExcel(seller, filePath);
    }

    // -----------------------------------------------------------------------
    // Admin operations
    // -----------------------------------------------------------------------

    /** Returns every product (active and inactive). ADMIN only. */
    public List<Product> listAllProducts(User admin) {
        return productService.getAllProducts(admin);
    }

    /** Hard-deletes a product. ADMIN only. */
    public void deleteProduct(User admin, long productId) {
        productService.deleteProduct(admin, productId);
    }
}
