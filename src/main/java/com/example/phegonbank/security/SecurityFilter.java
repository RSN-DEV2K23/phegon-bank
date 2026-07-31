package com.example.phegonbank.security;

import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

import com.example.phegonbank.exceptions.CustomAccessDenialHandler;
import com.example.phegonbank.exceptions.CustomAuthenticationEntryPoint;

import lombok.RequiredArgsConstructor;

@Component
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor

public class SecurityFilter {

    private final AuthFilter authFilter;
    private final CustomAccessDenialHandler accessDenialHandler;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity){
        httpSecurity.csrf(AbstractHttpConfigurer::disable)
                    .cors(Customizer<CorsConfigurer<HttpSecurity>>.withDefaults())
                    .exceptionHandling(ex -> ex.accessDenialHandler(customAccessDenialHandler).authenticationEntryPoint(CustomAuthenticationEntryPoint))
                    .authorizationHttpRequest(req -> req.requestMatchers("/api/auth/**").permitAll().anyRequest().authenticated())
                    .sessionManagement(mag -> mag.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .addFilterBefore(authFilter,UsernamePasswordAuthenticationFilter.class);
        return httpSecurity.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration){
        return authenticationConfiguration.getAuthenticationManager();
    }



}
