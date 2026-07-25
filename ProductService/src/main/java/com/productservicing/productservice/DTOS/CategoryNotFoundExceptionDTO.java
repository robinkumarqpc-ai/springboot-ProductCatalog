package com.productservicing.productservice.DTOS;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryNotFoundExceptionDTO {
    private String message;
    private String resolution;
}
