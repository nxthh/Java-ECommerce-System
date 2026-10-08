package org.marketplace.fx;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.marketplace.model.User;

import java.util.function.Consumer;

/**
 * Login and registration pane for JavaFX UI.
 */
public class LoginPane extends VBox {

    public LoginPane(Consumer<User> onLoginSuccess) {
        setAlignment(Pos.CENTER);
        setSpacing(20);
        setPadding(new Insets(30));

        Label title = FxForm.createHeaderLabel("Marketplace Login");

        GridPane form = FxForm.createGrid();

        Label userLabel = new Label("Username:");
        TextField userField = new TextField();
        Label passLabel = new Label("Password:");
        PasswordField passField = new PasswordField();

        form.add(userLabel, 0, 0);
        form.add(userField, 1, 0);
        form.add(passLabel, 0, 1);
        form.add(passField, 1, 1);

        Button loginBtn = new Button("Sign In");
        loginBtn.setStyle("-fx-background-color: #2b78e4; -fx-text-fill: white; -fx-font-weight: bold;");

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: red;");

        loginBtn.setOnAction(e -> {
            String u = userField.getText().trim();
            String p = passField.getText();
            try {
                User user = FxServices.getInstance().getAuthController().login(u, p);
                FxServices.getInstance().setCurrentUser(user);
                onLoginSuccess.accept(user);
            } catch (Exception ex) {
                statusLabel.setText(ex.getMessage());
            }
        });

        getChildren().addAll(title, form, loginBtn, statusLabel);
    }
}
