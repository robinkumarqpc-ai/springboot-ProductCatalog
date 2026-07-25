package com.productservicing.productservice.Exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductNotFoundExceptions extends Exception{
    private Long productid;

    public ProductNotFoundExceptions(String message,Long productId) {
        super(message);
        this.productid=productId;
    }
    public ProductNotFoundExceptions(String message) {

    }
}
