package com.example.phegonbank.auth_users.entity;

import org.hibernate.annotations.ManyToAny;

import jakarta.annotation.Generated;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor

public class User {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String firstname;
        private String lastname;
        private String phonenumber;

        @Email
        @Column(unique = true,nullable = false)
        @NotBlank(message = "Email is mandatory")
        private String email;
        private String password;
        private String profilePictureUrl;
        private boolean active=true;

        @ManyToAny(fetch = FetchType.EAGER)
        @JoinTable(name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
        )
        private List<Role> roles;

        @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
        private List<Account> accounts;
        private LocalDateTime createdAt=LocalDateTime.now();
        private LocalDateTime updatedAt;
}
