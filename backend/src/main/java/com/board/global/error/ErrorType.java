package com.board.global.error;

import lombok.Getter;

import org.springframework.http.HttpStatus;

@Getter
public enum ErrorType {

    UN_SUPPORT_ERROR_TYPE("E000000", HttpStatus.NOT_FOUND.value(), "지원하지 않는 예외 유형입니다.");

    private final String errorCode;
    private final int status;
    private final String message;

    ErrorType(String errorCode, int status, String message) {
        this.errorCode = errorCode;
        this.status = status;
        this.message = message;
    }

}
