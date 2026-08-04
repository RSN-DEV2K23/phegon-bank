package com.example.phegonbank.transaction.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)

public class TransacctionRequest {
    private TransactionType transactiontype;
    private BigDecimal amount;
    private String accountNumber;
    private String description;
    private String destinationAccount;
}
