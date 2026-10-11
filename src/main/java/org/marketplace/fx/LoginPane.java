package org.marketplace.fx;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import org.marketplace.model.User;

public class LoginPane extends HBox {

    private final MarketplaceApp app;
    private final FxServices services;

    private final TextField username = new TextField();
    private final PasswordField password = new PasswordField();
    private final Label status = new Label();

    public LoginPane(
            MarketplaceApp app,
            FxServices services
    ) {
        this.app = app;
        this.services = services;

        Label brand = new Label("M / MarketSpace");
        brand.getStyleClass().add("brand");

        Label slogan = new Label(
                "Discover.\nConnect.\nMake it yours."
        );
        slogan.getStyleClass().add("login-slogan");

        Label copy = new Label(
                "Your marketplace for products,\n"
                        + "people and possibilities."
        );
        copy.getStyleClass().add("hero-copy");

        VBox hero = new VBox(
                30, brand, slogan, copy
        );

        hero.getStyleClass().add("login-hero");
        hero.setAlignment(Pos.CENTER_LEFT);
        hero.setPrefWidth(490);

        Label heading = new Label("Welcome back");
        heading.getStyleClass().add("heading");

        Label hint = new Label(
                "Sign in to your marketplace account."
        );
        hint.getStyleClass().add("muted");

        username.setPromptText("Your username");
        password.setPromptText("Your password");

        Button login = FxUi.button(
                "Sign in",
                new Runnable() {
                    @Override
                    public void run() {
                        login();
                    }
                }
        );

        login.setDefaultButton(true);
        login.setMaxWidth(Double.MAX_VALUE);
        login.getStyleClass().add("primary");

        Button register = FxUi.button(
                "Create an account",
                new Runnable() {
                    @Override
                    public void run() {
                        registration();
                    }
                }
        );

        Button browse = FxUi.button(
                "Browse as guest",
                new Runnable() {
                    @Override
                    public void run() {
                        app.showGuestCatalog();
                    }
                }
        );

        status.setWrapText(true);
        status.getStyleClass().add("status");

        VBox card = new VBox(
                16,
                heading,
                hint,
                new Label("Username"),
                username,
                new Label("Password"),
                password,
                login,
                register,
                browse,
                status
        );

        card.getStyleClass().add("dashboard-card");
        card.setMaxWidth(440);
        card.setMaxHeight(Region.USE_PREF_SIZE);

        StackPane right = new StackPane(card);
        right.setPadding(new Insets(36));

        HBox.setHgrow(right, Priority.ALWAYS);

        getChildren().addAll(hero, right);
    }

    private void login() {
        final String name = username.getText().trim();
        final String secret = password.getText();

        if (name.isEmpty() || secret.isEmpty()) {
            status.setText(
                    "Enter your username and password."
            );
            return;
        }

        new FxUi.Job<User>() {
            @Override
            protected User call() throws Exception {
                return services.auth.login(name, secret);
            }

            @Override
            protected void onSuccess(User user) {
                password.clear();
                app.showDashboard(user);
            }
        }.submit(this, status);
    }

    private void registration() {
        final Dialog<Void> dialog = new Dialog<>();

        dialog.initOwner(getScene().getWindow());
        dialog.setTitle("Create account");

        dialog.getDialogPane()
                .getButtonTypes()
                .add(ButtonType.CANCEL);

        final TextField name = new TextField();
        final TextField fullName = new TextField();
        final PasswordField secret = new PasswordField();

        final ComboBox<String> role = new ComboBox<>();
        role.getItems().addAll("CUSTOMER", "SELLER");
        role.getSelectionModel().selectFirst();

        final Label feedback = new Label();
        feedback.setWrapText(true);

        Button create = FxUi.button(
                "Register",
                new Runnable() {
                    @Override
                    public void run() {
                        final String n = name.getText();
                        final String f = fullName.getText();
                        final String p = secret.getText();
                        final String r = role.getValue();

                        new FxUi.Job<Void>() {
                            @Override
                            protected Void call() throws Exception {
                                services.auth.register(n, p, f, r);
                                return null;
                            }

                            @Override
                            protected void onSuccess(Void ignored) {
                                username.setText(n.trim());
                                password.clear();

                                status.setText(
                                        "Account created. Please sign in."
                                );

                                dialog.close();
                            }
                        }.submit(
                                dialog.getDialogPane(),
                                feedback
                        );
                    }
                }
        );

        VBox form = new VBox(
                10,
                new Label("Username"), name,
                new Label("Full name"), fullName,
                new Label("Password (at least 8 characters)"), secret,
                new Label("Register as"), role,
                create,
                feedback
        );

        form.setPadding(new Insets(20));
        form.setPrefWidth(400);

        dialog.getDialogPane().setContent(form);
        dialog.showAndWait();
    }
}