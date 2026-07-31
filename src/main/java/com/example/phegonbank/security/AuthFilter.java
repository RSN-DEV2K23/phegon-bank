package com.example.phegonbank.security;

import java.io.IOException;
import java.rmi.server.ServerCloneException;
import org.springframework.security.core.AuthenticationException;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.phegonbank.exceptions.CustomAuthenticationEntryPoint;

// removed wrong IOException import
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor

public class AuthFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException{
                String token = getTokenFromRequest(request);

                if (token != null){
                    String email;
                    try{
                        email = tokenService.getUsernameFromToken(token);
                    } catch (Exception e){
                        log.error("Exception occured while extracting username from token");
                        AuthenticationException authenticationException = new BadCredentialsException(e.getMessage());
                        customAuthenticationEntryPoint.commence(request, response, authenticationException);
                        return;
                    }

                    UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

                    if(StringUtils.hasText(email) && tokenService.isTokenValid(token, userDetails)){
                        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());
                        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                        
                    }
                }

                try {

                } catch (Exception e){
                    log.error(e.getMessage());
                }

            }
    
    private String getTokenFromRequest(HttpServletRequest request){
        String tokenWithBearer = request.getHeader("Authorization");
        if (tokenWithBearer != null && tokenWithBearer.startsWith("Bearer ")){
            return tokenWithBearer.substring(7);
        }       
        return null;
    }
}
