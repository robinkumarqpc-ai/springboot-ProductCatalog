package com.productservicing.productservice.Controllers;

import com.productservicing.productservice.Exceptions.CategoryNotFoundException;
import com.productservicing.productservice.Exceptions.InvalidTokenException;
import com.productservicing.productservice.Exceptions.ProductNotFoundExceptions;
import com.productservicing.productservice.Models.Product;
import com.productservicing.productservice.Service.ProductService;
import com.productservicing.productservice.Utility.TokenValidation;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {

    private ProductService productService;
    private TokenValidation tokenValidation;

    //public ProductController(@Qualifier("${Variable_Name}"-For more Flexiblity in injecting , by deciding which to inject from config)  ProductService productService) {
    //public ProductController( /*@Qualifier("StorageProductService")*/  ProductService productService) {
    //    this.productService = productService;
    //}
    public ProductController( /*@Qualifier("StorageProductService")*/  ProductService productService, TokenValidation tokenValidation) {
        this.productService = productService;
        this.tokenValidation = tokenValidation;
    }


//    @GetMapping("/{id}")
//    //public ResponseEntity<Product> getSingleProduct(@PathVariable("id") Long productId,
//    //                                                  @RequestHeader("token") String tokenvalue) throws ProductNotFoundExceptions {
//    public ResponseEntity<Product> getSingleProduct(@PathVariable("id") Long productId,
//                                                      @RequestHeader("token") String tokenvalue) throws ProductNotFoundExceptions, InvalidTokenException {
//        //throw new RuntimeException("Something went wrong");
//        tokenValidation.validateToken(tokenvalue);
//        ResponseEntity<Product> responseEntityProduct=
//                new ResponseEntity<>(
//                        this.productService.getSingleProduct(productId),
//                        HttpStatus.OK
//
//                );
//        return responseEntityProduct;
//    }


    @GetMapping("/{id}")
    //public ResponseEntity<Product> getSingleProduct(@PathVariable("id") Long productId,
    //                                                  @RequestHeader("token") String tokenvalue) throws ProductNotFoundExceptions {
    public ResponseEntity<Product> getSingleProduct(@PathVariable("id") Long productId) throws ProductNotFoundExceptions {
        //throw new RuntimeException("Something went wrong");
        ResponseEntity<Product> responseEntityProduct=
                new ResponseEntity<>(
                        this.productService.getSingleProduct(productId),
                        HttpStatus.OK

                );
        return responseEntityProduct;
    }



/*    @GetMapping("/{id}")
    public Product getSingleProduct(@PathVariable("id") Long productId) throws ProductNotFoundExceptions {
        //throw new RuntimeException("Something went wrong");
        System.out.println("DEBUG POINT");
        Product response=this.productService.getSingleProduct(productId);
        response.setPrice(980.0);
        return response;
        Product dummyProduct = new Product();
        dummyProduct.setTitle("Dummy Product");
        dummyProduct.setPrice(980.0);
        dummyProduct.setDescription("This is a dummy product for testing");
        dummyProduct.setImageURL("http://example.com/dummy.png");
        return dummyProduct;
    }*/




    @GetMapping()
    public List<Product> getAllProducts() {
        return this.productService.getAllProducts();
    }

    //Get product(s) by title with sorting and pagination.
    //Example: GET /product/search?title=shirt&pageNumber=0&pageSize=10&sortBy=title&sortDirection=asc
    @GetMapping("/search")
    public ResponseEntity<Page<Product>> getProductsByTitle(
            @RequestParam("title") String title,
            @RequestParam(value = "pageNumber", defaultValue = "0") int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "sortBy", defaultValue = "title") String sortBy,
            @RequestParam(value = "sortDirection", defaultValue = "asc") String sortDirection) {
        return new ResponseEntity<>(
                this.productService.getProductsByTitle(title, pageNumber, pageSize, sortBy, sortDirection),
                HttpStatus.OK
        );
    }

    @PostMapping()
    public Product createProduct(@RequestBody Product product) throws CategoryNotFoundException {
        return productService.createProduct(product);
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
