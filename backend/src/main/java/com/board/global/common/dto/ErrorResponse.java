package com.board.global.common.dto;

import com.board.global.error.ErrorType;

import jakarta.validation.ConstraintViolation;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.springframework.validation.BindingResult;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorResponse {

    private String code;
    private String message;
    private List<Field> fields;

    private ErrorResponse(ErrorType errorType) {
        this.code = errorType.getErrorCode();
        this.message = errorType.getMessage();
        this.fields = Collections.emptyList();
    }

    private ErrorResponse(ErrorType errorType, BindingResult bindingResult) {
        this.code = errorType.getErrorCode();
        this.message = errorType.getMessage();
        this.fields = Field.errors(bindingResult);
    }

    private ErrorResponse(ErrorType errorType, Set<ConstraintViolation<?>> constraintViolations) {
        this.code = errorType.getErrorCode();
        this.message = errorType.getMessage();
        this.fields = Field.erros(constraintViolations);
    }

    public static ErrorResponse of(ErrorType errorType) {
        return new ErrorResponse(errorType);
    }

    public static ErrorResponse of(ErrorType errorType, BindingResult bindingResult) {
        return new ErrorResponse(errorType, bindingResult);
    }

    public static ErrorResponse of(ErrorType errorType, Set<ConstraintViolation<?>> constraintViolations) {
        return new ErrorResponse(errorType, constraintViolations);
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static class Field {

        private String field;
        private String input;
        private String message;

        private Field(String field, String input, String message) {
            this.field = field;
            this.input = input;
            this.message = message;
        }

        public static List<Field> errors(BindingResult bindingResult) {
            return bindingResult.getFieldErrors()
                    .stream()
                    .map(fieldError -> new Field(
                            fieldError.getField(),
                            fieldError.getRejectedValue() == null ? "" : fieldError.getRejectedValue().toString(),
                            fieldError.getDefaultMessage()
                    )).collect(Collectors.toList());
        }

        public static List<Field> erros(Set<ConstraintViolation<?>> constraintViolations) {
            return constraintViolations.stream()
                    .map(constraintViolation -> new Field(
                            constraintViolation.getPropertyPath().toString().split("\\.")[1],
                            constraintViolation.getInvalidValue().toString(),
                            constraintViolation.getMessage()
                    )).collect(Collectors.toList());
        }

    }

}
