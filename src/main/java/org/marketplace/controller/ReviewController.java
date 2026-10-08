package org.marketplace.controller;

import org.marketplace.model.Review;
import org.marketplace.service.ReviewService;

import java.util.List;
import java.util.Optional;

public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController() {
        this.reviewService = new ReviewService();
    }

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    public List<Review> getReviewsForProduct(long productId) {
        return reviewService.getReviewsForProduct(productId);
    }

    public List<Review> getReviewsByUser(long userId) {
        return reviewService.getReviewsByUser(userId);
    }

    public Optional<Review> getReview(long id) {
        return reviewService.getReview(id);
    }

    public Review addReview(Review review) {
        return reviewService.addReview(review);
    }

    public boolean updateReview(Review review) {
        return reviewService.updateReview(review);
    }

    public boolean deleteReview(long id) {
        return reviewService.deleteReview(id);
    }
}
