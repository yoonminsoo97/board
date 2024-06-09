package com.board.domain.token.service;

import com.board.domain.member.entity.Member;
import com.board.domain.token.dto.TokenResponse;
import com.board.domain.token.entity.Token;
import com.board.domain.token.repository.TokenRepository;
import com.board.global.security.dto.LoginMember;
import com.board.global.security.support.JwtManager;
import com.board.global.security.exception.InvalidTokenException;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenRepository tokenRepository;
    private final JwtManager jwtManager;

    @Transactional
    public void saveToken(String refreshToken, Member member) {
        tokenRepository.findByMemberId(member.getId())
                .ifPresentOrElse(
                        token -> token.update(refreshToken),
                        () -> tokenRepository.save(Token.builder()
                                .refreshToken(refreshToken)
                                .member(member)
                                .build())
                );
    }

    @Transactional
    public void deleteToken(Long memberId) {
        Token token = tokenRepository.findByMemberId(memberId)
                .orElseThrow(InvalidTokenException::new);
        tokenRepository.delete(token);
    }

    @Transactional(readOnly = true)
    public TokenResponse reIssueAccessToken(String refreshToken) {
        Token token = tokenRepository.findByRefreshTokenJoinFetchMember(refreshToken)
                .orElseThrow(InvalidTokenException::new);
        LoginMember loginMember = new LoginMember(token.getMember());
        String accessToken = jwtManager.createAccessToken(loginMember);
        return TokenResponse.of(accessToken);
    }

}
