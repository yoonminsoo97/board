package com.board.domain.member.repository;

import com.board.domain.member.entity.Member;
import com.board.global.common.config.JpaAuditConfig;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditConfig.class)
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("닉네임 존재 여부를 확인한다")
    void memberNicknameExists() {
        Member member = Member.builder()
                .nickname("yoonkun")
                .username("yoon1234")
                .password("12345678")
                .build();
        memberRepository.save(member);

        boolean existsNickname = memberRepository.existsMemberByNickname("yoonkun");
        boolean nonExistsNickname = memberRepository.existsMemberByNickname("yoonkong");

        assertThat(existsNickname).isTrue();
        assertThat(nonExistsNickname).isFalse();
    }

    @Test
    @DisplayName("아이디 존재 여부를 확인한다")
    void memberUsernameExists() {
        Member member = Member.builder()
                .nickname("yoonkun")
                .username("yoon1234")
                .password("12345678")
                .build();
        memberRepository.save(member);

        boolean existsUsername = memberRepository.existsMemberByUsername("yoon1234");
        boolean nonExistsUsername = memberRepository.existsMemberByUsername("yoon5678");

        assertThat(existsUsername).isTrue();
        assertThat(nonExistsUsername).isFalse();
    }

    @Test
    @DisplayName("회원을 저장한다")
    void memberSave() {
        Member member = Member.builder()
                .nickname("yoonkun")
                .username("yoon1234")
                .password("12345678")
                .build();

        Member saveMember = memberRepository.save(member);

        assertThat(saveMember.getId()).isNotNull();
    }

}