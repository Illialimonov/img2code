package com.example.ilia.aidemo.util;

import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AdviceController {

    @ExceptionHandler(LanguageNotRecognizedError.class)
    public ResponseEntity<String> handleListingOperationException(LanguageNotRecognizedError e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InsufficientCreditsException.class)
    public ResponseEntity<String> handleInsufficientCreditsException(InsufficientCreditsException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(FingerprintNotProvidedException.class)
    public ResponseEntity<String> handleFingerprintNotProvidedException(FingerprintNotProvidedException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_ACCEPTABLE);
    }


    @ExceptionHandler(FileSizeTooLargeException.class)
    public ResponseEntity<String> handleFileSizeTooLargeException(FileSizeTooLargeException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(FeatureIsNotAllowedException.class)
    public ResponseEntity<String> handleFeatureIsNotAllowedException(FeatureIsNotAllowedException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<String> handleExpiredJwtException(ExpiredJwtException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
    }




}
