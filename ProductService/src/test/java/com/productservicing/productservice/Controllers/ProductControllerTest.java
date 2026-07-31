package com.productservicing.productservice.Controllers;

import com.productservicing.productservice.Exceptions.InvalidTokenException;
import com.productservicing.productservice.Exceptions.ProductNotFoundExceptions;
import com.productservicing.productservice.Models.Product;
import com.productservicing.productservice.Service.ProductService;
import com.productservicing.productservice.Utility.TokenValidation;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest//if this annotation not used and autowired only used compilation error since , beans ,dependency concept no point when main class bean itself not there
class ProductControllerTest {
    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private TokenValidation tokenValidation;

    @Autowired
    private ProductController productController;

    @Test
    public void test_getSingleProduct_PositiveCase() throws ProductNotFoundExceptions, InvalidTokenException {
        //1.Arrange
        Long productId=10L;
        Product expectedResponse = new Product();
        expectedResponse.setId(productId);
        expectedResponse.setTitle("IPhone 16");
        expectedResponse.setPrice(1999.99);

        when(productService.getSingleProduct(productId)).thenReturn(expectedResponse);

        //2.Act

        Product actualResponse = productController.getSingleProduct(productId, "dummy-token").getBody();

        //3.Assert
        assertEquals(expectedResponse,actualResponse);
        //better to assert values instead of object , as controller may update mocked object after retrieving from dependent service which value is mocked leading to same input output for wrong logic
        assertEquals(1999.99,actualResponse.getPrice());

    }
    //this case can be moved to storageproductservicetest or productserictest new create
    @Test
    public void test_getSingleProduct_ExceptionCase() throws ProductNotFoundExceptions, InvalidTokenException {
        //1.Arrange - to manage input and output
        Long productID=-10L;
        when((productService.getSingleProduct(-10L))).thenThrow(new ProductNotFoundExceptions("Product:" + productID + "Not Found"));

        //2.Act - to get actual value



        //3.Assert

        assertThrows(
                ProductNotFoundExceptions.class, //expected
                ()->productController.getSingleProduct(productID, "dummy-token") //act part merged in actual
        );

        //play around to assert exception messages
    }

}