package org.marketplace.service;

import org.marketplace.exception.ValidationException;
import org.marketplace.model.Category;
import org.marketplace.repository.CategoryRepository;
import org.marketplace.repository.impl.PSQLCategoryRepository;

import java.util.List;

/**
 * Business-logic layer for {@link Category} management.
 * <p>
 * All validation is done here; the repository is treated as a pure
 * data-access layer with no business rules of its own.
 * </p>
 */
public class CategoryService {

    private final CategoryRepository categoryRepo;

    /** Default constructor wires the PostgreSQL implementation. */
    public CategoryService() {
        this(new PSQLCategoryRepository());
    }

    /** Injection constructor for testing. */
    public CategoryService(CategoryRepository categoryRepo) {
        this.categoryRepo = categoryRepo;
    }

    // -----------------------------------------------------------------------
    // Queries
    // -----------------------------------------------------------------------

    /** Returns all categories ordered by name. */
    public List<Category> getAllCategories() {
        return categoryRepo.findAll();
    }

    /**
     * Looks up a category by id.
     *
     * @throws ValidationException if no category exists with that id.
     */
    public Category getCategoryById(long id) {
        return categoryRepo.findById(id)
                .orElseThrow(() -> new ValidationException("Category not found: id=" + id));
    }

    // -----------------------------------------------------------------------
    // Commands
    // -----------------------------------------------------------------------

    /**
     * Creates a new category.
     *
     * @param name the category name.
     * @return the persisted {@link Category} with its generated id.
     * @throws ValidationException if the name is blank or already in use.
     */
    public Category createCategory(String name) {
        validateName(name);
        if (categoryRepo.findByName(name).isPresent()) {
            throw new ValidationException("Category already exists: " + name);
        }
        return categoryRepo.save(Category.builder().name(name.trim()).build());
    }

    /**
     * Renames an existing category.
     *
     * @throws ValidationException if the new name is blank, already taken, or the id is unknown.
     */
    public void renameCategory(long id, String newName) {
        validateName(newName);
        getCategoryById(id); // ensure it exists
        categoryRepo.findByName(newName).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new ValidationException("Another category already uses the name: " + newName);
            }
        });
        categoryRepo.update(Category.builder().id(id).name(newName.trim()).build());
    }

    /**
     * Deletes a category.
     * Note: the DB will reject the deletion if products still reference it.
     *
     * @throws ValidationException if the category does not exist.
     */
    public void deleteCategory(long id) {
        getCategoryById(id);
        categoryRepo.delete(id);
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Category name must not be blank.");
        }
        if (name.trim().length() > 100) {
            throw new ValidationException("Category name must not exceed 100 characters.");
        }
    }
}
