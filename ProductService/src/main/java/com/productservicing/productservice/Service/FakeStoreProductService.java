package com.productservicing.productservice.Service;

import com.productservicing.productservice.DTOS.FakeStoreProductDTO;
import com.productservicing.productservice.Models.Category;
import com.productservicing.productservice.Models.Product;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
@Service
public class FakeStoreProductService implements ProductService{
    //This Service Class Will Get Product info by interacting with 3rd party API-FakseStore.

    private RestTemplate restTemplate;

    public FakeStoreProductService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Product getSingleProduct(Long productId) {
        ResponseEntity<FakeStoreProductDTO> fakeStoreProductDTOResponseEntity=restTemplate.getForEntity(
                "https://fakestoreapi.com/products/"+productId, FakeStoreProductDTO.class);
        FakeStoreProductDTO fakeStoreProductDTO=fakeStoreProductDTOResponseEntity.getBody();

        //Parse FakeStoreProductDTO to Product
        return parseFakeStoreProductDTOToProduct(fakeStoreProductDTO);
    }
    private static Product parseFakeStoreProductDTOToProduct(FakeStoreProductDTO fakeStoreProductDTO){
        if(fakeStoreProductDTO==null)
            return null;
        Product product=new Product();
        product.setId(fakeStoreProductDTO.getId());
        product.setTitle(fakeStoreProductDTO.getTitle());
        product.setPrice(fakeStoreProductDTO.getPrice());
        product.setImageURL(fakeStoreProductDTO.getImage());
        product.setDescription(fakeStoreProductDTO.getDescription());
        Category category=new Category();
        category.setName(fakeStoreProductDTO.getCategory());
        product.setCategory(category);
        return product;
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
