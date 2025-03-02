package com.backend.domain.post.controller;

import com.backend.domain.post.dto.PostWriteRequest;
import com.backend.domain.post.service.PostService;
import com.backend.support.ControllerTest;

import com.backend.global.error.exception.ErrorType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;

import static org.mockito.BDDMockito.willThrow;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PostController.class)
class PostControllerTest extends ControllerTest {

    @MockitoBean
    private PostService postService;

    @DisplayName("게시글 작성에 성공하면 200을 응답한다.")
    @Test
    void postWrite() throws Exception {
        PostWriteRequest postWriteRequest = new PostWriteRequest("title", "content");
        Claims claims = Jwts.claims()
                .add("username", "yoon1234")
                .add("authority", "ROLE_MEMBER")
                .build();

        willDoNothing().given(tokenService).validateToken(anyString());
        given(tokenService.extractClaim(anyString())).willReturn(claims);
        willDoNothing().given(postService).postWrite(any(PostWriteRequest.class), anyString());

        mockMvc.perform(post("/api/posts/write")
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postWriteRequest))
                )
                .andExpect(status().isOk())
                .andDo(restdocs)
                .andDo(restdocs.document(
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        requestFields(
                                fieldWithPath("title").type(JsonFieldType.STRING).description("제목"),
                                fieldWithPath("content").type(JsonFieldType.STRING).description("내용")
                        )
                ));
    }

    @DisplayName("게시글 작성 시 입력값이 유효하지 않으면 400을 응답한다.")
    @ParameterizedTest
    @MethodSource("invalidInputPostWriteRequest")
    void postWriteInvalidInput(PostWriteRequest postWriteRequest) throws Exception {
        Claims claims = Jwts.claims()
                .add("username", "yoon1234")
                .add("authority", "ROLE_MEMBER")
                .build();

        willDoNothing().given(tokenService).validateToken(anyString());
        given(tokenService.extractClaim(anyString())).willReturn(claims);

        mockMvc.perform(post("/api/posts/write")
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postWriteRequest))
                )
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.status").value(400),
                        jsonPath("$.errorCode").value("E400001"),
                        jsonPath("$.message").value("입력값이 잘못 되었습니다."),
                        jsonPath("$.errors").isArray(),
                        jsonPath("$.errors[0].field").isNotEmpty(),
                        jsonPath("$.errors[0].message").isNotEmpty()
                );
    }

    private static Stream<Object> invalidInputPostWriteRequest() {
        return Stream.of(
                Arguments.of(Named.of("제목 공백", new PostWriteRequest("", "content"))),
                Arguments.of(Named.of("내용 공백", new PostWriteRequest("title", "")))
        );
    }

    @DisplayName("게시글 작성 시 access token이 만료되면 401을 응답한다.")
    @Test
    void postWriteExpiredAccessToken() throws Exception {
        PostWriteRequest postWriteRequest = new PostWriteRequest("title", "content");
        String errorCode = ErrorType.EXPIRED_TOKEN.getErrorCode();

        willThrow(new AuthenticationServiceException(errorCode)).given(tokenService).validateToken(anyString());

        mockMvc.perform(post("/api/posts/write")
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postWriteRequest))
                )
                .andExpectAll(
                        status().isUnauthorized(),
                        jsonPath("$.status").value(401),
                        jsonPath("$.errorCode").value("E401002"),
                        jsonPath("$.message").value("토큰이 만료 되었습니다.")
                );
    }

    @DisplayName("게시글 작성 시 access token 형식이 잘못되면 401을 응답한다.")
    @Test
    void postWriteInvalidAccessToken() throws Exception {
        PostWriteRequest postWriteRequest = new PostWriteRequest("title", "content");
        String errorCode = ErrorType.INVALID_TOKEN.getErrorCode();

        willThrow(new AuthenticationServiceException(errorCode)).given(tokenService).validateToken(anyString());

        mockMvc.perform(post("/api/posts/write")
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postWriteRequest))
                )
                .andExpectAll(
                        status().isUnauthorized(),
                        jsonPath("$.status").value(401),
                        jsonPath("$.errorCode").value("E401003"),
                        jsonPath("$.message").value("토큰 형식이 잘못 되었습니다.")
                );
    }

}