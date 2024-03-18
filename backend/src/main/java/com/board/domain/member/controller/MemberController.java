package com.board.domain.member.controller;

import com.board.domain.member.dto.MemberSignupRequest;
import com.board.domain.member.service.MemberService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/nickname/{nickname}")
    public ResponseEntity<String> memberNicknameExists(@PathVariable("nickname") String nickname) {
        memberService.memberNicknameExists(nickname);
        return ResponseEntity.ok().body("ok");
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<String> memberUsernameExists(@PathVariable("username") String username) {
        memberService.memberUsernameExists(username);
        return ResponseEntity.ok().body("ok");
    }

    @PostMapping("/signup")
    public ResponseEntity<Void> memberSignup(@RequestBody @Valid MemberSignupRequest memberSignupRequest) {
        memberService.memberSignup(memberSignupRequest);
        return ResponseEntity.ok().build();
    }

}
