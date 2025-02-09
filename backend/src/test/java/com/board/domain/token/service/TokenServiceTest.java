package com.board.domain.token.service;

import com.board.domain.member.entity.Member;
import com.board.domain.token.dto.TokenResponse;
import com.board.domain.token.entity.Token;
import com.board.domain.token.exception.NotFoundTokenException;
import com.board.domain.token.repository.TokenRepository;
import com.board.domain.token.util.JwtUtil;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @DisplayName("토큰을 저장하고 access token, refresh token을 반환한다.")
    @Test
    void tokenSave() {
        Member member = Member.builder()
                .nickname("yoonkun")
                .username("yoon1234")
                .password("12345678")
                .build();
        Token token = Token.builder()
                .refreshToken("refresh-token")
                .member(member)
                .build();

        given(jwtUtil.createAccessToken(anyString(), anyString())).willReturn("access-token");
        given(jwtUtil.createRefreshToken()).willReturn("refresh-token");
        given(tokenRepository.save(any(Token.class))).willReturn(token);

        TokenResponse tokenResponse = tokenService.saveToken(member);

        assertThat(tokenResponse.getAccessToken()).isEqualTo("access-token");
        assertThat(tokenResponse.getRefreshToken()).isEqualTo("refresh-token");
        then(jwtUtil).should().createAccessToken(anyString(), anyString());
        then(jwtUtil).should().createRefreshToken();
    }

    @DisplayName("refresh token을 삭제한다.")
    @Test
    void tokenDelete() {
        Member member = Member.builder()
                .nickname("yoonkun")
                .username("yoon1234")
                .password("12345678")
                .build();
        Token token = Token.builder()
                .refreshToken("refresh-token")
                .member(member)
                .build();

        given(tokenRepository.findByMemberUsername(anyString())).willReturn(Optional.of(token));
        willDoNothing().given(tokenRepository).delete(any(Token.class));

        tokenService.deleteToken("yoon1234");

        then(tokenRepository).should().findByMemberUsername(anyString());
        then(tokenRepository).should().delete(any(Token.class));
    }

    @DisplayName("refresh token이 존재하지 않으면 예외가 발생한다.")
    @Test
    void tokenDeleteNotFoundToken() {
        willThrow(new NotFoundTokenException()).given(tokenRepository).findByMemberUsername(anyString());

        assertThatThrownBy(() -> tokenService.deleteToken("yoon1234"))
                .isInstanceOf(NotFoundTokenException.class);

        then(tokenRepository).should().findByMemberUsername(anyString());
        then(tokenRepository).should(never()).delete(any(Token.class));
    }

}