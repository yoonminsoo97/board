package com.board.domain.comment.controller;

import com.board.domain.comment.dto.CommentListResponse;
import com.board.domain.comment.dto.CommentModifyRequest;
import com.board.domain.comment.dto.CommentWriteRequest;
import com.board.domain.comment.service.CommentService;

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
public class CommentController {

    private final CommentService commentService;

    @Secured("ROLE_MEMBER")
    @PostMapping("/{postNumber}/comments/write")
    public ResponseEntity<Void> commentWrite(@PathVariable("postNumber") Long postNumber,
                                             @RequestBody @Valid CommentWriteRequest commentWriteRequest,
                                             @AuthenticationPrincipal String loginUsername) {
        commentService.commentWrite(postNumber, commentWriteRequest, loginUsername);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{postNumber}/comments")
    public ResponseEntity<CommentListResponse> commentList(@PathVariable("postNumber") Long postNumber,
                                                           @RequestParam(value = "page", defaultValue = "0") int page) {
        page = page <= 0 ? 0 : page - 1;
        CommentListResponse commentListResponse = commentService.commentList(postNumber, page);
        return ResponseEntity.ok().body(commentListResponse);
    }

    @Secured("ROLE_MEMBER")
    @PutMapping("/{postNumber}/comments/{commentNumber}")
    public ResponseEntity<Void> commentModify(@PathVariable("postNumber") Long postNumber,
                                              @PathVariable("commentNumber") Long commentNumber,
                                              @RequestBody @Valid CommentModifyRequest commentModifyRequest,
                                              @AuthenticationPrincipal String loginUsername) {
        commentService.commentModify(postNumber, commentNumber, commentModifyRequest, loginUsername);
        return ResponseEntity.ok().build();
    }

    @Secured("ROLE_MEMBER")
    @DeleteMapping("/{postNumber}/comments/{commentNumber}")
    public ResponseEntity<Void> commentDelete(@PathVariable("postNumber") Long postNumber,
                                              @PathVariable("commentNumber") Long commentNumber,
                                              @AuthenticationPrincipal String loginUsername) {
        commentService.commentDelete(postNumber, commentNumber, loginUsername);
        return ResponseEntity.ok().build();
    }

}
