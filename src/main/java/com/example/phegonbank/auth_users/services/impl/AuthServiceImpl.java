package com.example.phegonbank.auth_users.services.impl;

import com.example.phegonbank.account.entity.Account;
import com.example.phegonbank.account.services.AccountService;
import com.example.phegonbank.auth_users.dtos.LoginRequest;
import com.example.phegonbank.auth_users.dtos.LoginResponse;
import com.example.phegonbank.auth_users.dtos.RegistrationRequest;
import com.example.phegonbank.auth_users.dtos.ResetPasswordRequest;
import com.example.phegonbank.auth_users.entity.PasswordResetCode;
import com.example.phegonbank.auth_users.entity.User;
import com.example.phegonbank.auth_users.repo.PasswordResetCodeRepo;
import com.example.phegonbank.auth_users.repo.UserRepo;
import com.example.phegonbank.auth_users.services.AuthService;
import com.example.phegonbank.auth_users.services.CodeGenerator;
import com.example.phegonbank.enums.AccountType;
import com.example.phegonbank.enums.Currency;
import com.example.phegonbank.exceptions.BadRequestException;
import com.example.phegonbank.exceptions.NotFoundException;
import com.example.phegonbank.notification.dtos.NotificationDTO;
import com.example.phegonbank.notification.services.NotificationService;
import com.example.phegonbank.res.Response;
import com.example.phegonbank.role.entity.Role;
import com.example.phegonbank.role.repo.RoleRepo;
import com.example.phegonbank.security.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final NotificationService notificationService;
    private final AccountService accountService;
    private final CodeGenerator codeGenerator;
    private final PasswordResetCodeRepo passwordResetCodeRepo;

    @Value("${password.reset.link}")
    private String resetLink;

    @Override
    public Response<String> register(RegistrationRequest request) {
        List<Role> roles;
        if (request.getRoles() == null || request.getRoles().isEmpty()) {
            Role defaultRole = roleRepo.findByName("CUSTOMER")
                    .orElseThrow(() -> new NotFoundException("CUSTOMER ROLE NOT FOUND"));
            roles = Collections.singletonList(defaultRole);
        } else {
            roles = request.getRoles().stream()
                    .map(roleName -> roleRepo.findByName(roleName)
                            .orElseThrow(() -> new NotFoundException("ROLE NOT FOUND: " + roleName)))
                    .toList();
        }

        if (userRepo.findByEmail(request.getEmail()).isPresent()) {
            throw new BadRequestException("Email Already Present");
        }

        User user = User.builder()
                .firstname(request.getFirstName())
                .lastname(request.getLastName())
                .email(request.getEmail())
                .phonenumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(roles)
                .active(true)
                .build();
        User savedUser = userRepo.save(user);

        Account savedAccount = accountService.createAccount(AccountType.SAVINGS, savedUser);

        Map<String, Object> vars = new HashMap<>();
        vars.put("name", savedUser.getFirstname());

        NotificationDTO notificationDTO = NotificationDTO.builder()
                .recipient(savedUser.getEmail())
                .subject("Welcome to Phegon Bank")
                .templateName("welcome")
                .templateVariables(vars)
                .build();
        notificationService.sendEmail(notificationDTO, savedUser);

        Map<String, Object> accountVars = new HashMap<>();
        accountVars.put("name", savedUser.getFirstname());
        accountVars.put("accountNumber", savedAccount.getAccountNumber());
        accountVars.put("accountType", savedAccount.getAccountType().toString());
        accountVars.put("currency", Currency.USD.toString());

        NotificationDTO accountCreatedEmail = NotificationDTO.builder()
                .recipient(savedUser.getEmail())
                .subject("Your New Bank Account Has Been Created")
                .templateName("account-created")
                .templateVariables(accountVars)
                .build();
        notificationService.sendEmail(accountCreatedEmail, savedUser);

        return Response.<String>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Your account has been created successfully")
                .data("Email of your account details has been sent to you. Your account number is: " + savedAccount.getAccountNumber())
                .build();
    }

    @Override
    public Response<LoginResponse> login(LoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        User user = userRepo.findByEmail(email).orElseThrow(() -> new NotFoundException("Email Not Found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadRequestException("Password doesn't match");
        }

        String token = tokenService.generateToken(user.getEmail());

        LoginResponse loginResponse = LoginResponse.builder()
                .roles(user.getRoles().stream().map(role -> role.getName()).toList())
                .token(token)
                .build();

        return Response.<LoginResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Login Successful")
                .data(loginResponse)
                .build();
    }

    @Override
    @Transactional
    public Response<?> forgetPassword(String email) {
        User user = userRepo.findByEmail(email).orElseThrow(() -> new NotFoundException("User Not Found"));
        passwordResetCodeRepo.deleteByUserId(user.getId());

        String code = codeGenerator.generateUniqueCode();
        PasswordResetCode resetCode = PasswordResetCode.builder()
                .user(user)
                .code(code)
                .expiryDate(calculateExpiryDate())
                .used(false)
                .build();
        passwordResetCodeRepo.save(resetCode);

        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("name", user.getFirstname());
        templateVariables.put("resetLink", resetLink + code);

        NotificationDTO notificationDTO = NotificationDTO.builder()
                .recipient(user.getEmail())
                .subject("Password Reset Code")
                .templateName("password-reset")
                .templateVariables(templateVariables)
                .build();
        notificationService.sendEmail(notificationDTO, user);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Password reset code sent to your email")
                .build();
    }

    @Override
    @Transactional
    public Response<?> updatePasswordViaResetCode(ResetPasswordRequest resetPasswordRequest) {
        String code = resetPasswordRequest.getCode();
        String newPassword = resetPasswordRequest.getNewPassword();

        PasswordResetCode resetCode = passwordResetCodeRepo.findByCode(code)
                .orElseThrow(() -> new BadRequestException("Invalid reset code"));

        if (resetCode.getExpiryDate().isBefore(LocalDateTime.now())) {
            passwordResetCodeRepo.delete(resetCode);
            throw new BadRequestException("Reset code has expired");
        }

        User user = resetCode.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);

        passwordResetCodeRepo.delete(resetCode);

        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("name", user.getFirstname());

        NotificationDTO confirmationEmail = NotificationDTO.builder()
                .recipient(user.getEmail())
                .subject("Password Updated Successfully")
                .templateName("password-update-confirmation")
                .templateVariables(templateVariables)
                .build();
        notificationService.sendEmail(confirmationEmail, user);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Password updated successfully")
                .build();
    }

    private LocalDateTime calculateExpiryDate() {
        return LocalDateTime.now().plusHours(5);
    }
}
