package com.productservicing.productservice.Service;

import com.productservicing.productservice.Exceptions.CategoryNotFoundException;
import com.productservicing.productservice.Models.Category;
import com.productservicing.productservice.Repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("StorageCategoryService")
public class StorageCategoryService implements CategoryService {

    CategoryRepository categoryRepository;

    public StorageCategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Category getSingleCategory(Long categoryId) throws CategoryNotFoundException {
        return this.categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category with id:" + categoryId + " not found!"));
    }

    @Override
    public List<Category> getAllCategories() {
        return this.categoryRepository.findAll();
    }

    @Override
    public Category createCategory(Category category) {
        return this.categoryRepository.save(category);
    }

    @Override
    public void deleteCategory(Long categoryId) throws CategoryNotFoundException {
        if (!this.categoryRepository.existsById(categoryId)) {
            throw new CategoryNotFoundException("Category with id:" + categoryId + " not found!");
        }
        this.categoryRepository.deleteById(categoryId);
    }
}