package com.productservicing.productservice.Service;

import com.productservicing.productservice.Exceptions.ProductNotFoundExceptions;
import com.productservicing.productservice.Models.Product;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface ProductService {
    Product getSingleProduct(Long productId) throws ProductNotFoundExceptions;
    List<Product> getAllProducts();
    Product createProduct( Product product);
    boolean deleteProduct( Long productId);
    //default  method-if only one implementation need to define it , out of many implementation,can be overridden optionally
    /*
    default void sample()
    {
        //sample body
    }
    */
}
