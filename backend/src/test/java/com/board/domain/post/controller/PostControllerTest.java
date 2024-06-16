package com.board.domain.post.controller;

import com.board.domain.post.dto.PostDetailResponse;
import com.board.domain.post.dto.PostListResponse;
import com.board.domain.post.dto.PostModifyRequest;
import com.board.domain.post.dto.PostWriteRequest;
import com.board.domain.post.exception.NotFoundPostException;
import com.board.domain.post.exception.PostDeleteAccessDeniedException;
import com.board.domain.post.exception.PostModifyAccessDeniedException;
import com.board.domain.post.service.PostService;
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
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PostController.class)
class PostControllerTest extends ControllerTest {

    @MockBean
    private PostService postService;

    @Nested
    @DisplayName("게시글 작성 요청")
    class PostWriteTest {

        @Test
        @DisplayName("게시글을 작성한다")
        void postWrite() throws Exception {
            PostWriteRequest postWriteRequest = PostWriteRequest.builder()
                    .title("title")
                    .content("content")
                    .build();

            Claims claims = Jwts.claims()
                    .subject(String.valueOf(1L))
                    .add("nickname", "yoonkun")
                    .add("authority", "ROLE_MEMBER")
                    .build();

            given(jwtManager.getPayload(anyString())).willReturn(claims);
            willDoNothing().given(postService).postWrite(any(PostWriteRequest.class), anyLong());

            mockMvc.perform(post("/api/posts")
                            .header("Authorization", "Bearer access-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(postWriteRequest))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andDo(restDocs.document(
                            requestHeaders(
                                    headerWithName("Authorization").description("액세스 토큰")
                            ),
                            requestFields(
                                    fieldWithPath("title").type(STRING).description("게시글 제목"),
                                    fieldWithPath("content").type(STRING).description("게시글 내용")
                            ),
                            responseFields(
                                    commonSuccessResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("제목이 비어있으면 예외가 발생한다")
        void postWriteInvalidTitleValue() throws Exception {
            PostWriteRequest postWriteRequest = PostWriteRequest.builder()
                    .title("")
                    .content("content")
                    .build();

            Claims claims = Jwts.claims()
                    .subject(String.valueOf(1L))
                    .add("nickname", "yoonkun")
                    .add("authority", "ROLE_MEMBER")
                    .build();

            given(jwtManager.getPayload(anyString())).willReturn(claims);

            mockMvc.perform(post("/api/posts")
                            .header("Authorization", "Bearer access-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(postWriteRequest))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("fail"))
                    .andExpect(jsonPath("$.error.code").value("E400001"))
                    .andExpect(jsonPath("$.error.message").value("입력값이 잘못되었습니다."))
                    .andExpect(jsonPath("$.error.fields[0].field").value("title"))
                    .andExpect(jsonPath("$.error.fields[0].input").value(""))
                    .andExpect(jsonPath("$.error.fields[0].message").value("제목을 입력해 주세요."))
                    .andDo(restDocs.document(
                            requestHeaders(
                                    headerWithName("Authorization").description("액세스 토큰")
                            ),
                            requestFields(
                                    fieldWithPath("title").type(STRING).description("게시글 제목"),
                                    fieldWithPath("content").type(STRING).description("게시글 내용")
                            ),
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("내용이 비어있으면 예외가 발생한다")
        void postWriteInvalidContentValue() throws Exception {
            PostWriteRequest postWriteRequest = PostWriteRequest.builder()
                    .title("title")
                    .content("")
                    .build();

            Claims claims = Jwts.claims()
                    .subject(String.valueOf(1L))
                    .add("nickname", "yoonkun")
                    .add("authority", "ROLE_MEMBER")
                    .build();

            given(jwtManager.getPayload(anyString())).willReturn(claims);

            mockMvc.perform(post("/api/posts")
                            .header("Authorization", "Bearer access-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(postWriteRequest))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("fail"))
                    .andExpect(jsonPath("$.error.code").value("E400001"))
                    .andExpect(jsonPath("$.error.message").value("입력값이 잘못되었습니다."))
                    .andExpect(jsonPath("$.error.fields[0].field").value("content"))
                    .andExpect(jsonPath("$.error.fields[0].input").value(""))
                    .andExpect(jsonPath("$.error.fields[0].message").value("내용을 입력해 주세요."))
                    .andDo(restDocs.document(
                            requestHeaders(
                                    headerWithName("Authorization").description("액세스 토큰")
                            ),
                            requestFields(
                                    fieldWithPath("title").type(STRING).description("게시글 제목"),
                                    fieldWithPath("content").type(STRING).description("게시글 내용")
                            ),
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("액세스 토큰이 유효하지 않으면 예외가 발생한다")
        void postWriteInvalidAccessToken() throws Exception {
            PostWriteRequest postWriteRequest = PostWriteRequest.builder()
                    .title("title")
                    .content("content")
                    .build();

            willThrow(new InvalidTokenException()).given(jwtManager).getPayload(anyString());

            mockMvc.perform(post("/api/posts")
                            .header("Authorization", "Bearer invalid-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(postWriteRequest))
                    )
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value("fail"))
                    .andExpect(jsonPath("$.error.code").value("E401002"))
                    .andExpect(jsonPath("$.error.message").value("토큰이 유효하지 않습니다."))
                    .andExpect(jsonPath("$.error.fields").isEmpty())
                    .andDo(restDocs.document(
                            requestHeaders(
                                    headerWithName("Authorization").description("액세스 토큰")
                            ),
                            requestFields(
                                    fieldWithPath("title").type(STRING).description("게시글 제목"),
                                    fieldWithPath("content").type(STRING).description("게시글 내용")
                            ),
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("액세스 토큰이 만료되면 예외가 발생한다")
        void postWriteExpiredAccessToken() throws Exception {
            PostWriteRequest postWriteRequest = PostWriteRequest.builder()
                    .title("title")
                    .content("content")
                    .build();

            willThrow(new ExpiredTokenException()).given(jwtManager).getPayload(anyString());

            mockMvc.perform(post("/api/posts")
                            .header("Authorization", "Bearer expired-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(postWriteRequest))
                    )
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value("fail"))
                    .andExpect(jsonPath("$.error.code").value("E401003"))
                    .andExpect(jsonPath("$.error.message").value("토큰이 만료되었습니다."))
                    .andExpect(jsonPath("$.error.fields").isEmpty())
                    .andDo(restDocs.document(
                            requestHeaders(
                                    headerWithName("Authorization").description("액세스 토큰")
                            ),
                            requestFields(
                                    fieldWithPath("title").type(STRING).description("게시글 제목"),
                                    fieldWithPath("content").type(STRING).description("게시글 내용")
                            ),
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

    }

    @Nested
    @DisplayName("게시글 상세조회 요청")
    class PostDetailTest {

        @Test
        @DisplayName("게시글을 상세조회 한다")
        void postDetail() throws Exception {
            PostDetailResponse postDetailResponse = PostDetailResponse.builder()
                    .postId(1L)
                    .title("title")
                    .writer("writer")
                    .content("content")
                    .createdAt(LocalDateTime.of(2024, 6, 17, 0, 0))
                    .build();

            given(postService.postDetail(anyLong())).willReturn(postDetailResponse);

            mockMvc.perform(get("/api/posts/{postId}", 1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data.postId").value(1))
                    .andExpect(jsonPath("$.data.title").value("title"))
                    .andExpect(jsonPath("$.data.writer").value("writer"))
                    .andExpect(jsonPath("$.data.content").value("content"))
                    .andExpect(jsonPath("$.data.createdAt").value("2024-06-17T00:00:00"))
                    .andDo(restDocs.document(
                            pathParameters(
                                    parameterWithName("postId").description("게시글 번호")
                            ),
                            responseFields(
                                    commonSuccessResponse())
                                    .and(
                                            fieldWithPath("data.postId").type(NUMBER).description("게시글 번호"),
                                            fieldWithPath("data.title").type(STRING).description("게시글 제목"),
                                            fieldWithPath("data.writer").type(STRING).description("게시글 작성자"),
                                            fieldWithPath("data.content").type(STRING).description("게시글 내용"),
                                            fieldWithPath("data.createdAt").type(STRING).description("게시글 작성일")
                                    )
                    ));
        }

        @Test
        @DisplayName("게시글이 존재하지 않으면 예외가 발생한다")
        void postDetailNotFound() throws Exception {
            willThrow(new NotFoundPostException()).given(postService).postDetail(anyLong());

            mockMvc.perform(get("/api/posts/{postId}", 1))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("fail"))
                    .andExpect(jsonPath("$.error.code").value("E404002"))
                    .andExpect(jsonPath("$.error.message").value("게시글을 찾을 수 없습니다."))
                    .andExpect(jsonPath("$.error.fields").isEmpty())
                    .andDo(restDocs.document(
                            pathParameters(
                                    parameterWithName("postId").description("게시글 번호")
                            ),
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

    }

    @Nested
    @DisplayName("게시글 목록조회 요청")
    class PostListTest {

        @Test
        @DisplayName("게시글 목록을 조회한다")
        void postList() throws Exception {
            PostListResponse postListResponse = PostListResponse.builder()
                    .posts(List.of(
                            PostListResponse.PostItem.builder()
                                    .postId(1L)
                                    .title("title")
                                    .writer("writer")
                                    .commentCount(0)
                                    .createdAt(LocalDateTime.of(2024, 6, 17, 0, 0))
                                    .build()
                    ))
                    .page(1)
                    .totalPages(1)
                    .totalElements(1)
                    .first(true)
                    .last(true)
                    .prev(false)
                    .next(false)
                    .build();

            given(postService.postList(anyInt())).willReturn(postListResponse);

            mockMvc.perform(get("/api/posts")
                            .param("page", "1")
                            .param("type", "title")
                            .param("keyword", "hello")
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data.posts[0].postId").value(1))
                    .andExpect(jsonPath("$.data.posts[0].title").value("title"))
                    .andExpect(jsonPath("$.data.posts[0].writer").value("writer"))
                    .andExpect(jsonPath("$.data.posts[0].commentCount").value(0))
                    .andExpect(jsonPath("$.data.posts[0].createdAt").value("2024-06-17T00:00:00"))
                    .andExpect(jsonPath("$.data.page").value(1))
                    .andExpect(jsonPath("$.data.totalPages").value(1))
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.first").value(true))
                    .andExpect(jsonPath("$.data.last").value(true))
                    .andExpect(jsonPath("$.data.prev").value(false))
                    .andExpect(jsonPath("$.data.next").value(false))
                    .andDo(restDocs.document(
                            queryParameters(
                                    parameterWithName("page").description("페이지 번호"),
                                    parameterWithName("type").description("검색 조건"),
                                    parameterWithName("keyword").description("검색 단어")
                            ),
                            responseFields(
                                    commonSuccessResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("제목으로 검색한 게시글 목록을 조회한다")
        void postSearchTitleList() throws Exception {
            PostListResponse postListResponse = PostListResponse.builder()
                    .posts(List.of(
                            PostListResponse.PostItem.builder()
                                    .postId(1L)
                                    .title("hello")
                                    .writer("writer")
                                    .commentCount(0)
                                    .createdAt(LocalDateTime.of(2024, 6, 17, 0, 0))
                                    .build()
                    ))
                    .page(1)
                    .totalPages(1)
                    .totalElements(1)
                    .first(true)
                    .last(true)
                    .prev(false)
                    .next(false)
                    .build();

            given(postService.postSearchList(anyInt(), anyString(), anyString())).willReturn(postListResponse);

            mockMvc.perform(get("/api/posts/search")
                            .param("page", "1")
                            .param("type", "title")
                            .param("keyword", "hello")
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data.posts[0].postId").value(1))
                    .andExpect(jsonPath("$.data.posts[0].title").value("hello"))
                    .andExpect(jsonPath("$.data.posts[0].writer").value("writer"))
                    .andExpect(jsonPath("$.data.posts[0].commentCount").value(0))
                    .andExpect(jsonPath("$.data.posts[0].createdAt").value("2024-06-17T00:00:00"))
                    .andExpect(jsonPath("$.data.page").value(1))
                    .andExpect(jsonPath("$.data.totalPages").value(1))
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.first").value(true))
                    .andExpect(jsonPath("$.data.last").value(true))
                    .andExpect(jsonPath("$.data.prev").value(false))
                    .andExpect(jsonPath("$.data.next").value(false))
                    .andDo(restDocs.document(
                            queryParameters(
                                    parameterWithName("page").description("페이지 번호"),
                                    parameterWithName("type").description("검색 조건"),
                                    parameterWithName("keyword").description("검색 단어")
                            ),
                            responseFields(
                                    commonSuccessResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("작성자로 검색한 게시글 목록을 조회한다")
        void postSearchWriterList() throws Exception {
            PostListResponse postListResponse = PostListResponse.builder()
                    .posts(List.of(
                            PostListResponse.PostItem.builder()
                                    .postId(1L)
                                    .title("title")
                                    .writer("yoonkun")
                                    .commentCount(0)
                                    .createdAt(LocalDateTime.of(2024, 6, 17, 0, 0))
                                    .build()
                    ))
                    .page(1)
                    .totalPages(1)
                    .totalElements(1)
                    .first(true)
                    .last(true)
                    .prev(false)
                    .next(false)
                    .build();

            given(postService.postSearchList(anyInt(), anyString(), anyString())).willReturn(postListResponse);

            mockMvc.perform(get("/api/posts/search")
                            .param("page", "1")
                            .param("type", "writer")
                            .param("keyword", "yoonkun")
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data.posts[0].postId").value(1))
                    .andExpect(jsonPath("$.data.posts[0].title").value("title"))
                    .andExpect(jsonPath("$.data.posts[0].writer").value("yoonkun"))
                    .andExpect(jsonPath("$.data.posts[0].commentCount").value(0))
                    .andExpect(jsonPath("$.data.posts[0].createdAt").value("2024-06-17T00:00:00"))
                    .andExpect(jsonPath("$.data.page").value(1))
                    .andExpect(jsonPath("$.data.totalPages").value(1))
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.first").value(true))
                    .andExpect(jsonPath("$.data.last").value(true))
                    .andExpect(jsonPath("$.data.prev").value(false))
                    .andExpect(jsonPath("$.data.next").value(false))
                    .andDo(restDocs.document(
                            queryParameters(
                                    parameterWithName("page").description("페이지 번호"),
                                    parameterWithName("type").description("검색 조건"),
                                    parameterWithName("keyword").description("검색 단어")
                            ),
                            responseFields(
                                    commonSuccessResponse()
                            )
                    ));
        }

    }

    @Nested
    @DisplayName("게시글 수정 요청")
    class PostModifyTest {

        @Test
        @DisplayName("게시글을 수정한다")
        void postModify() throws Exception {
            PostModifyRequest postModifyRequest = PostModifyRequest.builder()
                    .title("title")
                    .content("content")
                    .build();

            Claims claims = Jwts.claims()
                    .subject(String.valueOf(1L))
                    .add("nickname", "yoonkun")
                    .add("authority", "ROLE_MEMBER")
                    .build();

            given(jwtManager.getPayload(anyString())).willReturn(claims);
            willDoNothing().given(postService).postModify(anyLong(), any(PostModifyRequest.class), anyLong());

            mockMvc.perform(put("/api/posts/{postId}", 1)
                            .header("Authorization", "Bearer access-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(postModifyRequest))
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
                                    fieldWithPath("title").type(STRING).description("게시글 제목"),
                                    fieldWithPath("content").type(STRING).description("게시글 내용")
                            ),
                            responseFields(
                                    commonSuccessResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("제목이 비어있으면 예외가 발생한다")
        void postModifyInvalidTitleValue() throws Exception {
            PostModifyRequest postModifyRequest = PostModifyRequest.builder()
                    .title("")
                    .content("content")
                    .build();

            Claims claims = Jwts.claims()
                    .subject(String.valueOf(1L))
                    .add("nickname", "yoonkun")
                    .add("authority", "ROLE_MEMBER")
                    .build();

            given(jwtManager.getPayload(anyString())).willReturn(claims);

            mockMvc.perform(put("/api/posts/{postId}", 1)
                            .header("Authorization", "Bearer access-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(postModifyRequest))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("fail"))
                    .andExpect(jsonPath("$.error.code").value("E400001"))
                    .andExpect(jsonPath("$.error.message").value("입력값이 잘못되었습니다."))
                    .andExpect(jsonPath("$.error.fields[0].field").value("title"))
                    .andExpect(jsonPath("$.error.fields[0].input").value(""))
                    .andExpect(jsonPath("$.error.fields[0].message").value("제목을 입력해 주세요."))
                    .andDo(restDocs.document(
                            pathParameters(
                                    parameterWithName("postId").description("게시글 번호")
                            ),
                            requestHeaders(
                                    headerWithName("Authorization").description("액세스 토큰")
                            ),
                            requestFields(
                                    fieldWithPath("title").type(STRING).description("게시글 제목"),
                                    fieldWithPath("content").type(STRING).description("게시글 내용")
                            ),
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("내용이 비어있으면 예외가 발생한다")
        void postModifyInvalidContentValue() throws Exception {
            PostModifyRequest postModifyRequest = PostModifyRequest.builder()
                    .title("title")
                    .content("")
                    .build();

            Claims claims = Jwts.claims()
                    .subject(String.valueOf(1L))
                    .add("nickname", "yoonkun")
                    .add("authority", "ROLE_MEMBER")
                    .build();

            given(jwtManager.getPayload(anyString())).willReturn(claims);

            mockMvc.perform(put("/api/posts/{postId}", 1)
                            .header("Authorization", "Bearer access-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(postModifyRequest))
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
                                    fieldWithPath("title").type(STRING).description("게시글 제목"),
                                    fieldWithPath("content").type(STRING).description("게시글 내용")
                            ),
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("게시글이 존재하지 않으면 예외가 발생한다")
        void postModifyNotFoundPost() throws Exception {
            PostModifyRequest postModifyRequest = PostModifyRequest.builder()
                    .title("title")
                    .content("content")
                    .build();

            Claims claims = Jwts.claims()
                    .subject(String.valueOf(1L))
                    .add("nickname", "yoonkun")
                    .add("authority", "ROLE_MEMBER")
                    .build();

            given(jwtManager.getPayload(anyString())).willReturn(claims);
            willThrow(new NotFoundPostException()).given(postService).postModify(anyLong(), any(PostModifyRequest.class), anyLong());

            mockMvc.perform(put("/api/posts/{postId}", 1)
                            .header("Authorization", "Bearer access-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(postModifyRequest))
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
                                    fieldWithPath("title").type(STRING).description("게시글 제목"),
                                    fieldWithPath("content").type(STRING).description("게시글 내용")
                            ),
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("작성자가 아닌데 수정을 시도하면 예외가 발생한다")
        void postModifyNotPostOwner() throws Exception {
            PostModifyRequest postModifyRequest = PostModifyRequest.builder()
                    .title("title")
                    .content("content")
                    .build();

            Claims claims = Jwts.claims()
                    .subject(String.valueOf(1L))
                    .add("nickname", "yoonkun")
                    .add("authority", "ROLE_MEMBER")
                    .build();

            given(jwtManager.getPayload(anyString())).willReturn(claims);
            willThrow(new PostModifyAccessDeniedException()).given(postService).postModify(anyLong(), any(PostModifyRequest.class), any());

            mockMvc.perform(put("/api/posts/{postId}", 1)
                            .header("Authorization", "Bearer access-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(postModifyRequest))
                    )
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value("fail"))
                    .andExpect(jsonPath("$.error.code").value("E403001"))
                    .andExpect(jsonPath("$.error.message").value("게시글 수정은 작성자만 할 수 있습니다."))
                    .andExpect(jsonPath("$.error.fields").isEmpty())
                    .andDo(restDocs.document(
                            pathParameters(
                                    parameterWithName("postId").description("게시글 번호")
                            ),
                            requestHeaders(
                                    headerWithName("Authorization").description("액세스 토큰")
                            ),
                            requestFields(
                                    fieldWithPath("title").type(STRING).description("게시글 제목"),
                                    fieldWithPath("content").type(STRING).description("게시글 내용")
                            ),
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("액세스 토큰이 유효하지 않으면 예외가 발생한다")
        void postModifyInvalidAccessToken() throws Exception {
            PostModifyRequest postModifyRequest = PostModifyRequest.builder()
                    .title("title")
                    .content("content")
                    .build();

            willThrow(new InvalidTokenException()).given(jwtManager).getPayload(anyString());

            mockMvc.perform(put("/api/posts/{postId}", 1)
                            .header("Authorization", "Bearer invalid-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(postModifyRequest))
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
                                    fieldWithPath("title").type(STRING).description("게시글 제목"),
                                    fieldWithPath("content").type(STRING).description("게시글 내용")
                            ),
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("액세스 토큰이 만료되면 예외가 발생한다")
        void postModifyExpiredAccessToken() throws Exception {
            PostModifyRequest postModifyRequest = PostModifyRequest.builder()
                    .title("title")
                    .content("content")
                    .build();

            willThrow(new ExpiredTokenException()).given(jwtManager).getPayload(anyString());

            mockMvc.perform(put("/api/posts/{postId}", 1)
                            .header("Authorization", "Bearer expired-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(postModifyRequest))
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
                                    fieldWithPath("title").type(STRING).description("게시글 제목"),
                                    fieldWithPath("content").type(STRING).description("게시글 내용")
                            ),
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

    }

    @Nested
    @DisplayName("게시글 삭제 요청")
    class PostDeleteTest {

        @Test
        @DisplayName("게시글을 삭제한다")
        void postDelete() throws Exception {
            Claims claims = Jwts.claims()
                    .subject(String.valueOf(1L))
                    .add("nickname", "yoonkun")
                    .add("authority", "ROLE_MEMBER")
                    .build();

            given(jwtManager.getPayload(anyString())).willReturn(claims);
            willDoNothing().given(postService).postDelete(anyLong(), anyLong());

            mockMvc.perform(delete("/api/posts/{postId}", 1)
                            .header("Authorization", "Bearer access-token")
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
                            responseFields(
                                    commonSuccessResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("게시글이 존재하지 않으면 예외가 발생한다")
        void postDeleteNotFoundPost() throws Exception {
            Claims claims = Jwts.claims()
                    .subject(String.valueOf(1L))
                    .add("nickname", "yoonkun")
                    .add("authority", "ROLE_MEMBER")
                    .build();

            given(jwtManager.getPayload(anyString())).willReturn(claims);
            willThrow(new NotFoundPostException()).given(postService).postDelete(anyLong(), anyLong());

            mockMvc.perform(delete("/api/posts/{postId}", 1)
                            .header("Authorization", "Bearer access-token")
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
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("작성자가 아닌데 삭제를 시도하면 예외가 발생한다")
        void postDeletNotPostOwner() throws Exception {
            Claims claims = Jwts.claims()
                    .subject(String.valueOf(1L))
                    .add("nickname", "yoonkun")
                    .add("authority", "ROLE_MEMBER")
                    .build();

            given(jwtManager.getPayload(anyString())).willReturn(claims);
            willThrow(new PostDeleteAccessDeniedException()).given(postService).postDelete(anyLong(), anyLong());

            mockMvc.perform(delete("/api/posts/{postId}", 1)
                            .header("Authorization", "Bearer access-token")
                    )
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value("fail"))
                    .andExpect(jsonPath("$.error.code").value("E403002"))
                    .andExpect(jsonPath("$.error.message").value("게시글 삭제는 작성자만 할 수 있습니다."))
                    .andExpect(jsonPath("$.error.fields").isEmpty())
                    .andDo(restDocs.document(
                            pathParameters(
                                    parameterWithName("postId").description("게시글 번호")
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
        void postDeleteInvalidAccessToken() throws Exception {
            willThrow(new InvalidTokenException()).given(jwtManager).getPayload(anyString());

            mockMvc.perform(delete("/api/posts/{postId}", 1)
                            .header("Authorization", "Bearer invalid-token")
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
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

        @Test
        @DisplayName("액세스 토큰이 만료되면 예외가 발생한다")
        void postDeleteExpiredAccessToken() throws Exception {
            willThrow(new ExpiredTokenException()).given(jwtManager).getPayload(anyString());

            mockMvc.perform(delete("/api/posts/{postId}", 1)
                            .header("Authorization", "Bearer expired-token")
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
                            responseFields(
                                    commonErrorResponse()
                            )
                    ));
        }

    }

}