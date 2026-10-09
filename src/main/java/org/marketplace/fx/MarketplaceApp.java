package org.marketplace.fx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.marketplace.model.User;

/**
 * JavaFX Application class for E-Commerce Marketplace.
 */
public class MarketplaceApp extends Application {

    private Stage primaryStage;
    private StackPane root;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        this.root = new StackPane();

        showLogin();

        Scene scene = new Scene(root, 900, 600);
        stage.setTitle("E-Commerce Marketplace");
        stage.setScene(scene);
        stage.show();
    }

    private void showLogin() {
        LoginPane loginPane = new LoginPane(this::showDashboard);
        root.getChildren().setAll(loginPane);
    }

    private void showDashboard(User user) {
        DashboardPane dashboard = new DashboardPane(user, this::showLogin);
        root.getChildren().setAll(dashboard);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
