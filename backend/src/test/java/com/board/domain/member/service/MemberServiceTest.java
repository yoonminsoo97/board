package com.board.domain.member.service;

import com.board.domain.member.dto.MemberSignupRequest;
import com.board.domain.member.entity.Member;
import com.board.domain.member.exception.DuplicateNicknameException;
import com.board.domain.member.exception.DuplicateUsernameException;
import com.board.domain.member.exception.NotFoundMemberException;
import com.board.domain.member.exception.PasswordMismatchException;
import com.board.domain.member.repository.MemberRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberService memberService;

    @Test
    @DisplayName("닉네임 중복 확인을 한다")
    void memberNicknameExists() {
        String targetNickname = "yoonkun";

        given(memberRepository.existsMemberByNickname(anyString())).willReturn(false);

        memberService.memberNicknameExists(targetNickname);

        then(memberRepository).should().existsMemberByNickname(anyString());
    }

    @Test
    @DisplayName("닉네임 중복 시 예외가 발생한다")
    void memberNicknameExists_duplicateNickname() {
        String targetNickname = "yoonkun";

        given(memberRepository.existsMemberByNickname(anyString())).willReturn(true);

        assertThatThrownBy(() -> memberService.memberNicknameExists(targetNickname))
                        .isInstanceOf(DuplicateNicknameException.class);

        then(memberRepository).should().existsMemberByNickname(anyString());
    }

    @Test
    @DisplayName("아이디 중복 확인을 한다")
    void memberUsernameExists() {
        String targetUsername = "yoon1234";

        given(memberRepository.existsMemberByUsername(anyString())).willReturn(false);

        memberService.memberUsernameExists(targetUsername);

        then(memberRepository).should().existsMemberByUsername(anyString());
    }

    @Test
    @DisplayName("아이이 중복 시 예외가 발생한다")
    void memberUsernameExists_duplicateUsername() {
        String targetUsername = "yoon1234";

        given(memberRepository.existsMemberByUsername(anyString())).willReturn(true);

        assertThatThrownBy(() -> memberService.memberUsernameExists(targetUsername))
                .isInstanceOf(DuplicateUsernameException.class);

        then(memberRepository).should().existsMemberByUsername(anyString());
    }

    @Test
    @DisplayName("회원가입을 한다")
    void memberSignup() {
        MemberSignupRequest memberSignupRequest = new MemberSignupRequest("yoonkun", "yoon1234", "12345678", "12345678");

        String encoded = new BCryptPasswordEncoder().encode(memberSignupRequest.getPassword());

        Member member = Member.builder()
                .nickname(memberSignupRequest.getNickname())
                .username(memberSignupRequest.getUsername())
                .password(encoded)
                .build();

        given(memberRepository.existsMemberByNickname(anyString())).willReturn(false);
        given(memberRepository.existsMemberByUsername(anyString())).willReturn(false);
        given(passwordEncoder.encode(anyString())).willReturn(encoded);
        given(memberRepository.save(any(Member.class))).willReturn(member);

        memberService.memberSignup(memberSignupRequest);

        then(memberRepository).should().existsMemberByNickname(anyString());
        then(memberRepository).should().existsMemberByUsername(anyString());
        then(passwordEncoder).should().encode(anyString());
        then(memberRepository).should().save(any(Member.class));
    }

    @Test
    @DisplayName("회원가입 시 비밀번호가 일치하지 않으면 예외가 발생한다")
    void memberSignup_passwordMismatch() {
        MemberSignupRequest memberSignupRequest = new MemberSignupRequest("yoonkun", "yoon1234", "12345678", "12345679");

        given(memberRepository.existsMemberByNickname(anyString())).willReturn(false);
        given(memberRepository.existsMemberByUsername(anyString())).willReturn(false);

        assertThatThrownBy(() -> memberService.memberSignup(memberSignupRequest))
                .isInstanceOf(PasswordMismatchException.class);

        then(memberRepository).should().existsMemberByNickname(anyString());
        then(memberRepository).should().existsMemberByUsername(anyString());
        then(passwordEncoder).should(never()).encode(anyString());
        then(memberRepository).should(never()).save(any(Member.class));
    }

    @Test
    @DisplayName("회원 정보를 조회한다")
    void memberProfile() {
        Member member = Member.builder()
                .nickname("yoonkun")
                .username("yoon1234")
                .build();

        given(memberRepository.findMemberByUsername(anyString())).willReturn(Optional.of(member));

        memberService.memberProfile("yoon1234");

        then(memberRepository).should().findMemberByUsername(anyString());
    }

    @Test
    @DisplayName("회원 정보 조회 시 회원을 찾을 수 없으면 예외가 발생한다")
    void memberProfileNotFoundMember() {
        willThrow(new NotFoundMemberException()).given(memberRepository).findMemberByUsername(anyString());

        assertThatThrownBy(() -> memberService.memberProfile("yoon1234"))
                .isInstanceOf(NotFoundMemberException.class);

        then(memberRepository).should().findMemberByUsername(anyString());
    }

}