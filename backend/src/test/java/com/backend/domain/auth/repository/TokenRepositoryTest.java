package com.backend.domain.auth.repository;

import com.backend.domain.auth.entity.Token;
import com.backend.domain.member.entity.Member;
import com.backend.domain.member.repository.MemberRepository;
import com.backend.global.common.config.JpaAuditingConfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(JpaAuditingConfig.class)
class TokenRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TokenRepository tokenRepository;

    private static Member saveMember;

    @BeforeEach
    void setUp() {
        saveMember = memberRepository.save(Member.builder()
                .nickname("yoonkun")
                .username("yoon1234")
                .password("12345678")
                .build());
    }

    @DisplayName("토큰을 저장한다.")
    @Test
    void tokenSave() {
        Token token = Token.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .member(saveMember)
                .build();

        Token saveToken = tokenRepository.save(token);

        assertThat(saveToken.getId()).isNotNull();
    }

    @DisplayName("토큰 저장 시 필드가 null이면 예외가 발생한다.")
    @ParameterizedTest
    @MethodSource("nullFieldsToken")
    void tokenSaveNullFields(Token token) {
        assertThatThrownBy(() -> tokenRepository.save(token))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private static Stream<Object> nullFieldsToken() {
        return Stream.of(
                Arguments.of(Named.of("accessToken 필드 null", new Token(null, "refresh-token", saveMember))),
                Arguments.of(Named.of("refreshToken 필드 null", new Token("access-token", null, saveMember))),
                Arguments.of(Named.of("member 필드 null", new Token("access-token", "refresh-token", null)))
        );
    }

}