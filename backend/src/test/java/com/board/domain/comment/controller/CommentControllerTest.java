package com.board.domain.comment.controller;

import com.board.domain.comment.dto.CommentListItem;
import com.board.domain.comment.dto.CommentListResponse;
import com.board.domain.comment.dto.CommentModifyRequest;
import com.board.domain.comment.dto.CommentWriteRequest;
import com.board.domain.comment.exception.CommentDeleteAccessDeniedException;
import com.board.domain.comment.exception.CommentModifyAccessDeniedException;
import com.board.domain.comment.exception.NotFoundCommentException;
import com.board.domain.comment.service.CommentService;
import com.board.domain.post.exception.NotFoundPostException;
import com.board.support.RestDocsTestSupport;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.JsonFieldType.BOOLEAN;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;

import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CommentController.class)
class CommentControllerTest extends RestDocsTestSupport {

    @MockBean
    private CommentService commentService;

    @Test
    @DisplayName("댓글을 작성한다")
    void commentWrite() throws Exception {
        CommentWriteRequest commentWriteRequest = new CommentWriteRequest("댓글");

        given(tokenService.tokenPayload(anyString())).willReturn(mockClaims());
        willDoNothing().given(commentService).commentWrite(anyLong(), any(CommentWriteRequest.class), anyString());

        mockMvc.perform(post("/api/posts/{postNumber}/comments/write", 1)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentWriteRequest))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.result").isEmpty())
                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName("Authorization").description("액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("postNumber").description("게시글 번호")
                        ),
                        requestFields(
                                fieldWithPath("content").type(STRING).description("댓글")
                        ),
                        responseFields(
                                commonSuccessResponse()
                        )
                ));
    }

    @Test
    @DisplayName("댓글 작성 시 입력값이 잘못되면 예외가 발생한다")
    void commentWriteInvalidInputValue() throws Exception {
        CommentWriteRequest invalidCommentWriteRequest = new CommentWriteRequest("");

        given(tokenService.tokenPayload(anyString())).willReturn(mockClaims());

        mockMvc.perform(post("/api/posts/{postNumber}/comments/write", 1)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCommentWriteRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("fail"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.result.path").value("/api/posts/1/comments/write"))
                .andExpect(jsonPath("$.result.error.code").value("E400001"))
                .andExpect(jsonPath("$.result.error.message").value("입력값이 잘못되었습니다."))
                .andExpect(jsonPath("$.result.error.fieldErrors[0].field").value("content"))
                .andExpect(jsonPath("$.result.error.fieldErrors[0].input").value(""))
                .andExpect(jsonPath("$.result.error.fieldErrors[0].message").value("내용을 입력해 주세요."))
                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName("Authorization").description("액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("postNumber").description("게시글 번호")
                        ),
                        requestFields(
                                fieldWithPath("content").type(STRING).description("댓글")
                        ),
                        responseFields(
                                commonErrorResponse())
                                .and(
                                        fieldWithPath("result.error.fieldErrors[].field").description(STRING).description("필드명"),
                                        fieldWithPath("result.error.fieldErrors[].input").description(STRING).description("입력값"),
                                        fieldWithPath("result.error.fieldErrors[].message").description(STRING).description("메시지")
                                )
                ));
    }

    @Test
    @DisplayName("댓글 작성 시 게시글을 찾을 수 없으면 예외가 발생한다")
    void commentWriteNotFoundPost() throws Exception {
        CommentWriteRequest commentWriteRequest = new CommentWriteRequest("댓글");

        given(tokenService.tokenPayload(anyString())).willReturn(mockClaims());
        willThrow(new NotFoundPostException()).given(commentService).commentWrite(anyLong(), any(CommentWriteRequest.class), anyString());

        mockMvc.perform(post("/api/posts/{postNumber}/comments/write", 1)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentWriteRequest))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.result.path").value("/api/posts/1/comments/write"))
                .andExpect(jsonPath("$.result.error.code").value("E404002"))
                .andExpect(jsonPath("$.result.error.message").value("게시글을 찾을 수 없습니다."))
                .andExpect(jsonPath("$.result.error.fieldErrors").isEmpty())
                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName("Authorization").description("Access Token")
                        ),
                        pathParameters(
                                parameterWithName("postNumber").description("게시글 번호")
                        ),
                        requestFields(
                                fieldWithPath("content").type(STRING).description("댓글")
                        ),
                        responseFields(
                                commonErrorResponse()
                        )
                ));
    }

    @Test
    @DisplayName("대댓글을 작성한다")
    void replyWrite() throws Exception {
        CommentWriteRequest replyWriteRequest = new CommentWriteRequest("대댓글");

        given(tokenService.tokenPayload(anyString())).willReturn(mockClaims());
        willDoNothing().given(commentService).replyWrite(anyLong(), anyLong(), any(CommentWriteRequest.class), anyString());

        mockMvc.perform(post("/api/posts/{postId}/comments/{commentId}/replies", 1, 1)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(replyWriteRequest))
                )
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.result").isEmpty())
                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName("Authorization").description("액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("postId").description("게시글 번호"),
                                parameterWithName("commentId").description("댓글 번호")
                        ),
                        requestFields(
                                fieldWithPath("content").type(STRING).description("대댓글")
                        ),
                        responseFields(
                                commonSuccessResponse()
                        )
                ));

    }

    @Test
    @DisplayName("대댓글 작성 시 입력값이 잘못되면 예외가 발생한다")
    void replyWriteInvalidInputValue() throws Exception {
        CommentWriteRequest invalidReplyWriteRequest = new CommentWriteRequest("");

        given(tokenService.tokenPayload(anyString())).willReturn(mockClaims());

        mockMvc.perform(post("/api/posts/{postId}/comments/{commentId}/replies", 1, 1)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReplyWriteRequest))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("fail"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.result.path").value("/api/posts/1/comments/1/replies"))
                .andExpect(jsonPath("$.result.error.code").value("E400001"))
                .andExpect(jsonPath("$.result.error.message").value("입력값이 잘못되었습니다."))
                .andExpect(jsonPath("$.result.error.fieldErrors[0].field").value("content"))
                .andExpect(jsonPath("$.result.error.fieldErrors[0].input").value(""))
                .andExpect(jsonPath("$.result.error.fieldErrors[0].message").value("내용을 입력해 주세요."))
                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName("Authorization").description("액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("postId").description("게시글 번호"),
                                parameterWithName("commentId").description("댓글 번호")
                        ),
                        requestFields(
                                fieldWithPath("content").type(STRING).description("대댓글")
                        ),
                        responseFields(
                                commonErrorResponse())
                                .and(
                                        fieldWithPath("result.error.fieldErrors[].field").description(STRING).description("필드명"),
                                        fieldWithPath("result.error.fieldErrors[].input").description(STRING).description("입력값"),
                                        fieldWithPath("result.error.fieldErrors[].message").description(STRING).description("메시지")
                                )
                ));
    }

    @Test
    @DisplayName("대댓글 작성 시 댓글을 찾을 수 없으면 예외가 발생한다")
    void replyWriteNotFoundComment() throws Exception {
        CommentWriteRequest replyWriteRequest = new CommentWriteRequest("대댓글");

        given(tokenService.tokenPayload(anyString())).willReturn(mockClaims());
        willThrow(new NotFoundCommentException()).given(commentService).replyWrite(anyLong(), anyLong(), any(CommentWriteRequest.class), anyString());

        mockMvc.perform(post("/api/posts/{postId}/comments/{commentId}/replies", 1, 1)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(replyWriteRequest))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.result.path").value("/api/posts/1/comments/1/replies"))
                .andExpect(jsonPath("$.result.error.code").value("E404003"))
                .andExpect(jsonPath("$.result.error.message").value("댓글을 찾을 수 없습니다."))
                .andExpect(jsonPath("$.result.error.fieldErrors").isEmpty())
                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName("Authorization").description("액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("postId").description("게시글 번호"),
                                parameterWithName("commentId").description("댓글 번호")
                        ),
                        requestFields(
                                fieldWithPath("content").type(STRING).description("대댓글")
                        ),
                        responseFields(
                                commonErrorResponse()
                        )
                ));
    }

    @Test
    @DisplayName("댓글 목록을 조회한다")
    void commentList() throws Exception {
        List<CommentListItem> comments = List.of(
                new CommentListItem(1L, "작성자", "댓글", LocalDateTime.of(2024, 6, 17, 0, 0))
        );
        CommentListResponse commentListResponse = new CommentListResponse(comments, 1, 1, 1, false, false, true, true);

        given(commentService.commentList(anyLong(), anyInt())).willReturn(commentListResponse);

        mockMvc.perform(get("/api/posts/{postNumber}/comments", 1)
                        .param("page", "1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.result.comments[0].commentNum").value(1))
                .andExpect(jsonPath("$.result.comments[0].writer").value("작성자"))
                .andExpect(jsonPath("$.result.comments[0].content").value("댓글"))
                .andExpect(jsonPath("$.result.comments[0].createdAt").value("2024.06.17"))
                .andExpect(jsonPath("$.result.pageNumber").value(1))
                .andExpect(jsonPath("$.result.totalPages").value(1))
                .andExpect(jsonPath("$.result.totalElements").value(1))
                .andExpect(jsonPath("$.result.prev").value(false))
                .andExpect(jsonPath("$.result.next").value(false))
                .andExpect(jsonPath("$.result.first").value(true))
                .andExpect(jsonPath("$.result.last").value(true))
                .andDo(restDocs.document(
                        pathParameters(
                                parameterWithName("postNumber").description("게시글 번호")
                        ),
                        queryParameters(
                                parameterWithName("page").description("페이지 번호")
                        ),
                        responseFields(
                                commonSuccessResponse())
                                .and(
                                        fieldWithPath("result.comments").type(ARRAY).description("댓글 목록"),
                                        fieldWithPath("result.comments[].commentNum").type(NUMBER).description("댓글 번호"),
                                        fieldWithPath("result.comments[].writer").type(STRING).description("댓글 작성자"),
                                        fieldWithPath("result.comments[].content").type(STRING).description("댓글 내용"),
                                        fieldWithPath("result.comments[].createdAt").type(STRING).description("댓글 작성일"),
                                        fieldWithPath("result.pageNumber").type(NUMBER).description("페이지 번호"),
                                        fieldWithPath("result.totalPages").type(NUMBER).description("전체 페이지 개수"),
                                        fieldWithPath("result.totalElements").type(NUMBER).description("전체 게시글 개수"),
                                        fieldWithPath("result.prev").type(BOOLEAN).description("이전 페이지 이동 가능 여부"),
                                        fieldWithPath("result.next").type(BOOLEAN).description("다음 페이지 이동 가능 여부"),
                                        fieldWithPath("result.first").type(BOOLEAN).description("첫 번째 페이지 여부"),
                                        fieldWithPath("result.last").type(BOOLEAN).description("마지막 페이지 여부")
                                )
                ));
    }

    @Test
    @DisplayName("댓글을 수정한다")
    void commentModify() throws Exception {
        CommentModifyRequest commentModifyRequest = new CommentModifyRequest("댓글");

        given(tokenService.tokenPayload(anyString())).willReturn(mockClaims());
        willDoNothing().given(commentService).commentModify(anyLong(), anyLong(), any(CommentModifyRequest.class), anyString());

        mockMvc.perform(put("/api/posts/{postNumber}/comments/{commentNumber}", 1, 1)
                        .header("Authorization", "Bearer acces-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentModifyRequest))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.result").isEmpty())
                .andDo(restDocs.document(
                        pathParameters(
                                parameterWithName("postNumber").description("게시글 번호"),
                                parameterWithName("commentNumber").description("댓글 번호")
                        ),
                        requestHeaders(
                                headerWithName("Authorization").description("액세스 토큰")
                        ),
                        requestFields(
                                fieldWithPath("content").type(STRING).description("수정 댓글")
                        ),
                        responseFields(
                                commonSuccessResponse()
                        )
                ));
    }

    @Test
    @DisplayName("댓글 수정 시 입력값이 잘못되면 예외가 발생한다")
    void commentModifyInvalidInputValue() throws Exception {
        CommentModifyRequest invalidCommentModifyRequest = new CommentModifyRequest("");

        given(tokenService.tokenPayload(anyString())).willReturn(mockClaims());

        mockMvc.perform(put("/api/posts/{postNumber}/comments/{commentNumber}", 1, 1)
                        .header("Authorization", "Bearer acces-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCommentModifyRequest))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("fail"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.result.path").value("/api/posts/1/comments/1"))
                .andExpect(jsonPath("$.result.error.code").value("E400001"))
                .andExpect(jsonPath("$.result.error.message").value("입력값이 잘못되었습니다."))
                .andExpect(jsonPath("$.result.error.fieldErrors[0].field").value("content"))
                .andExpect(jsonPath("$.result.error.fieldErrors[0].input").value(""))
                .andExpect(jsonPath("$.result.error.fieldErrors[0].message").value("내용을 입력해 주세요."))
                .andDo(restDocs.document(
                        pathParameters(
                                parameterWithName("postNumber").description("게시글 번호"),
                                parameterWithName("commentNumber").description("댓글 번호")
                        ),
                        requestHeaders(
                                headerWithName("Authorization").description("액세스 토큰")
                        ),
                        requestFields(
                                fieldWithPath("content").type(STRING).description("수정 댓글")
                        ),
                        responseFields(
                                commonErrorResponse())
                                .and(
                                        fieldWithPath("result.error.fieldErrors[].field").description(STRING).description("필드명"),
                                        fieldWithPath("result.error.fieldErrors[].input").description(STRING).description("입력값"),
                                        fieldWithPath("result.error.fieldErrors[].message").description(STRING).description("메시지")
                                )
                ));
    }

    @Test
    @DisplayName("댓글 수정 시 댓글을 찾을 수 없으면 예외가 발생한다")
    void commentModifyNotFoundComment() throws Exception {
        CommentModifyRequest commentModifyRequest = new CommentModifyRequest("댓글");

        given(tokenService.tokenPayload(anyString())).willReturn(mockClaims());
        willThrow(new NotFoundCommentException()).given(commentService).commentModify(anyLong(), anyLong(), any(CommentModifyRequest.class), anyString());

        mockMvc.perform(put("/api/posts/{postNumber}/comments/{commentNumber}", 1, 1)
                        .header("Authorization", "Bearer acces-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentModifyRequest))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.result.path").value("/api/posts/1/comments/1"))
                .andExpect(jsonPath("$.result.error.code").value("E404003"))
                .andExpect(jsonPath("$.result.error.message").value("댓글을 찾을 수 없습니다."))
                .andExpect(jsonPath("$.result.error.fieldErrors").isEmpty())
                .andDo(restDocs.document(
                        pathParameters(
                                parameterWithName("postNumber").description("게시글 번호"),
                                parameterWithName("commentNumber").description("댓글 번호")
                        ),
                        requestHeaders(
                                headerWithName("Authorization").description("액세스 토큰")
                        ),
                        requestFields(
                                fieldWithPath("content").type(STRING).description("수정 댓글")
                        ),
                        responseFields(
                                commonErrorResponse()
                        )
                ));
    }

    @Test
    @DisplayName("댓글 수정 시 작성자가 아닌데 수정을 시도할 경우 예외가 발생한다")
    void commentModifyNotCommentOwner() throws Exception {
        CommentModifyRequest commentModifyRequest = new CommentModifyRequest("댓글");

        given(tokenService.tokenPayload(anyString())).willReturn(mockClaims());
        willThrow(new CommentModifyAccessDeniedException()).given(commentService).commentModify(anyLong(), anyLong(), any(CommentModifyRequest.class), anyString());

        mockMvc.perform(put("/api/posts/{postNumber}/comments/{commentNumber}", 1, 1)
                        .header("Authorization", "Bearer acces-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentModifyRequest))
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.result.path").value("/api/posts/1/comments/1"))
                .andExpect(jsonPath("$.result.error.code").value("E403003"))
                .andExpect(jsonPath("$.result.error.message").value("댓글 수정은 작성자만 할 수 있습니다."))
                .andExpect(jsonPath("$.result.error.fieldErrors").isEmpty())
                .andDo(restDocs.document(
                        pathParameters(
                                parameterWithName("postNumber").description("게시글 번호"),
                                parameterWithName("commentNumber").description("댓글 번호")
                        ),
                        requestHeaders(
                                headerWithName("Authorization").description("Access Token")
                        ),
                        requestFields(
                                fieldWithPath("content").type(STRING).description("수정 댓글")
                        ),
                        responseFields(
                                commonErrorResponse()
                        )
                ));
    }

    @Test
    @DisplayName("댓글을 삭제한다")
    void commentDelete() throws Exception {
        given(tokenService.tokenPayload(anyString())).willReturn(mockClaims());
        willDoNothing().given(commentService).commentDelete(anyLong(), anyLong(), anyString());

        mockMvc.perform(delete("/api/posts/{postNumber}/comments/{commentNumber}", 1, 1)
                        .header("Authorization", "Bearer access-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.result").isEmpty())
                .andDo(restDocs.document(
                        pathParameters(
                                parameterWithName("postNumber").description("게시글 번호"),
                                parameterWithName("commentNumber").description("댓글 번호")
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
    @DisplayName("댓글 삭제 시 댓글을 찾을 수 없으면 예외가 발생한다")
    void commentDeleteNotFoundComment() throws Exception {
        given(tokenService.tokenPayload(anyString())).willReturn(mockClaims());
        willThrow(new NotFoundCommentException()).given(commentService).commentDelete(anyLong(), anyLong(), anyString());

        mockMvc.perform(delete("/api/posts/{postNumber}/comments/{commentNumber}", 1, 1)
                        .header("Authorization", "Bearer access-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.result.path").value("/api/posts/1/comments/1"))
                .andExpect(jsonPath("$.result.error.code").value("E404003"))
                .andExpect(jsonPath("$.result.error.message").value("댓글을 찾을 수 없습니다."))
                .andExpect(jsonPath("$.result.error.fieldErrors").isEmpty())
                .andDo(restDocs.document(
                        pathParameters(
                                parameterWithName("postNumber").description("게시글 번호"),
                                parameterWithName("commentNumber").description("댓글 번호")
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
    @DisplayName("댓글 삭제 시 작성자가 아닌데 삭제를 시도할 경우 예외가 발생한다")
    void commentDeleteNotCommentOnwer() throws Exception {
        given(tokenService.tokenPayload(anyString())).willReturn(mockClaims());
        willThrow(new CommentDeleteAccessDeniedException()).given(commentService).commentDelete(anyLong(), anyLong(), anyString());

        mockMvc.perform(delete("/api/posts/{postNumber}/comments/{commentNumber}", 1, 1)
                        .header("Authorization", "Bearer access-token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.result.path").value("/api/posts/1/comments/1"))
                .andExpect(jsonPath("$.result.error.code").value("E403004"))
                .andExpect(jsonPath("$.result.error.message").value("댓글 삭제는 작성자만 할 수 있습니다."))
                .andExpect(jsonPath("$.result.error.fieldErrors").isEmpty())
                .andDo(restDocs.document(
                        pathParameters(
                                parameterWithName("postNumber").description("게시글 번호"),
                                parameterWithName("commentNumber").description("댓글 번호")
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