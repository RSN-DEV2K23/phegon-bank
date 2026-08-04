package com.example.phegonbank.account.services;

import com.example.phegonbank.account.dtos.AccountDTO;
import com.example.phegonbank.account.entity.Account;
import com.example.phegonbank.auth_users.entity.User;
import com.example.phegonbank.enums.AccountType;
import com.example.phegonbank.res.Response;
import java.util.List;

public interface AccountService {
    Account createAccount(AccountType accountType, User user);

    Response<List<AccountDTO>> getMyAccounts();

    Response<?> closeAccount(String accountNumber);
}
