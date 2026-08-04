package com.example.phegonbank.transaction.dtos;

import com.example.phegonbank.account.dtos.AccountDTO;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor

public class TransactionDTO {
    private Long id;
    private BigDecimal amount;
    private TransactionType transactiontype;
    private LocalDateTime transactionDate;
    private String description;
    private TransactionStatus status;

    @JsonBackReference
    private AccountDTO account;
    //For transfer transactions
    private String sourceAccount;
    private String destinationAccount;
}
