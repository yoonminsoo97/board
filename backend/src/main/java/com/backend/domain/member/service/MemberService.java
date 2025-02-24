package com.backend.domain.member.service;

import com.backend.domain.member.dto.MemberSignupRequest;
import com.backend.domain.member.entity.Member;
import com.backend.domain.member.exception.DuplicateNicknameException;
import com.backend.domain.member.exception.DuplicateUsernameException;
import com.backend.domain.member.repository.MemberRepository;

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
    public void memberSignup(MemberSignupRequest memberSignupRequest) {
        if (memberRepository.existsByNickname(memberSignupRequest.getNickname())) {
            throw new DuplicateNicknameException();
        }
        if (memberRepository.existsByUsername(memberSignupRequest.getUsername())) {
            throw new DuplicateUsernameException();
        }
        String encodedPassword = passwordEncoder.encode(memberSignupRequest.getPassword());
        Member member = Member.builder()
                .nickname(memberSignupRequest.getNickname())
                .username(memberSignupRequest.getUsername())
                .password(encodedPassword)
                .build();
        memberRepository.save(member);
    }

}
