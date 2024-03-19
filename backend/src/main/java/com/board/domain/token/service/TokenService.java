package com.board.domain.token.service;

import com.board.domain.member.entity.Member;
import com.board.domain.token.dto.TokenResponse;
import com.board.domain.token.entity.Token;
import com.board.domain.token.repository.TokenRepository;
import com.board.domain.token.util.JwtUtil;

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
        String accessToken = jwtUtil.createAccessToken(member.getUsername(), member.getNickname(), member.getRole().getAuthority());
        String refreshToken = jwtUtil.createRefreshToken(member.getUsername());
        Token token = Token.builder()
                .refreshToken(refreshToken)
                .member(member)
                .build();
        tokenRepository.save(token);
        return new TokenResponse(accessToken, refreshToken);
    }

    public Claims tokenPayload(String token) {
        return jwtUtil.getPayload(token);
    }

}
