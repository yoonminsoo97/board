package com.board.domain.token.service;

import com.board.domain.member.entity.Member;
import com.board.domain.token.dto.TokenResponse;
import com.board.domain.token.entity.Token;
import com.board.domain.token.repository.TokenRepository;
import com.board.domain.token.util.JwtUtil;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

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

}