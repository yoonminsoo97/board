package com.board.global.error.exception;

import lombok.Getter;

import org.springframework.http.HttpStatus;

import java.util.Arrays;

@Getter
public enum ErrorType {

    // HTTP 500 Internal Server Error
    UNSUPPORTED_ERROR_TYPE("0000", "지원하지 않는 예외 유형입니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    // HTTP 400 Bad Request
    INVALID_INPUT_VALUE("1001", "입력값이 잘못되었습니다.", HttpStatus.BAD_REQUEST),

    // HTTP 401 Unauthorized
    BAD_CRDENTIALS("3001", "아이디 또는 비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),
    EXPIRED_TOKEN("3002", "토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN("3003", "토큰 형식이 잘못되었습니다.", HttpStatus.UNAUTHORIZED),

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

    public static ErrorType of(String errorCode) {
        return Arrays.stream(ErrorType.values())
                .filter((errorType) -> errorType.errorCode.equals(errorCode))
                .findFirst()
                .orElse(UNSUPPORTED_ERROR_TYPE);
    }

}
