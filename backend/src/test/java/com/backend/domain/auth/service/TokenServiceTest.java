package com.backend.domain.auth.service;

import com.backend.domain.auth.dto.TokenResponse;
import com.backend.domain.auth.entity.Token;
import com.backend.domain.auth.exception.NotFoundTokenException;
import com.backend.domain.auth.repository.TokenRepository;
import com.backend.domain.auth.util.JwtUtil;
import com.backend.domain.member.entity.Member;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private TokenService tokenService;

    private Member member;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .nickname("yoonkun")
                .username("yoon1234")
                .password(new BCryptPasswordEncoder().encode("12345678"))
                .build();
    }

    @DisplayName("토큰을 저장한다.")
    @Test
    void tokenSave() {
        Token token = Token.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .member(member)
                .build();

        given(tokenRepository.save(any(Token.class))).willReturn(token);
        given(jwtUtil.createAccesStoken(any(Member.class))).willReturn("access-token");
        given(jwtUtil.createRefreshToken()).willReturn("refresh-token");

        TokenResponse tokenResponse = tokenService.tokenSave(member);

        assertThat(tokenResponse.getAccessToken()).isEqualTo("access-token");
        assertThat(tokenResponse.getRefreshToken()).isEqualTo("refresh-token");
        then(tokenRepository).should().save(any(Token.class));
        then(jwtUtil).should().createAccesStoken(any(Member.class));
        then(jwtUtil).should().createRefreshToken();
    }

    @DisplayName("토큰을 삭제한다.")
    @Test
    void tokenDelete() {
        Token token = Token.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .member(member)
                .build();

        given(tokenRepository.findByMemberUsername(anyString())).willReturn(Optional.of(token));
        willDoNothing().given(tokenRepository).delete(any(Token.class));

        tokenService.tokenDelete("yoon1234");

        then(tokenRepository).should().findByMemberUsername(anyString());
        then(tokenRepository).should().delete(any(Token.class));
    }

    @DisplayName("토큰 삭제 시 토큰이 존재하지 않으면 예외가 발생한다.")
    @Test
    void tokenDeleteNotFoundToken() {
        willThrow(new NotFoundTokenException()).given(tokenRepository).findByMemberUsername(anyString());

        assertThatThrownBy(() -> tokenService.tokenDelete(anyString()))
                .isInstanceOf(NotFoundTokenException.class);

        then(tokenRepository).should().findByMemberUsername(anyString());
        then(tokenRepository).should(never()).delete(any(Token.class));
    }

}