package com.example.phegonbank.exceptions;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAccessDenialHandler implements CustomAccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         CustomAccessDeniedHandler accessDeniedException)
            throws IOException, ServletException {
        response<?>. errorresponse = new Response<>.builder()
                .status(HttpStatus.FORBIDDEN.value())
                .message(accessDeniedException.getMessage())
                .build();
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.FORBIDDEN.value());
        response.getWriter().write(objectMapper.writeValueAsString(errorresponse));
    }

}
