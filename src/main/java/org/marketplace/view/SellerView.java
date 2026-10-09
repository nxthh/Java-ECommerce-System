package org.marketplace.view;

import org.marketplace.controller.CategoryController;
import org.marketplace.controller.ProductController;
import org.marketplace.exception.AuthorizationException;
import org.marketplace.exception.ValidationException;
import org.marketplace.model.Category;
import org.marketplace.model.Product;
import org.marketplace.model.TableData;
import org.marketplace.model.User;
import org.marketplace.service.ImportExportService.ImportResult;
import org.marketplace.util.InputUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Console-based seller view for product management.
 *
 * <p>Menu options:
 * <ol>
 *   <li>List my products</li>
 *   <li>Add new product</li>
 *   <li>Edit product</li>
 *   <li>Deactivate product</li>
 *   <li>Reactivate product</li>
 *   <li>Import products from CSV</li>
 *   <li>Import products from Excel</li>
 *   <li>Export products to CSV</li>
 *   <li>Export products to Excel</li>
 *   <li>Back</li>
 * </ol>
 * </p>
 */
public class SellerView {

    private final ProductController  productController;
    private final CategoryController categoryController;

    public SellerView() {
        this.productController  = new ProductController();
        this.categoryController = new CategoryController();
    }

    public SellerView(ProductController productController,
                      CategoryController categoryController) {
        this.productController  = productController;
        this.categoryController = categoryController;
    }

    /** Entry point: shows the seller product-management menu in a loop. */
    public void show(User seller) {
        boolean running = true;
        while (running) {
            ConsoleView.printHeader("Seller – Product Management");
            System.out.println("  1. List my products");
            System.out.println("  2. Add new product");
            System.out.println("  3. Edit product");
            System.out.println("  4. Deactivate product");
            System.out.println("  5. Reactivate product");
            System.out.println("  6. Import from CSV");
            System.out.println("  7. Import from Excel");
            System.out.println("  8. Export to CSV");
            System.out.println("  9. Export to Excel");
            System.out.println("  0. Back");

            int choice = InputUtil.readChoice("\nEnter choice: ", 0, 9);
            System.out.println();

            switch (choice) {
                case 1 -> listMyProducts(seller);
                case 2 -> addProduct(seller);
                case 3 -> editProduct(seller);
                case 4 -> deactivateProduct(seller);
                case 5 -> reactivateProduct(seller);
                case 6 -> importCsv(seller);
                case 7 -> importExcel(seller);
                case 8 -> exportCsv(seller);
                case 9 -> exportExcel(seller);
                case 0 -> running = false;
            }
        }
    }

    // -----------------------------------------------------------------------

    private void listMyProducts(User seller) {
        List<Product> products = productController.listMyProducts(seller);
        ConsoleView.printTable(toTableData(products));
        ConsoleView.printInfo("Total: " + products.size() + " product(s).");
    }

    private void addProduct(User seller) {
        ConsoleView.printHeader("Add New Product");
        try {
            String     name       = InputUtil.readLine("  Name        : ");
            BigDecimal price      = BigDecimal.valueOf(InputUtil.readDouble("  Price       : "));
            int        stock      = InputUtil.readInt("  Stock       : ");
            long       categoryId = pickCategory();

            Product p = productController.createProduct(seller, name, price, stock, categoryId);
            ConsoleView.printSuccess("Product created successfully! (id=" + p.getId() + ")");
        } catch (ValidationException | AuthorizationException e) {
            ConsoleView.printError(e.getMessage());
        }
    }

