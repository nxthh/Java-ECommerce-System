package org.marketplace.view;

import org.marketplace.controller.CategoryController;
import org.marketplace.controller.ProductController;
import org.marketplace.model.Category;
import org.marketplace.model.Product;
import org.marketplace.model.TableData;
import org.marketplace.model.User;
import org.marketplace.util.InputUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Console view exposing product browse/search functionality to customers.
 *
 * <p>Product-management actions (cart, wishlist, orders) are handled by
 * the other team members' views; this class focuses on what belongs to
 * the Product Management responsibility: browsing and searching products.
 * </p>
 *
 * <p>Menu options:
 * <ol>
 *   <li>Browse all products</li>
 *   <li>Search by name</li>
 *   <li>Browse by category</li>
 *   <li>View product details</li>
 *   <li>Back</li>
 * </ol>
 * </p>
 */
public class CustomerView {

    private final ProductController  productController;
    private final CategoryController categoryController;

    public CustomerView() {
        this.productController  = new ProductController();
        this.categoryController = new CategoryController();
    }

    public CustomerView(ProductController productController,
                        CategoryController categoryController) {
        this.productController  = productController;
        this.categoryController = categoryController;
    }

    /** Entry point: shows the product-browsing menu in a loop. */
    public void showProductMenu(User customer) {
        boolean running = true;
        while (running) {
            ConsoleView.printHeader("Browse Products");
            System.out.println("  1. All products");
            System.out.println("  2. Search by name");
            System.out.println("  3. Browse by category");
            System.out.println("  4. View product details");
            System.out.println("  0. Back");

            int choice = InputUtil.readChoice("\nEnter choice: ", 0, 4);
            System.out.println();

            switch (choice) {
                case 1 -> browseAll();
                case 2 -> search();
                case 3 -> browseByCategory();
                case 4 -> viewDetails();
                case 0 -> running = false;
            }
        }
    }

    // -----------------------------------------------------------------------

    private void browseAll() {
        List<Product> products = productController.listActiveProducts();
        ConsoleView.printTable(toTableData(products));
        ConsoleView.printInfo("Total: " + products.size() + " product(s).");
    }

    private void search() {
        String keyword = InputUtil.readLine("  Search keyword: ");
        List<Product> products = productController.searchProducts(keyword);
        ConsoleView.printTable(toTableData(products));
        ConsoleView.printInfo("Found: " + products.size() + " product(s).");
    }

    private void browseByCategory() {
        List<Category> categories = categoryController.listAllCategories();
        System.out.println("  Categories:");
        for (Category c : categories) {
            System.out.printf("    [%d] %s%n", c.getId(), c.getName());
        }
        int categoryId = InputUtil.readInt("  Category ID: ");
        List<Product> products = productController.listByCategory(categoryId);
        ConsoleView.printTable(toTableData(products));
        ConsoleView.printInfo("Found: " + products.size() + " product(s).");
    }

    private void viewDetails() {
        int productId = InputUtil.readInt("  Product ID: ");
        try {
            Product p = productController.getProduct(productId);
            ConsoleView.printHeader("Product Details");
            System.out.printf("  ID       : %d%n",         p.getId());
            System.out.printf("  Name     : %s%n",         p.getName());
            System.out.printf("  Price    : $%s%n",        p.getPrice().toPlainString());
            System.out.printf("  Stock    : %d%n",         p.getStock());
            System.out.printf("  Category : %s%n",         p.getCategoryName());
            System.out.printf("  Seller   : %s%n",         p.getSellerUsername());
            System.out.printf("  Active   : %s%n",         p.isActive() ? "Yes" : "No");
        } catch (Exception e) {
            ConsoleView.printError(e.getMessage());
        }
    }

    // -----------------------------------------------------------------------

    static TableData toTableData(List<Product> products) {
        List<String> headers = List.of("ID", "Name", "Price", "Stock", "Category", "Seller");
        List<List<String>> rows = new ArrayList<>();
        for (Product p : products) {
            rows.add(List.of(
                    String.valueOf(p.getId()),
                    p.getName(),
                    "$" + p.getPrice().toPlainString(),
                    String.valueOf(p.getStock()),
                    p.getCategoryName() != null ? p.getCategoryName() : "",
                    p.getSellerUsername() != null ? p.getSellerUsername() : ""
            ));
        }
        return new TableData(headers, rows);
    }
}
