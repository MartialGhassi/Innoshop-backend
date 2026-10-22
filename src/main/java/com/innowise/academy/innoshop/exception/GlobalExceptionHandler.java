package com.innowise.academy.innoshop.exception;

import com.innowise.academy.innoshop.model.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFound(ProductNotFoundException ex) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        ErrorResponse body = new ErrorResponse(status.value(), status.getReasonPhrase(), ex.getMessage());
        return ResponseEntity.status(status).body(body);
    }
}
