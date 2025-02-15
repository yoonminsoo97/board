package com.board.domain.comment.controller;

import com.board.domain.comment.dto.CommentModifyRequest;
import com.board.domain.comment.dto.CommentWriteRequest;
import com.board.domain.comment.exception.NotFoundCommentException;
import com.board.domain.comment.service.CommentService;
import com.board.domain.post.exception.NotFoundPostException;
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
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CommentController.class)
class CommentControllerTest extends RestDocs {

    @MockitoBean
    private CommentService commentService;

    @DisplayName("댓글 작성에 성공하면 200 상태 코드를 반환한다.")
    @Test
    void commentWrite() throws Exception {
        CommentWriteRequest commentWriteRequest = new CommentWriteRequest("comment");
        Claims claims = Jwts.claims()
                .subject("yoon1234")
                .add("authority", "ROLE_MEMBER")
                .build();

        given(tokenService.getClaims(anyString())).willReturn(claims);
        willDoNothing().given(commentService).commentWrite(anyLong(), anyString(), any(CommentWriteRequest.class));

        mockMvc.perform(post("/api/posts/{postId}/comments/write", 1)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentWriteRequest))
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
                                fieldWithPath("content").type(JsonFieldType.STRING).description("내용")
                        )
                ));
    }

    @DisplayName("댓글 작성 시 내용이 공백이면 예외 응답과 400 상태 코드를 반환한다.")
    @Test
    void commentWriteBlankContent() throws Exception {
        CommentWriteRequest commentWriteRequest = new CommentWriteRequest("");
        Claims claims = Jwts.claims()
                .subject("yoon1234")
                .add("authority", "ROLE_MEMBER")
                .build();

        given(tokenService.getClaims(anyString())).willReturn(claims);

        mockMvc.perform(post("/api/posts/{postId}/comments/write", 1)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentWriteRequest))
                )
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.errorCode").value("1001"),
                        jsonPath("$.message").value("입력값이 잘못되었습니다."),
                        jsonPath("$.fields").isArray(),
                        jsonPath("$.fields[0].field").value("content"),
                        jsonPath("$.fields[0].input").value(""),
                        jsonPath("$.fields[0].message").value("내용을 입력해 주세요.")
                );
    }

    @DisplayName("댓글 작성 시 게시글이 존재하지 않으면 예외 응답과 404 상태 코드를 반환한다.")
    @Test
    void commentWriteNotFoundPost() throws Exception {
        CommentWriteRequest commentWriteRequest = new CommentWriteRequest("comment");
        Claims claims = Jwts.claims()
                .subject("yoon1234")
                .add("authority", "ROLE_MEMBER")
                .build();

        given(tokenService.getClaims(anyString())).willReturn(claims);
        willThrow(new NotFoundPostException()).given(commentService).commentWrite(anyLong(), anyString(), any(CommentWriteRequest.class));

        mockMvc.perform(post("/api/posts/{postId}/comments/write", 1)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentWriteRequest))
                )
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.errorCode").value("4003"),
                        jsonPath("$.message").value("게시글이 존재하지 않습니다.")
                );
    }

    @DisplayName("댓글 작성 시 액세스 토큰이 만료되거나 형식이 잘못되면 예외 응답과 401 상태 코드를 반환한다.")
    @ParameterizedTest
    @MethodSource("expiredAndInvalidAccessToken")
    void commentWriteExpiredAndInvalidAccessToken(String errorCode, String message) throws Exception {
        CommentWriteRequest commentWriteRequest = new CommentWriteRequest("comment");

        willThrow(new AuthenticationServiceException(errorCode)).given(tokenService).getClaims(anyString());

        mockMvc.perform(post("/api/posts/{postId}/comments/write", 1)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentWriteRequest))
                )
                .andExpectAll(
                        status().isUnauthorized(),
                        jsonPath("$.errorCode").value(errorCode),
                        jsonPath("$.message").value(message)
                );
    }

    @DisplayName("댓글 수정에 성공하면 200 상태 코드를 반환한다.")
    @Test
    void commentModify() throws Exception {
        CommentModifyRequest commentModifyRequest = new CommentModifyRequest("comment");
        Claims claims = Jwts.claims()
                .subject("yoon1234")
                .add("authority", "ROLE_MEMBER")
                .build();

        given(tokenService.getClaims(anyString())).willReturn(claims);
        willDoNothing().given(commentService).commentModify(anyLong(), anyLong(), any(CommentModifyRequest.class));

        mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}", 1, 1)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentModifyRequest))
                )
                .andExpectAll(
                        status().isOk()
                )
                .andDo(restdocs.document(
                        pathParameters(
                                parameterWithName("postId").description("게시글 번호"),
                                parameterWithName("commentId").description("댓글 번호")
                        ),
                        requestHeaders(
                                headerWithName("Authorization").description("인증을 위한 Bearer 액세스 토큰")
                        ),
                        requestFields(
                                fieldWithPath("content").type(JsonFieldType.STRING).description("내용")
                        )
                ));
    }

    @DisplayName("댓글 수정 시 내용이 공백이면 예외 응답과 400 상태 코드를 반환한다.")
    @Test
    void commentModifyBlankContent() throws Exception {
        CommentModifyRequest commentModifyRequest = new CommentModifyRequest("");

        Claims claims = Jwts.claims()
                .subject("yoon1234")
                .add("authority", "ROLE_MEMBER")
                .build();

        given(tokenService.getClaims(anyString())).willReturn(claims);

        mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}", 1, 1)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentModifyRequest))
                )
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.errorCode").value("1001"),
                        jsonPath("$.message").value("입력값이 잘못되었습니다."),
                        jsonPath("$.fields").isArray(),
                        jsonPath("$.fields[0].field").value("content"),
                        jsonPath("$.fields[0].input").value(""),
                        jsonPath("$.fields[0].message").value("내용을 입력해 주세요.")
                );
    }

    @DisplayName("댓글 수정 시 댓글이 존재하지 않으면 예외 응답과 404 상태 코드를 반환한다.")
    @Test
    void commentModifyNotFoundComment() throws Exception {
        CommentModifyRequest commentModifyRequest = new CommentModifyRequest("comment");
        Claims claims = Jwts.claims()
                .subject("yoon1234")
                .add("authority", "ROLE_MEMBER")
                .build();

        given(tokenService.getClaims(anyString())).willReturn(claims);
        willThrow(new NotFoundCommentException()).given(commentService).commentModify(anyLong(), anyLong(), any(CommentModifyRequest.class));

        mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}", 1, 1)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentModifyRequest))
                )
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.errorCode").value("4004"),
                        jsonPath("$.message").value("댓글이 존재하지 않습니다.")
                );
    }

    @DisplayName("댓글 수정 시 액세스 토큰이 만료되거나 형식이 잘못되면 예외 응답과 401 상태 코드를 반환한다.")
    @ParameterizedTest
    @MethodSource("expiredAndInvalidAccessToken")
    void commentModifyExpiredAndInvalidAccessToken(String errorCode, String message) throws Exception {
        CommentModifyRequest commentModifyRequest = new CommentModifyRequest("comment");

        willThrow(new AuthenticationServiceException(errorCode)).given(tokenService).getClaims(anyString());

        mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}", 1, 1)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentModifyRequest))
                )
                .andExpectAll(
                        status().isUnauthorized(),
                        jsonPath("$.errorCode").value(errorCode),
                        jsonPath("$.message").value(message)
                );
    }

    @DisplayName("댓글 삭제에 성공하면 200 상태 코드를 반환한다.")
    @Test
    void commentDelete() throws Exception {
        Claims claims = Jwts.claims()
                .subject("yoon1234")
                .add("authority", "ROLE_MEMBER")
                .build();

        given(tokenService.getClaims(anyString())).willReturn(claims);
        willDoNothing().given(commentService).commentDelete(anyLong(), anyLong());

        mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", 1, 1)
                        .header("Authorization", "Bearer access-token")
                )
                .andExpect(
                        status().isOk()
                )
                .andDo(restdocs.document(
                        pathParameters(
                                parameterWithName("postId").description("게시글 번호"),
                                parameterWithName("commentId").description("댓글 번호")
                        ),
                        requestHeaders(
                                headerWithName("Authorization").description("인증을 위한 Bearer 액세스 토큰")
                        )
                ));
    }

    @DisplayName("댓글 삭제 시 댓글이 존재하지 않으면 예외 응답과 404 상태 코드를 반환한다.")
    @Test
    void commentDeleteNotFoundComment() throws Exception {
        Claims claims = Jwts.claims()
                .subject("yoon1234")
                .add("authority", "ROLE_MEMBER")
                .build();

        given(tokenService.getClaims(anyString())).willReturn(claims);
        willThrow(new NotFoundCommentException()).given(commentService).commentDelete(anyLong(), anyLong());

        mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", 1, 1)
                        .header("Authorization", "Bearer access-token")
                )
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.errorCode").value("4004"),
                        jsonPath("$.message").value("댓글이 존재하지 않습니다.")
                );
    }

    @DisplayName("댓글 삭제 시 액세스 토큰이 만료되거나 형식이 잘못되면 예외 응답과 401 상태 코드를 반환한다.")
    @ParameterizedTest
    @MethodSource("expiredAndInvalidAccessToken")
    void commentDeleteExpiredAndInvalidAccessToken(String errorCode, String message) throws Exception {
        willThrow(new AuthenticationServiceException(errorCode)).given(tokenService).getClaims(anyString());

        mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", 1, 1)
                        .header("Authorization", "Bearer access-token")
                )
                .andExpectAll(
                        status().isUnauthorized(),
                        jsonPath("$.errorCode").value(errorCode),
                        jsonPath("$.message").value(message)
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