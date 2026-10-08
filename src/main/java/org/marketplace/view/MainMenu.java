package org.marketplace.view;

import org.marketplace.model.User;

/**
 * Main application router directing logged-in users to their role-specific view.
 */
public class MainMenu {

    private final AuthView authView;
    private final SellerView sellerView;
    private final CustomerView customerView;
    private final AdminView adminView;

    public MainMenu() {
        this.authView = new AuthView();
        this.sellerView = new SellerView();
        this.customerView = new CustomerView();
        this.adminView = new AdminView();
    }

    public void start() {
        while (true) {
            User user = authView.showAuthMenu();
            if (user == null) {
                break; // Exit program
            }

            // Route to appropriate view based on role
            switch (user.getRole()) {
                case SELLER -> sellerView.show(user);
                case CUSTOMER -> customerView.showProductMenu(user);
                case ADMIN -> adminView.showProductMenu(user);
            }
        }
    }
}
