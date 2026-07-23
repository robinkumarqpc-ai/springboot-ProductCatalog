package com.productservicing.productservice.Service;

import com.productservicing.productservice.Models.Product;

import java.util.List;

public class FakeStoreProductService implements ProductService{
    //This Service Class Will Get Product info by interacting with 3rd party API-FakseStore.



    @Override
    public Product getSingleProduct(Long productId) {
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
