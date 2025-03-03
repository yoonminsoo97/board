package com.backend.domain.post.controller;

import com.backend.domain.post.dto.PostDetailResponse;
import com.backend.domain.post.dto.PostItem;
import com.backend.domain.post.dto.PostListResponse;
import com.backend.domain.post.dto.PostModifyRequest;
import com.backend.domain.post.dto.PostWriteRequest;
import com.backend.domain.post.exception.NotFoundPostException;
import com.backend.domain.post.service.PostService;
import com.backend.support.ControllerTest;
import com.backend.global.error.exception.ErrorType;

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
import java.util.List;
import java.util.stream.Stream;

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
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PostController.class)
class PostControllerTest extends ControllerTest {

    @MockitoBean
    private PostService postService;

    @DisplayName("게시글 작성에 성공하면 200을 응답한다.")
    @Test
    void postWrite() throws Exception {
        PostWriteRequest postWriteRequest = new PostWriteRequest("title", "content");
        Claims claims = Jwts.claims()
                .add("username", "yoon1234")
                .add("authority", "ROLE_MEMBER")
                .build();

        willDoNothing().given(tokenService).validateToken(anyString());
        given(tokenService.extractClaim(anyString())).willReturn(claims);
        willDoNothing().given(postService).postWrite(any(PostWriteRequest.class), anyString());

        mockMvc.perform(post("/api/posts/write")
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postWriteRequest))
                )
                .andExpect(status().isOk())
                .andDo(restdocs)
                .andDo(restdocs.document(
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        requestFields(
                                fieldWithPath("title").type(JsonFieldType.STRING).description("제목"),
                                fieldWithPath("content").type(JsonFieldType.STRING).description("내용")
                        )
                ));
    }

    @DisplayName("게시글 작성 시 입력값이 유효하지 않으면 400을 응답한다.")
    @ParameterizedTest
    @MethodSource("invalidInputPostWriteRequest")
    void postWriteInvalidInput(PostWriteRequest postWriteRequest) throws Exception {
        Claims claims = Jwts.claims()
                .add("username", "yoon1234")
                .add("authority", "ROLE_MEMBER")
                .build();

        willDoNothing().given(tokenService).validateToken(anyString());
        given(tokenService.extractClaim(anyString())).willReturn(claims);

        mockMvc.perform(post("/api/posts/write")
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postWriteRequest))
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

    private static Stream<Object> invalidInputPostWriteRequest() {
        return Stream.of(
                Arguments.of(Named.of("제목 공백", new PostWriteRequest("", "content"))),
                Arguments.of(Named.of("내용 공백", new PostWriteRequest("title", "")))
        );
    }

    @DisplayName("게시글 작성 시 access token이 만료되면 401을 응답한다.")
    @Test
    void postWriteExpiredAccessToken() throws Exception {
        PostWriteRequest postWriteRequest = new PostWriteRequest("title", "content");
        String errorCode = ErrorType.EXPIRED_TOKEN.getErrorCode();

        willThrow(new AuthenticationServiceException(errorCode)).given(tokenService).validateToken(anyString());

        mockMvc.perform(post("/api/posts/write")
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postWriteRequest))
                )
                .andExpectAll(
                        status().isUnauthorized(),
                        jsonPath("$.status").value(401),
                        jsonPath("$.errorCode").value("E401002"),
                        jsonPath("$.message").value("토큰이 만료 되었습니다.")
                );
    }

    @DisplayName("게시글 작성 시 access token 형식이 잘못되면 401을 응답한다.")
    @Test
    void postWriteInvalidAccessToken() throws Exception {
        PostWriteRequest postWriteRequest = new PostWriteRequest("title", "content");
        String errorCode = ErrorType.INVALID_TOKEN.getErrorCode();

        willThrow(new AuthenticationServiceException(errorCode)).given(tokenService).validateToken(anyString());

        mockMvc.perform(post("/api/posts/write")
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postWriteRequest))
                )
                .andExpectAll(
                        status().isUnauthorized(),
                        jsonPath("$.status").value(401),
                        jsonPath("$.errorCode").value("E401003"),
                        jsonPath("$.message").value("토큰 형식이 잘못 되었습니다.")
                );
    }

    @DisplayName("게시글 상세조회에 성공하면 200을 응답한다.")
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
                .andDo(restdocs)
                .andDo(restdocs.document(
                        pathParameters(
                                parameterWithName("postId").description("게시글 번호")
                        ),
                        responseFields(
                                fieldWithPath("postId").type(JsonFieldType.NUMBER).description("게시글 번호"),
                                fieldWithPath("title").type(JsonFieldType.STRING).description("제목"),
                                fieldWithPath("writer").type(JsonFieldType.STRING).description("작성자"),
                                fieldWithPath("content").type(JsonFieldType.STRING).description("내용"),
                                fieldWithPath("createdAt").type(JsonFieldType.STRING).description("작성일")
                        )
                ));
    }

    @DisplayName("게시글 상세조회 시 게시글이 존재하지 않으면 404를 응답한다.")
    @Test
    void postDetailNotFoundPost() throws Exception {
        willThrow(new NotFoundPostException()).given(postService).postDetail(anyLong());

        mockMvc.perform(get("/api/posts/{postId}", 1))
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.status").value(404),
                        jsonPath("$.errorCode").value("E404003"),
                        jsonPath("$.message").value("게시글이 존재하지 않습니다.")
                );
    }

    @DisplayName("게시글 목록 조회에 성공하면 200을 응답한다.")
    @Test
    void postList() throws Exception {
        List<PostItem> posts = List.of(
                new PostItem(1L, "title", "writer", LocalDateTime.now())
        );
        PostListResponse postListResponse = new PostListResponse(posts, 1, 1, 1, true, true, false, false);

        given(postService.postListResponse(anyInt())).willReturn(postListResponse);

        mockMvc.perform(get("/api/posts")
                        .param("page", "1")
                )
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.posts").isArray(),
                        jsonPath("$.posts[0].postId").value(1),
                        jsonPath("$.posts[0].title").value("title"),
                        jsonPath("$.posts[0].writer").value("writer"),
                        jsonPath("$.posts[0].createdAt").isNotEmpty(),
                        jsonPath("$.page").value(1),
                        jsonPath("$.totalPages").value(1),
                        jsonPath("$.totalPosts").value(1),
                        jsonPath("$.first").value(true),
                        jsonPath("$.last").value(true),
                        jsonPath("$.prev").value(false),
                        jsonPath("$.next").value(false)
                )
                .andDo(restdocs)
                .andDo(restdocs.document(
                        queryParameters(
                                parameterWithName("page").description("페이지 번호")
                        ),
                        responseFields(
                                fieldWithPath("posts").type(JsonFieldType.ARRAY).description("게시글 목록"),
                                fieldWithPath("posts[0].postId").type(JsonFieldType.NUMBER).description("글번호"),
                                fieldWithPath("posts[0].title").type(JsonFieldType.STRING).description("제목"),
                                fieldWithPath("posts[0].writer").type(JsonFieldType.STRING).description("작성자"),
                                fieldWithPath("posts[0].createdAt").type(JsonFieldType.STRING).description("작성일"),
                                fieldWithPath("page").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
                                fieldWithPath("totalPages").type(JsonFieldType.NUMBER).description("전체 페이지 개수"),
                                fieldWithPath("totalPosts").type(JsonFieldType.NUMBER).description("전체 게시글 개수"),
                                fieldWithPath("first").type(JsonFieldType.BOOLEAN).description("첫번째 페이지 여부"),
                                fieldWithPath("last").type(JsonFieldType.BOOLEAN).description("마지막 페이지 여부"),
                                fieldWithPath("prev").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부"),
                                fieldWithPath("next").type(JsonFieldType.BOOLEAN).description("이전 페이지 존재 여부")
                        )
                ));
    }

    @DisplayName("게시글 수정에 성공하면 200을 응답한다.")
    @Test
    void postModify() throws Exception {
        PostModifyRequest postModifyRequest = new PostModifyRequest("title", "content");
        Claims claims = Jwts.claims()
                .add("username", "yoon1234")
                .add("authority", "ROLE_MEMBER")
                .build();

        willDoNothing().given(tokenService).validateToken(anyString());
        given(tokenService.extractClaim(anyString())).willReturn(claims);
        willDoNothing().given(postService).postModify(anyLong(), any(PostModifyRequest.class));

        mockMvc.perform(put("/api/posts/{postId}", 1)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postModifyRequest))
                )
                .andExpectAll(
                        status().isOk()
                )
                .andDo(restdocs)
                .andDo(restdocs.document(
                        pathParameters(
                                parameterWithName("postId").description("게시글 번호")
                        ),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        requestFields(
                                fieldWithPath("title").type(JsonFieldType.STRING).description("제목"),
                                fieldWithPath("content").type(JsonFieldType.STRING).description("내용")
                        )
                ));
    }

    @DisplayName("게시글 수정 시 게시글이 존재하지 않으면 404를 응답한다.")
    @Test
    void postModifyNotFoundPost() throws Exception {
        PostModifyRequest postModifyRequest = new PostModifyRequest("title", "content");
        Claims claims = Jwts.claims()
                .add("username", "yoon1234")
                .add("authority", "ROLE_MEMBER")
                .build();

        willDoNothing().given(tokenService).validateToken(anyString());
        given(tokenService.extractClaim(anyString())).willReturn(claims);
        willThrow(new NotFoundPostException()).given(postService).postModify(anyLong(), any(PostModifyRequest.class));

        mockMvc.perform(put("/api/posts/{postId}", 1)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postModifyRequest))
                )
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.status").value(404),
                        jsonPath("$.errorCode").value("E404003"),
                        jsonPath("$.message").value("게시글이 존재하지 않습니다.")
                );
    }

    @DisplayName("게시글 수정 시 access token이 만료되면 401을 응답한다.")
    @Test
    void postModifyExpiredAccessToken() throws Exception {
        PostModifyRequest postModifyRequest = new PostModifyRequest("title", "content");
        ErrorType errorType = ErrorType.EXPIRED_TOKEN;

        willThrow(new AuthenticationServiceException(errorType.getErrorCode())).given(tokenService).validateToken(anyString());

        mockMvc.perform(put("/api/posts/{postId}", 1)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postModifyRequest))
                )
                .andExpectAll(
                        status().isUnauthorized(),
                        jsonPath("$.status").value(401),
                        jsonPath("$.errorCode").value("E401002"),
                        jsonPath("$.message").value("토큰이 만료 되었습니다.")
                );
    }

    @DisplayName("게시글 수정 시 access token 형식이 잘못되면 401을 응답한다.")
    @Test
    void postModifyInvalidAccessToken() throws Exception {
        PostModifyRequest postModifyRequest = new PostModifyRequest("title", "content");
        ErrorType errorType = ErrorType.INVALID_TOKEN;

        willThrow(new AuthenticationServiceException(errorType.getErrorCode())).given(tokenService).validateToken(anyString());

        mockMvc.perform(put("/api/posts/{postId}", 1)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postModifyRequest))
                )
                .andExpectAll(
                        status().isUnauthorized(),
                        jsonPath("$.status").value(401),
                        jsonPath("$.errorCode").value("E401003"),
                        jsonPath("$.message").value("토큰 형식이 잘못 되었습니다.")
                );
    }

    @DisplayName("게시글 삭제에 성공하면 200을 응답한다.")
    @Test
    void postDelete() throws Exception {
        Claims claims = Jwts.claims()
                .add("username", "yoon1234")
                .add("authority", "ROLE_MEMBER")
                .build();

        willDoNothing().given(tokenService).validateToken(anyString());
        given(tokenService.extractClaim(anyString())).willReturn(claims);
        willDoNothing().given(postService).postDelete(anyLong());

        mockMvc.perform(delete("/api/posts/{postId}", 1)
                        .header("Authorization", "Bearer access-token")
                )
                .andExpect(status().isOk())
                .andDo(restdocs)
                .andDo(restdocs.document(
                        pathParameters(
                                parameterWithName("postId").description("게시글 번호")
                        ),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        )
                ));
    }

    @DisplayName("게시글 삭제 시 게시글이 존재하지 않으면 404를 응답한다.")
    @Test
    void postDeleteNotFoundPost() throws Exception {
        Claims claims = Jwts.claims()
                .add("username", "yoon1234")
                .add("authority", "ROLE_MEMBER")
                .build();

        willDoNothing().given(tokenService).validateToken(anyString());
        given(tokenService.extractClaim(anyString())).willReturn(claims);
        willThrow(new NotFoundPostException()).given(postService).postDelete(anyLong());

        mockMvc.perform(delete("/api/posts/{postId}", 1)
                        .header("Authorization", "Bearer access-token")
                )
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.status").value(404),
                        jsonPath("$.errorCode").value("E404003"),
                        jsonPath("$.message").value("게시글이 존재하지 않습니다.")
                );
    }

    @DisplayName("게시글 삭제 시 access token이 만료되면 401을 응답한다.")
    @Test
    void postDeleteExpiredAccessToken() throws Exception {
        ErrorType errorType = ErrorType.EXPIRED_TOKEN;

        willThrow(new AuthenticationServiceException(errorType.getErrorCode())).given(tokenService).validateToken(anyString());

        mockMvc.perform(delete("/api/posts/{postId}", 1)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpectAll(
                        status().isUnauthorized(),
                        jsonPath("$.status").value(401),
                        jsonPath("$.errorCode").value("E401002"),
                        jsonPath("$.message").value("토큰이 만료 되었습니다.")
                );
    }

    @DisplayName("게시글 삭제 시 access token 형식이 잘못되면 401을 응답한다.")
    @Test
    void postDeleteInvalidAccessToken() throws Exception {
        ErrorType errorType = ErrorType.INVALID_TOKEN;

        willThrow(new AuthenticationServiceException(errorType.getErrorCode())).given(tokenService).validateToken(anyString());

        mockMvc.perform(delete("/api/posts/{postId}", 1)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpectAll(
                        status().isUnauthorized(),
                        jsonPath("$.status").value(401),
                        jsonPath("$.errorCode").value("E401003"),
                        jsonPath("$.message").value("토큰 형식이 잘못 되었습니다.")
                );
    }

}