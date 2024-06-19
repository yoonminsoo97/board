package com.board.domain.comment.controller;

import com.board.domain.comment.dto.CommentListResponse;
import com.board.domain.comment.dto.CommentModifyRequest;
import com.board.domain.comment.dto.CommentWriteRequest;
import com.board.domain.comment.service.CommentService;
import com.board.global.common.dto.ApiResponse;
import com.board.global.security.annotation.LoginMember;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
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
    @PostMapping("/{postId}/comments")
    public ResponseEntity<ApiResponse<Void>> commentWrite(@PathVariable("postId") Long postId,
                                                          @RequestBody @Valid CommentWriteRequest commentWriteRequest,
                                                          @LoginMember Long memberId) {
        commentService.commentWrite(postId, commentWriteRequest, memberId);
        return ResponseEntity.ok().body(ApiResponse.success());
    }

    @Secured("ROLE_MEMBER")
    @PostMapping("/{postId}/comments/{commentId}/replies")
    public ResponseEntity<ApiResponse<Void>> replyWrite(@PathVariable("postId") Long postId,
                                                        @PathVariable("commentId") Long commentId,
                                                        @RequestBody @Valid CommentWriteRequest commentWriteRequest,
                                                        @LoginMember Long memberId) {
        commentService.replyWrite(postId, commentId, commentWriteRequest, memberId);
        return ResponseEntity.ok().body(ApiResponse.success());
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<ApiResponse<CommentListResponse>> commentList(@PathVariable("postId") Long postId,
                                                                        @RequestParam(value = "page") int page) {
        page = page <= 0 ? 0 : page - 1;
        CommentListResponse commentListResponse = commentService.commentList(postId, page);
        return ResponseEntity.ok().body(ApiResponse.success(commentListResponse));
    }

    @Secured("ROLE_MEMBER")
    @PutMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> commentModify(@PathVariable("postId") Long postId,
                                                           @PathVariable("commentId") Long commentId,
                                                           @RequestBody @Valid CommentModifyRequest commentModifyRequest,
                                                           @LoginMember Long memberId) {
        commentService.commentModify(postId, commentId, commentModifyRequest, memberId);
        return ResponseEntity.ok().body(ApiResponse.success());
    }

    @Secured("ROLE_MEMBER")
    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> commentDelete(@PathVariable("postId") Long postId,
                                                           @PathVariable("commentId") Long commentId,
                                                           @LoginMember Long memberId) {
        commentService.commentDelete(postId, commentId, memberId);
        return ResponseEntity.ok().body(ApiResponse.success());
    }

}
