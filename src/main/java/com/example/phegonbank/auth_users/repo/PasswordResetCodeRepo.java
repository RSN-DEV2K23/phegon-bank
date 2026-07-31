package com.example.phegonbank.auth_users.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

public interface PasswordResetCodeRepo extends JpaRepository<PasswordResetCode, Long> {
    Optional<PasswordResetCode> findByCode(String code);
    Void deleteByUserId(Long userId);
}
