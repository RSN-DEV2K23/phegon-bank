package com.example.phegonbank.auth_users.services.impl;

import com.example.phegonbank.auth_users.services.AuthService;
import com.example.phegonbank.auth_users.services.CodeGenerator;
import com.example.phegonbank.enums.AccountType;
import com.example.phegonbank.exceptions.NotFoundException;
import com.example.phegonbank.notification.dtos.NotificationDTO;
import com.example.phegonbank.notification.entity.Notification;
import com.example.phegonbank.notification.services.NotificationService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

import org.apache.coyote.BadRequestException;
import org.aspectj.apache.bcel.classfile.Code;
import org.modelmapper.internal.bytebuddy.asm.Advice.Return;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.example.phegonbank.res.Response;
import com.example.phegonbank.security.TokenService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j

public class AuthServiceImpl implements AuthService{

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
    Response<String> register(RegistrationRequest request) {
        List<Role> roles;
        if(request.getRoles()== null || request.getRoles().isEmpty()){
            Role defaultRole = roleRepo.findByName("CUSTOMER")
                    .orElseThrow()-> new NotFoundException("CUSTOMER ROLE NOT FOUND")
            roles = Collections.singletonList(defaultRole);
        }else{
            roles = request.getRoles().stream()
                    .map(String roleName -> roleRepo.findByName(roleName)
                        .orElseThrow(()-> new NotFoundException("ROLE NOT FOUND" + roleName)))
                    .toList();
        }

        if(userRepo.findByEmail(request.getEmail()).isPresent()){
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
        
        // Create ACCOUNT NUMBER FOR THE USER
        Account savedAccount = accountService.createAccount(AccountType.SAVINGS, savedUser);

        //SEND A WELCOME EMAIL
        Map<String, Object> vars = new HashMap<>();
        vars.put("name", savedUser.getFirstname());

        NotificationDTO notificationDTO = NotificationDTO.builder()
                .recipient(savedUser.getEmail())
                .subject("Welcome to Phegon Bank")
                .templateName("wecome")
                .templateVariables(vars)
                .build();

        notificationService.sendEmail(notificationDTO, savedUser);

        //SEND ACCOUNT CREDENTAIL
        Map<String, Object> accountvars = new HashMap<>();
        accountvars.put("name", savedUser.getFirstname())
        accountvars.put("accountNumber", savedAccount.getAccountNumber())
        accountvars.put("accountType", AccountType.getAccountNumber())
        accountvars.put("currency", Currency.USD);

        NotificationDTO accountCreatedEmail = NotificationDTO.builder()
                .recipient(savedUser.getEmail())
                .subject("Your New Bank Account Has Been Created")
                .templateName("account-created")
                .templateVariables(accountvars)
                .build();
        
        notificationService.sendEmail(accountCreatedEmail, savedUser);

        return Response.<String>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Your account has been created successfully")
                .data("Email of your account details has been sent to you. Your account number is: " + savedAccount.getAccountNumber())
                .build();
    };
   
    @Override
    public Response<LoginRequest> login(LoginRequest loginRequest){

        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        User user = userRepo.findByEmail(email).orElseThrow(()-> new NotFoundException("Email Not Found"));

        if(!passwordEncoder.matches(password, user.getPassword())){
            throw new BadRequestException("Password doesn't match");
        }

        String Token = tokenService.generateToken(user.getEmail());

        LoginResponse loginResponse = loginResponse.builder()
                .roles(user.getRoles().stream().map(Role::getName).toList())
                .token(token)
                .build();
        return Response.<loginResponse>builder()
                    .statusCode(HttpStatus.OK.value())
                    .message("Login Successful")
                    .data(loginResponse)
                    .build();
    };
   
    @Override
    @Transactional
    Response<?> forgetPassword(String email){

        User user = userRepo.findByEmail(email).orElseThrow(()-> new NotFoundException("User Not Found"));
        passwordResetCodeRepo.deleteByUserId(user.getId());

        String code = codeGenerator.generateUniqueCode();
          PasswordResetCode resetCode = PasswordResetCode.builder()
                        .user(user)
                        .code(code)
                        .expiryDate(calculateExpiryDate())
                        .used(false)
                        .build();
          passwordResetCodeRepo.save(resetCode);
          
          //send email reset link out
          Map<String, Object> templateVariables = new HashMap<>();
          templateVariables.put("name", user.getFirstname());
          templateVariables.put("resetLink", resetLink + Code);

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


    };
   
    @Override
    @Transactional
    Response<?> updatePasswordViaResetCode(ResetPasswordRequest resetPasswordRequest){
        String code = resetPasswordRequest.getCode();
        String newPassword = resetPasswordRequest.getNewPassword();

        //Find and Validate code
        PasswordResetCode resetCode = passwordResetCodeRepo.findByCode(code)
                .orElseThrow(()-> new BadRequestException("Invalid reset code"));

        //Check expiration first
        if(resetCode.getExpiryDate().isBefore(LocalDateTime.now())){
            passwordResetCodeRepo.delete(resetCode);
            throw new BadRequestException("Reset code has expired");
        }

        //Update the password
        User user = resetCode.getUser();
        User.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);

        //Delete the code immediately after successful use
        passwordResetCodeRepo.delete(resetCode);

        // Send confirmation email
        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("name", user.getFirstName());

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
    ;

    private LocalDateTime calculateExpiryDate(){
        return LocalDateTime.now().plusHours(5);
    }

}
