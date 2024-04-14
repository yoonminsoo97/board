package com.board.domain.token.controller;

import com.board.support.RestDocsTestSupport;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;

import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
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
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.result").value("new-access-token"))
                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName("Authorization").description("리프레시 토큰")
                        ),
                        responseFields(
                                commonSuccessResponse()
                        )
                ));
    }

}