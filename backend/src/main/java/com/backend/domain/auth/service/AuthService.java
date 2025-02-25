package com.backend.domain.auth.service;

import com.backend.domain.auth.dto.MemberLoginRequest;
import com.backend.domain.auth.dto.MemberLoginResponse;
import com.backend.domain.auth.dto.TokenResponse;
import com.backend.domain.auth.exception.BadCredentialsException;
import com.backend.domain.member.entity.Member;
import com.backend.domain.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @Transactional
    public MemberLoginResponse memberLogin(MemberLoginRequest memberLoginRequest) {
        Member member = memberRepository.findByUsername(memberLoginRequest.getUsername())
                .orElseThrow(BadCredentialsException::new);
        if (!passwordEncoder.matches(memberLoginRequest.getPassword(), member.getPassword())) {
            throw new BadCredentialsException();
        }
        TokenResponse tokenResponse = tokenService.tokenSave(member);
        return new MemberLoginResponse(tokenResponse.getAccessToken(), tokenResponse.getRefreshToken());
    }

}
