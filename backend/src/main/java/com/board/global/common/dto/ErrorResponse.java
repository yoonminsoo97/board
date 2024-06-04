package com.board.global.common.dto;

import com.board.global.error.ErrorType;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.Collections;
import java.util.List;
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

    public static ErrorResponse of(ErrorType errorType) {
        return new ErrorResponse(errorType);
    }

    public static ErrorResponse of(ErrorType errorType, BindingResult bindingResult) {
        return new ErrorResponse(errorType, bindingResult);
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static class Field {

        private String field;
        private String input;
        private String message;

        private Field(FieldError fieldError) {
            this.field = fieldError.getField();
            this.input= fieldError.getRejectedValue() == null ? "" : fieldError.getRejectedValue().toString();
            this.message = fieldError.getDefaultMessage();
        }

        public static List<Field> errors(BindingResult bindingResult) {
            return bindingResult.getFieldErrors()
                    .stream()
                    .map(Field::new)
                    .collect(Collectors.toList());
        }

    }

}
