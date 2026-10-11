package org.marketplace.view;

import lombok.RequiredArgsConstructor;
import org.marketplace.controller.*;
import org.marketplace.model.User;
import org.marketplace.service.UserService;
import org.marketplace.util.InputUtil;

@RequiredArgsConstructor
public class SellerView {

    private final UserController users;
    private final CategoryController categories;
    private final ProductController products;
    private final OrderController orders;
    private final ReviewController reviews;
    private final ReportController reports;

    public void show(User user) {
        UserService.requireRole(user, "SELLER");

        while (true) {
            int choice = InputUtil.menu(
                    "SELLER ACCOUNT", "Logout",
                    "Profile",
                    "My products",
                    "Add product",
                    "Update product / stock",
                    "Deactivate product",
                    "Reactivate product",
                    "View categories",
                    "Browse products",
                    "My sales orders",
                    "Order details",
                    "View product reviews",
                    "My sales reports",
                    "Import products",
                    "Export my products",
                    "Export my sales report"
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
                        products.own(user);
                        break;
                    case 3:
                        products.save(user, false);
                        break;
                    case 4:
                        products.save(user, true);
                        break;
                    case 5:
                        products.active(user, false);
                        break;
                    case 6:
                        products.active(user, true);
                        break;
                    case 7:
                        categories.list();
                        break;
                    case 8:
                        products.browse();
                        break;
                    case 9:
                        orders.history(user);
                        break;
                    case 10:
                        orders.details(user);
                        break;
                    case 11:
                        reviews.list();
                        break;
                    case 12:
                        reports.show(user);
                        break;
                    case 13:
                        products.importProducts(user);
                        break;
                    case 14:
                        products.exportProducts(user);
                        break;
                    case 15:
                        reports.export(user);
                        break;
                }

            } catch (Exception exception) {
                InputUtil.error(exception);
            }
        }
    }
}