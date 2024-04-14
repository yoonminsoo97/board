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
import org.springframework.util.StringUtils;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        final String errorCode = authException.getMessage();
        final ErrorType errorType = ErrorType.of(errorCode);
        final ErrorResponse errorResponse = ErrorResponse.of(errorType, requestPath(request));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(errorType.getStatus());
        objectMapper.writeValue(response.getOutputStream(), ApiResponse.fail(errorType.getStatus(), errorResponse));
    }

    private String requestPath(HttpServletRequest request) {
        String queryString = request.getQueryString();
        if (StringUtils.hasText(queryString)) {
            return request.getRequestURI() + "?" + queryString;
        }
        return request.getRequestURI();
    }

}
