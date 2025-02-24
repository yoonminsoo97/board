package com.backend.domain.member.service;

import com.backend.domain.member.dto.MemberSignupRequest;
import com.backend.domain.member.entity.Member;
import com.backend.domain.member.exception.DuplicateNicknameException;
import com.backend.domain.member.exception.DuplicateUsernameException;
import com.backend.domain.member.repository.MemberRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberService memberService;

    @DisplayName("회원가입을 한다.")
    @Test
    void memberSignup() {
        MemberSignupRequest memberSignupRequest = new MemberSignupRequest("yoonkun", "yoon1234", "12345678");
        String encodedPassword = new BCryptPasswordEncoder().encode(memberSignupRequest.getPassword());
        Member member = Member.builder()
                .nickname(memberSignupRequest.getNickname())
                .username(memberSignupRequest.getUsername())
                .password(encodedPassword)
                .build();

        given(memberRepository.existsByNickname(anyString())).willReturn(false);
        given(memberRepository.existsByUsername(anyString())).willReturn(false);
        given(passwordEncoder.encode(anyString())).willReturn(encodedPassword);
        given(memberRepository.save(any(Member.class))).willReturn(member);

        memberService.memberSignup(memberSignupRequest);

        then(memberRepository).should().existsByNickname(anyString());
        then(memberRepository).should().existsByUsername(anyString());
        then(passwordEncoder).should().encode(anyString());
        then((memberRepository)).should().save(any(Member.class));
    }

    @DisplayName("회원가입 시 닉네임이 중복되면 예외가 발생한다.")
    @Test
    void memberSignupDuplicateNickname() {
        MemberSignupRequest memberSignupRequest = new MemberSignupRequest("yoonkun", "yoon1234", "12345678");

        given(memberRepository.existsByNickname(anyString())).willReturn(true);

        assertThatThrownBy(() -> memberService.memberSignup(memberSignupRequest))
                .isInstanceOf(DuplicateNicknameException.class);

        then(memberRepository).should().existsByNickname(anyString());
        then(memberRepository).should(never()).existsByUsername(anyString());
        then(passwordEncoder).should(never()).encode(anyString());
        then((memberRepository)).should(never()).save(any(Member.class));
    }

    @DisplayName("회원가입 시 아이디가 중복되면 예외가 발생한다.")
    @Test
    void memberSignupDuplicateUsername() {
        MemberSignupRequest memberSignupRequest = new MemberSignupRequest("yoonkun", "yoon1234", "12345678");

        given(memberRepository.existsByNickname(anyString())).willReturn(false);
        given(memberRepository.existsByUsername(anyString())).willReturn(true);

        assertThatThrownBy(() -> memberService.memberSignup(memberSignupRequest))
                .isInstanceOf(DuplicateUsernameException.class);

        then(memberRepository).should().existsByNickname(anyString());
        then(memberRepository).should().existsByUsername(anyString());
        then(passwordEncoder).should(never()).encode(anyString());
        then((memberRepository)).should(never()).save(any(Member.class));
    }

}