package com.productservicing.productservice.DTOS;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductNotFoundExceptionDTO {
    private Long productId;
    private String message;
    private String resolution;
}
