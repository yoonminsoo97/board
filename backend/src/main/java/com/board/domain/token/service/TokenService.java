package com.board.domain.token.service;

import com.board.domain.token.dto.TokenResponse;
import com.board.domain.token.entity.Token;
import com.board.domain.token.exception.NotFoundTokenException;
import com.board.domain.token.repository.TokenRepository;
import com.board.domain.member.entity.Member;
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
    public TokenResponse saveToken(Member member) {
        String accessToken = jwtUtil.createAccessToken(member.getUsername(), member.getRole().authority());
        String refreshToken = jwtUtil.createRefreshToken();
        Token token = Token.builder()
                .refreshToken(refreshToken)
                .member(member)
                .build();
        tokenRepository.save(token);
        return new TokenResponse(accessToken, refreshToken);
    }

    @Transactional
    public void deleteToken(String username) {
        Token token = tokenRepository.findByMemberUsername(username)
                .orElseThrow(NotFoundTokenException::new);
        tokenRepository.delete(token);
    }

    public Claims getClaims(String token) {
        return jwtUtil.getClaims(token);
    }

}
