package com.board.domain.token.service;

import com.board.domain.member.entity.Member;
import com.board.domain.token.entity.Token;
import com.board.domain.token.exception.InvalidTokenException;
import com.board.domain.token.repository.TokenRepository;
import com.board.domain.token.util.JwtUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
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
                .build();
    }

    @Test
    @DisplayName("토큰을 저장한다")
    void tokenSave() {
        Token token = Token.builder()
                .refreshToken("refresh-token")
                .member(member)
                .build();

        given(jwtUtil.createAccessToken(anyString(), anyString(), anyString())).willReturn("access-token");
        given(jwtUtil.createRefreshToken(anyString())).willReturn("refresh-token");
        given(tokenRepository.save(any(Token.class))).willReturn(token);

        tokenService.tokenSave(member);

        then(jwtUtil).should().createAccessToken(anyString(), anyString(), anyString());
        then(jwtUtil).should().createRefreshToken(anyString());
        then(tokenRepository).should().save(any(Token.class));
    }

    @Test
    @DisplayName("토큰을 삭제한다")
    void tokenDelete() {
        Token token = Token.builder()
                .refreshToken("refresh-token")
                .member(member)
                .build();

        given(tokenRepository.findByMemberUsername(anyString())).willReturn(Optional.of(token));
        willDoNothing().given(tokenRepository).delete(any(Token.class));

        tokenService.tokenDelete("yoon1234");

        then(tokenRepository).should().findByMemberUsername(anyString());
        then(tokenRepository).should().delete(any(Token.class));
    }

    @Test
    @DisplayName("토큰 삭제 시 토큰이 존재하지 않으면 예외가 발생한다")
    void tokenDelete_invalidToken() {
        given(tokenRepository.findByMemberUsername(anyString())).willReturn(Optional.empty());

        assertThatThrownBy(() -> tokenService.tokenDelete("yoon1234"))
                .isInstanceOf(InvalidTokenException.class);

        then(tokenRepository).should().findByMemberUsername(anyString());
        then(tokenRepository).should(never()).delete(any(Token.class));
    }

}