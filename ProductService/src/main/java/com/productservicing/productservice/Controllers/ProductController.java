package com.productservicing.productservice.Controllers;

import com.productservicing.productservice.Exceptions.ProductNotFoundExceptions;
import com.productservicing.productservice.Models.Product;
import com.productservicing.productservice.Service.ProductService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {

    private ProductService productService;

    //public ProductController(@Qualifier("${Variable_Name}"-For more Flexiblity in injecting , by deciding which to inject from config)  ProductService productService) {
    public ProductController(@Qualifier("FakeStoreProductService")  ProductService productService) {
        this.productService = productService;
    }


    @GetMapping("/{id}")
    public ResponseEntity<Product> getSingleProduct(@PathVariable("id") Long productId) throws ProductNotFoundExceptions {
        //throw new RuntimeException("Something went wrong");
        ResponseEntity<Product> responseEntityProduct=
                new ResponseEntity<>(
                        this.productService.getSingleProduct(productId),
                        HttpStatus.OK

                );
        return responseEntityProduct;
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

    /*
          @ExceptionalHandler
          @ExceptionHandler(ProductNotFoundExceptions.class)
    public ResponseEntity<ProductNotFoundExceptionDTO> handleProductNotFoundException(ProductNotFoundExceptions ex, HttpServletResponse response) {
        ProductNotFoundExceptionDTO productNotFoundExceptionDTO=new ProductNotFoundExceptionDTO();
        productNotFoundExceptionDTO.setMessage("Product Not Found");
        productNotFoundExceptionDTO.setResolution("Provide correct Product ID");
        //To-Do - productNotFoundExceptionDTO.setProductId("");
        return new ResponseEntity<>(
                productNotFoundExceptionDTO
                ,HttpStatus.NOT_FOUND);
    }
     */


}
