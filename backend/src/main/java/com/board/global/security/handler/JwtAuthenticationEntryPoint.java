package com.board.global.security.handler;

import com.board.global.common.dto.ApiResponse;
import com.board.global.error.ErrorType;
import com.board.global.common.dto.ErrorResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        ErrorType errorType = ErrorType.of(authException.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(errorType);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(errorType.getStatus());
        objectMapper.writeValue(response.getOutputStream(), ApiResponse.fail(errorResponse));
    }

}
