package com.board.global.security.handler;

import com.board.global.error.dto.ErrorResponse;
import com.board.global.error.exception.ErrorType;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;

public class MemberLoginFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        ErrorType errorType = ErrorType.UNSUPPORTED_ERROR_TYPE;
        if (exception instanceof BadCredentialsException) {
            errorType = ErrorType.BAD_CRDENTIALS;
        }
        ErrorResponse errorResponse = ErrorResponse.of(errorType);
        response.setStatus(errorType.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }

}
