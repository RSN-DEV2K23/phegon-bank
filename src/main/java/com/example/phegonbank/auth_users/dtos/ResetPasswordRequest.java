package com.example.phegonbank.auth_users.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResetPasswordRequest {
    //Will be used to request for forgotten password reset.
    private String email;
    
    //Will be used to verify the reset code sent to the user's email.
    private String code;
    private String newPassword;

}
