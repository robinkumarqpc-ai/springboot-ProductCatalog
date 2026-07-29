package com.productservicing.productservice.Controllers;

import com.productservicing.productservice.Exceptions.CategoryNotFoundException;
import com.productservicing.productservice.Models.Category;
import com.productservicing.productservice.Service.CategoryService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {

    private CategoryService categoryService;

    public CategoryController(@Qualifier("StorageCategoryService") CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getSingleCategory(@PathVariable("id") Long categoryId) throws CategoryNotFoundException {
        return new ResponseEntity<>(
                this.categoryService.getSingleCategory(categoryId),
                HttpStatus.OK
        );
    }

    @GetMapping()
    public List<Category> getAllCategories() {
        return this.categoryService.getAllCategories();
    }

    @PostMapping()
    public Category createCategory(@RequestBody Category category) {
        return this.categoryService.createCategory(category);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable("id") Long categoryId) throws CategoryNotFoundException {
        this.categoryService.deleteCategory(categoryId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
