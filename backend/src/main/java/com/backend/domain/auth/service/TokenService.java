package com.backend.domain.auth.service;

import com.backend.domain.auth.dto.TokenResponse;
import com.backend.domain.auth.exception.InvalidTokenException;
import com.backend.domain.auth.exception.NotFoundTokenException;
import com.backend.domain.auth.repository.TokenRepository;
import com.backend.domain.auth.token.TokenManager;

import io.jsonwebtoken.Claims;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenManager tokenManager;
    private final TokenRepository tokenRepository;

    public TokenResponse issueToken(String username, String authority) {
        String accessToken = tokenManager.createAccessToken(username, authority);
        String refreshToken = tokenManager.createRefreshToken(username);
        tokenRepository.add(username, refreshToken, tokenManager.getRefreshTokenExpire());
        return new TokenResponse(accessToken, refreshToken);
    }

    public void deleteToken(String accessToken, String username) {
        if (accessToken == null) {
            throw new NotFoundTokenException();
        }
        String refreshToken = tokenRepository.findByUsername(username)
                .orElseThrow(NotFoundTokenException::new);
        tokenRepository.deleteByUsername(username);
        tokenRepository.addBlackList(accessToken, tokenManager.getAccessTokenExpire());
        tokenRepository.addBlackList(refreshToken, tokenManager.getRefreshTokenExpire());
    }

    public void validateToken(String token) {
        if (tokenRepository.isBlocked(token)) {
            throw new InvalidTokenException();
        }
        tokenManager.validateToken(token);
    }

    public Claims extractClaim(String token) {
        return tokenManager.extractClaims(token);
    }

}
