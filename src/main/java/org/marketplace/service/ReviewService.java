package org.marketplace.service;

import org.marketplace.model.Review;
import org.marketplace.repository.ReviewRepository;
import org.marketplace.repository.impl.PSQLReviewRepository;

import java.util.List;
import java.util.Optional;

public class ReviewService {
    private final ReviewRepository reviewRepo;

    public ReviewService() {
        this(new PSQLReviewRepository());
    }

    public ReviewService(ReviewRepository reviewRepo) {
        this.reviewRepo = reviewRepo;
    }

    public List<Review> getReviewsForProduct(long productId) {
        return reviewRepo.findByProduct(productId);
    }

    public List<Review> getReviewsByUser(long userId) {
        return reviewRepo.findByUser(userId);
    }

    public Optional<Review> getReview(long id) {
        return reviewRepo.findById(id);
    }

    public Review addReview(Review review) {
        return reviewRepo.save(review);
    }

    public boolean updateReview(Review review) {
        return reviewRepo.update(review);
    }

    public boolean deleteReview(long id) {
        return reviewRepo.delete(id);
    }
}
