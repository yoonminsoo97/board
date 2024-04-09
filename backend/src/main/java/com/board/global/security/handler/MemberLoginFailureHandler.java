package com.board.global.security.handler;

import com.board.global.error.ErrorType;
import com.board.global.error.dto.ErrorResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;

@RequiredArgsConstructor
public class MemberLoginFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        ErrorType errorType = ErrorType.UN_SUPPORT_ERROR_TYPE;
        if (exception instanceof BadCredentialsException) {
            errorType = ErrorType.BAD_CREDENTIALS;
        }
        ErrorResponse errorResponse = ErrorResponse.of(errorType);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(errorResponse.getStatus());
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }

}
