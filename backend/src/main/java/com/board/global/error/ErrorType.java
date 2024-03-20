package com.board.global.error;

import lombok.Getter;

import org.springframework.http.HttpStatus;

import java.util.Arrays;

@Getter
public enum ErrorType {

    UN_SUPPORT_ERROR_TYPE("E000000", HttpStatus.NOT_FOUND.value(), "지원하지 않는 예외 유형입니다."),
    INVALID_INPUT_VALUE("E400001", HttpStatus.BAD_REQUEST.value(), "입력값이 잘못되었습니다."),
    PASSWORD_MISMATCH("E400002", HttpStatus.BAD_REQUEST.value(), "비밀번호가 일치하지 않습니다."),
    BAD_CREDENTIALS("E401001", HttpStatus.UNAUTHORIZED.value(), "아이디 또는 비밀번호가 일치하지 않습니다."),
    INVALID_TOKEN("E401002", HttpStatus.UNAUTHORIZED.value(), "토큰이 유효하지 않습니다."),
    EXPIRED_TOKEN("E401003", HttpStatus.UNAUTHORIZED.value(), "토큰이 만료되었습니다."),
    NOT_FOUND_MEMBER("E404001", HttpStatus.NOT_FOUND.value(), "회원을 찾을 수 없습니다."),
    NOT_FOUND_POST("E404002", HttpStatus.NOT_FOUND.value(), "게시글을 찾을 수 없습니다."),
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

    public static ErrorType of(String erroCode) {
        return Arrays.stream(ErrorType.values())
                .filter(errorType -> errorType.errorCode.equals(erroCode))
                .findFirst()
                .orElse(ErrorType.UN_SUPPORT_ERROR_TYPE);
    }

}
