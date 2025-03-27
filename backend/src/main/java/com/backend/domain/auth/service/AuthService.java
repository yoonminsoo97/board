package com.backend.domain.auth.service;

import com.backend.domain.auth.dto.LoginRequest;
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
    public TokenResponse memberLogin(LoginRequest loginRequest) {
        Member member = memberRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(BadCredentialsException::new);
        if (!passwordEncoder.matches(loginRequest.getPassword(), member.getPassword())) {
            throw new BadCredentialsException();
        }
        return tokenService.issueToken(member.getUsername(), member.getAuthority());
    }

    public void memberLogout(String accessToken, String username) {
        tokenService.deleteToken(accessToken, username);
    }

}
