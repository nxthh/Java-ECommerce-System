package org.marketplace.view;

import lombok.RequiredArgsConstructor;
import org.marketplace.controller.*;
import org.marketplace.model.User;
import org.marketplace.service.UserService;
import org.marketplace.util.InputUtil;

@RequiredArgsConstructor
public class AdminView {

    private final UserController users;
    private final CategoryController categories;
    private final ProductController products;
    private final OrderController orders;
    private final PaymentController payments;
    private final ReportController reports;

    public void show(User user) {
        UserService.requireRole(user, "ADMIN");

        while (true) {
            int choice = InputUtil.menu(
                    "ADMIN ACCOUNT", "Logout",
                    "Profile",
                    "View categories",
                    "Add category",
                    "Rename category",
                    "Delete unused category",
                    "Browse products",
                    "All orders",
                    "Order details",
                    "Complete paid order",
                    "Payment history",
                    "Reports",
                    "Export report",
                    "View users"
            );

            if (choice == 0) {
                return;
            }

            try {
                switch (choice) {
                    case 1:
                        users.profile(user);
                        break;
                    case 2:
                        categories.list();
                        break;
                    case 3:
                        categories.add(user);
                        break;
                    case 4:
                        categories.rename(user);
                        break;
                    case 5:
                        categories.delete(user);
                        break;
                    case 6:
                        products.browse();
                        break;
                    case 7:
                        orders.history(user);
                        break;
                    case 8:
                        orders.details(user);
                        break;
                    case 9:
                        orders.complete(user);
                        break;
                    case 10:
                        payments.history(user);
                        break;
                    case 11:
                        reports.show(user);
                        break;
                    case 12:
                        reports.export(user);
                        break;
                    case 13:
                        users.users(user);
                        break;
                }

            } catch (Exception exception) {
                InputUtil.error(exception);
            }
        }
    }
}