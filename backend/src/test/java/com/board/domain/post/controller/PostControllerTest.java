package com.board.domain.post.controller;

import com.board.domain.post.dto.PostDetailResponse;
import com.board.domain.post.dto.PostListItem;
import com.board.domain.post.dto.PostListResponse;
import com.board.domain.post.dto.PostModifyRequest;
import com.board.domain.post.dto.PostWriteRequest;
import com.board.domain.post.exception.NotFoundPostException;
import com.board.domain.post.exception.PostDeleteAccessDeniedException;
import com.board.domain.post.exception.PostModifyAccessDeniedException;
import com.board.domain.post.service.PostService;

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

import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.JsonFieldType.BOOLEAN;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
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

@WebMvcTest(controllers = PostController.class)
class PostControllerTest extends RestDocsTestSupport {

    @MockBean
    private PostService postService;

    @Test
    @DisplayName("게시글을 작성한다")
    void postWrite() throws Exception {
        PostWriteRequest postWriteRequest = new PostWriteRequest("제목", "내용");

        given(tokenService.tokenPayload(anyString())).willReturn(mockClaims());
        willDoNothing().given(postService).postWrite(any(PostWriteRequest.class), anyString());

        mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postWriteRequest))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.result").isEmpty())
                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName("Authorization").description("액세스 토큰")
                        ),
                        requestFields(
                                fieldWithPath("title").type(STRING).description("제목"),
                                fieldWithPath("content").type(STRING).description("내용")
                        ),
                        responseFields(
                                commonSuccessResponse()
                        )
                ));
    }

    @Test
    @DisplayName("게시글 작성 시 입력값이 잘못되면 예외가 발생한다")
    void postWriteInvalidInputValue() throws Exception {
        PostWriteRequest invalidPostWriteRequest = new PostWriteRequest("", "내용");

        given(tokenService.tokenPayload(anyString())).willReturn(mockClaims());
        willDoNothing().given(postService).postWrite(any(PostWriteRequest.class), anyString());

        mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPostWriteRequest))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("fail"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.result.path").value("/api/posts"))
                .andExpect(jsonPath("$.result.error.code").value("E400001"))
                .andExpect(jsonPath("$.result.error.message").value("입력값이 잘못되었습니다."))
                .andExpect(jsonPath("$.result.error.fieldErrors[0].field").value("title"))
                .andExpect(jsonPath("$.result.error.fieldErrors[0].input").value(""))
                .andExpect(jsonPath("$.result.error.fieldErrors[0].message").value("제목을 입력해 주세요."))
                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName("Authorization").description("액세스 토큰")
                        ),
                        requestFields(
                                fieldWithPath("title").type(STRING).description("제목"),
                                fieldWithPath("content").type(STRING).description("내용")
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
    @DisplayName("게시글을 상세조회 한다")
    void postDetail() throws Exception {
        PostDetailResponse postDetailResponse = new PostDetailResponse(1L, "제목", "yoonkun", "내용", LocalDateTime.of(2024, 6, 17, 0, 0));

        given(postService.postDetail(anyLong())).willReturn(postDetailResponse);

        mockMvc.perform(get("/api/posts/{postId}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.result.postId").value(1))
                .andExpect(jsonPath("$.result.title").value("제목"))
                .andExpect(jsonPath("$.result.writer").value("yoonkun"))
                .andExpect(jsonPath("$.result.content").value("내용"))
                .andExpect(jsonPath("$.result.createdAt").value("2024-06-17T00:00:00"))
                .andDo(restDocs.document(
                        pathParameters(
                                parameterWithName("postId").description("게시글 번호")
                        ),
                        responseFields(
                                commonSuccessResponse())
                                .and(
                                        fieldWithPath("result.postId").description("게시글 번호"),
                                        fieldWithPath("result.title").description("게시글 제목"),
                                        fieldWithPath("result.writer").description("게시글 작성자"),
                                        fieldWithPath("result.content").description("게시글 내용"),
                                        fieldWithPath("result.createdAt").description("게시글 작성일")
                                )
                ));
    }

    @Test
    @DisplayName("게시글 상세조회 시 게시글을 찾을 수 없으면 예외가 발생한다")
    void postDetailNotFoundPost() throws Exception {
        willThrow(new NotFoundPostException()).given(postService).postDetail(anyLong());

        mockMvc.perform(get("/api/posts/{postId}", 1))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("fail"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.result.path").value("/api/posts/1"))
                .andExpect(jsonPath("$.result.error.code").value("E404002"))
                .andExpect(jsonPath("$.result.error.message").value("게시글을 찾을 수 없습니다."))
                .andExpect(jsonPath("$.result.error.fieldErrors").isEmpty())
                .andDo(restDocs.document(
                        pathParameters(
                                parameterWithName("postId").description("게시글 번호")
                        ),
                        responseFields(
                                commonErrorResponse()
                        )
                ));
    }

    @Test
    @DisplayName("게시글 목록을 조회한다")
    void postList() throws Exception {
        List<PostListItem> posts = List.of(
                new PostListItem(1L, "제목", "작성자", 5, LocalDateTime.of(2024, 6, 17, 0, 0))
        );
        PostListResponse postListResponse = new PostListResponse(posts, 1, 1, 1, false, false, true, true);

        given(postService.postList(anyInt())).willReturn(postListResponse);

        mockMvc.perform(get("/api/posts")
                        .param("page", "1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.result.posts[0].postId").value(1))
                .andExpect(jsonPath("$.result.posts[0].title").value("제목"))
                .andExpect(jsonPath("$.result.posts[0].writer").value("작성자"))
                .andExpect(jsonPath("$.result.posts[0].commentCount").value(5))
                .andExpect(jsonPath("$.result.posts[0].createdAt").value("2024-06-17T00:00:00"))
                .andExpect(jsonPath("$.result.page").value(1))
                .andExpect(jsonPath("$.result.totalPages").value(1))
                .andExpect(jsonPath("$.result.totalElements").value(1))
                .andExpect(jsonPath("$.result.prev").value(false))
                .andExpect(jsonPath("$.result.next").value(false))
                .andExpect(jsonPath("$.result.first").value(true))
                .andExpect(jsonPath("$.result.last").value(true))
                .andDo(restDocs.document(
                        queryParameters(
                                parameterWithName("page").description("페이지 번호")
                        ),
                        responseFields(
                                commonSuccessResponse())
                                .and(
                                        fieldWithPath("result.posts").type(ARRAY).description("게시글 목록"),
                                        fieldWithPath("result.posts[].postId").type(NUMBER).description("게시글 번호"),
                                        fieldWithPath("result.posts[].title").type(STRING).description("게시글 제목"),
                                        fieldWithPath("result.posts[].writer").type(STRING).description("게시글 제목"),
                                        fieldWithPath("result.posts[].commentCount").type(NUMBER).description("댓글 개수"),
                                        fieldWithPath("result.posts[].createdAt").type(STRING).description("게시글 제목"),
                                        fieldWithPath("result.page").type(NUMBER).description("페이지 번호"),
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
    @DisplayName("게시글을 검색한다")
    void postListSearch() throws Exception {
        List<PostListItem> posts = List.of(
                new PostListItem(1L, "제목", "작성자", 5, LocalDateTime.of(2024, 6, 17, 0, 0))
        );
        PostListResponse postListResponse = new PostListResponse(posts, 1, 1, 1, false, false, true, true);

        given(postService.postListSearch(anyInt(), anyString(), anyString())).willReturn(postListResponse);

        mockMvc.perform(get("/api/posts/search")
                        .param("page", "1")
                        .param("type", "title")
                        .param("keyword", "제목")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.result.posts[0].postId").value(1))
                .andExpect(jsonPath("$.result.posts[0].title").value("제목"))
                .andExpect(jsonPath("$.result.posts[0].writer").value("작성자"))
                .andExpect(jsonPath("$.result.posts[0].commentCount").value(5))
                .andExpect(jsonPath("$.result.posts[0].createdAt").value("2024-06-17T00:00:00"))
                .andExpect(jsonPath("$.result.page").value(1))
                .andExpect(jsonPath("$.result.totalPages").value(1))
                .andExpect(jsonPath("$.result.totalElements").value(1))
                .andExpect(jsonPath("$.result.prev").value(false))
                .andExpect(jsonPath("$.result.next").value(false))
                .andExpect(jsonPath("$.result.first").value(true))
                .andExpect(jsonPath("$.result.last").value(true))
                .andDo(restDocs.document(
                        queryParameters(
                                parameterWithName("page").description("페이지 번호"),
                                parameterWithName("type").description("검색 기준"),
                                parameterWithName("keyword").description("검색 단어")
                        ),
                        responseFields(
                                commonSuccessResponse())
                                .and(
                                        fieldWithPath("result.posts").type(ARRAY).description("게시글 목록"),
                                        fieldWithPath("result.posts[].postId").type(NUMBER).description("게시글 번호"),
                                        fieldWithPath("result.posts[].title").type(STRING).description("게시글 제목"),
                                        fieldWithPath("result.posts[].writer").type(STRING).description("게시글 제목"),
                                        fieldWithPath("result.posts[].commentCount").type(NUMBER).description("댓글 개수"),
                                        fieldWithPath("result.posts[].createdAt").type(STRING).description("게시글 제목"),
                                        fieldWithPath("result.page").type(NUMBER).description("페이지 번호"),
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
    @DisplayName("게시글을 수정한다")
    void postModify() throws Exception {
        PostModifyRequest postModifyRequest = new PostModifyRequest("제목", "내용");

        given(tokenService.tokenPayload(anyString())).willReturn(mockClaims());
        willDoNothing().given(postService).postModify(anyLong(), any(PostModifyRequest.class), anyString());

        mockMvc.perform(put("/api/posts/{postId}", 1)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postModifyRequest))
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
                                parameterWithName("postId").description("게시글 번호")
                        ),
                        requestFields(
                                fieldWithPath("title").type(STRING).description("제목"),
                                fieldWithPath("content").type(STRING).description("내용")
                        ),
                        responseFields(
                                commonSuccessResponse()
                        )
                ));
    }

    @Test
    @DisplayName("게시글 수정 시 입력값이 잘못되면 예외가 발생한다")
    void postModifyInvalidInputValue() throws Exception {
        PostModifyRequest invalidPostModifyRequest = new PostModifyRequest("", "내용");

        given(tokenService.tokenPayload(anyString())).willReturn(mockClaims());
        willDoNothing().given(postService).postModify(anyLong(), any(PostModifyRequest.class), anyString());

        mockMvc.perform(put("/api/posts/{postId}", 1)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPostModifyRequest))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("fail"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.result.path").value("/api/posts/1"))
                .andExpect(jsonPath("$.result.error.code").value("E400001"))
                .andExpect(jsonPath("$.result.error.message").value("입력값이 잘못되었습니다."))
                .andExpect(jsonPath("$.result.error.fieldErrors[0].field").value("title"))
                .andExpect(jsonPath("$.result.error.fieldErrors[0].input").value(""))
                .andExpect(jsonPath("$.result.error.fieldErrors[0].message").value("제목을 입력해 주세요."))
                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName("Authorization").description("액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("postId").description("게시글 번호")
                        ),
                        requestFields(
                                fieldWithPath("title").type(STRING).description("제목"),
                                fieldWithPath("content").type(STRING).description("내용")
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
    @DisplayName("게시글 수정 시 게시글을 찾을 수 없으면 예외가 발생한다")
    void postModifyNotFoundPost() throws Exception {
        PostModifyRequest postModifyRequest = new PostModifyRequest("제목", "내용");

        given(tokenService.tokenPayload(anyString())).willReturn(mockClaims());
        willThrow(new NotFoundPostException()).given(postService).postModify(anyLong(), any(PostModifyRequest.class), anyString());

        mockMvc.perform(put("/api/posts/{postId}", 1)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postModifyRequest))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("fail"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.result.path").value("/api/posts/1"))
                .andExpect(jsonPath("$.result.error.code").value("E404002"))
                .andExpect(jsonPath("$.result.error.message").value("게시글을 찾을 수 없습니다."))
                .andExpect(jsonPath("$.result.error.fieldErrors").isEmpty())
                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName("Authorization").description("Access Token")
                        ),
                        pathParameters(
                                parameterWithName("postId").description("게시글 번호")
                        ),
                        requestFields(
                                fieldWithPath("title").type(STRING).description("제목"),
                                fieldWithPath("content").type(STRING).description("내용")
                        ),
                        responseFields(
                                commonErrorResponse()
                        )
                ));
    }

    @Test
    @DisplayName("게시글 수정 시 작성자가 아닌데 수정을 시도할 경우 예외가 발생한다")
    void postModifyNotPostOwner() throws Exception {
        PostModifyRequest postModifyRequest = new PostModifyRequest("제목", "내용");

        given(tokenService.tokenPayload(anyString())).willReturn(mockClaims());
        willThrow(new PostModifyAccessDeniedException()).given(postService).postModify(anyLong(), any(PostModifyRequest.class), anyString());

        mockMvc.perform(put("/api/posts/{postId}", 1)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postModifyRequest))
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("fail"))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.result.path").value("/api/posts/1"))
                .andExpect(jsonPath("$.result.error.code").value("E403001"))
                .andExpect(jsonPath("$.result.error.message").value("게시글 수정은 작성자만 할 수 있습니다."))
                .andExpect(jsonPath("$.result.error.fieldErrors").isEmpty())
                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName("Authorization").description("Access Token")
                        ),
                        pathParameters(
                                parameterWithName("postId").description("게시글 번호")
                        ),
                        requestFields(
                                fieldWithPath("title").type(STRING).description("제목"),
                                fieldWithPath("content").type(STRING).description("내용")
                        ),
                        responseFields(
                                commonErrorResponse()
                        )
                ));
    }

    @Test
    @DisplayName("게시글을 삭제한다")
    void postDelete() throws Exception {
        given(tokenService.tokenPayload(anyString())).willReturn(mockClaims());
        willDoNothing().given(postService).postDelete(anyLong(), anyString());

        mockMvc.perform(delete("/api/posts/{postId}", 1)
                        .header("Authorization", "Bearer access-token")
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
                                parameterWithName("postId").description("게시글 번호")
                        ),
                        responseFields(
                                commonSuccessResponse()
                        )
                ));
    }

    @Test
    @DisplayName("게시글 삭제 시 게시글을 찾을 수 없으면 예외가 발생한다")
    void postDeleteNotFoundPost() throws Exception {
        given(tokenService.tokenPayload(anyString())).willReturn(mockClaims());
        willThrow(new NotFoundPostException()).given(postService).postDelete(anyLong(), anyString());

        mockMvc.perform(delete("/api/posts/{postId}", 1)
                        .header("Authorization", "Bearer access-token")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("fail"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.result.path").value("/api/posts/1"))
                .andExpect(jsonPath("$.result.error.code").value("E404002"))
                .andExpect(jsonPath("$.result.error.message").value("게시글을 찾을 수 없습니다."))
                .andExpect(jsonPath("$.result.error.fieldErrors").isEmpty())
                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName("Authorization").description("액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("postId").description("게시글 번호")
                        ),
                        responseFields(
                                commonErrorResponse()
                        )
                ));
    }

    @Test
    @DisplayName("게시글 삭제 시 작성자가 아닌데 삭제를 시도할 경우 예외가 발생한다")
    void postDeleteNotPostOwner() throws Exception {
        given(tokenService.tokenPayload(anyString())).willReturn(mockClaims());
        willThrow(new PostDeleteAccessDeniedException()).given(postService).postDelete(anyLong(), anyString());

        mockMvc.perform(delete("/api/posts/{postId}", 1)
                        .header("Authorization", "Bearer access-token")
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("fail"))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.result.path").value("/api/posts/1"))
                .andExpect(jsonPath("$.result.error.code").value("E403002"))
                .andExpect(jsonPath("$.result.error.message").value("게시글 삭제는 작성자만 할 수 있습니다."))
                .andExpect(jsonPath("$.result.error.fieldErrors").isEmpty())
                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName("Authorization").description("액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("postId").description("게시글 번호")
                        ),
                        responseFields(
                                commonErrorResponse()
                        )
                ));
    }

}