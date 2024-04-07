package com.board.domain.token.controller;

import com.board.domain.token.exception.InvalidTokenException;
import com.board.support.RestDocsTestSupport;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TokenController.class)
class TokenControllerTest extends RestDocsTestSupport {

    @Test
    @DisplayName("새로운 Access Token을 발급 받는다")
    void reIssueAccessToken() throws Exception {
        given(tokenService.reIssueAccessToken(anyString())).willReturn("new-access-token");

        mockMvc.perform(post("/api/tokens/reissue")
                        .header("Authorization", "Bearer refresh-token")
                )
                .andExpect(status().isOk())
                .andExpect(content().string("new-access-token"))
                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName("Authorization").description("Refresh Token 헤더")
                        )
                ));
    }

    @Test
    @DisplayName("새로운 Access Token 발급 시 Refresh Token이 존재하지 않으면 예외가 발생한다")
    void reIssueAccessTokenNotFoundRefreshToken() throws Exception {
        willThrow(new InvalidTokenException()).given(tokenService).reIssueAccessToken(anyString());

        mockMvc.perform(post("/api/tokens/reissue")
                        .header("Authorization", "Bearer unknown")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("E401002"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("토큰이 유효하지 않습니다."))
                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName("Authorization").description("Refresh Token 헤더")
                        ),
                        responseFields(
                                fieldWithPath("errorCode").type(STRING).description("에러 코드"),
                                fieldWithPath("status").type(NUMBER).description("상태 코드"),
                                fieldWithPath("message").type(STRING).description("에러 메세지"),
                                fieldWithPath("timeStamp").type(STRING).description("에러 발생 시간")
                        )
                ));
    }

}