package com.example.phegonbank.transaction.services;

import com.example.phegonbank.res.Response;
import com.example.phegonbank.transaction.dtos.TransactionDTO;
import com.example.phegonbank.transaction.dtos.TransactionRequest;

import java.util.List;

public interface TransactionService {
    Response<?> createTransaction(TransactionRequest transactionRequest);
    Response<List<TransactionDTO>> getTransactionsForMyAccount(String accountNumber, int page, int size);
}
