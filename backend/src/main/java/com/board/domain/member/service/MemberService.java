package com.board.domain.member.service;

import com.board.domain.member.dto.MemberSignupRequest;
import com.board.domain.member.entity.Member;
import com.board.domain.member.exception.DuplicateNicknameException;
import com.board.domain.member.exception.DuplicateUsernameException;
import com.board.domain.member.exception.PasswordMismatchException;
import com.board.domain.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void memberNicknameExists(String nickname) {
        if (memberRepository.existsMemberByNickname(nickname)) {
            throw new DuplicateNicknameException();
        }
    }

    @Transactional
    public void memberUsernameExists(String username) {
        if (memberRepository.existsMemberByUsername(username)) {
            throw new DuplicateUsernameException();
        }
    }

    @Transactional
    public void memberSignup(MemberSignupRequest memberSignupRequest) {
        memberNicknameExists(memberSignupRequest.getNickname());
        memberUsernameExists(memberSignupRequest.getUsername());
        if (!memberSignupRequest.getPassword().equals(memberSignupRequest.getPasswordConfirm())) {
            throw new PasswordMismatchException();
        }
        String enocoded = passwordEncoder.encode(memberSignupRequest.getPassword());
        Member member = Member.builder()
                .nickname(memberSignupRequest.getNickname())
                .username(memberSignupRequest.getUsername())
                .password(enocoded)
                .build();
        memberRepository.save(member);
    }

}
