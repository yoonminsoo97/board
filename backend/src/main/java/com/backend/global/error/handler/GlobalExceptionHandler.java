package com.backend.global.error.handler;

import com.backend.global.error.dto.ErrorResponse;
import com.backend.global.error.exception.BoardException;
import com.backend.global.error.exception.ErrorType;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BoardException.class)
    public ResponseEntity<ErrorResponse> handleBoardException(BoardException ex) {
        ErrorType errorType = ex.getErrorType();
        ErrorResponse errorResponse = ErrorResponse.of(errorType);
        return ResponseEntity.status(errorType.getStatus()).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        ErrorResponse errorResponse = ErrorResponse.of(ErrorType.INVALID_INPUT, ex.getFieldErrors());
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        ErrorType errorType = ErrorType.of(ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(errorType);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

}
