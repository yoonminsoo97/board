package com.board.global.error;

import lombok.Getter;

import org.springframework.http.HttpStatus;

@Getter
public enum ErrorType {

    UN_SUPPORT_ERROR_TYPE("E000000", HttpStatus.NOT_FOUND.value(), "지원하지 않는 예외 유형입니다."),
    INVALID_INPUT_VALUE("E400001", HttpStatus.BAD_REQUEST.value(), "입력값이 잘못되었습니다."),
    PASSWORD_MISMATCH("E400002", HttpStatus.BAD_REQUEST.value(), "비밀번호가 일치하지 않습니다."),
    DUPLICATE_NICKNAME("E409001", HttpStatus.CONFLICT.value(), "사용 중인 닉네임입니다."),
    DUPLICATE_USERNAME("E409002", HttpStatus.CONFLICT.value(), "사용 중인 아이디입니다.");

    private final String errorCode;
    private final int status;
    private final String message;

    ErrorType(String errorCode, int status, String message) {
        this.errorCode = errorCode;
        this.status = status;
        this.message = message;
    }

}
