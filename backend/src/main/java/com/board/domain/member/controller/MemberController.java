package com.board.domain.member.controller;

import com.board.domain.member.dto.MemberSignupRequest;
import com.board.domain.member.service.MemberService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/signup")
    public ResponseEntity<Void> memberSignup(@RequestBody @Valid MemberSignupRequest memberSignupRequest) {
        memberService.memberSignup(memberSignupRequest);
        return ResponseEntity.ok().build();
    }

}
