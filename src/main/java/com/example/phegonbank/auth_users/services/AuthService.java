package com.example.phegonbank.auth_users.services;

import com.example.phegonbank.auth_users.dtos.LoginRequest;
import com.example.phegonbank.auth_users.dtos.RegistrationRequest;
import com.example.phegonbank.auth_users.dtos.ResetPasswordRequest;
import com.example.phegonbank.res.Response;

public interface AuthService {
    Response<String> register(RegistrationRequest request);
    Response<LoginRequest> login(LoginRequest loginRequest);
    Response<? > forgetPassword(String email);
    Response<? > updatePasswordViaResetCode(ResetPasswordRequest resetPasswordRequest);
}
