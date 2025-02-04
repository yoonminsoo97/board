package com.board.global.error.dto;

import com.board.global.error.exception.ErrorType;

import lombok.Getter;

@Getter
public class ErrorResponse {

    private String errorCode;
    private String message;

    private ErrorResponse(ErrorType errorType) {
        this.errorCode = errorType.getErrorCode();
        this.message = errorType.getMessage();
    }

    public static ErrorResponse of(ErrorType errorType) {
        return new ErrorResponse(errorType);
    }

}
