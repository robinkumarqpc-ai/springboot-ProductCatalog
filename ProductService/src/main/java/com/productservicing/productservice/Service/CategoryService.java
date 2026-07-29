package com.productservicing.productservice.Service;

import com.productservicing.productservice.Exceptions.CategoryNotFoundException;
import com.productservicing.productservice.Models.Category;

import java.util.List;

public interface CategoryService {
    Category getSingleCategory(Long categoryId) throws CategoryNotFoundException;
    List<Category> getAllCategories();
    Category createCategory(Category category);
    void deleteCategory(Long categoryId) throws CategoryNotFoundException;
}
