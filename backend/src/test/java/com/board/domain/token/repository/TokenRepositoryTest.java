package com.board.domain.token.repository;

import com.board.domain.member.entity.Member;
import com.board.domain.member.repository.MemberRepository;
import com.board.domain.token.entity.Token;
import com.board.global.common.config.JpaAuditConfig;
import com.board.support.config.QuerydslConfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({JpaAuditConfig.class, QuerydslConfig.class})
class TokenRepositoryTest {

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member member;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(Member.builder()
                .nickname("yoonkun")
                .username("yoon1234")
                .password("12345678")
                .build());
    }

    @Test
    @DisplayName("토큰을 저장한다")
    void tokenSave() {
        Token token = Token.builder()
                .refreshToken("refresh-token")
                .member(member)
                .build();

        Token saveToken = tokenRepository.save(token);

        assertThat(saveToken.getId()).isNotNull();
    }

    @Test
    @DisplayName("회원 아이디로 토큰을 조회한다")
    void tokenFindByMemberUsername() {
        Token token = Token.builder()
                .refreshToken("refresh-token")
                .member(member)
                .build();
        tokenRepository.save(token);

        Token findToken = tokenRepository.findByMemberUsername(member.getUsername()).get();

        assertThat(findToken.getRefreshToken()).isEqualTo("refresh-token");
    }

    @Test
    @DisplayName("토큰을 삭제한다")
    void tokenDelete() {
        Token token = Token.builder()
                .refreshToken("refresh-token")
                .member(member)
                .build();
        tokenRepository.save(token);

        Token findToken = tokenRepository.findByMemberUsername(member.getUsername()).get();

        tokenRepository.delete(findToken);

        assertThat(tokenRepository.findByMemberUsername(member.getUsername())).isEmpty();
    }

}