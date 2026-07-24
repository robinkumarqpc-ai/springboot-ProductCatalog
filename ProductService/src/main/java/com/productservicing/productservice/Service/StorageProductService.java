package com.productservicing.productservice.Service;

import com.productservicing.productservice.Exceptions.ProductNotFoundExceptions;
import com.productservicing.productservice.Models.Product;
import com.productservicing.productservice.Repository.ProductRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("StorageProductService")
//@Primary--Alternative of qualifier
public class StorageProductService implements ProductService{

    ProductRepository productRepository;

    public StorageProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Product getSingleProduct(Long productId) throws ProductNotFoundExceptions {
        return null;
    }

    @Override
    public List<Product> getAllProducts() {
        return List.of();
    }

    @Override
    public Product createProduct(Product product) {
        return null;
    }

    @Override
    public boolean deleteProduct(Long productId) {
        return false;
    }
}
