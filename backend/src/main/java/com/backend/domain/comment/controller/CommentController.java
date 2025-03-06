package com.backend.domain.comment.controller;

import com.backend.domain.comment.dto.CommentListResponse;
import com.backend.domain.comment.dto.CommentModifyRequest;
import com.backend.domain.comment.dto.CommentWriteRequest;
import com.backend.domain.comment.service.CommentService;

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
public class CommentController {

    private final CommentService commentService;

    @PreAuthorize("permitAll()")
    @GetMapping("/{postId}/comments")
    public ResponseEntity<CommentListResponse> commentList(@PathVariable("postId") Long postId,
                                                           @RequestParam("page") int page) {
        CommentListResponse commentListResponse = commentService.commentList(postId, page);
        return ResponseEntity.ok().body(commentListResponse);
    }

    @PreAuthorize("hasRole('MEMBER')")
    @PostMapping("/{postId}/comments/write")
    public ResponseEntity<Void> commentWrite(@PathVariable("postId") Long postId,
                                             @AuthenticationPrincipal String username,
                                             @RequestBody @Valid CommentWriteRequest commentWriteRequest) {
        commentService.commentWrite(postId, username, commentWriteRequest);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('MEMBER')")
    @PutMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<Void> commentModify(@PathVariable("postId") Long postId,
                                              @PathVariable("commentId") Long commentId,
                                              @RequestBody @Valid CommentModifyRequest commentModifyRequest) {
        commentService.commentModify(postId, commentId, commentModifyRequest);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('MEMBER')")
    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<Void> commentDelete(@PathVariable("postId") Long postId,
                                              @PathVariable("commentId") Long commentId) {
        commentService.commentDelete(postId, commentId);
        return ResponseEntity.ok().build();
    }

}
