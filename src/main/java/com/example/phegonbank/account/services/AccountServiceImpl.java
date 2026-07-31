package com.example.phegonbank.account.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.phegonbank.account.dtos.AccountDTO;
import com.example.phegonbank.account.entity.Account;
import com.example.phegonbank.account.repo.AccountRepo;
import com.example.phegonbank.auth_users.entity.User;
import com.example.phegonbank.auth_users.services.UserService;
import com.example.phegonbank.enums.AccountStatus;
import com.example.phegonbank.enums.AccountType;
import com.example.phegonbank.enums.Currency;
import com.example.phegonbank.exceptions.BadRequestException;
import com.example.phegonbank.exceptions.NotFoundException;
import com.example.phegonbank.res.Response;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j

public class AccountServiceImpl  implements AccountService{

    private final AccountRepo accountRepo;
    private final UserService userService;
    private final ModelMapper modelMapper;

    private final Random random = new Random();

    @Override
    public Account createAccount(AccountType accountType, User user){
        log.info("Inside createAccount()");
        Account account = Account.builder()
                .accountNumber(accountNumber)
                .accountType(accountType)
                .currency(Currency.USD)
                .balance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();

        return accountRepo.save(account);
    }

    @Override
    public Response<List<AccountDTO>> getMyAccounts() {

        User user = userService.getCurrentLoggedInUser();

        List<AccountDTO> accounts = accountRepo.findByUserId(user.getId())
                .stream()
                .map(account -> modelMapper.map(account, AccountDTO.class))
                .toList();

        return Response.<List<AccountDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("User accounts fetched successfully")
                .data(accounts)
                .build();
    }


    @Override
    public Response<?> closeAccount(String accountNumber) {

        User user = userService.getCurrentLoggedInUser();
        Account account = accountRepo.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new NotFoundException("Account Not Found"));

        if (!user.getAccounts().contains(account)) {
            throw new NotFoundException("Account doesn't belong to you");
        }

        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new BadRequestException("Account balance must be zero before closing");
        }
        account.setStatus(AccountStatus.CLOSED);
        account.setClosedAt(LocalDateTime.now());
        accountRepo.save(account);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Account closed successfully")
                .build();

    }


    private String generateAccountNumber() {
        String accountNumber;
        do {
            // Generate a random 10-digit number (from 0,000,000,000 to 9,999,999,999)
            // and combine it with the "00" prefix.
            accountNumber = "00" + (random.nextInt(0000000000) + 9999999999);

        } while (accountRepo.findByAccountNumber(accountNumber).isPresent());


        log.info("account number generated {}", accountRepo);
        return accountNumber;
    }

}
