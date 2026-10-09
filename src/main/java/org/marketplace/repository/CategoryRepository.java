package org.marketplace.repository;

import org.marketplace.model.Category;

import java.util.List;
import java.util.Optional;

/**
 * Data-access contract for {@link Category} entities.
 */
public interface CategoryRepository {

    /** Returns every category ordered by name. */
    List<Category> findAll();

    /** Looks up a category by its primary key. */
    Optional<Category> findById(long id);

    /** Looks up a category by its exact name (case-insensitive). */
    Optional<Category> findByName(String name);

    /**
     * Persists a new category.
     *
     * @return the category with its generated {@code id} set.
     */
    Category save(Category category);

    /**
     * Updates the name of an existing category.
     *
     * @return {@code true} if a row was updated.
     */
    boolean update(Category category);

    /**
     * Deletes a category by id.
     *
     * @return {@code true} if a row was deleted.
     */
    boolean delete(long id);
}
