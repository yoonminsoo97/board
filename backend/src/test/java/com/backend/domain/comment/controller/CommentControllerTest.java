package com.backend.domain.comment.controller;

import com.backend.domain.comment.dto.CommentItem;
import com.backend.domain.comment.dto.CommentListResponse;
import com.backend.domain.comment.dto.CommentModifyRequest;
import com.backend.domain.comment.dto.CommentWriteRequest;
import com.backend.domain.comment.exception.AccessDeniedCommentException;
import com.backend.domain.comment.exception.NotFoundCommentException;
import com.backend.domain.comment.service.CommentService;
import com.backend.domain.post.exception.NotFoundPostException;
import com.backend.global.error.exception.ErrorType;
import com.backend.support.ControllerTest;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CommentController.class)
class CommentControllerTest extends ControllerTest {

    @MockitoBean
    private CommentService commentService;

    @DisplayName("댓글 목록 조회에 성공하면 200을 응답한다.")
    @Test
    void commentList() throws Exception {
        List<CommentItem> comments = List.of(
                new CommentItem(1L, "writer", "comment", LocalDateTime.now())
        );
        CommentListResponse commentListResponse = new CommentListResponse(comments, 1, 1, 1, true, true, false, false);

        given(commentService.commentList(anyLong(), anyInt())).willReturn(commentListResponse);

        mockMvc.perform(get("/api/posts/{postId}/comments", 1)
                        .param("page", "1")
                )
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.comments").isArray(),
                        jsonPath("$.comments[0].commentId").value(1),
                        jsonPath("$.comments[0].writer").value("writer"),
                        jsonPath("$.comments[0].content").value("comment"),
                        jsonPath("$.comments[0].createdAt").isNotEmpty(),
                        jsonPath("$.page").value(1),
                        jsonPath("$.totalPages").value(1),
                        jsonPath("$.totalComments").value(1),
                        jsonPath("$.first").value(true),
                        jsonPath("$.last").value(true),
                        jsonPath("$.prev").value(false),
                        jsonPath("$.next").value(false)
                )
                .andDo(restdocs)
                .andDo(restdocs.document(
                        pathParameters(
                                parameterWithName("postId").description("게시글 번호")
                        ),
                        queryParameters(
                                parameterWithName("page").description("페이지 번호")
                        ),
                        responseFields(
                                fieldWithPath("comments").type(JsonFieldType.ARRAY).description("댓글 목록"),
                                fieldWithPath("comments[0].commentId").type(JsonFieldType.NUMBER).description("댓글번호"),
                                fieldWithPath("comments[0].writer").type(JsonFieldType.STRING).description("작성자"),
                                fieldWithPath("comments[0].content").type(JsonFieldType.STRING).description("내용"),
                                fieldWithPath("comments[0].createdAt").type(JsonFieldType.STRING).description("작성일"),
                                fieldWithPath("page").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
                                fieldWithPath("totalPages").type(JsonFieldType.NUMBER).description("전체 페이지 개수"),
                                fieldWithPath("totalComments").type(JsonFieldType.NUMBER).description("전체 댓글 개수"),
                                fieldWithPath("first").type(JsonFieldType.BOOLEAN).description("첫번째 페이지 여부"),
                                fieldWithPath("last").type(JsonFieldType.BOOLEAN).description("마지막 페이지 여부"),
                                fieldWithPath("prev").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부"),
                                fieldWithPath("next").type(JsonFieldType.BOOLEAN).description("이전 페이지 존재 여부")
                        )
                ));
    }

    @DisplayName("댓글 작성에 성공하면 200을 응답한다.")
    @Test
    void commentWrite() throws Exception {
        CommentWriteRequest commentWriteRequest = new CommentWriteRequest("comment");
        Claims claims = Jwts.claims()
                .subject("yoon1234")
                .add("authority", "ROLE_MEMBER")
                .build();

        willDoNothing().given(tokenService).validateToken(anyString());
        given(tokenService.extractClaim(anyString())).willReturn(claims);
        willDoNothing().given(commentService).commentWrite(anyLong(), anyString(), any(CommentWriteRequest.class));

        mockMvc.perform(post("/api/posts/{postId}/comments/write", 1)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentWriteRequest))
                )
                .andExpect(status().isOk())
                .andDo(restdocs)
                .andDo(restdocs.document(
                        pathParameters(
                                parameterWithName("postId").description("게시글 번호")
                        ),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        requestFields(
                                fieldWithPath("content").type(JsonFieldType.STRING).description("댓글")
                        )
                ));
    }

    @DisplayName("댓글 작성 시 내용이 공백이면 400을 응답한다.")
    @Test
    void commentWriteBlankContent() throws Exception {
        CommentWriteRequest commentWriteRequest = new CommentWriteRequest("");
        Claims claims = Jwts.claims()
                .subject("yoon1234")
                .add("authority", "ROLE_MEMBER")
                .build();

        willDoNothing().given(tokenService).validateToken(anyString());
        given(tokenService.extractClaim(anyString())).willReturn(claims);

        mockMvc.perform(post("/api/posts/{postId}/comments/write", 1)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentWriteRequest))
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

    @DisplayName("댓글 작성 시 게시글이 존재하지 않으면 404를 응답한다.")
    @Test
    void commentWriteNotFoundPost() throws Exception {
        CommentWriteRequest commentWriteRequest = new CommentWriteRequest("comment");
        Claims claims = Jwts.claims()
                .subject("yoon1234")
                .add("authority", "ROLE_MEMBER")
                .build();

        willDoNothing().given(tokenService).validateToken(anyString());
        given(tokenService.extractClaim(anyString())).willReturn(claims);
        willThrow(new NotFoundPostException()).given(commentService).commentWrite(anyLong(), anyString(), any(CommentWriteRequest.class));

        mockMvc.perform(post("/api/posts/{postId}/comments/write", 1)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentWriteRequest))
                )
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.status").value(404),
                        jsonPath("$.errorCode").value("E404003"),
                        jsonPath("$.message").value("게시글이 존재하지 않습니다.")
                );
    }

    @DisplayName("댓글 작성 시 access token이 만료되면 401을 응답한다.")
    @Test
    void commentWriteExpiredAccessToken() throws Exception {
        CommentWriteRequest commentWriteRequest = new CommentWriteRequest("comment");
        ErrorType errorType = ErrorType.EXPIRED_TOKEN;

        willThrow(new AuthenticationServiceException(errorType.getErrorCode())).given(tokenService).validateToken(anyString());

        mockMvc.perform(post("/api/posts/{postId}/comments/write", 1)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentWriteRequest))
                )
                .andExpectAll(
                        status().isUnauthorized(),
                        jsonPath("$.status").value(401),
                        jsonPath("$.errorCode").value("E401002"),
                        jsonPath("$.message").value("토큰이 만료 되었습니다.")
                );
    }

    @DisplayName("댓글 작성 시 access token 형식이 잘못되면 401을 응답한다.")
    @Test
    void commentWriteInvalicAccessToken() throws Exception {
        CommentWriteRequest commentWriteRequest = new CommentWriteRequest("comment");
        ErrorType errorType = ErrorType.INVALID_TOKEN;

        willThrow(new AuthenticationServiceException(errorType.getErrorCode())).given(tokenService).validateToken(anyString());

        mockMvc.perform(post("/api/posts/{postId}/comments/write", 1)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentWriteRequest))
                )
                .andExpectAll(
                        status().isUnauthorized(),
                        jsonPath("$.status").value(401),
                        jsonPath("$.errorCode").value("E401003"),
                        jsonPath("$.message").value("토큰 형식이 잘못 되었습니다.")
                );
    }

    @DisplayName("댓글 수정에 성공하면 200을 응답한다.")
    @Test
    void commentModify() throws Exception {
        CommentModifyRequest commentModifyRequest = new CommentModifyRequest("comment");
        Claims claims = Jwts.claims()
                .subject("yoon1234")
                .add("authority", "ROLE_MEMBER")
                .build();

        willDoNothing().given(tokenService).validateToken(anyString());
        given(tokenService.extractClaim(anyString())).willReturn(claims);
        willDoNothing().given(commentService).commentModify(anyLong(), anyLong(), anyString(), any(CommentModifyRequest.class));

        mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}", 1, 1)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentModifyRequest))
                )
                .andExpect(status().isOk())
                .andDo(restdocs)
                .andDo(restdocs.document(
                        pathParameters(
                                parameterWithName("postId").description("게시글 번호"),
                                parameterWithName("commentId").description("댓글 번호")
                        ),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        requestFields(
                                fieldWithPath("content").type(JsonFieldType.STRING).description("댓글")
                        )
                ));
    }

    @DisplayName("댓글 수정 시 내용이 공백이면 400을 응답한다.")
    @Test
    void commentModifyBlankContent() throws Exception {
        CommentModifyRequest commentModifyRequest = new CommentModifyRequest("");
        Claims claims = Jwts.claims()
                .subject("yoon1234")
                .add("authority", "ROLE_MEMBER")
                .build();

        willDoNothing().given(tokenService).validateToken(anyString());
        given(tokenService.extractClaim(anyString())).willReturn(claims);

        mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}", 1, 1)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentModifyRequest))
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

    @DisplayName("댓글 수정 시 댓글이 존재하지 않으면 404를 응답한다.")
    @Test
    void commentModifyNotFoundComment() throws Exception {
        CommentModifyRequest commentModifyRequest = new CommentModifyRequest("comment");
        Claims claims = Jwts.claims()
                .subject("yoon1234")
                .add("authority", "ROLE_MEMBER")
                .build();

        willDoNothing().given(tokenService).validateToken(anyString());
        given(tokenService.extractClaim(anyString())).willReturn(claims);
        willThrow(new NotFoundCommentException()).given(commentService).commentModify(anyLong(), anyLong(), anyString(), any(CommentModifyRequest.class));

        mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}", 1, 1)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentModifyRequest))
                )
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.status").value(404),
                        jsonPath("$.errorCode").value("E404004"),
                        jsonPath("$.message").value("댓글이 존재하지 않습니다.")
                );
    }

    @DisplayName("댓글 수정 시 access token이 만료되면 401을 응답한다.")
    @Test
    void commentModifyExpiredAccessToken() throws Exception {
        CommentModifyRequest commentModifyRequest = new CommentModifyRequest("comment");
        ErrorType errorType = ErrorType.EXPIRED_TOKEN;

        willThrow(new AuthenticationServiceException(errorType.getErrorCode())).given(tokenService).validateToken(anyString());

        mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}", 1, 1)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentModifyRequest))
                )
                .andExpectAll(
                        status().isUnauthorized(),
                        jsonPath("$.status").value(401),
                        jsonPath("$.errorCode").value("E401002"),
                        jsonPath("$.message").value("토큰이 만료 되었습니다.")
                );
    }

    @DisplayName("댓글 수정 시 access token 형식이 잘못되면 401을 응답한다.")
    @Test
    void commentModifyInvalidAccessToken() throws Exception {
        CommentModifyRequest commentModifyRequest = new CommentModifyRequest("comment");
        ErrorType errorType = ErrorType.INVALID_TOKEN;

        willThrow(new AuthenticationServiceException(errorType.getErrorCode())).given(tokenService).validateToken(anyString());

        mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}", 1, 1)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentModifyRequest))
                )
                .andExpectAll(
                        status().isUnauthorized(),
                        jsonPath("$.status").value(401),
                        jsonPath("$.errorCode").value("E401003"),
                        jsonPath("$.message").value("토큰 형식이 잘못 되었습니다.")
                );
    }

    @DisplayName("댓글 수정 시 작성자가 아닌데 수정하면 403을 응답한다.")
    @Test
    void commentModifyAccessDenied() throws Exception {
        CommentModifyRequest commentModifyRequest = new CommentModifyRequest("comment");
        Claims claims = Jwts.claims()
                .subject("yoon1234")
                .add("authority", "ROLE_MEMBER")
                .build();

        willDoNothing().given(tokenService).validateToken(anyString());
        given(tokenService.extractClaim(anyString())).willReturn(claims);
        willThrow(new AccessDeniedCommentException()).given(commentService).commentModify(anyLong(), anyLong(), anyString(), any(CommentModifyRequest.class));

        mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}", 1, 1)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentModifyRequest))
                )
                .andExpectAll(
                        status().isForbidden(),
                        jsonPath("$.status").value(403),
                        jsonPath("$.errorCode").value("E403002"),
                        jsonPath("$.message").value("다른 사용자의 댓글을 수정/삭제 할 수 없습니다.")
                );
    }

    @DisplayName("댓글 삭제에 성공하면 200을 응답한다.")
    @Test
    void commentDelete() throws Exception {
        Claims claims = Jwts.claims()
                .subject("yoon1234")
                .add("authority", "ROLE_MEMBER")
                .build();

        willDoNothing().given(tokenService).validateToken(anyString());
        given(tokenService.extractClaim(anyString())).willReturn(claims);
        willDoNothing().given(commentService).commentDelete(anyLong(), anyLong(), anyString());

        mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", 1, 1)
                        .header("Authorization", "Bearer access-token")
                )
                .andExpect(status().isOk())
                .andDo(restdocs)
                .andDo(restdocs.document(
                        pathParameters(
                                parameterWithName("postId").description("게시글 번호"),
                                parameterWithName("commentId").description("댓글 번호")
                        ),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        )
                ));
    }

    @DisplayName("댓글 삭제 시 댓글이 존재하지 않으면 404를 응답한다.")
    @Test
    void commentDeleteNotFoundComment() throws Exception {
        Claims claims = Jwts.claims()
                .subject("yoon1234")
                .add("authority", "ROLE_MEMBER")
                .build();

        willDoNothing().given(tokenService).validateToken(anyString());
        given(tokenService.extractClaim(anyString())).willReturn(claims);
        willThrow(new NotFoundCommentException()).given(commentService).commentDelete(anyLong(), anyLong(), anyString());

        mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", 1, 1)
                        .header("Authorization", "Bearer access-token")
                )
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.status").value(404),
                        jsonPath("$.errorCode").value("E404004"),
                        jsonPath("$.message").value("댓글이 존재하지 않습니다.")
                );
    }

    @DisplayName("댓글 삭제 시 access token이 만료되면 401을 응답한다.")
    @Test
    void commentDeleteExpiredAccessToken() throws Exception {
        ErrorType errorType = ErrorType.EXPIRED_TOKEN;

        willThrow(new AuthenticationServiceException(errorType.getErrorCode())).given(tokenService).validateToken(anyString());

        mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", 1, 1)
                        .header("Authorization", "Bearer access-token")
                )
                .andExpectAll(
                        status().isUnauthorized(),
                        jsonPath("$.status").value(401),
                        jsonPath("$.errorCode").value("E401002"),
                        jsonPath("$.message").value("토큰이 만료 되었습니다.")
                );
    }

    @DisplayName("댓글 삭제 시 access token 형식이 잘못되면 401을 응답한다.")
    @Test
    void commentDeleteInvalidAccessToken() throws Exception {
        ErrorType errorType = ErrorType.INVALID_TOKEN;

        willThrow(new AuthenticationServiceException(errorType.getErrorCode())).given(tokenService).validateToken(anyString());

        mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", 1, 1)
                        .header("Authorization", "Bearer access-token")
                )
                .andExpectAll(
                        status().isUnauthorized(),
                        jsonPath("$.status").value(401),
                        jsonPath("$.errorCode").value("E401003"),
                        jsonPath("$.message").value("토큰 형식이 잘못 되었습니다.")
                );
    }

    @DisplayName("댓글 삭제 시 사용자가 아닌데 수정하면 403을 응답한다.")
    @Test
    void commentDeleteAccessDenied() throws Exception {
        Claims claims = Jwts.claims()
                .subject("yoon1234")
                .add("authority", "ROLE_MEMBER")
                .build();

        willDoNothing().given(tokenService).validateToken(anyString());
        given(tokenService.extractClaim(anyString())).willReturn(claims);
        willThrow(new AccessDeniedCommentException()).given(commentService).commentDelete(anyLong(), anyLong(), anyString());

        mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", 1, 1)
                        .header("Authorization", "Bearer access-token")
                )
                .andExpectAll(
                        status().isForbidden(),
                        jsonPath("$.status").value(403),
                        jsonPath("$.errorCode").value("E403002"),
                        jsonPath("$.message").value("다른 사용자의 댓글을 수정/삭제 할 수 없습니다.")
                );
    }

}