package com.backend.domain.auth.service;

import com.backend.domain.auth.dto.TokenResponse;
import com.backend.domain.auth.entity.Token;
import com.backend.domain.auth.exception.NotFoundTokenException;
import com.backend.domain.auth.repository.TokenRepository;
import com.backend.domain.auth.util.JwtUtil;
import com.backend.domain.member.entity.Member;

import io.jsonwebtoken.Claims;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenRepository tokenRepository;
    private final JwtUtil jwtUtil;

    @Transactional
    public TokenResponse tokenSave(Member member) {
        String accesStoken = jwtUtil.createAccesStoken(member);
        String refreshToken = jwtUtil.createRefreshToken();
        Token token = Token.builder()
                .accessToken(accesStoken)
                .refreshToken(refreshToken)
                .member(member)
                .build();
        tokenRepository.save(token);
        return new TokenResponse(accesStoken, refreshToken);
    }

    @Transactional
    public void tokenDelete(String username) {
        Token token = tokenRepository.findByMemberUsername(username)
                .orElseThrow(NotFoundTokenException::new);
        tokenRepository.delete(token);
    }

    public void validateToken(String token) {
        jwtUtil.validateToken(token);
    }

    public Claims extractClaim(String token) {
        return jwtUtil.extractClaims(token);
    }

}
