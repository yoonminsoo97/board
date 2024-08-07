package com.board.global.error;

import com.board.global.common.dto.ApiResponse;
import com.board.global.common.dto.ErrorResponse;
import com.board.global.error.exception.BaseException;

import jakarta.validation.ConstraintViolationException;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handle(BaseException e) {
        ErrorType errorType = e.getErrorType();
        ErrorResponse errorResponse = ErrorResponse.of(errorType);
        return ResponseEntity.status(errorType.getStatus()).body(ApiResponse.fail(errorResponse));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handle(ConstraintViolationException e) {
        ErrorResponse errorResponse = ErrorResponse.of(ErrorType.INVALID_INPUT_VALUE, e.getConstraintViolations());
        return ResponseEntity.badRequest().body(ApiResponse.fail(errorResponse));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handle(BindingResult bindingResult) {
        ErrorResponse errorResponse = ErrorResponse.of(ErrorType.INVALID_INPUT_VALUE, bindingResult);
        return ResponseEntity.badRequest().body(ApiResponse.fail(errorResponse));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handle(AuthenticationException e) {
        ErrorType errorType = ErrorType.of(e.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(errorType);
        return ResponseEntity.status(errorType.getStatus()).body(ApiResponse.fail(errorResponse));
    }

}
