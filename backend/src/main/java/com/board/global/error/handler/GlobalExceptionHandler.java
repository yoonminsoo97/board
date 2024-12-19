package com.board.global.error.handler;

import com.board.global.error.dto.ErrorResponse;
import com.board.global.error.exception.BaseException;
import com.board.global.error.exception.ErrorType;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handle(BaseException ex) {
        ErrorType errorType = ex.getErrorType();
        return ResponseEntity.status(errorType.getStatus()).body(ErrorResponse.of(errorType));
    }

}
