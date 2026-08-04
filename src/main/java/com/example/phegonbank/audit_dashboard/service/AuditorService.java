package com.example.phegonbank.audit_dashboard.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.phegonbank.account.dtos.AccountDTO;
import com.example.phegonbank.auth_users.dtos.UserDTO;
import com.example.phegonbank.transaction.dtos.TransactionDTO;

public interface AuditorService {

    Map<String, Long> getSystemTotals();
    Optional<UserDTO> findUserByEmail(String email);
    Optional<AccountDTO> findAccountDetailsByAccountNumber(String accountNumber);
    List<TransactionDTO> findTransactionsByAccountNumber(String accountNumber);
    Optional<TransactionDTO> findTransactionById(Long transactionId);
}
