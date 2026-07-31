package com.example.phegonbank.transaction.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.phegonbank.res.Response;
import com.example.phegonbank.transaction.dtos.TransacctionRequest;
import com.example.phegonbank.transaction.services.TransactionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor

public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<Response<?>> createTransaction(@RequestBody @Valid TransacctionRequest request){
        return ResponseEntity.ok(TransactionService.createTransaction(request));
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<Response<?>> getTransactionsForMyAccount(
            @PathVariable String accountNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return ResponseEntity.ok(transactionService.getTransactionsForMyAccount(accountNumber, page, size));
    }
}
