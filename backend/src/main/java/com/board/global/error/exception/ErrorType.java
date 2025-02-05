package com.board.global.error.exception;

import lombok.Getter;

import org.springframework.http.HttpStatus;

@Getter
public enum ErrorType {

    // HTTP 400 Bad Request
    INVALID_INPUT_VALUE("1001", "입력값이 잘못되었습니다.", HttpStatus.BAD_REQUEST),

    // HTTP 409 Conflict
    DUPLICATE_NICKNAME("2001", "사용 중인 닉네임입니다.", HttpStatus.CONFLICT),
    DUPLICATE_USERNAME("2002", "사용 중인 아이디입니다.", HttpStatus.CONFLICT);

    private final String errorCode;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorType(String errorCode, String message, HttpStatus httpStatus) {
        this.errorCode = errorCode;
        this.message = message;
        this.httpStatus = httpStatus;
    }

}
