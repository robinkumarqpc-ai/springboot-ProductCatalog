package com.productservicing.productservice.ControllerAdvice;

import com.productservicing.productservice.DTOS.CategoryNotFoundExceptionDTO;
import com.productservicing.productservice.DTOS.ExceptionDTO;
import com.productservicing.productservice.DTOS.ProductNotFoundExceptionDTO;
import com.productservicing.productservice.Exceptions.CategoryNotFoundException;
import com.productservicing.productservice.Exceptions.InvalidTokenException;
import com.productservicing.productservice.Exceptions.ProductNotFoundExceptions;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ProductServiceExceptionHandler {
    /*@ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ExceptionDTO> handleRunTimeException(RuntimeException ex, HttpServletResponse response) {
        ExceptionDTO exceptionDTO=new ExceptionDTO();
        exceptionDTO.setMessage("Please try again");
        exceptionDTO.setResolutionDetails("corrct id ensured");
        return new ResponseEntity<>(
                exceptionDTO
                ,HttpStatus.NOT_FOUND);
    }*/
    //for this specific exceptions , if needs to be handled specifically by controller specific , use and override it there
    @ExceptionHandler(ProductNotFoundExceptions.class)
    public ResponseEntity<ProductNotFoundExceptionDTO> handleProductNotFoundException(ProductNotFoundExceptions ex, HttpServletResponse response) {
        ProductNotFoundExceptionDTO productNotFoundExceptionDTO=new ProductNotFoundExceptionDTO();
        productNotFoundExceptionDTO.setMessage(ex.getMessage());
        productNotFoundExceptionDTO.setResolution("Provide correct Product ID");
        //To-Do - productNotFoundExceptionDTO.setProductId("");
        productNotFoundExceptionDTO.setProductId(ex.getProductid());
        return new ResponseEntity<>(
                productNotFoundExceptionDTO
                ,HttpStatus.NOT_FOUND);
    }
    //for this specific exceptions , if needs to be handled specifically by controller specific , use and override it there
    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<CategoryNotFoundExceptionDTO> handleProductNotFoundException(CategoryNotFoundException ex, HttpServletResponse response) {
        CategoryNotFoundExceptionDTO categoryNotFoundExceptionDTO=new CategoryNotFoundExceptionDTO();
        categoryNotFoundExceptionDTO.setMessage("Product Not Found");
        categoryNotFoundExceptionDTO.setResolution("Provide correct Product ID");

        return new ResponseEntity<>(
                categoryNotFoundExceptionDTO
                ,HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ExceptionDTO> handleInvalidTokenException(InvalidTokenException ex, HttpServletResponse response) {
        ExceptionDTO exceptionDTO=new ExceptionDTO();
        exceptionDTO.setMessage(ex.getMessage());
        exceptionDTO.setResolutionDetails("Provide a valid token");
        return new ResponseEntity<>(
                exceptionDTO
                ,HttpStatus.UNAUTHORIZED);
    }


}
