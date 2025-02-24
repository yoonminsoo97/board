package com.backend.global.error.handler;

import com.backend.global.error.exception.BoardException;
import com.backend.global.error.exception.ErrorType;

import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BoardException.class)
    public ResponseEntity<ProblemDetail> handleBoardException(BoardException ex) {
        ErrorType errorType = ex.getErrorType();
        ProblemDetail problemDetail = ProblemDetail.forStatus(errorType.getStatus());
        problemDetail.setType(URI.create(""));
        problemDetail.setTitle(errorType.getMessage());
        problemDetail.setDetail("");
        problemDetail.setProperty("error_code", errorType.getErrorCode());
        return ResponseEntity.of(problemDetail).build();
    }

}
