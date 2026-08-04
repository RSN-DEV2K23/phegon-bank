package com.example.phegonbank.transaction.services;

import java.util.List;

import com.example.phegonbank.res.Response;
import com.example.phegonbank.transaction.dtos.TransacctionRequest;
import com.example.phegonbank.transaction.dtos.TransactionDTO;

public interface TransactionService {
    Response<?>createTransaction(TransacctionRequest transacctionRequest);
    Response<List<TransactionDTO>> getTransactionForMyAccount(String accountNumber, int page, int size);
}
