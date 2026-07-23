package com.productservicing.productservice.Controllers;

import com.productservicing.productservice.Models.Product;
import com.productservicing.productservice.Service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {

    private ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/{id}")
    public Product getSingleProduct(@PathVariable("id") Long productId) {
        return  this.productService.getSingleProduct(productId);
    }

    @GetMapping()
    public List<Product> getAllProducts() {
        return this.productService.getAllProducts();
    }

    @PostMapping()
    public Product createProduct(@RequestBody Product product) {
        return  new Product();
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable("id") Long productId) {
        return null;
    }
    //Update API-UpdateProduct-Patch,replaceProduct-PUT


}
