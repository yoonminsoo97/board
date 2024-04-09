package com.board.domain.post.controller;

import com.board.domain.member.exception.NotFoundMemberException;
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
        String accessToken = createAccessToken();
        Claims payload = getPayload(accessToken);

        given(tokenService.tokenPayload(anyString())).willReturn(payload);
        willDoNothing().given(postService).postWrite(any(PostWriteRequest.class), anyString());

        mockMvc.perform(post("/api/posts/write")
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postWriteRequest))
                )
                .andExpect(status().isOk())
                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName("Authorization").description("Access Token")
                        ),
                        requestFields(
                                fieldWithPath("title").type(STRING).description("제목"),
                                fieldWithPath("content").type(STRING).description("내용")
                        )
                ));
    }

    @Test
    @DisplayName("게시글 작성 시 입력값이 잘못되면 예외가 발생한다")
    void postWriteInvalidInputValue() throws Exception {
        PostWriteRequest invalidPostWriteRequest = new PostWriteRequest("", "내용");
        String accessToken = createAccessToken();
        Claims payload = getPayload(accessToken);

        given(tokenService.tokenPayload(anyString())).willReturn(payload);
        willDoNothing().given(postService).postWrite(any(PostWriteRequest.class), anyString());

        mockMvc.perform(post("/api/posts/write")
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPostWriteRequest))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("E400001"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("입력값이 잘못되었습니다."))
                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName("Authorization").description("Access Token")
                        ),
                        requestFields(
                                fieldWithPath("title").type(STRING).description("제목"),
                                fieldWithPath("content").type(STRING).description("내용")
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
    @DisplayName("게시글 작성 시 회원을 찾을 수 없으면 예외가 발생한다")
    void postWriteNotFoundMember() throws Exception {
        PostWriteRequest postWriteRequest = new PostWriteRequest("제목", "내용");
        String accessToken = createAccessToken();
        Claims payload = getPayload(accessToken);

        given(tokenService.tokenPayload(anyString())).willReturn(payload);
        willThrow(new NotFoundMemberException()).given(postService).postWrite(any(PostWriteRequest.class), anyString());

        mockMvc.perform(post("/api/posts/write")
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postWriteRequest))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("E404001"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("회원을 찾을 수 없습니다."))
                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName("Authorization").description("Access Token")
                        ),
                        requestFields(
                                fieldWithPath("title").type(STRING).description("제목"),
                                fieldWithPath("content").type(STRING).description("내용")
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
    @DisplayName("게시글을 상세조회 한다")
    void postDetail() throws Exception {
        PostDetailResponse postDetailResponse = new PostDetailResponse(1L, "제목", "yoonkun", "내용", LocalDateTime.now());

        given(postService.postDetail(anyLong())).willReturn(postDetailResponse);

        mockMvc.perform(get("/api/posts/{postNumber}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postNumber").value(1))
                .andExpect(jsonPath("$.title").value("제목"))
                .andExpect(jsonPath("$.writer").value("yoonkun"))
                .andExpect(jsonPath("$.content").value("내용"))
                .andDo(restDocs.document(
                        pathParameters(
                                parameterWithName("postNumber").description("게시글 번호")
                        ),
                        responseFields(
                                fieldWithPath("postNumber").type(NUMBER).description("게시글 번호"),
                                fieldWithPath("title").type(STRING).description("게시글 제목"),
                                fieldWithPath("writer").type(STRING).description("게시글 작성자"),
                                fieldWithPath("content").type(STRING).description("게시글 내용"),
                                fieldWithPath("createdAt").type(STRING).description("게시글 작성시간")
                        )
                ));
    }

    @Test
    @DisplayName("게시글 상세조회 시 게시글을 찾을 수 없으면 예외가 발생한다")
    void postDetailNotFoundPost() throws Exception {
        willThrow(new NotFoundPostException()).given(postService).postDetail(anyLong());

        mockMvc.perform(get("/api/posts/{postNumber}", 1))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("E404002"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("게시글을 찾을 수 없습니다."))
                .andDo(restDocs.document(
                        pathParameters(
                                parameterWithName("postNumber").description("게시글 번호")
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
    @DisplayName("게시글 목록을 조회한다")
    void postList() throws Exception {
        List<PostListItem> posts = List.of(
                new PostListItem(5L, "제목5", "작성자5", 0, LocalDateTime.now()),
                new PostListItem(4L, "제목4", "작성자4", 0, LocalDateTime.now()),
                new PostListItem(3L, "제목3", "작성자3", 0, LocalDateTime.now()),
                new PostListItem(2L, "제목2", "작성자2", 0, LocalDateTime.now()),
                new PostListItem(1L, "제목1", "작성자1", 0, LocalDateTime.now())
        );
        PostListResponse postListResponse = new PostListResponse(posts, 0, 1, 5, false, false, true, true);

        given(postService.postList(anyInt())).willReturn(postListResponse);

        mockMvc.perform(get("/api/posts/page/{pageNumber}", 1))
                .andExpect(status().isOk())
                .andDo(restDocs.document(
                        pathParameters(
                                parameterWithName("pageNumber").description("페이지 번호")
                        ),
                        responseFields(
                                fieldWithPath("posts").type(ARRAY).description("게시글 목록"),
                                fieldWithPath("posts[].postNumber").type(NUMBER).description("게시글 번호"),
                                fieldWithPath("posts[].title").type(STRING).description("게시글 제목"),
                                fieldWithPath("posts[].writer").type(STRING).description("게시글 제목"),
                                fieldWithPath("posts[].createdAt").type(STRING).description("게시글 제목"),
                                fieldWithPath("posts[].commentCount").type(NUMBER).description("댓글 개수"),
                                fieldWithPath("pageNumber").type(NUMBER).description("페이지 번호"),
                                fieldWithPath("totalPages").type(NUMBER).description("전체 페이지 개수"),
                                fieldWithPath("totalElements").type(NUMBER).description("전체 게시글 개수"),
                                fieldWithPath("prev").type(BOOLEAN).description("이전 페이지 이동 가능 여부"),
                                fieldWithPath("next").type(BOOLEAN).description("다음 페이지 이동 가능 여부"),
                                fieldWithPath("first").type(BOOLEAN).description("첫 번째 페이지 여부"),
                                fieldWithPath("last").type(BOOLEAN).description("마지막 페이지 여부")
                        )
                ));
    }

    @Test
    @DisplayName("게시글을 검색한다")
    void postListSearch() throws Exception {
        List<PostListItem> posts = List.of(
                new PostListItem(5L, "제목5", "작성자5", 0, LocalDateTime.now()),
                new PostListItem(4L, "제목4", "작성자4", 0, LocalDateTime.now()),
                new PostListItem(3L, "제목3", "작성자3", 0, LocalDateTime.now()),
                new PostListItem(2L, "제목2", "작성자2", 0, LocalDateTime.now()),
                new PostListItem(1L, "제목1", "작성자1", 0, LocalDateTime.now())
        );
        PostListResponse postListResponse = new PostListResponse(posts, 0, 1, 5, false, false, true, true);

        given(postService.postListSearch(anyInt(), anyString(), anyString())).willReturn(postListResponse);

        mockMvc.perform(get("/api/posts/search")
                        .param("page", "1")
                        .param("type", "title")
                        .param("keyword", "제목")
                )
                .andExpect(status().isOk())
                .andDo(restDocs.document(
                        queryParameters(
                                parameterWithName("page").description("페이지 번호"),
                                parameterWithName("type").description("검색 기준"),
                                parameterWithName("keyword").description("검색 단어")
                        ),
                        responseFields(
                                fieldWithPath("posts").type(ARRAY).description("게시글 목록"),
                                fieldWithPath("posts[].postNumber").type(NUMBER).description("게시글 번호"),
                                fieldWithPath("posts[].title").type(STRING).description("게시글 제목"),
                                fieldWithPath("posts[].writer").type(STRING).description("게시글 제목"),
                                fieldWithPath("posts[].createdAt").type(STRING).description("게시글 제목"),
                                fieldWithPath("posts[].commentCount").type(NUMBER).description("댓글 개수"),
                                fieldWithPath("pageNumber").type(NUMBER).description("페이지 번호"),
                                fieldWithPath("totalPages").type(NUMBER).description("전체 페이지 개수"),
                                fieldWithPath("totalElements").type(NUMBER).description("전체 게시글 개수"),
                                fieldWithPath("prev").type(BOOLEAN).description("이전 페이지 이동 가능 여부"),
                                fieldWithPath("next").type(BOOLEAN).description("다음 페이지 이동 가능 여부"),
                                fieldWithPath("first").type(BOOLEAN).description("첫 번째 페이지 여부"),
                                fieldWithPath("last").type(BOOLEAN).description("마지막 페이지 여부")
                        )
                ));
    }

    @Test
    @DisplayName("게시글을 수정한다")
    void postModify() throws Exception {
        PostModifyRequest postModifyRequest = new PostModifyRequest("제목", "내용");
        String accessToken = createAccessToken();
        Claims payload = getPayload(accessToken);

        given(tokenService.tokenPayload(anyString())).willReturn(payload);
        willDoNothing().given(postService).postModify(anyLong(), any(PostModifyRequest.class), anyString());

        mockMvc.perform(put("/api/posts/{postNumber}", 1)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postModifyRequest))
                )
                .andExpect(status().isOk())
                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName("Authorization").description("Access Token")
                        ),
                        pathParameters(
                                parameterWithName("postNumber").description("게시글 번호")
                        ),
                        requestFields(
                                fieldWithPath("title").type(STRING).description("제목"),
                                fieldWithPath("content").type(STRING).description("내용")
                        )
                ));
    }

    @Test
    @DisplayName("게시글 수정 시 입력값이 잘못되면 예외가 발생한다")
    void postModifyInvalidInputValue() throws Exception {
        PostModifyRequest invalidPostModifyRequest = new PostModifyRequest("", "내용");
        String accessToken = createAccessToken();
        Claims payload = getPayload(accessToken);

        given(tokenService.tokenPayload(anyString())).willReturn(payload);
        willDoNothing().given(postService).postModify(anyLong(), any(PostModifyRequest.class), anyString());

        mockMvc.perform(put("/api/posts/{postNumber}", 1)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPostModifyRequest))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("E400001"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("입력값이 잘못되었습니다."))
                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName("Authorization").description("Access Token")
                        ),
                        pathParameters(
                                parameterWithName("postNumber").description("게시글 번호")
                        ),
                        requestFields(
                                fieldWithPath("title").type(STRING).description("제목"),
                                fieldWithPath("content").type(STRING).description("내용")
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
    @DisplayName("게시글 수정 시 게시글을 찾을 수 없으면 예외가 발생한다")
    void postModifyNotFoundPost() throws Exception {
        PostModifyRequest postModifyRequest = new PostModifyRequest("제목", "내용");
        String accessToken = createAccessToken();
        Claims payload = getPayload(accessToken);

        given(tokenService.tokenPayload(anyString())).willReturn(payload);
        willThrow(new NotFoundPostException()).given(postService).postModify(anyLong(), any(PostModifyRequest.class), anyString());

        mockMvc.perform(put("/api/posts/{postNumber}", 1)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postModifyRequest))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("E404002"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("게시글을 찾을 수 없습니다."))
                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName("Authorization").description("Access Token")
                        ),
                        pathParameters(
                                parameterWithName("postNumber").description("게시글 번호")
                        ),
                        requestFields(
                                fieldWithPath("title").type(STRING).description("제목"),
                                fieldWithPath("content").type(STRING).description("내용")
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
    @DisplayName("게시글 수정 시 작성자가 아닌데 수정을 시도할 경우 예외가 발생한다")
    void postModifyNotPostOwner() throws Exception {
        PostModifyRequest postModifyRequest = new PostModifyRequest("제목", "내용");
        String accessToken = createAccessToken();
        Claims payload = getPayload(accessToken);

        given(tokenService.tokenPayload(anyString())).willReturn(payload);
        willThrow(new PostModifyAccessDeniedException()).given(postService).postModify(anyLong(), any(PostModifyRequest.class), anyString());

        mockMvc.perform(put("/api/posts/{postNumber}", 1)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postModifyRequest))
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("E403001"))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("게시글 수정은 작성자만 할 수 있습니다."))
                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName("Authorization").description("Access Token")
                        ),
                        pathParameters(
                                parameterWithName("postNumber").description("게시글 번호")
                        ),
                        requestFields(
                                fieldWithPath("title").type(STRING).description("제목"),
                                fieldWithPath("content").type(STRING).description("내용")
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
    @DisplayName("게시글을 삭제한다")
    void postDelete() throws Exception {
        String accessToken = createAccessToken();
        Claims payload = getPayload(accessToken);

        given(tokenService.tokenPayload(anyString())).willReturn(payload);
        willDoNothing().given(postService).postDelete(anyLong(), anyString());

        mockMvc.perform(delete("/api/posts/{postNumber}", 1)
                        .header("Authorization", "Bearer access-token")
                )
                .andExpect(status().isOk())
                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName("Authorization").description("Access Token")
                        ),
                        pathParameters(
                                parameterWithName("postNumber").description("게시글 번호")
                        )
                ));
    }

    @Test
    @DisplayName("게시글 삭제 시 게시글을 찾을 수 없으면 예외가 발생한다")
    void postDeleteNotFoundPost() throws Exception {
        String accessToken = createAccessToken();
        Claims payload = getPayload(accessToken);

        given(tokenService.tokenPayload(anyString())).willReturn(payload);
        willThrow(new NotFoundPostException()).given(postService).postDelete(anyLong(), anyString());

        mockMvc.perform(delete("/api/posts/{postNumber}", 1)
                        .header("Authorization", "Bearer access-token")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("E404002"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("게시글을 찾을 수 없습니다."))
                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName("Authorization").description("Access Token")
                        ),
                        pathParameters(
                                parameterWithName("postNumber").description("게시글 번호")
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
    @DisplayName("게시글 삭제 시 작성자가 아닌데 삭제를 시도할 경우 예외가 발생한다")
    void postDeleteNotPostOwner() throws Exception {
        String accessToken = createAccessToken();
        Claims payload = getPayload(accessToken);

        given(tokenService.tokenPayload(anyString())).willReturn(payload);
        willThrow(new PostDeleteAccessDeniedException()).given(postService).postDelete(anyLong(), anyString());

        mockMvc.perform(delete("/api/posts/{postNumber}", 1)
                        .header("Authorization", "Bearer access-token")
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("E403002"))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("게시글 삭제는 작성자만 할 수 있습니다."))
                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName("Authorization").description("Access Token")
                        ),
                        pathParameters(
                                parameterWithName("postNumber").description("게시글 번호")
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