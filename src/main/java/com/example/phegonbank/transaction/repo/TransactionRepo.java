package com.example.phegonbank.transaction.repo;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.phegonbank.transaction.entity.Transaction;

public interface TransactionRepo extends JpaRepository<Transaction, Long> {
    Page<Transaction> findByAccount_AccountNumber(String accountNumber, Pageable pageable);

    List<Transaction> findByAccount_AccountNumber(String accountNumber);

}
