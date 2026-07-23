package com.productservicing.productservice.DTOS;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExceptionDTO {
    private String message;
    private String resolutionDetails;
}
