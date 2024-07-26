package com.board.domain.token.controller;

import com.board.domain.token.dto.TokenResponse;
import com.board.global.security.exception.ExpiredTokenException;
import com.board.global.security.exception.InvalidTokenException;
import com.board.support.ControllerTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TokenController.class)
class TokenControllerTest extends ControllerTest {

    @Nested
    @DisplayName("액세스 토큰 재발급 요청")
    class ReIssueAccessTokenTest {

        @Test
        @DisplayName("새로운 액세스 토큰을 발급한다")
        void reIssueAccessToken() throws Exception {
            TokenResponse tokenResponse = TokenResponse.builder()
                    .accessToken("new-access-token")
                    .build();

            given(tokenService.reIssueAccessToken(anyString())).willReturn(tokenResponse);

            mockMvc.perform(post("/api/token/reissue")
                            .header("Authorization", "Bearer refresh-token")
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
                    .andDo(restDocs.document(
                            requestHeaders(
                                    headerWithName("Authorization").description("리프레시 토큰")
                            ),
                            responseFields(
                                    commonSuccessResponse())
                                    .and(
                                            fieldWithPath("data.accessToken").type(STRING).description("새로운 액세스 토큰")
                                    )
                    ));
        }

        @Test
        @DisplayName("리프레시 토큰이 유효하지 않으면 예외가 발생한다")
        void reIssueAccessTokenInvalidRefreshToken() throws Exception {
            willThrow(new InvalidTokenException()).given(tokenService).reIssueAccessToken(anyString());

            mockMvc.perform(post("/api/token/reissue")
                            .header("Authorization", "Bearer refresh-token")
                    )
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value("fail"))
                    .andExpect(jsonPath("$.error.code").value("E401002"))
                    .andExpect(jsonPath("$.error.message").value("토큰이 유효하지 않습니다."))
                    .andExpect(jsonPath("$.error.fields").isEmpty())
                    .andDo(restDocs.document(
                            requestHeaders(
                                    headerWithName("Authorization").description("리프레시 토큰")
                            ),
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("리프레시 토큰이 만료되면 예외가 발생한다")
        void reIssueAccessTokenExpiredRefreshToken() throws Exception {
            willThrow(new ExpiredTokenException()).given(tokenService).reIssueAccessToken(anyString());

            mockMvc.perform(post("/api/token/reissue")
                            .header("Authorization", "Bearer refresh-token")
                    )
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value("fail"))
                    .andExpect(jsonPath("$.error.code").value("E401003"))
                    .andExpect(jsonPath("$.error.message").value("토큰이 만료되었습니다."))
                    .andExpect(jsonPath("$.error.fields").isEmpty())
                    .andDo(restDocs.document(
                            requestHeaders(
                                    headerWithName("Authorization").description("리프레시 토큰")
                            ),
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

    }

}