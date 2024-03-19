package com.board.global.error;

import com.board.global.error.dto.ErrorResponse;
import com.board.global.error.exception.BaseException;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException e) {
        final ErrorResponse errorResponse = ErrorResponse.of(e.getErrorType());
        return ResponseEntity.status(errorResponse.getStatus()).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException() {
        final ErrorResponse errorResponse = ErrorResponse.of(ErrorType.INVALID_INPUT_VALUE);
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException e) {
        final ErrorResponse errorResponse = ErrorResponse.of(ErrorType.of(e.getMessage()));
        return ResponseEntity.status(errorResponse.getStatus()).body(errorResponse);
    }

}
