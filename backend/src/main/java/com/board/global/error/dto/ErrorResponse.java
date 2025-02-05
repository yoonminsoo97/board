package com.board.global.error.dto;

import com.board.global.error.exception.ErrorType;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;

import org.springframework.validation.FieldError;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class ErrorResponse {

    private String errorCode;
    private String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Field> fields;

    private ErrorResponse(ErrorType errorType) {
        this.errorCode = errorType.getErrorCode();
        this.message = errorType.getMessage();
    }

    private ErrorResponse(ErrorType errorType, List<FieldError> fieldErrors) {
        this(errorType);
        this.fields = Field.of(fieldErrors);
    }

    public static ErrorResponse of(ErrorType errorType) {
        return new ErrorResponse(errorType);
    }

    public static ErrorResponse of(ErrorType errorType, List<FieldError> fieldErrors) {
        return new ErrorResponse(errorType, fieldErrors);
    }

    @Getter
    private static class Field {

        private String field;
        private String input;
        private String message;

        private Field(FieldError fieldError) {
            this.field = fieldError.getField();
            this.input = fieldError.getRejectedValue().toString();
            this.message = fieldError.getDefaultMessage();
        }

        private static List<Field> of(List<FieldError> fieldErrors) {
            return fieldErrors.stream()
                    .map(Field::new)
                    .collect(Collectors.toList());
        }

    }

}
