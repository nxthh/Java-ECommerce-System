package org.marketplace.fx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import org.marketplace.model.User;

import java.net.URL;

public class MarketplaceApp extends Application {

    private final FxServices services = new FxServices();
    private Scene scene;

    @Override
    public void start(Stage stage) {
        scene = new Scene(
                new StackPane(),
                1240,
                820
        );

        URL css = MarketplaceApp.class.getResource(
                "/org/marketplace/fx/marketplace.css"
        );

        if (css == null) {
            throw new IllegalStateException(
                    "Cannot find marketplace.css. "
                            + "Place it in src/main/resources/org/marketplace/fx/"
            );
        }

        scene.getStylesheets().add(css.toExternalForm());

        System.out.println("Loaded stylesheet: " + css);

        stage.setScene(scene);
        stage.setTitle("MarketSpace | Marketplace");
        stage.setMinWidth(1050);
        stage.setMinHeight(760);

        showLogin();
        stage.show();
    }

    void showLogin() {
        scene.setRoot(
                new LoginPane(this, services)
        );
    }

    void showDashboard(User user) {
        scene.setRoot(
                new DashboardPane(this, services, user)
        );
    }

    void showGuestCatalog() {
        User guest = new User();

        guest.setFullName("Guest");
        guest.setRole("GUEST");

        DashboardPane dashboard = new DashboardPane(
                this, services, guest
        );

        scene.setRoot(dashboard);
        dashboard.open("CATALOG");
    }
}