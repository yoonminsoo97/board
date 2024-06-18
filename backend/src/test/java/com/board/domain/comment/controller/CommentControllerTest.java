package com.board.domain.comment.controller;

import com.board.domain.comment.dto.CommentModifyRequest;
import com.board.domain.comment.dto.CommentWriteRequest;
import com.board.domain.comment.exception.NotFoundCommentException;
import com.board.domain.comment.service.CommentService;
import com.board.domain.post.exception.NotFoundPostException;
import com.board.global.security.exception.ExpiredTokenException;
import com.board.global.security.exception.InvalidTokenException;
import com.board.support.ControllerTest;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CommentController.class)
class CommentControllerTest extends ControllerTest {

    @MockBean
    private CommentService commentService;

    @Nested
    @DisplayName("댓글 작성 요청")
    class CommentWriteTest {

        @Test
        @DisplayName("댓글을 작성한다")
        void commentWrite() throws Exception {
            CommentWriteRequest commentWriteRequest = CommentWriteRequest.builder()
                    .content("comment")
                    .build();

            Claims claims = Jwts.claims()
                    .subject(String.valueOf(1L))
                    .add("nickname", "yoonkun")
                    .add("authority", "ROLE_MEMBER")
                    .build();

            given(jwtManager.getPayload(anyString())).willReturn(claims);
            willDoNothing().given(commentService).commentWrite(anyLong(), any(CommentWriteRequest.class), anyLong());

            mockMvc.perform(post("/api/posts/{postId}/comments", 1)
                            .header("Authorization", "Bearer access-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(commentWriteRequest))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andDo(restDocs.document(
                            pathParameters(
                                    parameterWithName("postId").description("게시글 번호")
                            ),
                            requestHeaders(
                                    headerWithName("Authorization").description("액세스 토큰")
                            ),
                            requestFields(
                                    fieldWithPath("content").type(STRING).description("댓글 내용")
                            ),
                            responseFields(
                                    commonSuccessResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("대댓글을 작성한다")
        void replyWrite() throws Exception {
            CommentWriteRequest replyWriteRequest = CommentWriteRequest.builder()
                    .content("reply")
                    .build();

            Claims claims = Jwts.claims()
                    .subject(String.valueOf(1L))
                    .add("nickname", "yoonkun")
                    .add("authority", "ROLE_MEMBER")
                    .build();

            given(jwtManager.getPayload(anyString())).willReturn(claims);
            willDoNothing().given(commentService).replyWrite(anyLong(), anyLong(), any(CommentWriteRequest.class), anyLong());

            mockMvc.perform(post("/api/posts/{postId}/comments/{commentId}/replies", 1, 1)
                            .header("Authorization", "Bearer access-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(replyWriteRequest))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andDo(restDocs.document(
                            pathParameters(
                                    parameterWithName("postId").description("게시글 번호"),
                                    parameterWithName("commentId").description("댓글 번호")
                            ),
                            requestHeaders(
                                    headerWithName("Authorization").description("액세스 토큰")
                            ),
                            requestFields(
                                    fieldWithPath("content").type(STRING).description("댓글 내용")
                            ),
                            responseFields(
                                    commonSuccessResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("내용이 비어있으면 예외가 발생한다")
        void commentWriteInvalidContentValue() throws Exception {
            CommentWriteRequest commentWriteRequest = CommentWriteRequest.builder()
                    .content("")
                    .build();

            Claims claims = Jwts.claims()
                    .subject(String.valueOf(1L))
                    .add("nickname", "yoonkun")
                    .add("authority", "ROLE_MEMBER")
                    .build();

            given(jwtManager.getPayload(anyString())).willReturn(claims);

            mockMvc.perform(post("/api/posts/{postId}/comments", 1)
                            .header("Authorization", "Bearer access-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(commentWriteRequest))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("fail"))
                    .andExpect(jsonPath("$.error.code").value("E400001"))
                    .andExpect(jsonPath("$.error.message").value("입력값이 잘못되었습니다."))
                    .andExpect(jsonPath("$.error.fields[0].field").value("content"))
                    .andExpect(jsonPath("$.error.fields[0].input").value(""))
                    .andExpect(jsonPath("$.error.fields[0].message").value("내용을 입력해 주세요."))
                    .andDo(restDocs.document(
                            pathParameters(
                                    parameterWithName("postId").description("게시글 번호")
                            ),
                            requestHeaders(
                                    headerWithName("Authorization").description("액세스 토큰")
                            ),
                            requestFields(
                                    fieldWithPath("content").type(STRING).description("댓글 내용")
                            ),
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("게시글이 존재하지 않으면 예외가 발생한다")
        void commentWriteNotFoundPost() throws Exception {
            CommentWriteRequest commentWriteRequest = CommentWriteRequest.builder()
                    .content("comment")
                    .build();

            Claims claims = Jwts.claims()
                    .subject(String.valueOf(1L))
                    .add("nickname", "yoonkun")
                    .add("authority", "ROLE_MEMBER")
                    .build();

            given(jwtManager.getPayload(anyString())).willReturn(claims);
            willThrow(new NotFoundPostException()).given(commentService).commentWrite(anyLong(), any(CommentWriteRequest.class), anyLong());

            mockMvc.perform(post("/api/posts/{postId}/comments", 1)
                            .header("Authorization", "Bearer access-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(commentWriteRequest))
                    )
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("fail"))
                    .andExpect(jsonPath("$.error.code").value("E404002"))
                    .andExpect(jsonPath("$.error.message").value("게시글을 찾을 수 없습니다."))
                    .andExpect(jsonPath("$.error.fields").isEmpty())
                    .andDo(restDocs.document(
                            pathParameters(
                                    parameterWithName("postId").description("게시글 번호")
                            ),
                            requestHeaders(
                                    headerWithName("Authorization").description("액세스 토큰")
                            ),
                            requestFields(
                                    fieldWithPath("content").type(STRING).description("댓글 내용")
                            ),
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("액세스 토큰이 유효하지 않으면 예외가 발생한다")
        void commentWriteInvalidAccessToken() throws Exception {
            CommentWriteRequest commentWriteRequest = CommentWriteRequest.builder()
                    .content("comment")
                    .build();

            willThrow(new InvalidTokenException()).given(jwtManager).getPayload(anyString());

            mockMvc.perform(post("/api/posts/{postId}/comments", 1)
                            .header("Authorization", "Bearer access-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(commentWriteRequest))
                    )
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value("fail"))
                    .andExpect(jsonPath("$.error.code").value("E401002"))
                    .andExpect(jsonPath("$.error.message").value("토큰이 유효하지 않습니다."))
                    .andExpect(jsonPath("$.error.fields").isEmpty())
                    .andDo(restDocs.document(
                            pathParameters(
                                    parameterWithName("postId").description("게시글 번호")
                            ),
                            requestHeaders(
                                    headerWithName("Authorization").description("액세스 토큰")
                            ),
                            requestFields(
                                    fieldWithPath("content").type(STRING).description("댓글 내용")
                            ),
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("액세스 토큰이 만료되면 예외가 발생한다")
        void commentWriteExpiredAccessToken() throws Exception {
            CommentWriteRequest commentWriteRequest = CommentWriteRequest.builder()
                    .content("comment")
                    .build();

            willThrow(new ExpiredTokenException()).given(jwtManager).getPayload(anyString());

            mockMvc.perform(post("/api/posts/{postId}/comments", 1)
                            .header("Authorization", "Bearer access-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(commentWriteRequest))
                    )
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value("fail"))
                    .andExpect(jsonPath("$.error.code").value("E401003"))
                    .andExpect(jsonPath("$.error.message").value("토큰이 만료되었습니다."))
                    .andExpect(jsonPath("$.error.fields").isEmpty())
                    .andDo(restDocs.document(
                            pathParameters(
                                    parameterWithName("postId").description("게시글 번호")
                            ),
                            requestHeaders(
                                    headerWithName("Authorization").description("액세스 토큰")
                            ),
                            requestFields(
                                    fieldWithPath("content").type(STRING).description("댓글 내용")
                            ),
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

    }

    @Nested
    @DisplayName("댓글 수정 요청")
    class CommentModifyTest {

        @Test
        @DisplayName("댓글을 수정한다")
        void commentModify() throws Exception {
            CommentModifyRequest commentModifyRequest = CommentModifyRequest.builder()
                    .content("newComment")
                    .build();

            Claims claims = Jwts.claims()
                    .subject(String.valueOf(1L))
                    .add("nickname", "yoonkun")
                    .add("authority", "ROLE_MEMBER")
                    .build();

            given(jwtManager.getPayload(anyString())).willReturn(claims);
            willDoNothing().given(commentService).commentModify(anyLong(), anyLong(), any(CommentModifyRequest.class), anyLong());

            mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}", 1, 1)
                            .header("Authorization", "Bearer access-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(commentModifyRequest))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andDo(restDocs.document(
                            pathParameters(
                                    parameterWithName("postId").description("게시글 번호"),
                                    parameterWithName("commentId").description("댓글 번호")
                            ),
                            requestHeaders(
                                    headerWithName("Authorization").description("액세스 토큰")
                            ),
                            requestFields(
                                    fieldWithPath("content").type(STRING).description("수정할 댓글 내용")
                            ),
                            responseFields(
                                    commonSuccessResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("내용이 비어있으면 예외가 발생한다")
        void commentModifyInvalidContentValue() throws Exception {
            CommentModifyRequest commentModifyRequest = CommentModifyRequest.builder()
                    .content("")
                    .build();

            Claims claims = Jwts.claims()
                    .subject(String.valueOf(1L))
                    .add("nickname", "yoonkun")
                    .add("authority", "ROLE_MEMBER")
                    .build();

            given(jwtManager.getPayload(anyString())).willReturn(claims);

            mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}", 1, 1)
                            .header("Authorization", "Bearer access-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(commentModifyRequest))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("fail"))
                    .andExpect(jsonPath("$.error.code").value("E400001"))
                    .andExpect(jsonPath("$.error.message").value("입력값이 잘못되었습니다."))
                    .andExpect(jsonPath("$.error.fields[0].field").value("content"))
                    .andExpect(jsonPath("$.error.fields[0].input").value(""))
                    .andExpect(jsonPath("$.error.fields[0].message").value("내용을 입력해 주세요."))
                    .andDo(restDocs.document(
                            pathParameters(
                                    parameterWithName("postId").description("게시글 번호"),
                                    parameterWithName("commentId").description("댓글 번호")
                            ),
                            requestHeaders(
                                    headerWithName("Authorization").description("액세스 토큰")
                            ),
                            requestFields(
                                    fieldWithPath("content").type(STRING).description("댓글 내용")
                            ),
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("댓글이 존재하지 않으면 예외가 발생한다")
        void commentModifyNotFoundComment() throws Exception {
            CommentModifyRequest commentModifyRequest = CommentModifyRequest.builder()
                    .content("newComment")
                    .build();

            Claims claims = Jwts.claims()
                    .subject(String.valueOf(1L))
                    .add("nickname", "yoonkun")
                    .add("authority", "ROLE_MEMBER")
                    .build();

            given(jwtManager.getPayload(anyString())).willReturn(claims);
            willThrow(new NotFoundCommentException()).given(commentService).commentModify(anyLong(), anyLong(), any(CommentModifyRequest.class), anyLong());

            mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}", 1, 1)
                            .header("Authorization", "Bearer access-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(commentModifyRequest))
                    )
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("fail"))
                    .andExpect(jsonPath("$.error.code").value("E404003"))
                    .andExpect(jsonPath("$.error.message").value("댓글을 찾을 수 없습니다."))
                    .andExpect(jsonPath("$.error.fields").isEmpty())
                    .andDo(restDocs.document(
                            pathParameters(
                                    parameterWithName("postId").description("게시글 번호"),
                                    parameterWithName("commentId").description("댓글 번호")
                            ),
                            requestHeaders(
                                    headerWithName("Authorization").description("액세스 토큰")
                            ),
                            requestFields(
                                    fieldWithPath("content").type(STRING).description("수정할 댓글 내용")
                            ),
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("액세스 토큰이 유효하지 않으면 예외가 발생한다")
        void commentModifyInvalidAccessToken() throws Exception {
            CommentModifyRequest commentModifyRequest = CommentModifyRequest.builder()
                    .content("newComment")
                    .build();

            willThrow(new InvalidTokenException()).given(jwtManager).getPayload(anyString());

            mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}", 1, 1)
                            .header("Authorization", "Bearer access-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(commentModifyRequest))
                    )
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value("fail"))
                    .andExpect(jsonPath("$.error.code").value("E401002"))
                    .andExpect(jsonPath("$.error.message").value("토큰이 유효하지 않습니다."))
                    .andExpect(jsonPath("$.error.fields").isEmpty())
                    .andDo(restDocs.document(
                            pathParameters(
                                    parameterWithName("postId").description("게시글 번호"),
                                    parameterWithName("commentId").description("댓글 번호")
                            ),
                            requestHeaders(
                                    headerWithName("Authorization").description("액세스 토큰")
                            ),
                            requestFields(
                                    fieldWithPath("content").type(STRING).description("수정할 댓글 내용")
                            ),
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("액세스 토큰이 만료되면 예외가 발생한다")
        void commentModifyExpiredAccessToken() throws Exception {
            CommentModifyRequest commentModifyRequest = CommentModifyRequest.builder()
                    .content("newComment")
                    .build();

            willThrow(new ExpiredTokenException()).given(jwtManager).getPayload(anyString());

            mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}", 1, 1)
                            .header("Authorization", "Bearer access-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(commentModifyRequest))
                    )
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value("fail"))
                    .andExpect(jsonPath("$.error.code").value("E401003"))
                    .andExpect(jsonPath("$.error.message").value("토큰이 만료되었습니다."))
                    .andExpect(jsonPath("$.error.fields").isEmpty())
                    .andDo(restDocs.document(
                            pathParameters(
                                    parameterWithName("postId").description("게시글 번호"),
                                    parameterWithName("commentId").description("댓글 번호")
                            ),
                            requestHeaders(
                                    headerWithName("Authorization").description("액세스 토큰")
                            ),
                            requestFields(
                                    fieldWithPath("content").type(STRING).description("수정할 댓글 내용")
                            ),
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

    }

    @Nested
    @DisplayName("댓글 삭제 요청")
    class CommentDeleteTest {

        @Test
        @DisplayName("댓글을 삭제한다")
        void commentDelete() throws Exception {
            Claims claims = Jwts.claims()
                    .subject(String.valueOf(1L))
                    .add("nickname", "yoonkun")
                    .add("authority", "ROLE_MEMBER")
                    .build();

            given(jwtManager.getPayload(anyString())).willReturn(claims);
            willDoNothing().given(commentService).commentDelete(anyLong(), anyLong(), anyLong());

            mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", 1, 1)
                            .header("Authorization", "Bearer access-token")
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andDo(restDocs.document(
                            pathParameters(
                                    parameterWithName("postId").description("게시글 번호"),
                                    parameterWithName("commentId").description("댓글 번호")
                            ),
                            requestHeaders(
                                    headerWithName("Authorization").description("액세스 토큰")
                            ),
                            responseFields(
                                    commonSuccessResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("댓글이 존재하지 않으면 예외가 발생한다")
        void commentDeleteNotFoundComment() throws Exception {
            Claims claims = Jwts.claims()
                    .subject(String.valueOf(1L))
                    .add("nickname", "yoonkun")
                    .add("authority", "ROLE_MEMBER")
                    .build();

            given(jwtManager.getPayload(anyString())).willReturn(claims);
            willThrow(new NotFoundCommentException()).given(commentService).commentDelete(anyLong(), anyLong(), anyLong());

            mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", 1, 1)
                            .header("Authorization", "Bearer access-token")
                    )
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("fail"))
                    .andExpect(jsonPath("$.error.code").value("E404003"))
                    .andExpect(jsonPath("$.error.message").value("댓글을 찾을 수 없습니다."))
                    .andExpect(jsonPath("$.error.fields").isEmpty())
                    .andDo(restDocs.document(
                            pathParameters(
                                    parameterWithName("postId").description("게시글 번호"),
                                    parameterWithName("commentId").description("댓글 번호")
                            ),
                            requestHeaders(
                                    headerWithName("Authorization").description("액세스 토큰")
                            ),
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("액세스 토큰이 유효하지 않으면 예외가 발생한다")
        void commentDeleteInvalidAccessToken() throws Exception {
            willThrow(new InvalidTokenException()).given(jwtManager).getPayload(anyString());

            mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", 1, 1)
                            .header("Authorization", "Bearer access-token")
                    )
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value("fail"))
                    .andExpect(jsonPath("$.error.code").value("E401002"))
                    .andExpect(jsonPath("$.error.message").value("토큰이 유효하지 않습니다."))
                    .andExpect(jsonPath("$.error.fields").isEmpty())
                    .andDo(restDocs.document(
                            pathParameters(
                                    parameterWithName("postId").description("게시글 번호"),
                                    parameterWithName("commentId").description("댓글 번호")
                            ),
                            requestHeaders(
                                    headerWithName("Authorization").description("액세스 토큰")
                            ),
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("액세스 토큰이 만료되면 예외가 발생한다")
        void commentDeleteExpiredAccessToken() throws Exception {
            willThrow(new ExpiredTokenException()).given(jwtManager).getPayload(anyString());

            mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", 1, 1)
                            .header("Authorization", "Bearer access-token")
                    )
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value("fail"))
                    .andExpect(jsonPath("$.error.code").value("E401003"))
                    .andExpect(jsonPath("$.error.message").value("토큰이 만료되었습니다."))
                    .andExpect(jsonPath("$.error.fields").isEmpty())
                    .andDo(restDocs.document(
                            pathParameters(
                                    parameterWithName("postId").description("게시글 번호"),
                                    parameterWithName("commentId").description("댓글 번호")
                            ),
                            requestHeaders(
                                    headerWithName("Authorization").description("액세스 토큰")
                            ),
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

    }

}