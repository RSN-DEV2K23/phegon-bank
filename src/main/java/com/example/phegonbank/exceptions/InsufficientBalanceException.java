package com.example.phegonbank.exceptions;

public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(String errorMessage) {
        super(errorMessage);
    }

}
