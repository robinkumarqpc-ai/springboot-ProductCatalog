package com.productservicing.productservice.Service;

import com.productservicing.productservice.Models.Product;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface ProductService {
    Product getSingleProduct(Long productId);
    List<Product> getAllProducts();
    Product createProduct( Product product);
    boolean deleteProduct( Long productId);
}
