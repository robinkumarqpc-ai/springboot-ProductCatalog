package com.productservicing.productservice.Service;

import com.productservicing.productservice.Exceptions.ProductNotFoundExceptions;
import com.productservicing.productservice.Models.Product;
import com.productservicing.productservice.Repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
class StorageCategoryServiceTest {
    @Autowired
    private CategoryService categoryService;
    @Mock
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductService productService;



}