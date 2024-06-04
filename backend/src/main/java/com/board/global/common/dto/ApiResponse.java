package com.board.global.common.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {

    private static final String API_REQUEST_SUCCESS_STATUS = "success";
    private static final String API_RESPONSE_SUCCESS_PROPERTY = "data";
    private static final String API_REQUEST_FAIL_STATUS = "fail";
    private static final String API_RESPONSE_FAIL_PROPERTY = "error";

    private String status;
    private Map<String, T> data;

    private ApiResponse(String status, String property, T data) {
        this.status = status;
        this.data = Collections.singletonMap(property, data);
    }

    public static ApiResponse<Void> success() {
        return new ApiResponse<>(API_REQUEST_SUCCESS_STATUS, API_RESPONSE_SUCCESS_PROPERTY, null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(API_REQUEST_SUCCESS_STATUS, API_RESPONSE_SUCCESS_PROPERTY, data);
    }

    public static ApiResponse<ErrorResponse> fail(ErrorResponse errorResponse) {
        return new ApiResponse<>(API_REQUEST_FAIL_STATUS, API_RESPONSE_FAIL_PROPERTY, errorResponse);
    }

    @JsonAnyGetter
    public Map<String, T> getData() {
        return this.data;
    }

}
