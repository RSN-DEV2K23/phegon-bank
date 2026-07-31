package com.example.phegonbank.account.dtos;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class AccountDTO {
    private Long id;
    private String accountNumber;
    private BigDecimal balance;
    private AccountType accountType;

    @JsonBackReference
    private UserDTO user;
    private Currency currency;
    private AccountStatus accountStatus;

    @JsonManagedReference
    private List<TransactionDTO> transactions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime closedAt;


}
