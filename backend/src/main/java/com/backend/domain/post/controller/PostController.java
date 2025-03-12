package com.backend.domain.post.controller;

import com.backend.domain.post.dto.PostDetailResponse;
import com.backend.domain.post.dto.PostListResponse;
import com.backend.domain.post.dto.PostModifyRequest;
import com.backend.domain.post.dto.PostWriteRequest;
import com.backend.domain.post.service.PostService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PreAuthorize("hasRole('MEMBER')")
    @PostMapping("/write")
    public ResponseEntity<Void> postWrite(@RequestBody @Valid PostWriteRequest postWriteRequest,
                                          @AuthenticationPrincipal String username) {
        postService.postWrite(postWriteRequest, username);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/{postId}")
    public ResponseEntity<PostDetailResponse> postDetail(@PathVariable("postId") Long postId) {
        PostDetailResponse postDetailResponse = postService.postDetail(postId);
        return ResponseEntity.ok().body(postDetailResponse);
    }

    @PreAuthorize("permitAll()")
    @GetMapping
    public ResponseEntity<PostListResponse> postList(@RequestParam("page") int page) {
        PostListResponse postListResponse = postService.postList(page);
        return ResponseEntity.ok().body(postListResponse);
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/search")
    public ResponseEntity<PostListResponse> postListSearch(@RequestParam("type") String type,
                                                           @RequestParam("keyword") String keyword,
                                                           @RequestParam("page") int page) {
        PostListResponse postListResponse = postService.postListSearch(type, keyword, page);
        return ResponseEntity.ok().body(postListResponse);
    }

    @PreAuthorize("hasRole('MEMBER')")
    @PutMapping("/{postId}")
    public ResponseEntity<Void> postModify(@PathVariable("postId") Long postId,
                                           @RequestBody @Valid PostModifyRequest postModifyRequest) {
        postService.postModify(postId, postModifyRequest);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('MEMBER')")
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> postDelete(@PathVariable("postId") Long postId) {
        postService.postDelete(postId);
        return ResponseEntity.ok().build();
    }

}
