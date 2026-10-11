package org.marketplace.view;

import lombok.RequiredArgsConstructor;
import org.marketplace.controller.*;
import org.marketplace.model.User;
import org.marketplace.service.UserService;
import org.marketplace.util.InputUtil;

@RequiredArgsConstructor
public class CustomerView {

    private final UserController users;
    private final ProductController products;
    private final CartController cart;
    private final WishlistController wishlist;
    private final OrderController orders;
    private final PaymentController payments;
    private final ReviewController reviews;

    public void show(User user) {
        UserService.requireRole(user, "CUSTOMER");

        while (true) {
            int choice = InputUtil.menu(
                    "CUSTOMER ACCOUNT", "Logout",
                    "Profile",
                    "Browse products",
                    "Add to cart / replace quantity",
                    "View cart",
                    "Remove cart item",
                    "Add to wishlist",
                    "View wishlist",
                    "Remove wishlist item",
                    "Checkout",
                    "My orders",
                    "Order details",
                    "Payment history",
                    "View product reviews",
                    "Add / update my review"
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
                        products.browse();
                        break;
                    case 3:
                        cart.set(user);
                        break;
                    case 4:
                        cart.list(user);
                        break;
                    case 5:
                        cart.remove(user);
                        break;
                    case 6:
                        wishlist.add(user);
                        break;
                    case 7:
                        wishlist.list(user);
                        break;
                    case 8:
                        wishlist.remove(user);
                        break;
                    case 9:
                        orders.checkout(user);
                        break;
                    case 10:
                        orders.history(user);
                        break;
                    case 11:
                        orders.details(user);
                        break;
                    case 12:
                        payments.history(user);
                        break;
                    case 13:
                        reviews.list();
                        break;
                    case 14:
                        reviews.save(user);
                        break;
                }

            } catch (Exception exception) {
                InputUtil.error(exception);
            }
        }
    }
}