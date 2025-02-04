package com.board.global.error.exception;

import lombok.Getter;

import org.springframework.http.HttpStatus;

@Getter
public enum ErrorType {

    ;

    private final String errorCode;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorType(String errorCode, String message, HttpStatus httpStatus) {
        this.errorCode = errorCode;
        this.message = message;
        this.httpStatus = httpStatus;
    }

}
