package com.example.phegonbank.account.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.phegonbank.account.entity.Account;
import java.util.Optional;

public interface AccountRepo extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountNumber(String accountNumber);
    List<Account> findByUserId(Long userId);
}
