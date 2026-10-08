package org.marketplace.fx;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.marketplace.model.Product;
import org.marketplace.model.User;

import java.math.BigDecimal;
import java.util.List;

/**
 * Main dashboard pane displaying products with management actions.
 */
public class DashboardPane extends BorderPane {

    private final TableView<Product> table = new TableView<>();

    public DashboardPane(User user, Runnable onLogout) {
        setPadding(new Insets(15));

        // Top bar
        HBox topBar = new HBox(15);
        topBar.setPadding(new Insets(0, 0, 15, 0));
        Label welcomeLabel = new Label("Logged in as: " + user.getFullName() + " (" + user.getRole() + ")");
        welcomeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Button logoutBtn = new Button("Logout");
        logoutBtn.setOnAction(e -> {
            FxServices.getInstance().getAuthController().logout();
            onLogout.run();
        });

        topBar.getChildren().addAll(welcomeLabel, logoutBtn);
        setTop(topBar);

        // Center table
        TableColumn<Product, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Product, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Product, BigDecimal> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn<Product, Integer> stockCol = new TableColumn<>("Stock");
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stock"));

        TableColumn<Product, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("categoryName"));

        TableColumn<Product, Boolean> activeCol = new TableColumn<>("Active");
        activeCol.setCellValueFactory(new PropertyValueFactory<>("active"));

        table.getColumns().addAll(idCol, nameCol, priceCol, stockCol, categoryCol, activeCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        refreshTable(user);
        setCenter(table);

        // Bottom actions
        HBox bottomBar = new HBox(10);
        bottomBar.setPadding(new Insets(15, 0, 0, 0));

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> refreshTable(user));
        bottomBar.getChildren().add(refreshBtn);

        setBottom(bottomBar);
    }

    private void refreshTable(User user) {
        List<Product> products;
        if (user.getRole() == User.Role.SELLER) {
            products = FxServices.getInstance().getProductController().listMyProducts(user);
        } else {
            products = FxServices.getInstance().getProductController().listActiveProducts();
        }
        table.setItems(FXCollections.observableArrayList(products));
    }
}
