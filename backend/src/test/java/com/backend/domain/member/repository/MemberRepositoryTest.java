package com.backend.domain.member.repository;

import com.backend.domain.member.entity.Member;
import com.backend.global.common.config.JpaAuditingConfig;

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
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("회원을 저장한다.")
    @Test
    void memberSave() {
        Member member = Member.builder()
                .nickname("yoonkun")
                .username("yoon1234")
                .password("12345678")
                .build();

        Member saveMember = memberRepository.save(member);

        assertThat(saveMember.getId()).isNotNull();
    }

    @DisplayName("회원 저장 시 필드가 null이면 예외가 발생한다.")
    @ParameterizedTest
    @MethodSource("nullFieldsMember")
    void memberSaveNullFields(Member member) {
        assertThatThrownBy(() -> memberRepository.save(member))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private static Stream<Object> nullFieldsMember() {
        return Stream.of(
                Arguments.of(Named.of("nickname 필드 null", new Member(null, "yoon1234", "12345678"))),
                Arguments.of(Named.of("username 필드 null", new Member("yoonkun", null, "12345678"))),
                Arguments.of(Named.of("password 필드 null", new Member("yoonkun", "yoon1234", null)))
        );
    }

    @DisplayName("동일한 닉네임을 가진 회원을 저장하면 예외가 발생한다.")
    @Test
    void memberSaveUniqueNickname() {
        Member memberA = Member.builder()
                .nickname("yoonkun")
                .username("yoon1234")
                .password("12345678")
                .build();
        Member memberB = Member.builder()
                .nickname("yoonkun")
                .username("yoon5678")
                .password("12345678")
                .build();
        memberRepository.save(memberA);

        assertThatThrownBy(() -> memberRepository.save(memberB))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @DisplayName("동일한 아이디를 가진 회원을 저장하면 예외가 발생한다.")
    @Test
    void memberSaveUniqueUsername() {
        Member memberA = Member.builder()
                .nickname("yoonkun")
                .username("yoon1234")
                .password("12345678")
                .build();
        Member memberB = Member.builder()
                .nickname("yoonyoon")
                .username("yoon1234")
                .password("12345678")
                .build();

        memberRepository.save(memberA);

        assertThatThrownBy(() -> memberRepository.save(memberB))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @DisplayName("동일한 닉네임이 존재하면 true를 반환한다.")
    @Test
    void memberExistsByNickname() {
        Member member = Member.builder()
                .nickname("yoonkun")
                .username("yoon1234")
                .password("12345678")
                .build();
        memberRepository.save(member);

        boolean exists = memberRepository.existsByNickname("yoonkun");

        assertThat(exists).isTrue();
    }

    @DisplayName("동일한 닉네임이 존재하지 않으면 flase를 반환한다.")
    @Test
    void memberNotExistsByNickname() {
        boolean exists = memberRepository.existsByNickname("yoonkun");

        assertThat(exists).isFalse();
    }

    @DisplayName("동일한 아이디가 존재하면 true를 반환한다.")
    @Test
    void memberExistsByUsername() {
        Member member = Member.builder()
                .nickname("yoonkun")
                .username("yoon1234")
                .password("12345678")
                .build();
        memberRepository.save(member);

        boolean exists = memberRepository.existsByUsername("yoon1234");

        assertThat(exists).isTrue();
    }

    @DisplayName("동일한 아이디가 존재하지 않으면 flase를 반환한다.")
    @Test
    void memberNotExistsByUsername() {
        boolean exists = memberRepository.existsByUsername("yoon1234");

        assertThat(exists).isFalse();
    }

}