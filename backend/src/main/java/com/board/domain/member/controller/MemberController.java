package com.board.domain.member.controller;

import com.board.domain.member.dto.MemberCommentListResponse;
import com.board.domain.member.dto.MemberNicknameRequest;
import com.board.domain.member.dto.MemberPasswordRequest;
import com.board.domain.member.dto.MemberProfileResponse;
import com.board.domain.member.dto.MemberSignupRequest;
import com.board.domain.member.service.MemberService;
import com.board.domain.post.dto.PostListResponse;
import com.board.global.common.dto.ApiResponse;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

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
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberProfileResponse>> memberProfile(@AuthenticationPrincipal Long memberId) {
        MemberProfileResponse memberProfileResponse = memberService.memberProfile(memberId);
        return ResponseEntity.ok().body(ApiResponse.success(memberProfileResponse));
    }

    @Secured("ROLE_MEMBER")
    @GetMapping("/me/posts")
    public ResponseEntity<ApiResponse<PostListResponse>> memberPostList(@RequestParam("page") int page,
                                                                        @AuthenticationPrincipal Long memberId) {
        page = page <= 0 ? 0 : page - 1;
        PostListResponse postListResponse = memberService.memberPostList(page, memberId);
        return ResponseEntity.ok().body(ApiResponse.success(postListResponse));
    }

    @Secured("ROLE_MEMBER")
    @GetMapping("/me/comments")
    public ResponseEntity<ApiResponse<MemberCommentListResponse>> memberCommentList(@RequestParam("page") int page,
                                                                                    @AuthenticationPrincipal Long memberId) {
        page = page <= 0 ? 0 : page - 1;
        MemberCommentListResponse memberCommentListResponse = memberService.memberCommentList(page, memberId);
        return ResponseEntity.ok().body(ApiResponse.success(memberCommentListResponse));
    }

    @Secured("ROLE_MEMBER")
    @PutMapping("/me/nickname")
    public ResponseEntity<ApiResponse<Void>> memberNicknameChange(@RequestBody @Valid MemberNicknameRequest memberNicknameRequest,
                                                                  @AuthenticationPrincipal Long memberId) {
        memberService.memberNicknameChange(memberNicknameRequest, memberId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Secured("ROLE_MEMBER")
    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> memberPasswordChange(@RequestBody @Valid MemberPasswordRequest memberPasswordRequest,
                                                                  @AuthenticationPrincipal Long memberId) {
        memberService.memberPasswordChange(memberPasswordRequest, memberId);
        return ResponseEntity.ok().body(ApiResponse.success());
    }

}
