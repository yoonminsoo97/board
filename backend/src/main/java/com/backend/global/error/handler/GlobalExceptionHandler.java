package com.backend.global.error.handler;

import com.backend.global.error.exception.BoardException;
import com.backend.global.error.exception.ErrorType;

import lombok.Getter;

import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BoardException.class)
    public ResponseEntity<ProblemDetail> handleBoardException(BoardException ex) {
        ProblemDetail problemDetail = createProblemDetail(ex.getErrorType());
        return ResponseEntity.of(problemDetail).build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        ProblemDetail problemDetail = createProblemDetail(ErrorType.INVALID_INPUT);
        problemDetail.setProperty("errors", Field.errors(ex.getFieldErrors()));
        return ResponseEntity.of(problemDetail).build();
    }

    private ProblemDetail createProblemDetail(ErrorType errorType) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(errorType.getStatus());
        problemDetail.setType(URI.create(""));
        problemDetail.setTitle(errorType.getMessage());
        problemDetail.setDetail("");
        problemDetail.setProperty("error_code", errorType.getErrorCode());
        return problemDetail;
    }

    @Getter
    private static class Field {

        private String field;
        private String message;

        private Field(FieldError fieldError) {
            this.field = fieldError.getField();
            this.message = fieldError.getDefaultMessage();
        }

        public static List<Field> errors(List<FieldError> fieldErrors) {
            return fieldErrors.stream()
                    .map(Field::new)
                    .collect(Collectors.toList());
        }

    }

}
