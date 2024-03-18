package com.board.domain.token.service;

import com.board.domain.member.entity.Member;
import com.board.domain.token.entity.Token;
import com.board.domain.token.repository.TokenRepository;
import com.board.domain.token.util.JwtUtil;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @Test
    @DisplayName("토큰을 저장한다")
    void tokenSave() {
        Member member = Member.builder()
                .nickname("yoonkun")
                .username("yoon1234")
                .build();
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

}