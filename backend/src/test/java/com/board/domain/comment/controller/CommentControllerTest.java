package com.board.domain.comment.controller;

import com.board.domain.comment.dto.CommentListItem;
import com.board.domain.comment.dto.CommentListResponse;
import com.board.domain.comment.dto.CommentModifyRequest;
import com.board.domain.comment.dto.CommentWriteRequest;
import com.board.domain.comment.exception.CommentDeleteAccessDeniedException;
import com.board.domain.comment.exception.CommentModifyAccessDeniedException;
import com.board.domain.comment.exception.NotFoundCommentException;
import com.board.domain.comment.service.CommentService;
import com.board.domain.member.exception.NotFoundMemberException;
import com.board.domain.post.exception.NotFoundPostException;
import com.board.support.RestDocsTestSupport;

import io.jsonwebtoken.Claims;

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

        Claims payload = getPayload(createAccessToken());

        given(tokenService.tokenPayload(anyString())).willReturn(payload);
        willDoNothing().given(commentService).commentWrite(anyLong(), any(CommentWriteRequest.class), anyString());

        mockMvc.perform(post("/api/posts/{postNumber}/comments/write", 1)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentWriteRequest))
                )
                .andExpect(status().isOk())
                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName("Authorization").description("Authorization 헤더")
                        ),
                        pathParameters(
                                parameterWithName("postNumber").description("게시글 번호")
                        ),
                        requestFields(
                                fieldWithPath("content").type(STRING).description("댓글")
                        )
                ));
    }

    @Test
    @DisplayName("댓글 작성 시 입력값이 잘못되면 예외가 발생한다")
    void commentWriteInvalidInputValue() throws Exception {
        CommentWriteRequest invalidCommentWriteRequest = new CommentWriteRequest("");

        Claims payload = getPayload(createAccessToken());

        given(tokenService.tokenPayload(anyString())).willReturn(payload);

        mockMvc.perform(post("/api/posts/{postNumber}/comments/write", 1)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCommentWriteRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("E400001"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("입력값이 잘못되었습니다."))
                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName("Authorization").description("Authorization 헤더")
                        ),
                        pathParameters(
                                parameterWithName("postNumber").description("게시글 번호")
                        ),
                        requestFields(
                                fieldWithPath("content").type(STRING).description("댓글")
                        ),
                        responseFields(
                                fieldWithPath("errorCode").type(STRING).description("에러 코드"),
                                fieldWithPath("status").type(NUMBER).description("상태 코드"),
                                fieldWithPath("message").type(STRING).description("에러 메세지"),
                                fieldWithPath("timeStamp").type(STRING).description("에러 발생 시간")
                        )
                ));
    }

    @Test
    @DisplayName("댓글 작성 시 게시글을 찾을 수 없으면 예외가 발생한다")
    void commentWriteNotFoundPost() throws Exception {
        CommentWriteRequest commentWriteRequest = new CommentWriteRequest("댓글");

        Claims payload = getPayload(createAccessToken());

        given(tokenService.tokenPayload(anyString())).willReturn(payload);
        willThrow(new NotFoundPostException()).given(commentService).commentWrite(anyLong(), any(CommentWriteRequest.class), anyString());

        mockMvc.perform(post("/api/posts/{postNumber}/comments/write", 1)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentWriteRequest))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("E404002"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("게시글을 찾을 수 없습니다."))
                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName("Authorization").description("Authorization 헤더")
                        ),
                        pathParameters(
                                parameterWithName("postNumber").description("게시글 번호")
                        ),
                        requestFields(
                                fieldWithPath("content").type(STRING).description("댓글")
                        ),
                        responseFields(
                                fieldWithPath("errorCode").type(STRING).description("에러 코드"),
                                fieldWithPath("status").type(NUMBER).description("상태 코드"),
                                fieldWithPath("message").type(STRING).description("에러 메세지"),
                                fieldWithPath("timeStamp").type(STRING).description("에러 발생 시간")
                        )
                ));
    }

    @Test
    @DisplayName("댓글 작성 시 회원을 찾을 수 없으면 예외가 발생한다")
    void commentWriteNotFoundMember() throws Exception {
        CommentWriteRequest commentWriteRequest = new CommentWriteRequest("댓글");

        Claims payload = getPayload(createAccessToken());

        given(tokenService.tokenPayload(anyString())).willReturn(payload);
        willThrow(new NotFoundMemberException()).given(commentService).commentWrite(anyLong(), any(CommentWriteRequest.class), anyString());

        mockMvc.perform(post("/api/posts/{postNumber}/comments/write", 1)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentWriteRequest))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("E404001"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("회원을 찾을 수 없습니다."))
                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName("Authorization").description("Authorization 헤더")
                        ),
                        pathParameters(
                                parameterWithName("postNumber").description("게시글 번호")
                        ),
                        requestFields(
                                fieldWithPath("content").type(STRING).description("댓글")
                        ),
                        responseFields(
                                fieldWithPath("errorCode").type(STRING).description("에러 코드"),
                                fieldWithPath("status").type(NUMBER).description("상태 코드"),
                                fieldWithPath("message").type(STRING).description("에러 메세지"),
                                fieldWithPath("timeStamp").type(STRING).description("에러 발생 시간")
                        )
                ));
    }

    @Test
    @DisplayName("댓글 목록을 조회한다")
    void commentList() throws Exception {
        List<CommentListItem> comments = List.of(
                new CommentListItem(5L, "작성자5", "댓글5", LocalDateTime.now()),
                new CommentListItem(4L, "작성자4", "댓글4", LocalDateTime.now()),
                new CommentListItem(3L, "작성자3", "댓글3", LocalDateTime.now()),
                new CommentListItem(2L, "작성자2", "댓글2", LocalDateTime.now()),
                new CommentListItem(1L, "작성자1", "댓글1", LocalDateTime.now())
        );
        CommentListResponse commentListResponse = new CommentListResponse(comments, 0, 1, 5, false, false, true, true);

        given(commentService.commentList(anyLong(), anyInt())).willReturn(commentListResponse);

        mockMvc.perform(get("/api/posts/{postNumber}/comments/page/{pageNumber}", 1, 1))
                .andExpect(status().isOk())
                .andDo(restDocs.document(
                   pathParameters(
                           parameterWithName("postNumber").description("게시글 번호"),
                           parameterWithName("pageNumber").description("댓글 페이지 번호")
                   ),
                        responseFields(
                                fieldWithPath("comments").type(ARRAY).description("댓글 목록"),
                                fieldWithPath("comments[].commentNum").type(NUMBER).description("댓글 번호"),
                                fieldWithPath("comments[].writer").type(STRING).description("댓글 작성자"),
                                fieldWithPath("comments[].content").type(STRING).description("댓글 내용"),
                                fieldWithPath("comments[].createdAt").type(STRING).description("댓글 작성 시간"),
                                fieldWithPath("pageNumber").type(NUMBER).description("현재 페이지 번호"),
                                fieldWithPath("totalElements").type(NUMBER).description("전체 댓글 개수"),
                                fieldWithPath("totalPages").type(NUMBER).description("전체 페이지 개수"),
                                fieldWithPath("prev").type(BOOLEAN).description("이전 페이지 여부"),
                                fieldWithPath("next").type(BOOLEAN).description("다음 페이지 여부"),
                                fieldWithPath("first").type(BOOLEAN).description("첫째 페이지 여부"),
                                fieldWithPath("last").type(BOOLEAN).description("마지막 페이지 여부")
                        )
                ));
    }

    @Test
    @DisplayName("댓글을 수정한다")
    void commentModify() throws Exception {
        CommentModifyRequest commentModifyRequest = new CommentModifyRequest("댓글");
        Claims payload = getPayload(createAccessToken());

        given(tokenService.tokenPayload(anyString())).willReturn(payload);
        willDoNothing().given(commentService).commentModify(anyLong(), anyLong(), any(CommentModifyRequest.class), anyString());

        mockMvc.perform(put("/api/posts/{postNumber}/comments/{commentNumber}", 1, 1)
                        .header("Authorization", "Bearer acces-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentModifyRequest))
                )
                .andExpect(status().isOk())
                .andDo(restDocs.document(
                        pathParameters(
                                parameterWithName("postNumber").description("게시글 번호"),
                                parameterWithName("commentNumber").description("댓글 번호")
                        ),
                        requestHeaders(
                                headerWithName("Authorization").description("Authorization 헤더")
                        ),
                        requestFields(
                                fieldWithPath("content").type(STRING).description("수정 댓글")
                        )
                ));
    }

    @Test
    @DisplayName("댓글 수정 시 입력값이 잘못되면 예외가 발생한다")
    void commentModifyInvalidInputValue() throws Exception {
        CommentModifyRequest invalidCommentModifyRequest = new CommentModifyRequest("");
        Claims payload = getPayload(createAccessToken());

        given(tokenService.tokenPayload(anyString())).willReturn(payload);

        mockMvc.perform(put("/api/posts/{postNumber}/comments/{commentNumber}", 1, 1)
                        .header("Authorization", "Bearer acces-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCommentModifyRequest))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("E400001"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("입력값이 잘못되었습니다."))
                .andDo(restDocs.document(
                        pathParameters(
                                parameterWithName("postNumber").description("게시글 번호"),
                                parameterWithName("commentNumber").description("댓글 번호")
                        ),
                        requestHeaders(
                                headerWithName("Authorization").description("Authorization 헤더")
                        ),
                        requestFields(
                                fieldWithPath("content").type(STRING).description("수정 댓글")
                        ),
                        responseFields(
                                fieldWithPath("errorCode").type(STRING).description("에러 코드"),
                                fieldWithPath("status").type(NUMBER).description("상태 코드"),
                                fieldWithPath("message").type(STRING).description("에러 메세지"),
                                fieldWithPath("timeStamp").type(STRING).description("에러 발생 시간")
                        )
                ));
    }

    @Test
    @DisplayName("댓글 수정 시 댓글을 찾을 수 없으면 예외가 발생한다")
    void commentModifyNotFoundComment() throws Exception {
        CommentModifyRequest commentModifyRequest = new CommentModifyRequest("댓글");
        Claims payload = getPayload(createAccessToken());

        given(tokenService.tokenPayload(anyString())).willReturn(payload);
        willThrow(new NotFoundCommentException()).given(commentService).commentModify(anyLong(), anyLong(), any(CommentModifyRequest.class), anyString());

        mockMvc.perform(put("/api/posts/{postNumber}/comments/{commentNumber}", 1, 1)
                        .header("Authorization", "Bearer acces-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentModifyRequest))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("E404003"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("댓글을 찾을 수 없습니다."))
                .andDo(restDocs.document(
                        pathParameters(
                                parameterWithName("postNumber").description("게시글 번호"),
                                parameterWithName("commentNumber").description("댓글 번호")
                        ),
                        requestHeaders(
                                headerWithName("Authorization").description("Authorization 헤더")
                        ),
                        requestFields(
                                fieldWithPath("content").type(STRING).description("수정 댓글")
                        ),
                        responseFields(
                                fieldWithPath("errorCode").type(STRING).description("에러 코드"),
                                fieldWithPath("status").type(NUMBER).description("상태 코드"),
                                fieldWithPath("message").type(STRING).description("에러 메세지"),
                                fieldWithPath("timeStamp").type(STRING).description("에러 발생 시간")
                        )
                ));
    }

    @Test
    @DisplayName("댓글 수정 시 작성자가 아닌데 수정을 시도할 경우 예외가 발생한다")
    void commentModifyNotCommentOwner() throws Exception {
        CommentModifyRequest commentModifyRequest = new CommentModifyRequest("댓글");
        Claims payload = getPayload(createAccessToken());

        given(tokenService.tokenPayload(anyString())).willReturn(payload);
        willThrow(new CommentModifyAccessDeniedException()).given(commentService).commentModify(anyLong(), anyLong(), any(CommentModifyRequest.class), anyString());

        mockMvc.perform(put("/api/posts/{postNumber}/comments/{commentNumber}", 1, 1)
                        .header("Authorization", "Bearer acces-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentModifyRequest))
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("E403003"))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("댓글 수정은 작성자만 할 수 있습니다."))
                .andDo(restDocs.document(
                        pathParameters(
                                parameterWithName("postNumber").description("게시글 번호"),
                                parameterWithName("commentNumber").description("댓글 번호")
                        ),
                        requestHeaders(
                                headerWithName("Authorization").description("Authorization 헤더")
                        ),
                        requestFields(
                                fieldWithPath("content").type(STRING).description("수정 댓글")
                        ),
                        responseFields(
                                fieldWithPath("errorCode").type(STRING).description("에러 코드"),
                                fieldWithPath("status").type(NUMBER).description("상태 코드"),
                                fieldWithPath("message").type(STRING).description("에러 메세지"),
                                fieldWithPath("timeStamp").type(STRING).description("에러 발생 시간")
                        )
                ));
    }

    @Test
    @DisplayName("댓글을 삭제한다")
    void commentDelete() throws Exception {
        Claims payload = getPayload(createAccessToken());

        given(tokenService.tokenPayload(anyString())).willReturn(payload);
        willDoNothing().given(commentService).commentDelete(anyLong(), anyLong(), anyString());

        mockMvc.perform(delete("/api/posts/{postNumber}/comments/{commentNumber}", 1, 1)
                        .header("Authorization", "Bearer access-token"))
                .andExpect(status().isOk())
                .andDo(restDocs.document(
                        pathParameters(
                                parameterWithName("postNumber").description("게시글 번호"),
                                parameterWithName("commentNumber").description("댓글 번호")
                        ),
                        requestHeaders(
                                headerWithName("Authorization").description("Authorization 헤더")
                        )
                ));
    }

    @Test
    @DisplayName("댓글 삭제 시 댓글을 찾을 수 없으면 예외가 발생한다")
    void commentDeleteNotFoundComment() throws Exception {
        Claims payload = getPayload(createAccessToken());

        given(tokenService.tokenPayload(anyString())).willReturn(payload);
        willThrow(new NotFoundCommentException()).given(commentService).commentDelete(anyLong(), anyLong(), anyString());

        mockMvc.perform(delete("/api/posts/{postNumber}/comments/{commentNumber}", 1, 1)
                        .header("Authorization", "Bearer access-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("E404003"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("댓글을 찾을 수 없습니다."))
                .andDo(restDocs.document(
                        pathParameters(
                                parameterWithName("postNumber").description("게시글 번호"),
                                parameterWithName("commentNumber").description("댓글 번호")
                        ),
                        requestHeaders(
                                headerWithName("Authorization").description("Authorization 헤더")
                        ),
                        responseFields(
                                fieldWithPath("errorCode").type(STRING).description("에러 코드"),
                                fieldWithPath("status").type(NUMBER).description("상태 코드"),
                                fieldWithPath("message").type(STRING).description("에러 메세지"),
                                fieldWithPath("timeStamp").type(STRING).description("에러 발생 시간")
                        )
                ));
    }

    @Test
    @DisplayName("댓글 삭제 시 작성자가 아닌데 삭제를 시도할 경우 예외가 발생한다")
    void commentDeleteNotCommentOnwer() throws Exception {
        Claims payload = getPayload(createAccessToken());

        given(tokenService.tokenPayload(anyString())).willReturn(payload);
        willThrow(new CommentDeleteAccessDeniedException()).given(commentService).commentDelete(anyLong(), anyLong(), anyString());

        mockMvc.perform(delete("/api/posts/{postNumber}/comments/{commentNumber}", 1, 1)
                        .header("Authorization", "Bearer access-token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("E403004"))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("댓글 삭제는 작성자만 할 수 있습니다."))
                .andDo(restDocs.document(
                        pathParameters(
                                parameterWithName("postNumber").description("게시글 번호"),
                                parameterWithName("commentNumber").description("댓글 번호")
                        ),
                        requestHeaders(
                                headerWithName("Authorization").description("Authorization 헤더")
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