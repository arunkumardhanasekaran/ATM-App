package com.akcode.atm.exceptions;

import com.akcode.atm.model.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(AccountDoesNotExistsException.class)
    public ResponseEntity<ErrorResponse> handleAccountDoesNotExistsException(AccountDoesNotExistsException e) {
        return new ResponseEntity<>(ErrorResponse.builder()
                .errorCode(1001)
                .errorMessage(e.getMessage())
                .build(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(UserAlreadyExistsException e) {
        return new ResponseEntity<>(ErrorResponse.builder()
                .errorCode(1002)
                .errorMessage(e.getMessage())
                .build(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(GenericException.class)
    public ResponseEntity<ErrorResponse> handleGenericException(GenericException e) {
        return new ResponseEntity<>(ErrorResponse.builder()
                .errorCode(1003)
                .errorMessage(e.getMessage())
                .build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
