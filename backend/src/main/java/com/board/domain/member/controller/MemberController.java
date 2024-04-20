package com.board.domain.member.controller;

import com.board.domain.comment.dto.CommentListResponse;
import com.board.domain.comment.service.CommentService;
import com.board.domain.member.dto.MemberProfileResponse;
import com.board.domain.member.dto.MemberSignupRequest;
import com.board.domain.member.service.MemberService;
import com.board.domain.post.dto.PostListResponse;
import com.board.domain.post.service.PostService;
import com.board.global.common.dto.ApiResponse;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final PostService postService;
    private final CommentService commentService;

    @GetMapping("/nickname/{nickname}")
    public ResponseEntity<ApiResponse<Void>> memberNicknameExists(@PathVariable("nickname") String nickname) {
        memberService.memberNicknameExists(nickname);
        return ResponseEntity.ok().body(ApiResponse.success());
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<ApiResponse<Void>> memberUsernameExists(@PathVariable("username") String username) {
        memberService.memberUsernameExists(username);
        return ResponseEntity.ok().body(ApiResponse.success());
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> memberSignup(@RequestBody @Valid MemberSignupRequest memberSignupRequest) {
        memberService.memberSignup(memberSignupRequest);
        return ResponseEntity.ok().body(ApiResponse.success());
    }

    @Secured("ROLE_MEMBER")
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<MemberProfileResponse>> memberProfile(@AuthenticationPrincipal String username) {
        MemberProfileResponse memberProfileResponse = memberService.memberProfile(username);
        return ResponseEntity.ok().body(ApiResponse.success(memberProfileResponse));
    }

    @Secured("ROLE_MEMBER")
    @GetMapping("/profile/posts")
    public ResponseEntity<ApiResponse<PostListResponse>> memberProfilePostList(@RequestParam("page") int page,
                                                                               @AuthenticationPrincipal String username) {
        page = page <= 0 ? 0 : page - 1;
        PostListResponse postListResponse = postService.postListFromMember(page, username);
        return ResponseEntity.ok().body(ApiResponse.success(postListResponse));
    }

    @Secured("ROLE_MEMBER")
    @GetMapping("/profile/comments")
    public ResponseEntity<ApiResponse<CommentListResponse>> memberProfileCommentList(@RequestParam("page") int page,
                                                                                     @AuthenticationPrincipal String username) {
        page = page <= 0 ? 0 : page - 1;
        CommentListResponse commentListResponse = commentService.commentListFromMember(page, username);
        return ResponseEntity.ok().body(ApiResponse.success(commentListResponse));
    }

}