    private void editProduct(User seller) {
        ConsoleView.printHeader("Edit Product");
        listMyProducts(seller);
        try {
            long productId = InputUtil.readInt("  Product ID to edit: ");
            Product existing = productController.getProduct(productId);
            System.out.println("  Editing: " + existing.getName()
                    + " | Price: " + existing.getPrice()
                    + " | Stock: " + existing.getStock()
                    + " | Category: " + existing.getCategoryName());
            System.out.println("  (Press ENTER to keep current value)");

            String name = InputUtil.readLine("  New name [" + existing.getName() + "]: ");
            if (name.isBlank()) name = existing.getName();

            String priceStr = InputUtil.readLine("  New price [" + existing.getPrice() + "]: ");
            BigDecimal price = priceStr.isBlank()
                    ? existing.getPrice()
                    : new BigDecimal(priceStr.trim());

            String stockStr = InputUtil.readLine("  New stock [" + existing.getStock() + "]: ");
            int stock = stockStr.isBlank() ? existing.getStock() : Integer.parseInt(stockStr.trim());

            String catStr = InputUtil.readLine("  New category id [" + existing.getCategoryId() + "]: ");
            long categoryId = catStr.isBlank() ? existing.getCategoryId() : Long.parseLong(catStr.trim());

            productController.updateProduct(seller, productId, name, price, stock, categoryId);
            ConsoleView.printSuccess("Product updated successfully.");
        } catch (ValidationException | AuthorizationException e) {
            ConsoleView.printError(e.getMessage());
        } catch (NumberFormatException e) {
            ConsoleView.printError("Invalid number: " + e.getMessage());
        }
    }

    private void deactivateProduct(User seller) {
        ConsoleView.printHeader("Deactivate Product");
        listMyProducts(seller);
        try {
            long productId = InputUtil.readInt("  Product ID to deactivate: ");
            productController.deactivateProduct(seller, productId);
            ConsoleView.printSuccess("Product deactivated.");
        } catch (ValidationException | AuthorizationException e) {
            ConsoleView.printError(e.getMessage());
        }
    }

    private void reactivateProduct(User seller) {
        ConsoleView.printHeader("Reactivate Product");
        listMyProducts(seller);
        try {
            long productId = InputUtil.readInt("  Product ID to reactivate: ");
            productController.reactivateProduct(seller, productId);
            ConsoleView.printSuccess("Product reactivated.");
        } catch (ValidationException | AuthorizationException e) {
            ConsoleView.printError(e.getMessage());
        }
    }

    private void importCsv(User seller) {
        String path = InputUtil.readLine("  CSV file path: ");
        try {
            ImportResult result = productController.importFromCsv(seller, path);
            ConsoleView.printSuccess(result.toString());
            result.errors.forEach(ConsoleView::printError);
        } catch (Exception e) {
            ConsoleView.printError(e.getMessage());
        }
    }

    private void importExcel(User seller) {
        String path = InputUtil.readLine("  Excel file path: ");
        try {
            ImportResult result = productController.importFromExcel(seller, path);
            ConsoleView.printSuccess(result.toString());
            result.errors.forEach(ConsoleView::printError);
        } catch (Exception e) {
            ConsoleView.printError(e.getMessage());
        }
    }

    private void exportCsv(User seller) {
        String path = InputUtil.readLine("  Save CSV to: ");
        try {
            productController.exportToCsv(seller, path);
            ConsoleView.printSuccess("Exported to " + path);
        } catch (Exception e) {
            ConsoleView.printError(e.getMessage());
        }
    }

    private void exportExcel(User seller) {
        String path = InputUtil.readLine("  Save Excel to: ");
        try {
            productController.exportToExcel(seller, path);
            ConsoleView.printSuccess("Exported to " + path);
        } catch (Exception e) {
            ConsoleView.printError(e.getMessage());
        }
    }

    /** Displays available categories and returns the selected category id. */
    private long pickCategory() {
        List<Category> categories = categoryController.listAllCategories();
        System.out.println("  Available categories:");
        for (Category c : categories) {
            System.out.printf("    [%d] %s%n", c.getId(), c.getName());
        }
        return InputUtil.readInt("  Category ID : ");
    }

    // -----------------------------------------------------------------------
    // Table helpers
    // -----------------------------------------------------------------------

    static TableData toTableData(List<Product> products) {
        List<String> headers = List.of("ID", "Name", "Price", "Stock", "Category", "Active");
        List<List<String>> rows = new ArrayList<>();
        for (Product p : products) {
            rows.add(List.of(
                    String.valueOf(p.getId()),
                    p.getName(),
                    p.getPrice().toPlainString(),
                    String.valueOf(p.getStock()),
                    p.getCategoryName() != null ? p.getCategoryName() : "",
                    p.isActive() ? "Yes" : "No"
            ));
        }
        return new TableData(headers, rows);
    }
}
