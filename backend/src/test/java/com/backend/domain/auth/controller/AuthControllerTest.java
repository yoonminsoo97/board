package com.backend.domain.auth.controller;

import com.backend.domain.auth.dto.MemberLoginRequest;
import com.backend.domain.auth.dto.MemberLoginResponse;
import com.backend.domain.auth.exception.BadCredentialsException;
import com.backend.domain.auth.service.AuthService;
import com.backend.global.security.config.SecurityConfig;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @DisplayName("로그인에 성공하면 access token과 refresh token을 응답한다.")
    @Test
    void memberLogin() throws Exception {
        MemberLoginRequest memberLoginRequest = new MemberLoginRequest("yoon1234", "12345678");
        MemberLoginResponse memberLoginResponse = new MemberLoginResponse("access-token", "refresh-token");

        given(authService.memberLogin(any(MemberLoginRequest.class))).willReturn(memberLoginResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberLoginRequest))
                )
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.accessToken").value("access-token"),
                        jsonPath("$.refreshToken").value("refresh-token")
                )
                .andDo(print());
    }

    @DisplayName("로그인 시 아이디 또는 비밀번호가 잘못되면 401을 응답한다.")
    @Test
    void memberLoginBadCredentials() throws Exception {
        MemberLoginRequest memberLoginRequest = new MemberLoginRequest("yoon1234", "12345678");

        willThrow(new BadCredentialsException()).given(authService).memberLogin(any(MemberLoginRequest.class));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberLoginRequest))
                )
                .andExpectAll(
                        status().isUnauthorized(),
                        jsonPath("$.title").value("아이디 또는 비밀번호가 잘못 되었습니다."),
                        jsonPath("$.status").value(401),
                        jsonPath("$.instance").value("/api/auth/login"),
                        jsonPath("$.error_code").value("E401001")
                )
                .andDo(print());
    }

}