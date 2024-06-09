package com.board.domain.token.service;

import com.board.domain.member.entity.Member;
import com.board.domain.token.entity.Token;
import com.board.domain.token.repository.TokenRepository;
import com.board.global.security.dto.LoginMember;
import com.board.global.security.exception.InvalidTokenException;
import com.board.global.security.support.JwtManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
    private JwtManager jwtManager;

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
        ReflectionTestUtils.setField(member, "id", 1L);
    }

    @Nested
    @DisplayName("토큰 저장")
    class TokenSaveTest {

        @Test
        @DisplayName("리프레시 토큰을 저장한다")
        void saveToken() {
            Token token = Token.builder()
                    .refreshToken("refresh-token")
                    .member(member)
                    .build();

            given(tokenRepository.findByMemberId(anyLong())).willReturn(Optional.empty());
            given(tokenRepository.save(any(Token.class))).willReturn(token);

            tokenService.saveToken("refresh-token", member);

            then(tokenRepository).should().findByMemberId(anyLong());
            then(tokenRepository).should().save(any(Token.class));
        }

        @Test
        @DisplayName("토큰이 존재하면 리프레시 토큰을 수정한다")
        void updateToken() {
            Token token = Token.builder()
                    .refreshToken("refresh-token")
                    .member(member)
                    .build();

            given(tokenRepository.findByMemberId(anyLong())).willReturn(Optional.of(token));

            tokenService.saveToken("refresh-token", member);

            then(tokenRepository).should().findByMemberId(anyLong());
            then(tokenRepository).should(never()).save(any(Token.class));
        }

    }

    @Nested
    @DisplayName("토큰 삭제")
    class TokenDeleteTest {

        @Test
        @DisplayName("토큰을 삭제한다")
        void deleteToken() {
            Token token = Token.builder()
                    .refreshToken("refresh-token")
                    .member(member)
                    .build();

            given(tokenRepository.findByMemberId(anyLong())).willReturn(Optional.of(token));
            willDoNothing().given(tokenRepository).delete(any(Token.class));

            tokenService.deleteToken(member.getId());

            then(tokenRepository).should().findByMemberId(anyLong());
            then(tokenRepository).should().delete(any(Token.class));
        }

        @Test
        @DisplayName("토큰이 존재하지 않으면 예외가 발생한다")
        void deleteTokenNotFoundToken() {
            willThrow(new InvalidTokenException()).given(tokenRepository).findByMemberId(anyLong());

            assertThatThrownBy(() -> tokenService.deleteToken(member.getId()))
                    .isInstanceOf(InvalidTokenException.class);

            then(tokenRepository).should().findByMemberId(anyLong());
            then(tokenRepository).should(never()).delete(any(Token.class));
        }

    }

    @Nested
    @DisplayName("액세스 토큰 재발급")
    class ReIssueAccessTokenTest {

        @Test
        @DisplayName("새로운 액세스 토큰을 발급한다")
        void reIssueAccessToken() {
            Token token = Token.builder()
                    .refreshToken("refresh-token")
                    .member(member)
                    .build();

            given(tokenRepository.findByRefreshTokenJoinFetchMember(anyString())).willReturn(Optional.of(token));
            given(jwtManager.createAccessToken(any(LoginMember.class))).willReturn("new-access-token");

            tokenService.reIssueAccessToken("refresh-token");

            then(tokenRepository).should().findByRefreshTokenJoinFetchMember(anyString());
            then(jwtManager).should().createAccessToken(any(LoginMember.class));
        }

        @Test
        @DisplayName("토큰이 존재하지 않으면 예외가 발생한다")
        void reIssueAccessTokenNotFoundRefreshToken() {
            willThrow(new InvalidTokenException()).given(tokenRepository).findByRefreshTokenJoinFetchMember(anyString());

            assertThatThrownBy(() -> tokenService.reIssueAccessToken("refresh-token"))
                    .isInstanceOf(InvalidTokenException.class);

            then(tokenRepository).should().findByRefreshTokenJoinFetchMember(anyString());
            then(jwtManager).should(never()).createAccessToken(any(LoginMember.class));
        }

    }

}