package com.productservicing.productservice.Service;

import com.productservicing.productservice.Exceptions.CategoryNotFoundException;
import com.productservicing.productservice.Exceptions.ProductNotFoundExceptions;
import com.productservicing.productservice.Models.Category;
import com.productservicing.productservice.Models.Product;
import com.productservicing.productservice.Repository.CategoryRepository;
import com.productservicing.productservice.Repository.ProductRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service("StorageProductService")
//@Primary--Alternative of qualifier
public class StorageProductService implements ProductService{

    ProductRepository productRepository;
    CategoryRepository categoryRepository;

    public StorageProductService(ProductRepository productRepository,CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Product getSingleProduct(Long productId) throws ProductNotFoundExceptions {
        /*
        // Naive way (without .orElseThrow()) — for reference:

        Optional<Product> optionalProduct = this.productRepository.findById(productId);  // Step 1: fetch the Optional

        if (optionalProduct.isPresent()) {                                              // Step 2: check if it has a value
              return optionalProduct.get();                                              // Step 3a: if present, unwrap and return it
        } else {
            throw new ProductNotFoundExceptions("Product with id:" + productId + " not found!"); // Step 3b: if empty, throw
        }
        */
        return this.productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundExceptions("Product Not Found",productId));
    }

    @Override
    public List<Product> getAllProducts() {
        //what if id is not there but title is there in model
        return productRepository.findAll();
    }

    @Override
    public Product createProduct(Product product) throws CategoryNotFoundException {
        /*Category category=product.getCategory();
        if(category==null)
            throw  new CategoryNotFoundException("Category Mandatory For creation of Product");
        if(category.getId()==null)
        {
            //get category_id with provided name
            Optional<Category> optionalCategory = this.categoryRepository.findByName(category.getName());
            if(optionalCategory.isEmpty()){
                //no category with provided name so just save it
                category=categoryRepository.save(category);
            }
            else{
                //category.setId(optionalCategory.get().getId());
                category=optionalCategory.get();
            }

        }

        product.setCategory(category);*/
        return this.productRepository.save(product);
    }

    @Override
    public void deleteProduct(Long productId) {
        this.productRepository.deleteById(productId);
    }
}
