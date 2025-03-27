package com.backend.domain.auth.service;

import com.backend.domain.auth.dto.TokenResponse;
import com.backend.domain.auth.exception.ExpiredTokenException;
import com.backend.domain.auth.exception.InvalidTokenException;
import com.backend.domain.auth.exception.NotFoundTokenException;
import com.backend.domain.auth.repository.TokenRepository;
import com.backend.domain.auth.token.TokenManager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private TokenManager tokenManager;

    @Mock
    private TokenRepository tokenRepository;

    @InjectMocks
    private TokenService tokenService;

    @DisplayName("refresh token을 저장한 후 access token과 refresh token을 발급한다.")
    @Test
    void tokenIssue() {
        given(tokenManager.createAccessToken(anyString(), anyString())).willReturn("access-token");
        given(tokenManager.createRefreshToken(anyString())).willReturn("refresh-token");
        willDoNothing().given(tokenRepository).add(anyString(), anyString(), anyLong());

        TokenResponse tokenResponse = tokenService.issueToken("yoon1234", "ROLE_MEMBER");

        assertThat(tokenResponse.getAccessToken()).isEqualTo("access-token");
        assertThat(tokenResponse.getRefreshToken()).isEqualTo("refresh-token");
        then(tokenManager).should().createAccessToken(anyString(), anyString());
        then(tokenManager).should().createRefreshToken(anyString());
        then(tokenRepository).should().add(anyString(), anyString(), anyLong());
    }

    @DisplayName("refresh token을 삭제하고 access token과 refresh token을 blacklist로 등록한다.")
    @Test
    void tokenDelete() {
        given(tokenRepository.findByUsername(anyString())).willReturn(Optional.of("refresh-token"));
        willDoNothing().given(tokenRepository).deleteByUsername(anyString());
        given(tokenManager.getAccessTokenExpire()).willReturn(100000L);
        given(tokenManager.getRefreshTokenExpire()).willReturn(10000000L);
        willDoNothing().given(tokenRepository).addBlackList(anyString(), anyLong());

        tokenService.deleteToken("access-token", "yoon1234");

        then(tokenRepository).should().findByUsername(anyString());
        then(tokenRepository).should().deleteByUsername(anyString());
        then(tokenRepository).should(times(2)).addBlackList(anyString(), anyLong());
    }

    @DisplayName("refresh token 삭제 시 access token이 null이면 예외가 발생한다.")
    @Test
    void tokenDeleteAccessTokenNull() {
        assertThatThrownBy(() -> tokenService.deleteToken(null, "yoon1234"))
                .isInstanceOf(NotFoundTokenException.class);

        then(tokenRepository).should(never()).findByUsername(anyString());
        then(tokenRepository).should(never()).deleteByUsername(anyString());
        then(tokenRepository).should(times(0)).addBlackList(anyString(), anyLong());
    }

    @DisplayName("refresh token 삭제 시 refresh token이 null이면 예외가 발생한다.")
    @Test
    void tokenDeleteRefreshTokenNull() {
        willThrow(new NotFoundTokenException()).given(tokenRepository).findByUsername(anyString());

        assertThatThrownBy(() -> tokenService.deleteToken("access-token", "yoon1234"))
                .isInstanceOf(NotFoundTokenException.class);

        then(tokenRepository).should().findByUsername(anyString());
        then(tokenRepository).should(never()).deleteByUsername(anyString());
        then(tokenRepository).should(times(0)).addBlackList(anyString(), anyLong());
    }

    @DisplayName("토큰의 blacklist 등록 여부와 유효성 검증을 한다.")
    @Test
    void validateToken() {
        given(tokenRepository.isBlocked(anyString())).willReturn(false);
        willDoNothing().given(tokenManager).validateToken(anyString());

        tokenService.validateToken("access-token");

        then(tokenRepository).should().isBlocked(anyString());
        then(tokenManager).should().validateToken(anyString());
    }

    @DisplayName("토큰 검증 시 토큰이 blacklist에 등록되어 있으면 예외가 발생한다.")
    @Test
    void validateTokenBlackListToken() {
        given(tokenRepository.isBlocked(anyString())).willReturn(true);

        assertThatThrownBy(() ->tokenService.validateToken("access-token"))
                .isInstanceOf(InvalidTokenException.class);

        then(tokenRepository).should().isBlocked(anyString());
        then(tokenManager).should(never()).validateToken(anyString());
    }

    @DisplayName("토큰 검증 시 토큰이 만료되면 예외가 발생한다.")
    @Test
    void validateTokenExpiredToken() {
        given(tokenRepository.isBlocked(anyString())).willReturn(false);
        willThrow(new ExpiredTokenException()).given(tokenManager).validateToken(anyString());

        assertThatThrownBy(() -> tokenService.validateToken("access-token"))
                .isInstanceOf(ExpiredTokenException.class);

        then(tokenRepository).should().isBlocked(anyString());
        then(tokenManager).should().validateToken(anyString());
    }

    @DisplayName("토큰 검증 시 토큰이 유효하지 않으면 예외가 발생한다.")
    @Test
    void validateTokenInvalidToken() {
        given(tokenRepository.isBlocked(anyString())).willReturn(false);
        willThrow(new InvalidTokenException()).given(tokenManager).validateToken(anyString());

        assertThatThrownBy(() -> tokenService.validateToken("access-token"))
                .isInstanceOf(InvalidTokenException.class);

        then(tokenRepository).should().isBlocked(anyString());
        then(tokenManager).should().validateToken(anyString());
    }

}