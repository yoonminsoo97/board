package com.board.domain.reply.controller;

import com.board.domain.reply.dto.ReplyWriteRequest;
import com.board.domain.reply.service.ReplyService;
import com.board.global.common.dto.ApiResponse;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class ReplyController {

    private final ReplyService replyService;

    @Secured("ROLE_MEMBER")
    @PostMapping("/{postId}/comments/{commentId}/reply")
    public ResponseEntity<ApiResponse<Void>> writeReply(@AuthenticationPrincipal String username,
                                                        @PathVariable("postId") Long postId,
                                                        @PathVariable("commentId") Long commentId,
                                                        @RequestBody @Valid ReplyWriteRequest replyWriteRequest) {
        replyService.replyWrite(username, postId, commentId, replyWriteRequest);
        return ResponseEntity.ok().body(ApiResponse.success());
    }

}
