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

    /**
     * 사용자가 입력한 닉네임, 아이디, 비밀번호를 전달받아 유효성 검사를 거친 후 회원가입을 진행한다.
     *
     * @param memberSignupRequest 회원가입 요청 데이터(닉네임, 아이디, 비밀번호)
     * @return 200 OK – 회원가입 성공 시 "ok" 문자열 반환
     */
    @PostMapping("/signup")
    public ResponseEntity<Void> memberSignup(@RequestBody @Valid MemberSignupRequest memberSignupRequest) {
        memberService.memberSignup(memberSignupRequest);
        return ResponseEntity.ok().build();
    }

}
