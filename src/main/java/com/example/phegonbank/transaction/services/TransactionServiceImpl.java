package com.example.phegonbank.transaction.services;

import com.example.phegonbank.account.entity.Account;
import com.example.phegonbank.account.repo.AccountRepo;
import com.example.phegonbank.auth_users.entity.User;
import com.example.phegonbank.auth_users.services.UserService;
import com.example.phegonbank.enums.TransactionStatus;
import com.example.phegonbank.enums.TransactionType;
import com.example.phegonbank.exceptions.BadRequestException;
import com.example.phegonbank.exceptions.InsufficientBalanceException;
import com.example.phegonbank.exceptions.InvalidTransactionException;
import com.example.phegonbank.exceptions.NotFoundException;
import com.example.phegonbank.notification.dtos.NotificationDTO;
import com.example.phegonbank.notification.services.NotificationService;
import com.example.phegonbank.res.Response;
import com.example.phegonbank.transaction.dtos.TransactionDTO;
import com.example.phegonbank.transaction.dtos.TransactionRequest;
import com.example.phegonbank.transaction.entity.Transaction;
import com.example.phegonbank.transaction.repo.TransactionRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepo transactionRepo;
    private final AccountRepo accountRepo;
    private final NotificationService notificationService;
    private final UserService userService;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public Response<?> createTransaction(TransactionRequest transactionRequest) {
        Transaction transaction = new Transaction();

        transaction.setTransactionType(transactionRequest.getTransactionType());
        transaction.setAmount(transactionRequest.getAmount());
        transaction.setDescription(transactionRequest.getDescription());

        switch (transactionRequest.getTransactionType()) {
            case DEPOSIT -> handleDeposit(transactionRequest, transaction);
            case WITHDRAWAL -> handleWithdrawal(transactionRequest, transaction);
            case TRANSFER -> handleTransfer(transactionRequest, transaction);
            default -> throw new InvalidTransactionException("Invalid transaction type");
        }

        transaction.setStatus(TransactionStatus.SUCCESS);
        Transaction savedTxn = transactionRepo.save(transaction);

        sendTransactionNotifications(savedTxn);

        return Response.builder()
                .statusCode(200)
                .message("Transaction successful")
                .build();
    }

    @Override
    @Transactional
    public Response<List<TransactionDTO>> getTransactionsForMyAccount(String accountNumber, int page, int size) {
        User user = userService.getCurrentLoggedInUser();

        Account account = accountRepo.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        if (!account.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Account does not belong to the authenticated user");
        }

        PageRequest pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());
        Page<Transaction> txns = transactionRepo.findByAccount_AccountNumber(accountNumber, pageable);

        List<TransactionDTO> transactionDTOS = txns.getContent().stream()
                .map(transaction -> modelMapper.map(transaction, TransactionDTO.class))
                .toList();

        Map<String, Serializable> meta = new HashMap<>();
        meta.put("currentPage", txns.getNumber());
        meta.put("totalItems", txns.getTotalElements());
        meta.put("totalPages", txns.getTotalPages());
        meta.put("pageSize", txns.getSize());

        return Response.<List<TransactionDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Transactions retrieved")
                .data(transactionDTOS)
                .metadata(meta)
                .build();
    }

    private void handleDeposit(TransactionRequest request, Transaction transaction) {
        Account account = accountRepo.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new NotFoundException("Account not found"));

        account.setBalance(account.getBalance().add(request.getAmount()));
        transaction.setAccount(account);
        accountRepo.save(account);
    }

    private void handleWithdrawal(TransactionRequest request, Transaction transaction) {
        Account account = accountRepo.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new NotFoundException("Account not found"));

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        account.setBalance(account.getBalance().subtract(request.getAmount()));
        transaction.setAccount(account);
        accountRepo.save(account);
    }

    private void handleTransfer(TransactionRequest request, Transaction transaction) {
        Account sourceAccount = accountRepo.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new NotFoundException("Account not found"));

        Account destination = accountRepo.findByAccountNumber(request.getDestinationAccountNumber())
                .orElseThrow(() -> new NotFoundException("Destination Account not found"));

        if (sourceAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance in source account");
        }

        sourceAccount.setBalance(sourceAccount.getBalance().subtract(request.getAmount()));
        accountRepo.save(sourceAccount);

        destination.setBalance(destination.getBalance().add(request.getAmount()));
        accountRepo.save(destination);

        transaction.setAccount(sourceAccount);
        transaction.setSourceAccount(sourceAccount.getAccountNumber());
        transaction.setDestinationAccount(destination.getAccountNumber());
    }

    private void sendTransactionNotifications(Transaction tnx) {
        User user = tnx.getAccount().getUser();

        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("name", user.getFirstname());
        templateVariables.put("amount", tnx.getAmount());
        templateVariables.put("accountNumber", tnx.getAccount().getAccountNumber());
        templateVariables.put("date", tnx.getTransactionDate());
        templateVariables.put("balance", tnx.getAccount().getBalance());

        String subject;
        String template;

        if (tnx.getTransactionType() == TransactionType.DEPOSIT) {
            subject = "Credit Alert";
            template = "credit-alert";

            notificationService.sendEmail(NotificationDTO.builder()
                    .recipient(user.getEmail())
                    .subject(subject)
                    .templateName(template)
                    .templateVariables(templateVariables)
                    .build(), user);

        } else if (tnx.getTransactionType() == TransactionType.WITHDRAWAL) {
            subject = "Debit Alert";
            template = "debit-alert";

            notificationService.sendEmail(NotificationDTO.builder()
                    .recipient(user.getEmail())
                    .subject(subject)
                    .templateName(template)
                    .templateVariables(templateVariables)
                    .build(), user);

        } else if (tnx.getTransactionType() == TransactionType.TRANSFER) {
            subject = "Debit Alert";
            template = "debit-alert";

            notificationService.sendEmail(NotificationDTO.builder()
                    .recipient(user.getEmail())
                    .subject(subject)
                    .templateName(template)
                    .templateVariables(templateVariables)
                    .build(), user);

            Account destination = accountRepo.findByAccountNumber(tnx.getDestinationAccount())
                    .orElseThrow(() -> new NotFoundException("Destination account not found"));

            User receiver = destination.getUser();

            Map<String, Object> recvVars = new HashMap<>();
            recvVars.put("name", receiver.getFirstname());
            recvVars.put("amount", tnx.getAmount());
            recvVars.put("accountNumber", destination.getAccountNumber());
            recvVars.put("date", tnx.getTransactionDate());
            recvVars.put("balance", destination.getBalance());

            notificationService.sendEmail(NotificationDTO.builder()
                    .recipient(receiver.getEmail())
                    .subject("Credit Alert")
                    .templateName("credit-alert")
                    .templateVariables(recvVars)
                    .build(), receiver);
        }
    }
}
