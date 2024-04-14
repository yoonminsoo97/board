package com.board.global.error;

import com.board.global.common.dto.ApiResponse;
import com.board.global.common.dto.ErrorResponse;
import com.board.global.error.exception.BaseException;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handle(HttpServletRequest request, BaseException e) {
        ErrorType errorType = e.getErrorType();
        ErrorResponse errorResponse = ErrorResponse.of(errorType, requestPath((request)));
        return ResponseEntity
                .status(errorType.getStatus())
                .body(ApiResponse.fail(errorType.getStatus(), errorResponse));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handle(HttpServletRequest request, BindingResult bindingResult) {
        ErrorType errorType = ErrorType.INVALID_INPUT_VALUE;
        ErrorResponse errorResponse = ErrorResponse.of(errorType, requestPath(request), bindingResult);
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.fail(errorType.getStatus(), errorResponse));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handle(HttpServletRequest request, AuthenticationException e) {
        ErrorType errorType = ErrorType.of(e.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(errorType, requestPath(request));
        return ResponseEntity
                .status(errorType.getStatus())
                .body(ApiResponse.fail(errorType.getStatus(), errorResponse));
    }

    private String requestPath(HttpServletRequest request) {
        String queryString = request.getQueryString();
        if (StringUtils.hasText(queryString)) {
            return request.getRequestURI() + "?" + queryString;
        }
        return request.getRequestURI();
    }

}
