package com.backend.domain.comment.controller;

import com.backend.domain.comment.dto.CommentWriteRequest;
import com.backend.domain.comment.service.CommentService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PreAuthorize("hasRole('MEMBER')")
    @PostMapping("/{postId}/comments/write")
    public ResponseEntity<Void> commentWrite(@PathVariable("postId") Long postId,
                                             @AuthenticationPrincipal String username,
                                             @RequestBody @Valid CommentWriteRequest commentWriteRequest) {
        commentService.commentWrite(postId, username, commentWriteRequest);
        return ResponseEntity.ok().build();
    }

}
