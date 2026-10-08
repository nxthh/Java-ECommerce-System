package org.marketplace.repository;

import org.marketplace.model.Review;

import java.util.List;
import java.util.Optional;

/**
 * Data-access contract for {@link Review} entities.
 */
public interface ReviewRepository {

    /** Returns all reviews for the given product, most recent first. */
    List<Review> findByProduct(long productId);

    /** Returns all reviews written by the given user. */
    List<Review> findByUser(long userId);

    /** Looks up a single review by its primary key. */
    Optional<Review> findById(long id);

    /** Returns the review written by a specific user for a specific product. */
    Optional<Review> findByUserAndProduct(long userId, long productId);

    /**
     * Persists a new review (one per user/product pair).
     *
     * @return the saved review with its generated id.
     */
    Review save(Review review);

    /**
     * Updates rating and comment of an existing review.
     *
     * @return {@code true} if a row was updated.
     */
    boolean update(Review review);

    /**
     * Deletes a review by id.
     *
     * @return {@code true} if a row was deleted.
     */
    boolean delete(long id);
}
