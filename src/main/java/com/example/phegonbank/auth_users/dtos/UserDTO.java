package com.example.phegonbank.auth_users.dtos;

import com.example.phegonbank.account.dtos.AccountDTO;
import com.example.phegonbank.role.entity.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDTO {
    private Long id;
    private String firstname;
    private String lastname;
    private String phonenumber;
    private String email;

    @JsonIgnore
    private String password;
    private String profilePictureUrl;
    private boolean active;
    private List<Role> roles;

    @JsonManagedReference
    private List<AccountDTO> accounts;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
