package org.marketplace.fx;

import org.marketplace.controller.AuthController;
import org.marketplace.controller.CategoryController;
import org.marketplace.controller.ProductController;
import org.marketplace.controller.UserController;
import org.marketplace.model.User;

/**
 * Service container for JavaFX UI layer.
 */
public class FxServices {
    private static final FxServices INSTANCE = new FxServices();

    private final AuthController authController = new AuthController();
    private final ProductController productController = new ProductController();
    private final CategoryController categoryController = new CategoryController();
    private final UserController userController = new UserController();

    private User currentUser;

    private FxServices() {}

    public static FxServices getInstance() {
        return INSTANCE;
    }

    public AuthController getAuthController() {
        return authController;
    }

    public ProductController getProductController() {
        return productController;
    }

    public CategoryController getCategoryController() {
        return categoryController;
    }

    public UserController getUserController() {
        return userController;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }
}
