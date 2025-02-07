package com.board.domain.token.repository;

import com.board.domain.member.entity.Member;
import com.board.domain.member.repository.MemberRepository;
import com.board.domain.token.entity.Token;
import com.board.global.common.config.JpaAuditingConfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(JpaAuditingConfig.class)
class TokenRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TokenRepository tokenRepository;

    private Member saveMember;

    @BeforeEach
    void setUp() {
        saveMember = memberRepository.save(Member.builder()
                .nickname("yoonkun")
                .username("yoon1234")
                .password("12345678")
                .build());
    }

    @DisplayName("토큰 엔티티를 저장한다.")
    @Test
    void tokenSave() {
        Token token = Token.builder()
                .refreshToken("refresh-token")
                .member(saveMember)
                .build();

        Token saveToken = tokenRepository.save(token);

        assertThat(saveToken.getId()).isNotNull();
        assertThat(saveToken.getRefreshToken()).isEqualTo("refresh-token");
    }

    @DisplayName("토큰 엔티티 저장 시 refreshToken 필드가 null이면 예외가 발생한다.")
    @Test
    void tokenSaveNullRefreshTokenField() {
        Token token = Token.builder()
                .refreshToken(null)
                .member(saveMember)
                .build();

        assertThatThrownBy(() -> tokenRepository.save(token))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @DisplayName("토큰 엔티티 저장 시 member 필드가 null이면 예외가 발생한다.")
    @Test
    void tokenSaveNullMemberField() {
        Token token = Token.builder()
                .refreshToken("refresh-token")
                .member(null)
                .build();

        assertThatThrownBy(() -> tokenRepository.save(token))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

}