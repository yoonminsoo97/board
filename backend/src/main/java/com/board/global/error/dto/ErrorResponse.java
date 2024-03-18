package com.board.global.error.dto;

import com.board.global.error.ErrorType;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ErrorResponse {

    private final String errorCode;
    private final int status;
    private final String message;
    private final LocalDateTime timeStamp;

    private ErrorResponse(ErrorType errorType) {
        this.errorCode = errorType.getErrorCode();
        this.status = errorType.getStatus();
        this.message = errorType.getMessage();
        this.timeStamp = LocalDateTime.now();
    }

    public static ErrorResponse of(ErrorType errorType) {
        return new ErrorResponse(errorType);
    }

}
