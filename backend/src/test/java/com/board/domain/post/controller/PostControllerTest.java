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
import com.board.domain.token.service.TokenService;
import com.board.global.security.config.SecurityConfig;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PostController.class)
@Import(SecurityConfig.class)
class PostControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private PostService postService;

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.access-token.expire}")
    private long accessTokenExpire;

    @Test
    @DisplayName("게시글을 작성한다")
    void postWrite() throws Exception {
        PostWriteRequest postWriteRequest = new PostWriteRequest("제목", "내용");
        String accessToken = createAccessToken();
        Claims payload = getPayload(accessToken);

        given(tokenService.tokenPayload(anyString())).willReturn(payload);
        willDoNothing().given(postService).postWrite(any(PostWriteRequest.class), anyString());

        mockMvc.perform(post("/api/posts/write")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postWriteRequest))
                )
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("게시글 작성 시 입력값이 잘못되면 예외가 발생한다")
    void postWrite_invalidInputValue() throws Exception {
        PostWriteRequest invalidPostWriteRequest = new PostWriteRequest("", "내용");
        String accessToken = createAccessToken();
        Claims payload = getPayload(accessToken);

        given(tokenService.tokenPayload(anyString())).willReturn(payload);
        willDoNothing().given(postService).postWrite(any(PostWriteRequest.class), anyString());

        mockMvc.perform(post("/api/posts/write")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPostWriteRequest))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("E400001"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("입력값이 잘못되었습니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("게시글 작성 시 회원을 찾을 수 없으면 예외가 발생한다")
    void postWrite_notFoundMember() throws Exception {
        PostWriteRequest postWriteRequest = new PostWriteRequest("제목", "내용");
        String accessToken = createAccessToken();
        Claims payload = getPayload(accessToken);

        given(tokenService.tokenPayload(anyString())).willReturn(payload);
        willThrow(new NotFoundMemberException()).given(postService).postWrite(any(PostWriteRequest.class), anyString());

        mockMvc.perform(post("/api/posts/write")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postWriteRequest))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("E404001"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("회원을 찾을 수 없습니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("게시글을 상세조회 한다")
    void postDetail() throws Exception {
        PostDetailResponse postDetailResponse = new PostDetailResponse(1L, "제목", "yoonkun", "내용", LocalDateTime.now());

        given(postService.postDetail(anyLong())).willReturn(postDetailResponse);

        mockMvc.perform(get("/api/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postNumber").value(1))
                .andExpect(jsonPath("$.title").value("제목"))
                .andExpect(jsonPath("$.writer").value("yoonkun"))
                .andExpect(jsonPath("$.content").value("내용"))
                .andDo(print());
    }

    @Test
    @DisplayName("게시글 상세조회 시 게시글을 찾을 수 없으면 예외가 발생한다")
    void postDetail_notFoundPost() throws Exception {
        willThrow(new NotFoundPostException()).given(postService).postDetail(anyLong());

        mockMvc.perform(get("/api/posts/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("E404002"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("게시글을 찾을 수 없습니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("게시글 목록을 조회한다")
    void postList() throws Exception {
        List<PostListItem> posts = List.of(
                new PostListItem(5L, "제목5", "작성자5", LocalDateTime.now()),
                new PostListItem(4L, "제목4", "작성자4", LocalDateTime.now()),
                new PostListItem(3L, "제목3", "작성자3", LocalDateTime.now()),
                new PostListItem(2L, "제목2", "작성자2", LocalDateTime.now()),
                new PostListItem(1L, "제목1", "작성자1", LocalDateTime.now())
        );
        PostListResponse postListResponse = new PostListResponse(posts, 0, 1, 5, false, false, true, true);

        given(postService.postList(anyInt())).willReturn(postListResponse);

        mockMvc.perform(get("/api/posts/page/1"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("게시글을 수정한다")
    void postModify() throws Exception {
        PostModifyRequest postModifyRequest = new PostModifyRequest("제목", "내용");
        String accessToken = createAccessToken();
        Claims payload = getPayload(accessToken);

        given(tokenService.tokenPayload(anyString())).willReturn(payload);
        willDoNothing().given(postService).postModify(anyLong(), any(PostModifyRequest.class), anyString());

        mockMvc.perform(put("/api/posts/1")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postModifyRequest))
                )
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("게시글 수정 시 입력값이 잘못되면 예외가 발생한다")
    void postModify_invalidInputValue() throws Exception {
        PostModifyRequest invalidPostModifyRequest = new PostModifyRequest("", "내용");
        String accessToken = createAccessToken();
        Claims payload = getPayload(accessToken);

        given(tokenService.tokenPayload(anyString())).willReturn(payload);
        willDoNothing().given(postService).postModify(anyLong(), any(PostModifyRequest.class), anyString());

        mockMvc.perform(put("/api/posts/1")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPostModifyRequest))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("E400001"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("입력값이 잘못되었습니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("게시글 수정 시 게시글을 찾을 수 없으면 예외가 발생한다")
    void postModify_notFoundPost() throws Exception {
        PostModifyRequest postModifyRequest = new PostModifyRequest("제목", "내용");
        String accessToken = createAccessToken();
        Claims payload = getPayload(accessToken);

        given(tokenService.tokenPayload(anyString())).willReturn(payload);
        willThrow(new NotFoundPostException()).given(postService).postModify(anyLong(), any(PostModifyRequest.class), anyString());

        mockMvc.perform(put("/api/posts/1")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postModifyRequest))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("E404002"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("게시글을 찾을 수 없습니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("게시글 수정 시 작성자가 아닌데 수정을 시도할 경우 예외가 발생한다")
    void postModify_notPostOwner() throws Exception {
        PostModifyRequest postModifyRequest = new PostModifyRequest("제목", "내용");
        String accessToken = createAccessToken();
        Claims payload = getPayload(accessToken);

        given(tokenService.tokenPayload(anyString())).willReturn(payload);
        willThrow(new PostModifyAccessDeniedException()).given(postService).postModify(anyLong(), any(PostModifyRequest.class), anyString());

        mockMvc.perform(put("/api/posts/1")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postModifyRequest))
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("E403001"))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("게시글 수정은 작성자만 할 수 있습니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("게시글을 삭제한다")
    void postDelete() throws Exception {
        String accessToken = createAccessToken();
        Claims payload = getPayload(accessToken);

        given(tokenService.tokenPayload(anyString())).willReturn(payload);
        willDoNothing().given(postService).postDelete(anyLong(), anyString());

        mockMvc.perform(delete("/api/posts/1")
                        .header("Authorization", "Bearer " + accessToken)
                )
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("게시글 삭제 시 게시글을 찾을 수 없으면 예외가 발생한다")
    void postDelete_notFoundPost() throws Exception {
        String accessToken = createAccessToken();
        Claims payload = getPayload(accessToken);

        given(tokenService.tokenPayload(anyString())).willReturn(payload);
        willThrow(new NotFoundPostException()).given(postService).postDelete(anyLong(), anyString());

        mockMvc.perform(delete("/api/posts/1")
                        .header("Authorization", "Bearer " + accessToken)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("E404002"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("게시글을 찾을 수 없습니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("게시글 삭제 시 작성자가 아닌데 삭제를 시도할 경우 예외가 발생한다")
    void postDelete_notPostOwner() throws Exception {
        String accessToken = createAccessToken();
        Claims payload = getPayload(accessToken);

        given(tokenService.tokenPayload(anyString())).willReturn(payload);
        willThrow(new PostDeleteAccessDeniedException()).given(postService).postDelete(anyLong(), anyString());

        mockMvc.perform(delete("/api/posts/1")
                        .header("Authorization", "Bearer " + accessToken)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("E403002"))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("게시글 삭제는 작성자만 할 수 있습니다."))
                .andDo(print());
    }

    private String createAccessToken() {
        Date iat = new Date();
        Date exp = new Date(iat.getTime() + accessTokenExpire);
        return Jwts.builder()
                .subject("yoon1234")
                .claim("nickname", "yoonkun")
                .claim("authority", "ROLE_MEMBER")
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
                .issuedAt(iat)
                .expiration(exp)
                .compact();
    }

    private Claims getPayload(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}