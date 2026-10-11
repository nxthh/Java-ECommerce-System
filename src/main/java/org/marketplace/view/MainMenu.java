package org.marketplace.view;

import lombok.RequiredArgsConstructor;
import org.marketplace.controller.*;
import org.marketplace.model.User;
import org.marketplace.util.InputUtil;

@RequiredArgsConstructor
public class MainMenu {

    private final AuthController auth;
    private final ProductController products;
    private final AdminView adminView;
    private final SellerView sellerView;
    private final CustomerView customerView;

    public void start() {
        while (true) {
            int choice = InputUtil.menu(
                    "MARKETPLACE", "Exit",
                    "Register", "Login", "Browse products"
            );

            if (choice == 0) {
                InputUtil.message("Goodbye!");
                return;
            }

            try {
                switch (choice) {
                    case 1:
                        auth.register();
                        break;

                    case 2:
                        User user = auth.login();

                        if ("ADMIN".equals(user.getRole())) {
                            adminView.show(user);

                        } else if ("SELLER".equals(user.getRole())) {
                            sellerView.show(user);

                        } else if ("CUSTOMER".equals(user.getRole())) {
                            customerView.show(user);

                        } else {
                            InputUtil.message("Unknown role.");
                        }
                        break;

                    case 3:
                        products.browse();
                        break;
                }

            } catch (Exception exception) {
                InputUtil.error(exception);
            }
        }
    }
}