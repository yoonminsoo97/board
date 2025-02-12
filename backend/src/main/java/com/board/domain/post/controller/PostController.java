package com.board.domain.post.controller;

import com.board.domain.post.dto.PostDetailResponse;
import com.board.domain.post.dto.PostListResponse;
import com.board.domain.post.dto.PostModifyRequest;
import com.board.domain.post.dto.PostWriteRequest;
import com.board.domain.post.service.PostService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
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

    @PostMapping("/write")
    public ResponseEntity<Void> postWrite(@AuthenticationPrincipal String username,
                                          @RequestBody @Valid PostWriteRequest postWriteRequest) {
        postService.postWrite(username, postWriteRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostDetailResponse> postDetail(@PathVariable("postId") Long postId) {
        PostDetailResponse postDetailResponse = postService.postDetail(postId);
        return ResponseEntity.ok().body(postDetailResponse);
    }

    @GetMapping
    public ResponseEntity<PostListResponse> postList(@RequestParam("page") int page) {
        PostListResponse postListResponse = postService.postList(page);
        return ResponseEntity.ok().body(postListResponse);
    }

    @PutMapping("/{postId}")
    public ResponseEntity<Void> postModify(@PathVariable("postId") Long postId,
                                           @RequestBody @Valid PostModifyRequest postModifyRequest) {
        postService.postModify(postId, postModifyRequest);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> postDelete(@PathVariable("postId") Long postId) {
        postService.postDelete(postId);
        return ResponseEntity.ok().build();
    }

}
