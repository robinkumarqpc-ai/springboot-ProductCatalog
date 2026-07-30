package com.productservicing.productservice.Service;

import com.productservicing.productservice.DTOS.FakeStoreProductDTO;
import com.productservicing.productservice.Exceptions.CategoryNotFoundException;
import com.productservicing.productservice.Exceptions.ProductNotFoundExceptions;
import com.productservicing.productservice.Models.Category;
import com.productservicing.productservice.Models.Product;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
@Service//("FakeStoreProductService")
@Primary
public class FakeStoreProductService implements ProductService{
    //This Service Class Will Get Product info by interacting with 3rd party API-FakseStore.

    private RestTemplate restTemplate;

    public FakeStoreProductService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Product getSingleProduct(Long productId) throws ProductNotFoundExceptions {
        //throw new RuntimeException("Something went wrong");

        ResponseEntity<FakeStoreProductDTO> fakeStoreProductDTOResponseEntity=restTemplate.getForEntity(
                "https://fakestoreapi.com/products/"+productId, FakeStoreProductDTO.class);
        FakeStoreProductDTO fakeStoreProductDTO=fakeStoreProductDTOResponseEntity.getBody();

        //Parse FakeStoreProductDTO to Product
        //throw new RuntimeException("Something went wrong");
        if(fakeStoreProductDTO==null)
            throw  new ProductNotFoundExceptions("Product:" + productId + "Not Found");
        return parseFakeStoreProductDTOToProduct(fakeStoreProductDTO);


    }
    //Parse FakeStoreProductDTO to Product
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
        /*
        ResponseEntity<List<FakeStoreProductDTO>> fakeStoreProductDtoListResponseEntity=restTemplate.getForEntity(
                "https://fakestoreapi.com/products",
                List<FakeStoreProductDTO>.class  // compilation error as at run time generic type is erased so its class cant be derived
        );

        */
        //instead of List<FakeStoreProductDto>-Generic use array of FakeStoreProductDto which is not generics
        ResponseEntity<FakeStoreProductDTO[]> fakeStoreProductDtoListResponseEntity=restTemplate.getForEntity(
                "https://fakestoreapi.com/products",
                FakeStoreProductDTO[].class
        );
        FakeStoreProductDTO[] fakeStoreProductDtoList=fakeStoreProductDtoListResponseEntity.getBody();
        List<Product> products=new ArrayList<>();
        for(FakeStoreProductDTO fakeStoreProductDTO:fakeStoreProductDtoList){
            products.add(parseFakeStoreProductDTOToProduct(fakeStoreProductDTO));
        }
        return products;
    }

    @Override
    public Product createProduct(Product product) throws CategoryNotFoundException {
        return null;
    }

    
    @Override
    public void deleteProduct(Long productId) {

    }
}
