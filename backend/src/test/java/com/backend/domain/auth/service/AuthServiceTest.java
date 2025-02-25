package com.backend.domain.auth.service;

import com.backend.domain.auth.dto.MemberLoginRequest;
import com.backend.domain.auth.dto.MemberLoginResponse;
import com.backend.domain.auth.dto.TokenResponse;
import com.backend.domain.auth.exception.BadCredentialsException;
import com.backend.domain.member.entity.Member;
import com.backend.domain.member.repository.MemberRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthService authService;

    private Member member;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .nickname("yoonkun")
                .username("yoon1234")
                .password(new BCryptPasswordEncoder().encode("12345678"))
                .build();
    }

    @DisplayName("로그인을 한다.")
    @Test
    void memberLogin() {
        MemberLoginRequest memberLoginRequest = new MemberLoginRequest("yoon1234", "12345678");
        TokenResponse tokenResponse = new TokenResponse("access-token", "refresh-token");

        given(memberRepository.findByUsername(anyString())).willReturn(Optional.of(member));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);
        given(tokenService.tokenSave(any(Member.class))).willReturn(tokenResponse);

        MemberLoginResponse memberLoginResponse = authService.memberLogin(memberLoginRequest);

        assertThat(memberLoginResponse.getAccessToken()).isEqualTo("access-token");
        assertThat(memberLoginResponse.getRefreshToken()).isEqualTo("refresh-token");
        then(memberRepository).should().findByUsername(anyString());
        then(passwordEncoder).should().matches(anyString(), anyString());
        then(tokenService).should().tokenSave(any(Member.class));
    }

    @DisplayName("로그인 시 회원이 존재하지 않으면 예외가 발생한다.")
    @Test
    void memberLoginNotFoundMember() {
        MemberLoginRequest memberLoginRequest = new MemberLoginRequest("yoon1234", "12345678");

        willThrow(new BadCredentialsException()).given(memberRepository).findByUsername(anyString());

        assertThatThrownBy(() -> authService.memberLogin(memberLoginRequest))
                .isInstanceOf(BadCredentialsException.class);

        then(memberRepository).should().findByUsername(anyString());
        then(passwordEncoder).should(never()).matches(anyString(), anyString());
        then(tokenService).should(never()).tokenSave(any(Member.class));
    }

    @DisplayName("로그인 시 비밀번호가 일치하지 않으면 예외가 발생한다.")
    @Test
    void memberLoginWrongPassword() {
        MemberLoginRequest memberLoginRequest = new MemberLoginRequest("yoon1234", "12345678");

        given(memberRepository.findByUsername(anyString())).willReturn(Optional.of(member));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

        assertThatThrownBy(() -> authService.memberLogin(memberLoginRequest))
                .isInstanceOf(BadCredentialsException.class);

        then(memberRepository).should().findByUsername(anyString());
        then(passwordEncoder).should().matches(anyString(), anyString());
        then(tokenService).should(never()).tokenSave(any(Member.class));
    }

}