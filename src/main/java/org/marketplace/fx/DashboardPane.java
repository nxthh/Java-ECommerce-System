package org.marketplace.fx;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import org.marketplace.model.*;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DashboardPane extends BorderPane {

    private final MarketplaceApp app;
    private final FxServices s;
    private final User user;

    private final Label status = new Label("Ready");
    private final Label identity = new Label();
    private final Label title = new Label();
    private final Label summary = new Label();

    private final VBox content = new VBox(16);
    private final FlowPane actions = new FlowPane(10, 10);

    private final TableView<String[]> table = FxUi.table();
    private final TextField search = new TextField();

    private final ComboBox<Choice> category = new ComboBox<>();
    private final ComboBox<String> report = new ComboBox<>();

    private final List<Button> navigation = new ArrayList<>();

    private String page = "HOME";
    private long orderId;
    private long reviewProductId;
    private String reviewProductName;

    private TableData data;
    private int loadSequence;

    private interface Command {
        void run() throws Exception;
    }

    private static class Choice {

        final long id;
        final String name;

        Choice(long id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public DashboardPane(
            MarketplaceApp app,
            FxServices services,
            User user
    ) {
        this.app = app;
        this.s = services;
        this.user = user;

        VBox sidebar = new VBox(7);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(210);

        Label brand = new Label("M / MarketSpace");
        brand.getStyleClass().add("brand");

        Label role = new Label(user.getRole() + " WORKSPACE");
        role.getStyleClass().add("sidebar-caption");

        sidebar.getChildren().addAll(brand, role);

        nav(sidebar, "HOME", "Overview");
        nav(sidebar, "CATALOG", "Browse products");

        if (is("SELLER")) {
            nav(sidebar, "PRODUCTS", "My products");
        }

        nav(sidebar, "CATEGORIES", "Categories");

        if (is("CUSTOMER")) {
            nav(sidebar, "CART", "Shopping cart");
            nav(sidebar, "WISHLIST", "Wishlist");
        }

        if (!is("GUEST")) {
            nav(
                    sidebar,
                    "ORDERS",
                    is("SELLER") ? "Sales orders" : "Orders"
            );
        }

        if (is("ADMIN") || is("CUSTOMER")) {
            nav(sidebar, "PAYMENTS", "Payments");
        }

        nav(sidebar, "REVIEWS", "Reviews");

        if (is("ADMIN") || is("SELLER")) {
            nav(sidebar, "REPORTS", "Sales reports");
        }

        if (is("SELLER")) {
            nav(sidebar, "TRANSFER", "Import / Export");
        }

        if (is("ADMIN")) {
            nav(sidebar, "USERS", "Users");
        }

        if (!is("GUEST")) {
            nav(sidebar, "PROFILE", "My profile");
        }

        Region space = new Region();
        VBox.setVgrow(space, Priority.ALWAYS);

        Button logout = FxUi.button(
                is("GUEST") ? "Login / Register" : "Logout",
                new Runnable() {
                    @Override
                    public void run() {
                        app.showLogin();
                    }
                }
        );

        logout.setMaxWidth(Double.MAX_VALUE);
        sidebar.getChildren().addAll(space, logout);
        setLeft(sidebar);

        identity.setText(
                user.getFullName() + " / " + user.getRole()
        );
        identity.getStyleClass().add("muted");
        title.getStyleClass().add("heading");

        content.setPadding(new Insets(26));
        setCenter(content);

        status.getStyleClass().add("status");
        status.setWrapText(true);
        status.setPadding(new Insets(10, 24, 10, 24));
        setBottom(status);

        search.setPromptText("Search product name...");
        search.setPrefWidth(250);

        category.setPrefWidth(180);

        report.getItems().addAll(
                "Revenue summary",
                "Daily sales",
                "Top products"
        );
        report.getSelectionModel().selectFirst();

        open("HOME");
    }

    private boolean is(String role) {
        return role.equals(user.getRole());
    }

    private Window window() {
        return getScene().getWindow();
    }

    private void nav(
            VBox sidebar,
            final String key,
            String name
    ) {
        Button button = FxUi.button(
                name,
                new Runnable() {
                    @Override
                    public void run() {
                        open(key);
                    }
                }
        );

        button.setUserData(key);
        button.getStyleClass().add("nav-button");
        button.setMaxWidth(Double.MAX_VALUE);

        navigation.add(button);
        sidebar.getChildren().add(button);
    }

    private void action(final String name) {
        Button button = FxUi.button(
                name,
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            handle(name);
                        } catch (RuntimeException exception) {
                            status.setText(
                                    FxUi.errorText(exception)
                            );
                        }
                    }
                }
        );

        actions.getChildren().add(button);
    }

    public void open(String key) {
        page = key;
        status.setText("");

        String active = "DETAILS".equals(key)
                ? "ORDERS"
                : "REVIEWDETAIL".equals(key)
                ? "REVIEWS"
                : key;

        for (Button button : navigation) {
            button.getStyleClass().remove("nav-selected");

            if (active.equals(button.getUserData())) {
                button.getStyleClass().add("nav-selected");
            }
        }

        content.getChildren().clear();
        actions.getChildren().clear();
        table.getItems().clear();
        table.getColumns().clear();

        data = null;
        title.setText(titleFor(key));
        summary.setText("");

        content.getChildren().addAll(identity, title);

        if ("HOME".equals(key)) {
            home();
            return;
        }

        if ("CATALOG".equals(key) || "REVIEWS".equals(key)) {
            category.getItems().setAll(
                    new Choice(0, "All categories")
            );
            category.getSelectionModel().selectFirst();

            actions.getChildren().addAll(search, category);

            action("Search");
            action("View reviews");

            if (is("CUSTOMER") && "CATALOG".equals(key)) {
                action("Add to cart");
                action("Save to wishlist");
            }

        } else if ("PRODUCTS".equals(key)) {
            action("Add product");
            action("Edit product");
            action("Deactivate");
            action("Reactivate");
            action("View reviews");

        } else if ("CATEGORIES".equals(key) && is("ADMIN")) {
            action("Add category");
            action("Rename category");
            action("Delete unused category");

        } else if ("CART".equals(key)) {
            action("Change quantity");
            action("Remove cart item");
            action("Checkout");

        } else if ("WISHLIST".equals(key)) {
            action("Add to cart");
            action("Remove wishlist item");
            action("View reviews");

        } else if ("ORDERS".equals(key)) {
            action("Order details");

        } else if ("DETAILS".equals(key)) {
            action("Back to orders");
            action("View reviews");

        } else if ("REVIEWDETAIL".equals(key)) {
            action("Back to products");

            if (is("CUSTOMER")) {
                action("Write / update review");
            }

        } else if ("PROFILE".equals(key)) {
            action("Edit full name");

        } else if ("REPORTS".equals(key)) {
            Label note = new Label(
                    (is("ADMIN") ? "All sellers" : "Your products only")
                            + " | PAID and COMPLETED orders."
                            + " Export uses the report selected below."
            );

            note.setWrapText(true);
            content.getChildren().add(note);

            actions.getChildren().add(report);
            action("Generate report");
            action("Export report");

        } else if ("TRANSFER".equals(key)) {
            Label help = new Label(
                    "Import columns: category_id, name, price, stock, is_active.\n"
                            + "Imports create new products."
                            + " Repeating an import creates copies."
            );

            help.setWrapText(true);
            content.getChildren().add(help);

            action("Import products");
            action("Export products");
        }

        action("Refresh");

        content.getChildren().addAll(
                actions,
                table,
                summary
        );

        VBox.setVgrow(table, Priority.ALWAYS);

        load();
    }

    private String titleFor(String key) {
        switch (key) {
            case "HOME":
                return "Your marketplace, in one place.";
            case "CATALOG":
                return "Discover products";
            case "PRODUCTS":
                return "Manage your products";
            case "CATEGORIES":
                return "Product categories";
            case "CART":
                return "Your shopping cart";
            case "WISHLIST":
                return "Saved for later";
            case "ORDERS":
                return is("SELLER")
                        ? "Your sales orders"
                        : "Order history";
            case "DETAILS":
                return "Order #" + orderId;
            case "PAYMENTS":
                return "Payment history";
            case "PROFILE":
                return "Your profile";
            case "USERS":
                return "Marketplace users";
            case "TRANSFER":
                return "Product import / export";
            case "REVIEWS":
                return "Choose a product to review";
            case "REVIEWDETAIL":
                return "Reviews: " + reviewProductName;
            default:
                return "Sales & performance";
        }
    }

    private void home() {
        Label hello = new Label(
                "Welcome, " + user.getFullName()
        );
        hello.getStyleClass().add("hero-title");

        String text;

        if (is("CUSTOMER")) {
            text = "Find something you love. Manage your cart, orders and reviews.";
        } else if (is("SELLER")) {
            text = "Keep your catalog up to date and follow your sales.";
        } else if (is("ADMIN")) {
            text = "Manage categories, oversee orders and understand marketplace sales.";
        } else {
            text = "Explore the catalog. Log in when you are ready to shop.";
        }

        Label description = new Label(text);
        description.setWrapText(true);
        description.getStyleClass().add("hero-copy");

        VBox hero = new VBox(12, hello, description);
        hero.getStyleClass().add("hero");

        TilePane cards = new TilePane(16, 16);
        cards.setPrefColumns(2);

        cards.getChildren().add(
                card(
                        "Explore",
                        "Browse active products.",
                        "CATALOG"
                )
        );

        if (is("SELLER")) {
            cards.getChildren().add(
                    card(
                            "Your catalog",
                            "Add products and manage stock.",
                            "PRODUCTS"
                    )
            );
        }

        if (is("CUSTOMER")) {
            cards.getChildren().add(
                    card(
                            "Your cart",
                            "Review items and check out.",
                            "CART"
                    )
            );
        }

        if (is("ADMIN")) {
            cards.getChildren().add(
                    card(
                            "Manage categories",
                            "Organize the marketplace.",
                            "CATEGORIES"
                    )
            );
        }

        cards.getChildren().add(
                card(
                        "Community reviews",
                        "Read ratings and customer feedback.",
                        "REVIEWS"
                )
        );

        if (is("ADMIN") || is("SELLER")) {
            cards.getChildren().add(
                    card(
                            "Sales insights",
                            "Revenue, daily sales and top products.",
                            "REPORTS"
                    )
            );
        } else if (is("CUSTOMER")) {
            cards.getChildren().add(
                    card(
                            "Your orders",
                            "View purchases and order details.",
                            "ORDERS"
                    )
            );
        }

        content.getChildren().addAll(hero, cards);
    }

    private VBox card(
            String heading,
            String text,
            final String destination
    ) {
        Label h = new Label(heading);
        h.getStyleClass().add("card-title");

        Label detail = new Label(text);
        detail.setWrapText(true);

        Button go = FxUi.button(
                "Open →",
                new Runnable() {
                    @Override
                    public void run() {
                        open(destination);
                    }
                }
        );

        go.getStyleClass().add("primary");

        VBox card = new VBox(12, h, detail, go);
        card.setPrefWidth(310);
        card.getStyleClass().add("dashboard-card");

        return card;
    }

    private String reportCode() {
        switch (report.getSelectionModel().getSelectedIndex()) {
            case 1:
                return "DAILY";
            case 2:
                return "TOP";
            default:
                return "SUMMARY";
        }
    }

    private void load() {
        final String target = page;
        final int request = ++loadSequence;
        final String keyword = search.getText();

        final long categoryId = category.getValue() == null
                ? 0
                : category.getValue().id;

        final long requestedOrder = orderId;
        final long product = reviewProductId;

        final String reportType = reportCode();
        final String reportTitle = report.getValue();

        data = null;
        table.getItems().clear();
        summary.setText("Loading...");

        new FxUi.Job<TableData>() {

            private TableData categories;

            @Override
            protected TableData call() throws Exception {
                switch (target) {
                    case "CATALOG":
                    case "REVIEWS":
                        categories = s.categories.list();

                        return s.products.search(
                                keyword,
                                categoryId
                        );

                    case "PRODUCTS":
                    case "TRANSFER":
                        return s.products.own(user);

                    case "CATEGORIES":
                        return s.categories.list();

                    case "CART":
                        return s.cart.list(user);

                    case "WISHLIST":
                        return s.wishlist.list(user);

                    case "ORDERS":
                        return s.orders.history(user);

                    case "DETAILS":
                        return s.orders.details(
                                user,
                                requestedOrder
                        );

                    case "PAYMENTS":
                        return s.payments.history(user);

                    case "PROFILE":
                        return s.users.profile(user);

                    case "REVIEWDETAIL":
                        return s.reviews.list(product);

                    case "REPORTS":
                        return s.reports.generate(
                                user,
                                reportType
                        );

                    case "USERS":
                        List<String[]> rows = new ArrayList<>();

                        for (User u : s.users.users(user)) {
                            rows.add(new String[]{
                                    String.valueOf(u.getId()),
                                    u.getUsername(),
                                    u.getFullName(),
                                    u.getRole()
                            });
                        }

                        return new TableData(
                                new String[]{
                                        "id",
                                        "username",
                                        "full_name",
                                        "role"
                                },
                                rows
                        );

                    default:
                        throw new IllegalArgumentException(
                                "Unknown page."
                        );
                }
            }

            @Override
            protected void onSuccess(TableData result) {
                if (request != loadSequence || !target.equals(page)) {
                    return;
                }

                data = result;
                FxUi.fill(table, result);

                if (categories != null) {
                    category.getItems().clear();

                    Choice chosen = new Choice(
                            0,
                            "All categories"
                    );

                    category.getItems().add(chosen);

                    for (String[] row : categories.getRow()) {
                        Choice c = new Choice(
                                Long.parseLong(row[0]),
                                row[1]
                        );

                        category.getItems().add(c);

                        if (c.id == categoryId) {
                            chosen = c;
                        }
                    }

                    category.getSelectionModel().select(chosen);
                }

                summary.setText(
                        result.getRow().size() + " record(s)"
                );

                if ("CART".equals(target)) {
                    summary.setText(
                            "Cart total: " + cartTotal().toPlainString()
                    );
                }

                if ("REPORTS".equals(target)) {
                    summary.setText(
                            "Showing: " + reportTitle
                                    + " | " + result.getRow().size()
                                    + " row(s)"
                    );
                }

                if ("REVIEWDETAIL".equals(target)
                        && !result.getRow().isEmpty()) {
                    int total = 0;

                    for (String[] row : result.getRow()) {
                        total += Integer.parseInt(
                                value(row, "rating")
                        );
                    }

                    summary.setText(
                            String.format(
                                    "Average: %.1f / 5 | %d review(s)",
                                    (double) total / result.getRow().size(),
                                    result.getRow().size()
                            )
                    );
                }

                status.setText("Up to date");
            }

        }.submit(actions, status);
    }

    private String value(String[] row, String key) {
        if (data == null) {
            throw new IllegalArgumentException(
                    "Refresh this page first."
            );
        }

        for (int i = 0; i < data.getHeader().length; i++) {
            if (key.equalsIgnoreCase(data.getHeader()[i])) {
                return row[i];
            }
        }

        throw new IllegalArgumentException(
                "Column not found: " + key
        );
    }

    private String[] selectedRow() {
        String[] row = table.getSelectionModel().getSelectedItem();

        if (row == null) {
            throw new IllegalArgumentException(
                    "Please select a row first."
            );
        }

        return row;
    }

    private long selectedId() {
        String key = "CART".equals(page) || "DETAILS".equals(page)
                ? "product_id"
                : "id";

        return Long.parseLong(
                value(selectedRow(), key)
        );
    }

    private BigDecimal cartTotal() {
        BigDecimal total = BigDecimal.ZERO;

        if (data != null) {
            for (String[] row : data.getRow()) {
                total = total.add(
                        new BigDecimal(
                                value(row, "subtotal")
                        )
                );
            }
        }

        return total;
    }

    private void change(
            final Command command,
            final String message
    ) {
        new FxUi.Job<Void>() {
            @Override
            protected Void call() throws Exception {
                command.run();
                return null;
            }

            @Override
            protected void onSuccess(Void ignored) {
                identity.setText(
                        user.getFullName() + " / " + user.getRole()
                );

                FxUi.message(window(), "Saved", message);
                load();
            }
        }.submit(this, status);
    }

    private void handle(String action) {
        switch (action) {
            case "Refresh":
            case "Search":
            case "Generate report":
                load();
                break;

            case "Add product":
                productForm(0);
                break;

            case "Edit product":
                productForm(selectedId());
                break;

            case "Add category":
                categoryForm(false);
                break;

            case "Rename category":
                categoryForm(true);
                break;

            case "Delete unused category":
                deleteCategory();
                break;

            case "Deactivate":
                setActive(false);
                break;

            case "Reactivate":
                setActive(true);
                break;

            case "Add to cart":
            case "Change quantity":
                cartForm();
                break;

            case "Save to wishlist":
                final long product = selectedId();

                change(
                        new Command() {
                            @Override
                            public void run() throws Exception {
                                s.wishlist.add(user, product);
                            }
                        },
                        "Product saved to wishlist."
                );
                break;

            case "Remove cart item":
            case "Remove wishlist item":
                removeItem();
                break;

            case "Checkout":
                checkout();
                break;

            case "Order details":
                orderId = selectedId();
                open("DETAILS");
                break;

            case "Back to orders":
                open("ORDERS");
                break;

            case "View reviews":
                reviewProductId = selectedId();
                reviewProductName = value(selectedRow(), "name");
                open("REVIEWDETAIL");
                break;

            case "Back to products":
                open("REVIEWS");
                break;

            case "Write / update review":
                reviewForm();
                break;

            case "Edit full name":
                profileForm();
                break;

            case "Import products":
                importProducts();
                break;

            case "Export products":
                exportFile(false);
                break;

            case "Export report":
                exportFile(true);
                break;

            default:
                throw new IllegalArgumentException(
                        "Unknown action."
                );
        }
    }

    private void productForm(final long id) {
        new FxUi.Job<TableData>() {

            private Product product;

            @Override
            protected TableData call() throws Exception {
                product = id == 0
                        ? new Product()
                        : s.findProduct(id);

                if (id != 0
                        && !user.getId().equals(product.getSellerId())) {
                    throw new IllegalArgumentException(
                            "This product is not yours."
                    );
                }

                return s.categories.list();
            }

            @Override
            protected void onSuccess(TableData categories) {
                if (categories.getRow().isEmpty()) {
                    status.setText(
                            "An administrator must create a category first."
                    );
                    return;
                }

                final Product editing = product;

                FxForm form = new FxForm(
                        window(),
                        id == 0 ? "Add product" : "Edit product"
                );

                final TextField name = form.text(
                        "Product name",
                        id == 0 ? "" : product.getName()
                );

                final ComboBox<Choice> choices = new ComboBox<>();

                for (String[] row : categories.getRow()) {
                    choices.getItems().add(
                            new Choice(
                                    Long.parseLong(row[0]),
                                    row[1]
                            )
                    );
                }

                choices.getSelectionModel().selectFirst();

                for (Choice c : choices.getItems()) {
                    if (Long.valueOf(c.id).equals(product.getCategoryId())) {
                        choices.getSelectionModel().select(c);
                    }
                }

                form.add("Category", choices);

                final TextField price = form.text(
                        "Price",
                        id == 0 ? "" : product.getPrice().toPlainString()
                );

                final TextField stock = form.text(
                        "Available stock",
                        id == 0 ? "0" : String.valueOf(product.getStock())
                );

                if (!form.show(new Runnable() {
                    @Override
                    public void run() {
                        editing.setName(name.getText());
                        editing.setCategoryId(choices.getValue().id);

                        try {
                            editing.setPrice(
                                    new BigDecimal(price.getText().trim())
                            );

                            editing.setStock(
                                    Integer.parseInt(stock.getText().trim())
                            );
                        } catch (NumberFormatException exception) {
                            throw new IllegalArgumentException(
                                    "Enter a valid price and whole-number stock."
                            );
                        }

                        s.products.validate(editing);
                    }
                })) {
                    return;
                }

                change(
                        new Command() {
                            @Override
                            public void run() throws Exception {
                                s.products.save(user, editing);
                            }
                        },
                        "Product saved."
                );
            }

        }.submit(this, status);
    }

    private void categoryForm(boolean edit) {
        final long id = edit ? selectedId() : 0;

        FxForm form = new FxForm(
                window(),
                edit ? "Rename category" : "Add category"
        );

        final TextField name = form.text(
                "Category name",
                edit ? value(selectedRow(), "name") : ""
        );

        if (!form.show(new Runnable() {
            @Override
            public void run() {
                required(name.getText(), 100);
            }
        })) {
            return;
        }

        final String text = name.getText();

        change(
                new Command() {
                    @Override
                    public void run() throws Exception {
                        s.categories.save(user, id, text);
                    }
                },
                "Category saved."
        );
    }

    private void deleteCategory() {
        final long id = selectedId();

        if (!FxUi.confirm(
                window(),
                "Delete this category? It must not be used by any product."
        )) {
            return;
        }

        change(
                new Command() {
                    @Override
                    public void run() throws Exception {
                        s.categories.delete(user, id);
                    }
                },
                "Category deleted."
        );
    }

    private void setActive(final boolean active) {
        final long id = selectedId();

        if (!FxUi.confirm(
                window(),
                (active ? "Reactivate" : "Deactivate")
                        + " this product?"
        )) {
            return;
        }

        change(
                new Command() {
                    @Override
                    public void run() throws Exception {
                        s.products.active(user, id, active);
                    }
                },
                "Product availability updated."
        );
    }

    private void cartForm() {
        final long id = selectedId();
        String[] row = selectedRow();

        int stock = Integer.parseInt(
                value(row, "stock")
        );

        if (stock < 1) {
            throw new IllegalArgumentException(
                    "This product is out of stock."
            );
        }

        int initial = "CART".equals(page)
                ? Math.min(
                stock,
                Integer.parseInt(value(row, "quantity"))
        )
                : 1;

        FxForm form = new FxForm(
                window(),
                "Set cart quantity"
        );

        Spinner<Integer> quantity = new Spinner<>(
                1,
                stock,
                initial
        );

        form.add(
                value(row, "name") + " | Available: " + stock,
                quantity
        );

        form.add(
                "Existing cart quantities are replaced.",
                new Label("Choose the TOTAL quantity you want.")
        );

        if (!form.show(null)) {
            return;
        }

        final int count = quantity.getValue();

        change(
                new Command() {
                    @Override
                    public void run() throws Exception {
                        s.cart.set(user, id, count);
                    }
                },
                "Cart quantity saved."
        );
    }

    private void removeItem() {
        final long id = selectedId();
        final boolean cart = "CART".equals(page);

        if (!FxUi.confirm(window(), "Remove this item?")) {
            return;
        }

        change(
                new Command() {
                    @Override
                    public void run() throws Exception {
                        if (cart) {
                            s.cart.remove(user, id);
                        } else {
                            s.wishlist.remove(user, id);
                        }
                    }
                },
                "Item removed."
        );
    }

    private void checkout() {
        if (data == null || data.getRow().isEmpty()) {
            throw new IllegalArgumentException(
                    "Your cart is empty. Browse products first."
            );
        }

        final BigDecimal total = cartTotal();

        FxForm form = new FxForm(
                window(),
                "Checkout | Total: " + total.toPlainString()
        );

        ComboBox<String> method = new ComboBox<>();
        method.getItems().addAll("CASH", "CARD", "QR");
        method.getSelectionModel().selectFirst();

        form.add("Payment method", method);

        form.add(
                "Confirm your order",
                new Label(
                        "Total amount: " + total.toPlainString()
                )
        );

        if (!form.show(null)) {
            return;
        }

        final String paymentMethod = method.getValue();
        final boolean success = true;

        new FxUi.Job<Long>() {
            @Override
            protected Long call() throws Exception {
                return s.orders.checkout(
                        user,
                        paymentMethod,
                        success,
                        total
                );
            }

            @Override
            protected void onSuccess(Long id) {
                String message;

                if (success) {
                    message = "Order #" + id
                            + " is PAID. Purchased items were removed from your cart.";
                } else {
                    message = "Order #" + id
                            + " is CANCELLED. Payment failed;"
                            + " your cart and stock are unchanged.";
                }

                FxUi.message(
                        window(),
                        "Checkout result",
                        message
                );

                open(success ? "ORDERS" : "CART");
            }
        }.submit(this, status);
    }

    private void reviewForm() {
        if (data == null) {
            throw new IllegalArgumentException(
                    "Refresh the reviews first."
            );
        }

        FxForm form = new FxForm(
                window(),
                "Review: " + reviewProductName
        );

        ComboBox<Integer> rating = new ComboBox<>();
        rating.getItems().addAll(1, 2, 3, 4, 5);
        rating.getSelectionModel().select(Integer.valueOf(5));

        TextArea comment = new TextArea();
        comment.setPrefRowCount(4);
        comment.setWrapText(true);

        for (String[] row : data.getRow()) {
            if (user.getUsername().equals(value(row, "username"))) {
                rating.getSelectionModel().select(
                        Integer.valueOf(value(row, "rating"))
                );

                comment.setText(value(row, "comment"));
                break;
            }
        }

        form.add("Rating (1-5)", rating);
        form.add("Your review", comment);

        form.add(
                "Purchase required",
                new Label(
                        "Your order must be PAID or COMPLETED."
                )
        );

        if (!form.show(null)) {
            return;
        }

        final int stars = rating.getValue();
        final String text = comment.getText().trim();
        final long id = reviewProductId;

        change(
                new Command() {
                    @Override
                    public void run() throws Exception {
                        s.reviews.save(user, id, stars, text);
                    }
                },
                "Your review was saved."
        );
    }

    private void profileForm() {
        FxForm form = new FxForm(
                window(),
                "Update profile"
        );

        final TextField name = form.text(
                "Full name",
                user.getFullName()
        );

        if (!form.show(new Runnable() {
            @Override
            public void run() {
                required(name.getText(), 100);
            }
        })) {
            return;
        }

        final String fullName = name.getText();

        change(
                new Command() {
                    @Override
                    public void run() throws Exception {
                        s.users.updateName(user, fullName);
                    }
                },
                "Profile updated."
        );
    }

    private void required(String text, int max) {
        if (text == null
                || text.trim().isEmpty()
                || text.trim().length() > max) {
            throw new IllegalArgumentException(
                    "Enter between 1 and " + max + " characters."
            );
        }
    }

    private FileChooser chooser(String title) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);

        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(
                        "CSV file",
                        "*.csv"
                ),
                new FileChooser.ExtensionFilter(
                        "Excel workbook",
                        "*.xlsx"
                )
        );

        chooser.setSelectedExtensionFilter(
                chooser.getExtensionFilters().get(0)
        );

        return chooser;
    }

    private void importProducts() {
        File file = chooser("Import products")
                .showOpenDialog(window());

        if (file == null) {
            return;
        }

        if (!FxUi.confirm(
                window(),
                "Import new products from " + file.getName()
                        + "?\nRepeated imports create copies."
                        + " Category IDs must already exist."
        )) {
            return;
        }

        final String path = file.getAbsolutePath();

        new FxUi.Job<Integer>() {
            @Override
            protected Integer call() throws Exception {
                return s.files.importProducts(user, path);
            }

            @Override
            protected void onSuccess(Integer count) {
                FxUi.message(
                        window(),
                        "Import complete",
                        count + " product(s) imported."
                );

                load();
            }
        }.submit(this, status);
    }

    private void exportFile(final boolean salesReport) {
        final String type = reportCode();

        FileChooser chooser = chooser(
                salesReport
                        ? "Export report"
                        : "Export products"
        );

        chooser.setInitialFileName(
                salesReport
                        ? type.toLowerCase(Locale.ROOT) + "-report"
                        : "products"
        );

        File file = chooser.showSaveDialog(window());

        if (file == null) {
            return;
        }

        String name = file.getAbsolutePath();
        String lower = name.toLowerCase(Locale.ROOT);

        if (!lower.endsWith(".csv")
                && !lower.endsWith(".xlsx")) {
            boolean excel = chooser.getSelectedExtensionFilter()
                    .getExtensions()
                    .contains("*.xlsx");

            name += excel ? ".xlsx" : ".csv";
        }

        final String path = name;

        if (new File(path).exists()
                && !FxUi.confirm(
                window(),
                "Replace existing file?\n" + path
        )) {
            return;
        }

        new FxUi.Job<Path>() {
            @Override
            protected Path call() throws Exception {
                if (salesReport) {
                    return s.files.exportReport(
                            user,
                            type,
                            path
                    );
                }

                return s.files.exportProducts(user, path);
            }

            @Override
            protected void onSuccess(Path saved) {
                status.setText("Exported: " + saved);

                FxUi.message(
                        window(),
                        "Export complete",
                        "Saved to:\n" + saved
                );
            }
        }.submit(this, status);
    }
}