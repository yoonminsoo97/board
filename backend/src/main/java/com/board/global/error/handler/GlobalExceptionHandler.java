package com.board.global.error.handler;

import com.board.global.error.dto.ErrorResponse;
import com.board.global.error.exception.APIException;
import com.board.global.error.exception.ErrorType;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(APIException.class)
    public ResponseEntity<ErrorResponse> handle(APIException ex) {
        ErrorType errorType = ex.getErrorType();
        ErrorResponse errorResponse = ErrorResponse.of(errorType);
        return ResponseEntity.status(errorType.getHttpStatus()).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handle(MethodArgumentNotValidException ex) {
        ErrorResponse errorResponse = ErrorResponse.of(ErrorType.INVALID_INPUT_VALUE, ex.getFieldErrors());
        return ResponseEntity.badRequest().body(errorResponse);
    }

}
