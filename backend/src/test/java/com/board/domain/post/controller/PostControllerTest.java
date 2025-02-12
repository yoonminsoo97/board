package com.board.domain.post.controller;

import com.board.domain.post.dto.PostDetailResponse;
import com.board.domain.post.dto.PostModifyRequest;
import com.board.domain.post.dto.PostWriteRequest;
import com.board.domain.post.exception.NotFoundPostException;
import com.board.domain.post.service.PostService;
import com.board.domain.token.service.TokenService;
import com.board.global.security.service.MemberUserDetailsService;
import com.board.restdocs.RestDocs;

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

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PostController.class)
class PostControllerTest extends RestDocs {

    @MockitoBean
    private MemberUserDetailsService memberUserDetailsService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private PostService postService;

    @DisplayName("게시글 작성에 성공하면 200 상태 코드를 반환한다.")
    @Test
    void postWrite() throws Exception {
        PostWriteRequest postWriteRequest = new PostWriteRequest("title", "content");
        Claims claims = Jwts.claims()
                .subject("yoon1234")
                .add("authority", "ROLE_MEMBER")
                .build();

        given(tokenService.getClaims(anyString())).willReturn(claims);
        willDoNothing().given(postService).postWrite(anyString(), any(PostWriteRequest.class));

        mockMvc.perform(post("/api/posts/write")
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postWriteRequest))
                )
                .andExpect(
                        status().isOk()
                )
                .andDo(restdocs.document(
                        requestHeaders(
                                headerWithName("Authorization").description("인증을 위한 Bearer 액세스 토큰")
                        ),
                        requestFields(
                                fieldWithPath("title").type(JsonFieldType.STRING).description("게시글 제목"),
                                fieldWithPath("content").type(JsonFieldType.STRING).description("게시글 내용")
                        )
                ));
    }

    @DisplayName("게시글 작성 시 입력값이 비어 있으면 예외 메시지와 400 상태 코드를 반환한다.")
    @ParameterizedTest
    @MethodSource("postWriteRequestBlankFields")
    void postWriteBlankFields(PostWriteRequest postWriteRequest, String field, String message) throws Exception {
        Claims claims = Jwts.claims()
                .subject("yoon1234")
                .add("authority", "ROLE_MEMBER")
                .build();

        given(tokenService.getClaims(anyString())).willReturn(claims);

        mockMvc.perform(post("/api/posts/write")
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postWriteRequest))
                )
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.errorCode").value("1001"),
                        jsonPath("$.message").value("입력값이 잘못되었습니다."),
                        jsonPath("$.fields").isArray(),
                        jsonPath("$.fields[0].field").value(field),
                        jsonPath("$.fields[0].input").value(""),
                        jsonPath("$.fields[0].message").value(message)
                );
    }

    @DisplayName("게시글 작성 시 액세스 토큰이 만료되거나 형식이 잘못되면 예외 응답과 401 상태 코드를 반환한다.")
    @ParameterizedTest
    @MethodSource("expiredAndInvalidAccessToken")
    void postWriteExpiredAndInvalidAccessToken(String errorCode, String message) throws Exception {
        PostWriteRequest postWriteRequest = new PostWriteRequest("title", "content");

        willThrow(new AuthenticationServiceException(errorCode)).given(tokenService).getClaims(anyString());

        mockMvc.perform(post("/api/posts/write")
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postWriteRequest))
                )
                .andExpectAll(
                        status().isUnauthorized(),
                        jsonPath("$.errorCode").value(errorCode),
                        jsonPath("$.message").value(message)
                );
    }

    @DisplayName("게시글 상세조회에 성공하면 게시글 정보와 200 상태 코드를 반환한다.")
    @Test
    void postDetail() throws Exception {
        PostDetailResponse postDetailResponse = new PostDetailResponse(1L, "title", "writer", "content", LocalDateTime.now());

        given(postService.postDetail(anyLong())).willReturn(postDetailResponse);

        mockMvc.perform(get("/api/posts/{postId}", 1))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.postId").value(1),
                        jsonPath("$.title").value("title"),
                        jsonPath("$.writer").value("writer"),
                        jsonPath("$.content").value("content"),
                        jsonPath("$.createdAt").isNotEmpty()
                )
                .andDo(restdocs.document(
                        pathParameters(
                                parameterWithName("postId").description("게시글 번호")
                        ),
                        responseFields(
                                fieldWithPath("postId").type(JsonFieldType.NUMBER).description("글번호"),
                                fieldWithPath("title").type(JsonFieldType.STRING).description("제목"),
                                fieldWithPath("writer").type(JsonFieldType.STRING).description("작성자"),
                                fieldWithPath("content").type(JsonFieldType.STRING).description("내용"),
                                fieldWithPath("createdAt").type(JsonFieldType.STRING).description("작성일")
                        )
                ));
    }

    @DisplayName("게시글 상세조회 시 게시글이 존재하지 않으면 예외 응답과 404 상태 코드를 반환한다.")
    @Test
    void postDetailNotFoundPost() throws Exception {
        willThrow(new NotFoundPostException()).given(postService).postDetail(anyLong());

        mockMvc.perform(get("/api/posts/{postId}", 1))
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.errorCode").value("4003"),
                        jsonPath("$.message").value("게시글이 존재하지 않습니다.")
                );
    }

    @DisplayName("게시글 수정에 성공하면 200 상태 코드를 반환한다.")
    @Test
    void postModify() throws Exception {
        PostModifyRequest postModifyRequest = new PostModifyRequest("title", "content");
        Claims claims = Jwts.claims()
                .subject("yoon1234")
                .add("authority", "ROLE_MEMBER")
                .build();

        given(tokenService.getClaims(anyString())).willReturn(claims);
        willDoNothing().given(postService).postModify(anyLong(), any(PostModifyRequest.class));

        mockMvc.perform(put("/api/posts/{postId}", 1)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postModifyRequest))
                )
                .andExpect(
                        status().isOk()
                )
                .andDo(restdocs.document(
                        pathParameters(
                                parameterWithName("postId").description("게시글 번호")
                        ),
                        requestHeaders(
                                headerWithName("Authorization").description("인증을 위한 Bearer 액세스 토큰")
                        ),
                        requestFields(
                                fieldWithPath("title").type(JsonFieldType.STRING).description("게시글 제목"),
                                fieldWithPath("content").type(JsonFieldType.STRING).description("게시글 내용")
                        )
                ));
    }

    @DisplayName("게시글 수정 시 입력값이 비어 있으면 예외 메시지와 400 상태 코드를 반환한다.")
    @ParameterizedTest
    @MethodSource("postModifyRequestBlankFields")
    void postModifyBlankFields(PostModifyRequest postModifyRequest, String field, String message) throws Exception {
        Claims claims = Jwts.claims()
                .subject("yoon1234")
                .add("authority", "ROLE_MEMBER")
                .build();

        given(tokenService.getClaims(anyString())).willReturn(claims);

        mockMvc.perform(put("/api/posts/{postId}", 1)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postModifyRequest))
                )
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.errorCode").value("1001"),
                        jsonPath("$.message").value("입력값이 잘못되었습니다."),
                        jsonPath("$.fields").isArray(),
                        jsonPath("$.fields[0].field").value(field),
                        jsonPath("$.fields[0].input").value(""),
                        jsonPath("$.fields[0].message").value(message)
                );
    }

    @DisplayName("게시글 수정 시 액세스 토큰이 만료되거나 형식이 잘못되면 예외 응답과 401 상태 코드를 반환한다.")
    @ParameterizedTest
    @MethodSource("expiredAndInvalidAccessToken")
    void postModifyExpiredAndInvalidAccessToken(String errorCode, String message) throws Exception {
        PostModifyRequest postModifyRequest = new PostModifyRequest("title", "content");

        willThrow(new AuthenticationServiceException(errorCode)).given(tokenService).getClaims(anyString());

        mockMvc.perform(put("/api/posts/{postId}", 1)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postModifyRequest))
                )
                .andExpectAll(
                        status().isUnauthorized(),
                        jsonPath("$.errorCode").value(errorCode),
                        jsonPath("$.message").value(message)
                );
    }

    @DisplayName("게시글 삭제에 성공하면 200 상태 코드를 반환한다.")
    @Test
    void postDelete() throws Exception {
        Claims claims = Jwts.claims()
                .subject("yoon1234")
                .add("authority", "ROLE_MEMBER")
                .build();

        given(tokenService.getClaims(anyString())).willReturn(claims);
        willDoNothing().given(postService).postDelete(anyLong());

        mockMvc.perform(delete("/api/posts/{postId}", 1)
                        .header("Authorization", "Bearer access-token")
                )
                .andExpect(
                        status().isOk()
                )
                .andDo(restdocs.document(
                        pathParameters(
                                parameterWithName("postId").description("게시글 번호")
                        ),
                        requestHeaders(
                                headerWithName("Authorization").description("인증을 위한 Bearer 액세스 토큰")
                        )
                ));
    }

    @DisplayName("게시글 삭제 시 게시글이 존재하지 않으면 예외 응답과 404 상태 코드를 반환한다.")
    @Test
    void postDeleteNotFoundPost() throws Exception {
        Claims claims = Jwts.claims()
                .subject("yoon1234")
                .add("authority", "ROLE_MEMBER")
                .build();

        given(tokenService.getClaims(anyString())).willReturn(claims);
        willThrow(new NotFoundPostException()).given(postService).postDelete(anyLong());

        mockMvc.perform(delete("/api/posts/{postId}", 1)
                        .header("Authorization", "Bearer access-token")
                )
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.errorCode").value("4003"),
                        jsonPath("$.message").value("게시글이 존재하지 않습니다.")
                );
    }

    @DisplayName("게시글 삭제 시 액세스 토큰이 만료되거나 형식이 잘못되면 예외 응답과 401 상태 코드를 반환한다.")
    @ParameterizedTest
    @MethodSource("expiredAndInvalidAccessToken")
    void postDeleteExpiredAndInvalidAccessToken(String errorCode, String message) throws Exception {
        willThrow(new AuthenticationServiceException(errorCode)).given(tokenService).getClaims(anyString());

        mockMvc.perform(delete("/api/posts/{postId}", 1)
                        .header("Authorization", "Bearer access-token")
                )
                .andExpectAll(
                        status().isUnauthorized(),
                        jsonPath("$.errorCode").value(errorCode),
                        jsonPath("$.message").value(message)
                );
    }

    // 게시글 작성 시 입력값 빈값 검증 반복 테스트 시 사용하는 메서드
    private static Stream<Arguments> postWriteRequestBlankFields() {
        return Stream.of(
                Arguments.of(
                        Named.of("제목 공백", new PostWriteRequest("", "content")),
                        "title", "제목을 입력해 주세요."
                ),
                Arguments.of(
                        Named.of("내용 공백", new PostWriteRequest("title", "")),
                        "content", "내용을 입력해 주세요."
                )
        );
    }

    // 게시글 수정 시 입력값 빈값 검증 반복 테스트 시 사용하는 메서드
    private static Stream<Arguments> postModifyRequestBlankFields() {
        return Stream.of(
                Arguments.of(
                        Named.of("제목 공백", new PostModifyRequest("", "content")),
                        "title", "제목을 입력해 주세요."
                ),
                Arguments.of(
                        Named.of("제목 공백", new PostModifyRequest("", "content")),
                        "title", "제목을 입력해 주세요."
                )
        );
    }

    // 액세스 토큰 만료 및 잘못된 형식 검증 반복 테스트 시 사용하는 메서드
    private static Stream<Arguments> expiredAndInvalidAccessToken() {
        return Stream.of(
                Arguments.of(Named.of("액세스 토큰 만료", "3002"), "토큰이 만료되었습니다."),
                Arguments.of(Named.of("액세스 토큰 잘못된 형식", "3003"), "토큰 형식이 잘못되었습니다.")
        );
    }

}