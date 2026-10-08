package org.marketplace.controller;

import org.marketplace.model.Category;
import org.marketplace.service.CategoryService;

import java.util.List;

/**
 * Thin controller for category management.
 * Primarily used by the admin view to manage categories.
 */
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController() {
        this.categoryService = new CategoryService();
    }

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /** @return all categories ordered by name. */
    public List<Category> listAllCategories() {
        return categoryService.getAllCategories();
    }

    /** @return the category with the given id. */
    public Category getCategory(long id) {
        return categoryService.getCategoryById(id);
    }

    /**
     * Creates a new category.
     *
     * @return the persisted category.
     */
    public Category createCategory(String name) {
        return categoryService.createCategory(name);
    }

    /** Renames an existing category. */
    public void renameCategory(long id, String newName) {
        categoryService.renameCategory(id, newName);
    }

    /** Deletes a category by id. */
    public void deleteCategory(long id) {
        categoryService.deleteCategory(id);
    }
}
