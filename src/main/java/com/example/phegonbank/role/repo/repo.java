package com.example.phegonbank.role.repo;

import org.springframework.data.jpa.repository.JpaRepository;

public interface repo extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}
