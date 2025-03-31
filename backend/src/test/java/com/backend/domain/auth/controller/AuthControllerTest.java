package com.backend.domain.auth.controller;

import com.backend.domain.auth.dto.LoginRequest;
import com.backend.domain.auth.dto.TokenResponse;
import com.backend.domain.auth.exception.BadCredentialsException;
import com.backend.domain.auth.exception.ExpiredTokenException;
import com.backend.domain.auth.exception.InvalidTokenException;
import com.backend.domain.auth.service.AuthService;
import com.backend.support.ControllerTest;
import com.backend.global.error.exception.ErrorType;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
class AuthControllerTest extends ControllerTest {

    @MockitoBean
    private AuthService authService;

    @DisplayName("로그인에 성공하면 access token과 refresh token을 응답한다.")
    @Test
    void memberLogin() throws Exception {
        LoginRequest loginRequest = new LoginRequest("yoon1234", "12345678");
        TokenResponse tokenResponse = new TokenResponse("access-token", "refresh-token");

        given(authService.memberLogin(any(LoginRequest.class))).willReturn(tokenResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
                )
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.accessToken").value("access-token"),
                        jsonPath("$.refreshToken").value("refresh-token")
                )
                .andDo(restdocs)
                .andDo(restdocs.document(
                        requestFields(
                                fieldWithPath("username").type(JsonFieldType.STRING).description("아이디"),
                                fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호")
                        ),
                        responseFields(
                                fieldWithPath("accessToken").type(JsonFieldType.STRING).description("액세스 토큰"),
                                fieldWithPath("refreshToken").type(JsonFieldType.STRING).description("리프레시 토큰")
                        )
                ));
    }

    @DisplayName("로그인 시 아이디 또는 비밀번호가 잘못되면 401을 응답한다.")
    @Test
    void memberLoginBadCredentials() throws Exception {
        LoginRequest loginRequest = new LoginRequest("yoon1234", "12345678");

        willThrow(new BadCredentialsException()).given(authService).memberLogin(any(LoginRequest.class));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
                )
                .andExpectAll(
                        status().isUnauthorized(),
                        jsonPath("$.status").value(401),
                        jsonPath("$.errorCode").value("E401001"),
                        jsonPath("$.message").value("아이디 또는 비밀번호가 잘못 되었습니다.")
                );
    }

    @DisplayName("로그아웃에 성공하면 200을 응답한다.")
    @Test
    void memberLogout() throws Exception {
        Claims claims = Jwts.claims()
                .add("username", "yoon1234")
                .add("authority", "ROLE_MEMBER")
                .build();

        willDoNothing().given(tokenService).validateToken(anyString());
        given(tokenService.extractClaim(anyString())).willReturn(claims);
        willDoNothing().given(authService).memberLogout(anyString(), anyString());

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer access-token")
                )
                .andExpect(status().isOk())
                .andDo(restdocs)
                .andDo(restdocs.document(
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        )
                ));
    }

    @DisplayName("로그아웃 시 액세스 토큰이 만료되면 401을 응답한다.")
    @Test
    void memberLogoutExpiredAccessToken() throws Exception {
        ErrorType errorType = ErrorType.EXPIRED_TOKEN;

        willThrow(new ExpiredTokenException()).given(tokenService).validateToken(anyString());

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer access-token")
                )
                .andExpectAll(
                        status().isUnauthorized(),
                        jsonPath("$.status").value(401),
                        jsonPath("$.errorCode").value("E401002"),
                        jsonPath("$.message").value("토큰이 만료 되었습니다.")
                );
    }

    @DisplayName("로그아웃 시 액세스 토큰 형식이 잘못되면 401을 응답한다.")
    @Test
    void memberLogoutInvalidAccessToken() throws Exception {
        ErrorType errorType = ErrorType.INVALID_TOKEN;

        willThrow(new InvalidTokenException()).given(tokenService).validateToken(anyString());

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer access-token")
                )
                .andExpectAll(
                        status().isUnauthorized(),
                        jsonPath("$.status").value(401),
                        jsonPath("$.errorCode").value("E401003"),
                        jsonPath("$.message").value("토큰 형식이 잘못 되었습니다.")
                );
    }

}