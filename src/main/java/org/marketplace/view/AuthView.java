package org.marketplace.view;

import org.marketplace.util.InputUtil;

public class AuthView {

    public String username() {
        while (true) {
            String username = InputUtil.text("Username: ");

            if (username.matches("[A-Za-z0-9_]{3,50}")) {
                return username;
            }

            InputUtil.message(
                    "Use 3–50 letters, digits or underscores."
            );
        }
    }

    public String registrationPassword() {
        while (true) {
            String password = InputUtil.password("Password: ");

            if (password.length() >= 8) {
                return password;
            }

            InputUtil.message(
                    "Password must contain at least 8 characters."
            );
        }
    }

    public String fullName() {
        return InputUtil.required("Full name: ", 100);
    }

    public String role() {
        int choice = InputUtil.menu(
                "REGISTER AS",
                "Cancel",
                "Customer",
                "Seller"
        );

        switch (choice) {
            case 1:
                return "CUSTOMER";
            case 2:
                return "SELLER";
            default:
                return null;
        }
    }

    public String loginUsername() {
        return InputUtil.text("Username: ");
    }

    public String loginPassword() {
        return InputUtil.password("Password: ");
    }

    public void message(String message) {
        InputUtil.message(message);
    }
}