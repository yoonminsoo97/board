package com.board.domain.reply.controller;

import com.board.domain.comment.exception.NotFoundCommentException;
import com.board.domain.member.exception.NotFoundMemberException;
import com.board.domain.reply.dto.ReplyWriteRequest;
import com.board.domain.reply.service.ReplyService;
import com.board.support.RestDocsTestSupport;

import org.junit.jupiter.api.DisplayName;
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
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ReplyController.class)
class ReplyControllerTest extends RestDocsTestSupport {

    @MockBean
    private ReplyService replyService;

    @Test
    @DisplayName("대댓글을 작성한다")
    void replyWrite() throws Exception {
        ReplyWriteRequest replyWriteRequest = new ReplyWriteRequest("reply");

        given(tokenService.tokenPayload(anyString())).willReturn(mockClaims());
        willDoNothing().given(replyService).replyWrite(anyString(), anyLong(), anyLong(), any(ReplyWriteRequest.class));

        mockMvc.perform(post("/api/posts/{postId}/comments/{commentId}/reply", 1, 1)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(replyWriteRequest))
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
        ReplyWriteRequest invalidReplyWriteRequest = new ReplyWriteRequest("");

        given(tokenService.tokenPayload(anyString())).willReturn(mockClaims());

        mockMvc.perform(post("/api/posts/{postId}/comments/{commentId}/reply", 1, 1)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReplyWriteRequest))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("fail"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.result.path").value("/api/posts/1/comments/1/reply"))
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
    @DisplayName("대댓글 작성 시 회원을 찾을 수 없으면 예외가 발생한다")
    void replyWriteNotFoundMember() throws Exception {
        ReplyWriteRequest replyWriteRequest = new ReplyWriteRequest("reply");

        given(tokenService.tokenPayload(anyString())).willReturn(mockClaims());
        willThrow(new NotFoundMemberException()).given(replyService).replyWrite(anyString(), anyLong(), anyLong(), any(ReplyWriteRequest.class));

        mockMvc.perform(post("/api/posts/{postId}/comments/{commentId}/reply", 1, 1)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(replyWriteRequest))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("fail"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.result.path").value("/api/posts/1/comments/1/reply"))
                .andExpect(jsonPath("$.result.error.code").value("E404001"))
                .andExpect(jsonPath("$.result.error.message").value("회원을 찾을 수 없습니다."))
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
    @DisplayName("대댓글 작성 시 댓글을 찾을 수 없으면 예외가 발생한다")
    void replyWriteNotFoundComment() throws Exception {
        ReplyWriteRequest replyWriteRequest = new ReplyWriteRequest("reply");

        given(tokenService.tokenPayload(anyString())).willReturn(mockClaims());
        willThrow(new NotFoundCommentException()).given(replyService).replyWrite(anyString(), anyLong(), anyLong(), any(ReplyWriteRequest.class));

        mockMvc.perform(post("/api/posts/{postId}/comments/{commentId}/reply", 1, 1)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(replyWriteRequest))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("fail"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.result.path").value("/api/posts/1/comments/1/reply"))
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

}