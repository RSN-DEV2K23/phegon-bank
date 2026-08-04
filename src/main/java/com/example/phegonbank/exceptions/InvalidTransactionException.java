package com.example.phegonbank.exceptions;

public class InvalidTransactionException extends RuntimeException {
    public InvalidTransactionException(String errorMessage) {
        super(errorMessage);
    }

}
