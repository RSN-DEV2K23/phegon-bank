package com.example.phegonbank.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.phegonbank.auth_users.repo.UserRepo;
import com.example.phegonbank.exceptions.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user =userRepo.findByEmail(username)
                .orElseThrow(()-> new NotFoundException("Email Not Found"));

        return AuthUser.builder()
                    .user(null)
                    .build()
    }
}
