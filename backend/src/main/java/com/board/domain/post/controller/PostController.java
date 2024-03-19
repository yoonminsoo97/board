package com.board.domain.post.controller;

import com.board.domain.post.dto.PostWriteRequest;
import com.board.domain.post.service.PostService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @Secured("ROLE_MEMBER")
    @PostMapping("/write")
    public ResponseEntity<Void> postWrite(@RequestBody @Valid PostWriteRequest postWriteRequest,
                                          @AuthenticationPrincipal String username) {
        postService.postWrite(postWriteRequest, username);
        return ResponseEntity.ok().build();
    }

}
