package com.board.domain.post.controller;

import com.board.domain.post.dto.PostDetailResponse;
import com.board.domain.post.dto.PostListResponse;
import com.board.domain.post.dto.PostModifyRequest;
import com.board.domain.post.dto.PostWriteRequest;
import com.board.domain.post.service.PostService;
import com.board.global.common.dto.ApiResponse;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @Secured("ROLE_MEMBER")
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> postWrite(@RequestBody @Valid PostWriteRequest postWriteRequest,
                                                       @AuthenticationPrincipal Long memberId) {
        postService.postWrite(postWriteRequest, memberId);
        return ResponseEntity.ok().body(ApiResponse.success());
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> postDetail(@PathVariable("postId") Long postId) {
        PostDetailResponse postDetailResponse = postService.postDetail(postId);
        return ResponseEntity.ok().body(ApiResponse.success(postDetailResponse));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PostListResponse>> postList(@RequestParam("page") int page) {
        page = page <= 0 ? 0 : page - 1;
        PostListResponse postListResponse = postService.postList(page);
        return ResponseEntity.ok().body(ApiResponse.success(postListResponse));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PostListResponse>> postSearchList(@RequestParam("page") int page,
                                                                        @RequestParam("type") String type,
                                                                        @RequestParam("keyword") String keyword) {
        page = page <= 0 ? 0 : page - 1;
        PostListResponse postListResponse = postService.postSearchList(page, type, keyword);
        return ResponseEntity.ok().body(ApiResponse.success(postListResponse));
    }

    @Secured("ROLE_MEMBER")
    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> postModify(@PathVariable("postId") Long postId,
                                                        @RequestBody @Valid PostModifyRequest postModifyRequest,
                                                        @AuthenticationPrincipal Long memberId) {
        postService.postModify(postId, postModifyRequest, memberId);
        return ResponseEntity.ok().body(ApiResponse.success());
    }

    @Secured("ROLE_MEMBER")
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> postDelete(@PathVariable("postId") Long postId,
                                                        @AuthenticationPrincipal Long memberId) {
        postService.postDelete(postId, memberId);
        return ResponseEntity.ok().body(ApiResponse.success());
    }

}
