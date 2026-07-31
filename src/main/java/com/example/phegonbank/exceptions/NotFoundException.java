package com.example.phegonbank.exceptions;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String errorMessage) {
        super(errorMessage);
    }

}
