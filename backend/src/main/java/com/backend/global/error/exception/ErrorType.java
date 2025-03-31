package com.backend.global.error.exception;

import lombok.Getter;

import org.springframework.http.HttpStatus;

import java.util.Arrays;

@Getter
public enum ErrorType {

    // 500
    UN_SUPPORT_ERROR_TYPE(HttpStatus.INTERNAL_SERVER_ERROR, "E000000", "지원하지 않는 예외 유형입니다."),

    // 400
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "E400001", "입력값이 잘못 되었습니다."),

    // 401
    BAD_CREDENTIALS(HttpStatus.UNAUTHORIZED, "E401001", "아이디 또는 비밀번호가 잘못 되었습니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "E401002", "토큰이 만료 되었습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "E401003", "토큰 형식이 잘못 되었습니다."),

    // 403
    ACCESS_DENIED_MODIFY_POST(HttpStatus.FORBIDDEN, "E403001", "다른 사용자의 게시글을 수정 할 수 없습니다."),
    ACCESS_DENIED_DELETE_POST(HttpStatus.FORBIDDEN, "E403002", "다른 사용자의 게시글을 삭제 할 수 없습니다."),
    ACCESS_DENIED_MODIFY_COMMENT(HttpStatus.FORBIDDEN, "E403003", "다른 사용자의 댓글을 수정 할 수 없습니다."),
    ACCESS_DENIED_DELETE_COMMENT(HttpStatus.FORBIDDEN, "E403004", "다른 사용자의 댓글을 삭제 할 수 없습니다."),

    // 404
    NOT_FOUND_TOKEN(HttpStatus.NOT_FOUND, " E404001", "토큰이 존재하지 않습니다."),
    NOT_FOUND_MEMBER(HttpStatus.NOT_FOUND, "E404002", "회원이 존재하지 않습니다."),
    NOT_FOUND_POST(HttpStatus.NOT_FOUND, "E404003", "게시글이 존재하지 않습니다."),
    NOT_FOUND_COMMENT(HttpStatus.NOT_FOUND, "E404004", "댓글이 존재하지 않습니다."),

    // 409
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "E409001", "사용 중인 닉네임입니다."),
    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "E409002", "사용 중인 아이디입니다.");

    private final HttpStatus status;
    private final String errorCode;
    private final String message;

    ErrorType(HttpStatus status, String errorCode, String message) {
        this.status = status;
        this.errorCode = errorCode;
        this.message = message;
    }

    public static ErrorType of(String errorCode) {
        return Arrays.stream(ErrorType.values())
                .filter(errorType -> errorType.errorCode.equals(errorCode))
                .findFirst()
                .orElse(UN_SUPPORT_ERROR_TYPE);
    }

}
