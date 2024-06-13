package com.board.domain.member.repository;

import com.board.domain.member.entity.Member;
import com.board.domain.member.exception.NotFoundMemberException;
import com.board.support.RepositoryTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberRepositoryTest extends RepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Nested
    @DisplayName("회원 저장")
    class MemberSaveTest {

        @Test
        @DisplayName("회원을 저장한다")
        void save() {
            Member member = Member.builder()
                    .nickname("yoonkun")
                    .username("yoon1234")
                    .password(new BCryptPasswordEncoder().encode("12345678"))
                    .build();

            Member saveMember = memberRepository.save(member);

            assertThat(saveMember.getId()).isNotNull();
        }

        @Test
        @DisplayName("닉네임이 null이면 예외가 발생한다")
        void saveNullNickname() {
            Member member = Member.builder()
                    .nickname(null)
                    .username("yoon1234")
                    .password(new BCryptPasswordEncoder().encode("12345678"))
                    .build();

            assertThatThrownBy(() -> memberRepository.save(member))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("아이디가 null이면 예외가 발생한다")
        void saveNullUsername() {
            Member member = Member.builder()
                    .nickname("yoonkun")
                    .username(null)
                    .password(new BCryptPasswordEncoder().encode("12345678"))
                    .build();

            assertThatThrownBy(() -> memberRepository.save(member))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("비밀번호가 null이면 예외가 발생한다")
        void saveNullPassword() {
            Member member = Member.builder()
                    .nickname("yoonkun")
                    .username("yoon1234")
                    .password(null)
                    .build();

            assertThatThrownBy(() -> memberRepository.save(member))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("이미 존재하는 닉네임을 가진 회원을 저장하면 예외가 발생한다")
        void saveUniqueNickname() {
            Member memberA = Member.builder()
                    .nickname("yoonkun")
                    .username("yoon1234")
                    .password(new BCryptPasswordEncoder().encode("12345678"))
                    .build();
            Member memberB = Member.builder()
                    .nickname("yoonkun")
                    .username("yoon5678")
                    .password(new BCryptPasswordEncoder().encode("12345678"))
                    .build();
            memberRepository.save(memberA);

            assertThatThrownBy(() -> memberRepository.save(memberB))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("이미 존재하는 아이디를 가진 회원을 저장하면 예외가 발생한다")
        void saveUniqueUsername() {
            Member memberA = Member.builder()
                    .nickname("yoonkun")
                    .username("yoon1234")
                    .password(new BCryptPasswordEncoder().encode("12345678"))
                    .build();
            Member memberB = Member.builder()
                    .nickname("yoongun")
                    .username("yoon1234")
                    .password(new BCryptPasswordEncoder().encode("12345678"))
                    .build();
            memberRepository.save(memberA);

            assertThatThrownBy(() -> memberRepository.save(memberB))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

    }

    @Nested
    @DisplayName("회원 닉네임 존재 여부")
    class MemberNicknameExistsTest {

        @Test
        @DisplayName("닉네임이 존재하면 true를 반환한다")
        void existsNicknameReturnTrue() {
            Member member = Member.builder()
                    .nickname("yoonkun")
                    .username("yoon1234")
                    .password(new BCryptPasswordEncoder().encode("12345678"))
                    .build();
            memberRepository.save(member);

            boolean exists = memberRepository.existsByNickname("yoonkun");

            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("닉네임이 존재하지 않으면 false를 반환한다")
        void nonExistsNicknameReturnFalse() {
            boolean exists = memberRepository.existsByNickname("yoonkun");

            assertThat(exists).isFalse();
        }

    }

    @Nested
    @DisplayName("회원 아이디 존재 여부")
    class MemberUsernameExistsTest {

        @Test
        @DisplayName("아이디가 존재하면 true를 반환한다")
        void existsUsernameRetrunTrue() {
            Member member = Member.builder()
                    .nickname("yoonkun")
                    .username("yoon1234")
                    .password(new BCryptPasswordEncoder().encode("12345678"))
                    .build();
            memberRepository.save(member);

            boolean exists = memberRepository.existsByUsername("yoon1234");

            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("아이디가 존재하지 않으면 false를 반환한다")
        void existsUsernameReturnFalse() {
            boolean exists = memberRepository.existsByUsername("yoon1234");

            assertThat(exists).isFalse();
        }

    }

    @Nested
    @DisplayName("회원 조회")
    class MemberFindTest {

        @Test
        @DisplayName("아이디(username)으로 조회한다")
        void findByUsername() {
            Member member = Member.builder()
                    .nickname("yoonkun")
                    .username("yoon1234")
                    .password(new BCryptPasswordEncoder().encode("12345678"))
                    .build();
            memberRepository.save(member);

            Member findMember = memberRepository.findByUsername("yoon1234").get();

            assertThat(findMember.getId()).isNotNull();
            assertThat(findMember.getNickname()).isEqualTo("yoonkun");
            assertThat(findMember.getUsername()).isEqualTo("yoon1234");
            assertThat(findMember.getAuthority()).isEqualTo("ROLE_MEMBER");
        }

        @Test
        @DisplayName("기본키(id)로 조회한다")
        void findByMemberId() {
            Member member = Member.builder()
                    .nickname("yoonkun")
                    .username("yoon1234")
                    .password(new BCryptPasswordEncoder().encode("12345678"))
                    .build();
            memberRepository.save(member);

            Member findMember = memberRepository.findByMemberId(member.getId());

            assertThat(findMember.getId()).isNotNull();
            assertThat(findMember.getNickname()).isEqualTo("yoonkun");
            assertThat(findMember.getUsername()).isEqualTo("yoon1234");
            assertThat(findMember.getAuthority()).isEqualTo("ROLE_MEMBER");
        }

        @Test
        @DisplayName("기본키(id)에 해당하는 회원이 없으면 예외가 발생한다")
        void findByMemberIdNotFoundMember() {
            assertThatThrownBy(() -> memberRepository.findByMemberId(1L))
                    .isInstanceOf(NotFoundMemberException.class);
        }

    }

}