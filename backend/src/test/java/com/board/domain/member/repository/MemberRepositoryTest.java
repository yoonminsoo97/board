package com.board.domain.member.repository;

import com.board.domain.member.entity.Member;
import com.board.domain.member.entity.Role;
import com.board.global.common.config.JpaAuditingConfig;

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

    @DisplayName("회원 엔티티를 저장한다.")
    @Test
    void memberSave() {
        Member member = Member.builder()
                .nickname("yoonkun")
                .username("yoon1234")
                .password("12345678")
                .build();

        Member saveMember = memberRepository.save(member);

        assertThat(saveMember.getId()).isNotNull();
        assertThat(saveMember.getNickname()).isEqualTo("yoonkun");
        assertThat(saveMember.getUsername()).isEqualTo("yoon1234");
        assertThat(saveMember.getPassword()).isEqualTo("12345678");
        assertThat(saveMember.getRole()).isEqualTo(Role.MEMBER);
    }

    @DisplayName("회원 엔티티 저장 시 필드가 null이면 예외가 발생한다.")
    @ParameterizedTest
    @MethodSource("memberNullFields")
    void memberSaveNullFields(Member member) {
        assertThatThrownBy(() -> memberRepository.save(member))
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

    private static Stream<Arguments> memberNullFields() {
        return Stream.of(
                Arguments.of(Named.of("닉네임 NULL", new Member(null, "yoon1234", "12345678"))),
                Arguments.of(Named.of("아이디 NULL", new Member("yoonkun", null, "12345678"))),
                Arguments.of(Named.of("비밀번호 NULL", new Member("yoonkun", "yoon1234", null)))
        );
    }

}