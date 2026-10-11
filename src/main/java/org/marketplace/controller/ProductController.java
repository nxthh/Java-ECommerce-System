package org.marketplace.controller;

import lombok.RequiredArgsConstructor;
import org.marketplace.model.*;
import org.marketplace.service.*;
import org.marketplace.util.InputUtil;

import java.math.BigDecimal;
import java.sql.SQLException;

@RequiredArgsConstructor
public class ProductController {

    private final ProductService service;
    private final CategoryService categoryService;
    private final ImportExportService files;

    public void browse() throws SQLException {
        InputUtil.table(categoryService.list());

        String keyword = InputUtil.text("Search name (Enter for all): ");
        String input = InputUtil.text("Category ID (Enter for all): ");

        long categoryId = 0;

        if (!input.isEmpty()) {
            categoryId = Long.parseLong(input);
        }

        InputUtil.table(service.search(keyword, categoryId));
    }

    public void own(User user) throws SQLException {
        InputUtil.table(service.own(user));
    }

    public void save(User user, boolean update) throws SQLException {
        TableData categories = categoryService.list();

        if (categories.getRow().isEmpty()) {
            InputUtil.message(
                    "No categories available. Ask an admin to create one."
            );
            return;
        }

        Product product = new Product();

        if (update) {
            TableData products = service.own(user);

            if (products.getRow().isEmpty()) {
                InputUtil.message("You have no products to update.");
                return;
            }

            InputUtil.table(products);
            product.setId(InputUtil.id("Product ID to update: "));
        }

        InputUtil.table(categories);

        while (true) {
            String input = InputUtil.text("Category ID (0 to cancel): ");

            long categoryId;

            try {
                categoryId = Long.parseLong(input);
            } catch (NumberFormatException exception) {
                InputUtil.message("Enter a valid category ID.");
                continue;
            }

            if (categoryId == 0) {
                return;
            }

            boolean found = false;

            for (String[] row : categories.getRow()) {
                if (Long.parseLong(row[0]) == categoryId) {
                    found = true;
                    break;
                }
            }

            if (found) {
                product.setCategoryId(categoryId);
                break;
            }

            InputUtil.message("Choose an ID from the category table.");
        }

        product.setName(InputUtil.required("Product name: ", 150));

        while (true) {
            BigDecimal price = InputUtil.money("Price: ");

            if (price.signum() > 0
                    && price.stripTrailingZeros().scale() <= 2
                    && price.compareTo(
                    new BigDecimal("9999999999.99")) <= 0) {
                product.setPrice(price);
                break;
            }

            InputUtil.message("Enter a positive price with up to 2 decimals.");
        }

        while (true) {
            int stock = InputUtil.number("Stock: ");

            if (stock >= 0) {
                product.setStock(stock);
                break;
            }

            InputUtil.message("Stock cannot be negative.");
        }

        if (InputUtil.yes("Save this product?")) {
            service.save(user, product);
            InputUtil.message("Product saved.");
        }
    }

    public void active(User user, boolean active) throws SQLException {
        own(user);
        long id = InputUtil.id("Product ID: ");

        if (InputUtil.yes("Confirm this change?")) {
            service.active(user, id, active);
            InputUtil.message("Product status updated.");
        }
    }

    public void importProducts(User user)
            throws SQLException, java.io.IOException {

        InputUtil.message(
                "Import creates new products. Repeating it creates copies."
        );

        String filename = InputUtil.text(
                "Input CSV/XLSX path (Enter to cancel): "
        );

        if (!filename.isEmpty() && InputUtil.yes("Import products?")) {
            int count = files.importProducts(user, filename);
            InputUtil.message("Imported " + count + " products.");
        }
    }

    public void exportProducts(User user)
            throws SQLException, java.io.IOException {

        String filename = InputUtil.text(
                "Output CSV/XLSX path (Enter to cancel): "
        );

        if (!filename.isEmpty()
                && InputUtil.yes("Write file? Existing file will be replaced.")) {
            InputUtil.message(
                    "Exported to: " + files.exportProducts(user, filename)
            );
        }
    }
}