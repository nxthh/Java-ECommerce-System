package org.marketplace.view;

import org.marketplace.controller.CategoryController;
import org.marketplace.controller.ProductController;
import org.marketplace.exception.AuthorizationException;
import org.marketplace.exception.ValidationException;
import org.marketplace.model.Category;
import org.marketplace.model.Product;
import org.marketplace.model.TableData;
import org.marketplace.model.User;
import org.marketplace.util.InputUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Console view for admin product and category management.
 *
 * <p>Menu options:
 * <ol>
 *   <li>List all products</li>
 *   <li>Delete product (hard delete)</li>
 *   <li>Deactivate product</li>
 *   <li>Reactivate product</li>
 *   <li>List all categories</li>
 *   <li>Add category</li>
 *   <li>Rename category</li>
 *   <li>Delete category</li>
 *   <li>Back</li>
 * </ol>
 * </p>
 */
public class AdminView {

    private final ProductController  productController;
    private final CategoryController categoryController;

    public AdminView() {
        this.productController  = new ProductController();
        this.categoryController = new CategoryController();
    }

    public AdminView(ProductController productController,
                     CategoryController categoryController) {
        this.productController  = productController;
        this.categoryController = categoryController;
    }

    /** Entry point: shows the admin product/category management menu in a loop. */
    public void showProductMenu(User admin) {
        boolean running = true;
        while (running) {
            ConsoleView.printHeader("Admin – Product & Category Management");
            System.out.println("  1. List all products");
            System.out.println("  2. Hard-delete product");
            System.out.println("  3. Deactivate product");
            System.out.println("  4. Reactivate product");
            System.out.println("  ─────────────────────");
            System.out.println("  5. List all categories");
            System.out.println("  6. Add category");
            System.out.println("  7. Rename category");
            System.out.println("  8. Delete category");
            System.out.println("  ─────────────────────");
            System.out.println("  0. Back");

            int choice = InputUtil.readChoice("\nEnter choice: ", 0, 8);
            System.out.println();

            switch (choice) {
                case 1 -> listAllProducts(admin);
                case 2 -> hardDeleteProduct(admin);
                case 3 -> deactivateProduct(admin);
                case 4 -> reactivateProduct(admin);
                case 5 -> listCategories();
                case 6 -> addCategory();
                case 7 -> renameCategory();
                case 8 -> deleteCategory();
                case 0 -> running = false;
            }
        }
    }

    // -----------------------------------------------------------------------
    // Product management
    // -----------------------------------------------------------------------

    private void listAllProducts(User admin) {
        List<Product> products = productController.listAllProducts(admin);
        ConsoleView.printTable(toTableData(products));
        ConsoleView.printInfo("Total: " + products.size() + " product(s) (including inactive).");
    }

    private void hardDeleteProduct(User admin) {
        listAllProducts(admin);
        try {
            long productId = InputUtil.readInt("  Product ID to delete: ");
            if (!InputUtil.readYesNo("  Are you sure you want to permanently delete this product?")) {
                ConsoleView.printInfo("Cancelled.");
                return;
            }
            productController.deleteProduct(admin, productId);
            ConsoleView.printSuccess("Product deleted.");
        } catch (ValidationException | AuthorizationException e) {
            ConsoleView.printError(e.getMessage());
        }
    }

    private void deactivateProduct(User admin) {
        try {
            long productId = InputUtil.readInt("  Product ID to deactivate: ");
            productController.deactivateProduct(admin, productId);
            ConsoleView.printSuccess("Product deactivated.");
        } catch (ValidationException | AuthorizationException e) {
            ConsoleView.printError(e.getMessage());
        }
    }

    private void reactivateProduct(User admin) {
        try {
            long productId = InputUtil.readInt("  Product ID to reactivate: ");
            productController.reactivateProduct(admin, productId);
            ConsoleView.printSuccess("Product reactivated.");
        } catch (ValidationException | AuthorizationException e) {
            ConsoleView.printError(e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Category management
    // -----------------------------------------------------------------------

    private void listCategories() {
        List<Category> categories = categoryController.listAllCategories();
        ConsoleView.printTable(categoryTableData(categories));
        ConsoleView.printInfo("Total: " + categories.size() + " category(ies).");
    }

    private void addCategory() {
        String name = InputUtil.readLine("  New category name: ");
        try {
            Category c = categoryController.createCategory(name);
            ConsoleView.printSuccess("Category created: [" + c.getId() + "] " + c.getName());
        } catch (ValidationException e) {
            ConsoleView.printError(e.getMessage());
        }
    }

    private void renameCategory() {
        listCategories();
        try {
            long   id      = InputUtil.readInt("  Category ID to rename: ");
            String newName = InputUtil.readLine("  New name             : ");
            categoryController.renameCategory(id, newName);
            ConsoleView.printSuccess("Category renamed.");
        } catch (ValidationException e) {
            ConsoleView.printError(e.getMessage());
        }
    }

    private void deleteCategory() {
        listCategories();
        try {
            long id = InputUtil.readInt("  Category ID to delete: ");
            if (!InputUtil.readYesNo("  This will fail if products reference it. Continue?")) {
                ConsoleView.printInfo("Cancelled.");
                return;
            }
            categoryController.deleteCategory(id);
            ConsoleView.printSuccess("Category deleted.");
        } catch (ValidationException e) {
            ConsoleView.printError(e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Table helpers
    // -----------------------------------------------------------------------

    private static TableData toTableData(List<Product> products) {
        List<String> headers = List.of("ID", "Name", "Price", "Stock", "Category", "Seller", "Active");
        List<List<String>> rows = new ArrayList<>();
        for (Product p : products) {
            rows.add(List.of(
                    String.valueOf(p.getId()),
                    p.getName(),
                    "$" + p.getPrice().toPlainString(),
                    String.valueOf(p.getStock()),
                    p.getCategoryName() != null ? p.getCategoryName() : "",
                    p.getSellerUsername() != null ? p.getSellerUsername() : "",
                    p.isActive() ? "✔" : "✘"
            ));
        }
        return new TableData(headers, rows);
    }

    private static TableData categoryTableData(List<Category> categories) {
        List<String> headers = List.of("ID", "Name");
        List<List<String>> rows = new ArrayList<>();
        for (Category c : categories) {
            rows.add(List.of(String.valueOf(c.getId()), c.getName()));
        }
        return new TableData(headers, rows);
    }
}
