package com.productservicing.productservice.Controllers;

import com.productservicing.productservice.Models.Product;
import com.productservicing.productservice.Service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
public class ProductControllerMVCTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private ProductService productService;
    @Autowired
    private ObjectMapper objectMapper;
    @Test
    public void testGetAllProductsAPI() throws Exception {
        //Arrange
        Product product1 = new Product();
        product1.setId(1L);
        product1.setTitle("IPhone 16");
        product1.setPrice(1999.99);

        Product product2 = new Product();
        product2.setId(2L);
        product2.setTitle("Wireless Mouse");
        product2.setPrice(499.0);

        when(productService.getAllProducts()).thenReturn(List.of(product1, product2));

        //Act & Assert
        mockMvc.perform(
                get("/product") //actual
        ).andExpect(status().isOk()) //expect
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("IPhone 16"))
                .andExpect(jsonPath("$[0].price").value(1999.99))
                .andExpect(jsonPath("$[1].title").value("Wireless Mouse"))
                .andExpect(jsonPath("$[1].price").value(499.0))
                //validate entire response - literal JSON
                .andExpect(content().json("""
                        [
                          {"id":1,"title":"IPhone 16","price":1999.99},
                          {"id":2,"title":"Wireless Mouse","price":499.0}
                        ]
                        """))
                //validate entire response - via ObjectMapper serialization of the expected objects
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(product1, product2))));
    }
}
