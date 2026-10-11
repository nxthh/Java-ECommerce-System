package org.marketplace;

import org.marketplace.controller.*;
import org.marketplace.repository.*;
import org.marketplace.repository.impl.*;
import org.marketplace.service.*;
import org.marketplace.view.*;

public class Main {

    void main() {

        UserRepository userRepository =
                new PSQLUserRepository();

        CategoryRepository categoryRepository =
                new PSQLCategoryRepository();

        ProductRepository productRepository =
                new PSQLProductRepository();

        CartRepository cartRepository =
                new PSQLCartRepository();

        WishlistRepository wishlistRepository =
                new PSQLWishlistRepository();

        OrderRepository orderRepository =
                new PSQLOrderRepository();

        PaymentRepository paymentRepository =
                new PSQLPaymentRepository();

        ReviewRepository reviewRepository =
                new PSQLReviewRepository();

        ReportRepository reportRepository =
                new PSQLReportRepository();

        AuthService authService =
                new AuthService(userRepository);

        UserService userService =
                new UserService(userRepository);

        CategoryService categoryService =
                new CategoryService(categoryRepository);

        ProductService productService =
                new ProductService(productRepository);

        CartService cartService =
                new CartService(
                        cartRepository,
                        productRepository,
                        userRepository
                );

        WishlistService wishlistService =
                new WishlistService(wishlistRepository);

        PaymentService paymentService =
                new PaymentService(paymentRepository);

        OrderService orderService =
                new OrderService(
                        orderRepository,
                        cartRepository,
                        productRepository,
                        userRepository,
                        paymentService
                );

        ReviewService reviewService =
                new ReviewService(reviewRepository);

        ReportService reportService =
                new ReportService(reportRepository);

        ImportExportService importExportService =
                new ImportExportService(
                        productRepository,
                        productService,
                        reportService
                );

        AuthView authView = new AuthView();

        AuthController authController =
                new AuthController(authService, authView);

        UserController userController =
                new UserController(userService);

        CategoryController categoryController =
                new CategoryController(categoryService);

        ProductController productController =
                new ProductController(
                        productService,
                        categoryService,
                        importExportService
                );

        CartController cartController =
                new CartController(cartService);

        WishlistController wishlistController =
                new WishlistController(wishlistService);

        OrderController orderController =
                new OrderController(orderService, cartService);

        PaymentController paymentController =
                new PaymentController(paymentService);

        ReviewController reviewController =
                new ReviewController(reviewService);

        ReportController reportController =
                new ReportController(
                        reportService,
                        importExportService
                );

        AdminView adminView = new AdminView(
                userController,
                categoryController,
                productController,
                orderController,
                paymentController,
                reportController
        );

        SellerView sellerView = new SellerView(
                userController,
                categoryController,
                productController,
                orderController,
                reviewController,
                reportController
        );

        CustomerView customerView = new CustomerView(
                userController,
                productController,
                cartController,
                wishlistController,
                orderController,
                paymentController,
                reviewController
        );

        MainMenu mainMenu = new MainMenu(
                authController,
                productController,
                adminView,
                sellerView,
                customerView
        );

        mainMenu.start();
    }
}