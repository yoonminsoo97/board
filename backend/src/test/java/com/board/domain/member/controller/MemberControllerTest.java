package com.board.domain.member.controller;

import com.board.domain.member.dto.MemberSignupRequest;
import com.board.domain.member.entity.Member;
import com.board.domain.member.exception.DuplicateNicknameException;
import com.board.domain.member.exception.DuplicateUsernameException;
import com.board.domain.member.service.MemberService;
import com.board.domain.token.dto.TokenResponse;
import com.board.domain.token.service.TokenService;
import com.board.global.security.dto.AuthPrincipal;
import com.board.global.security.dto.MemberLoginRequest;
import com.board.global.security.service.MemberUserDetailsService;
import com.board.restdocs.RestDocs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.PayloadDocumentation;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MemberController.class)
class MemberControllerTest extends RestDocs {

    @MockitoBean
    private MemberUserDetailsService memberUserDetailsService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private MemberService memberService;

    @DisplayName("회원가입에 성공하면 200 상태 코드를 반환한다.")
    @Test
    void memberSignup() throws Exception {
        MemberSignupRequest memberSignupRequest = new MemberSignupRequest("yoonkun", "yoon1234", "12345678");

        willDoNothing().given(memberService).memberSignup(any(MemberSignupRequest.class));

        mockMvc.perform(post("/api/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberSignupRequest))
                )
                .andExpect(
                        status().isOk()
                )
                .andDo(restdocs.document(
                        requestFields(
                                fieldWithPath("nickname").type(JsonFieldType.STRING).description("닉네임"),
                                fieldWithPath("username").type(JsonFieldType.STRING).description("아이디"),
                                fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호")
                        )
                ));
    }

    @DisplayName("회원가입 시 입력값이 비어 있으면 예외 메시지와 400 상태 코드를 반환한다.")
    @ParameterizedTest
    @MethodSource("memberSignupRequestBlankFields")
    void memberSignupBlankFields(MemberSignupRequest memberSignupRequest, String field, String message) throws Exception {
        mockMvc.perform(post("/api/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberSignupRequest))
                )
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.errorCode").value("1001"),
                        jsonPath("$.message").value("입력값이 잘못되었습니다."),
                        jsonPath("$.fields").isArray(),
                        jsonPath("$.fields[0].field").value(field),
                        jsonPath("$.fields[0].input").value(""),
                        jsonPath("$.fields[0].message").value(message)
                );
    }

    @DisplayName("회원가입 시 입력값이 정규 표현식에 일치하지 않으면 예외 메시지와 400 상태 코드를 반환한다.")
    @ParameterizedTest
    @MethodSource("memberSignupRequestMismatchRegexpFields")
    void memberSignupMismatchRegexpFields(MemberSignupRequest memberSignupRequest, String field, String input, String message) throws Exception {
        mockMvc.perform(post("/api/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberSignupRequest))
                )
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.errorCode").value("1001"),
                        jsonPath("$.message").value("입력값이 잘못되었습니다."),
                        jsonPath("$.fields").isArray(),
                        jsonPath("$.fields[0].field").value(field),
                        jsonPath("$.fields[0].input").value(input),
                        jsonPath("$.fields[0].message").value(message)
                );
    }

    @DisplayName("회원가입 시 닉네임 또는 아이디가 중복되면 예외 메시지와 409 상태 코드를 반환한다.")
    @ParameterizedTest
    @MethodSource("memberSignupRequestDuplicateNicknameAndUsername")
    void memberSignupDuplicateNicknameOrUsername(Exception duplicateException, String errorCode, String message) throws Exception {
        MemberSignupRequest memberSignupRequest = new MemberSignupRequest("yoonkun", "yoon1234", "12345678");

        willThrow(duplicateException).given(memberService).memberSignup(any(MemberSignupRequest.class));

        mockMvc.perform(post("/api/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberSignupRequest))
                )
                .andExpectAll(
                        status().isConflict(),
                        jsonPath("$.errorCode").value(errorCode),
                        jsonPath("$.message").value(message)
                );
    }

    @DisplayName("로그인에 성공하면 access token, refresh token과 200 상태 코드를 반환한다.")
    @Test
    void memberLogin() throws Exception {
        MemberLoginRequest memberLoginRequest = new MemberLoginRequest("yoon1234", "12345678");
        Member member = Member.builder()
                .username("yoon1234")
                .password(new BCryptPasswordEncoder().encode("12345678"))
                .build();
        AuthPrincipal authPrincipal = new AuthPrincipal(member);
        TokenResponse tokenResponse = new TokenResponse("access-token", "refresh-token");

        given(memberUserDetailsService.loadUserByUsername(anyString())).willReturn(authPrincipal);
        given(tokenService.saveToken(any(Member.class))).willReturn(tokenResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberLoginRequest))
                )
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.accessToken").value("access-token"),
                        jsonPath("$.refreshToken").value("refresh-token")
                ).andDo(restdocs.document(
                        PayloadDocumentation.requestFields(
                                fieldWithPath("username").type(JsonFieldType.STRING).description("아이디"),
                                fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호")
                        ),
                        PayloadDocumentation.responseFields(
                                fieldWithPath("accessToken").description("액세스 토큰"),
                                fieldWithPath("refreshToken").description("리프레시 토큰")
                        )
                ));
    }

    @DisplayName("로그인 시 아이디 또는 비밀번호가 일치하지 않으면 예외 응답과 401 상태 코드를 반환한다.")
    @Test
    void memberLoginBadCredentials() throws Exception {
        MemberLoginRequest memberLoginRequest = new MemberLoginRequest("yoon1234", "12345678");

        willThrow(UsernameNotFoundException.class).given(memberUserDetailsService).loadUserByUsername(anyString());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberLoginRequest))
                )
                .andExpectAll(
                        status().isUnauthorized(),
                        jsonPath("$.errorCode").value("3001"),
                        jsonPath("$.message").value("아이디 또는 비밀번호가 일치하지 않습니다.")
                );
    }

    // 회원가입 시 MemberSignupRequest 입력값 빈값 검증 반복 테스트 시 사용하는 메서드
    private static Stream<Arguments> memberSignupRequestBlankFields() {
        return Stream.of(
                Arguments.of(
                        Named.of("닉네임 공백", new MemberSignupRequest("", "yoon1234", "12345678")),
                        "nickname", "닉네임을 입력해 주세요."
                ),
                Arguments.of(
                        Named.of("아이디 공백", new MemberSignupRequest("yoonkun", "", "12345678")),
                        "username", "아이디를 입력해 주세요."
                ),
                Arguments.of(
                        Named.of("비밀번호 공백", new MemberSignupRequest("yoonkun", "yoon1234", "")),
                        "password", "비밀번호를 입력해 주세요."
                )
        );
    }

    // 회원가입 시 MemberSignupRequest 입력값 정규표션식 검증 반복 테스트 시 사용하는 메서드
    private static Stream<Arguments> memberSignupRequestMismatchRegexpFields() {
        return Stream.of(
                Arguments.of(
                        Named.of("닉네임 정규표현식 불일치", new MemberSignupRequest("@@!@", "yoon1234", "12345678")),
                        "nickname", "@@!@", "닉네임은 6~10자의 영문 대소문자, 한글, 숫자만 사용할 수 있습니다."
                ),
                Arguments.of(
                        Named.of("아이디 정규표현식 불일치", new MemberSignupRequest("yoonkun", "yo~!#$@", "12345678")),
                        "username", "yo~!#$@", "아이디는 8~16자의 영문 소문자, 숫자만 사용할 수 있습니다."
                ),
                Arguments.of(
                        Named.of("비밀번호 정규표현식 불일치", new MemberSignupRequest("yoonkun", "yoon1234", "123")),
                        "password", "123", "비밀번호는 8~16자의 영문 대소문자, 숫자, 특수문자만 사용할 수 있습니다."
                )
        );
    }

    // 회원가입 시 닉네임 또는 아이디 중복 테스트 시 사용하는 메서드
    private static Stream<Arguments> memberSignupRequestDuplicateNicknameAndUsername() {
        return Stream.of(
                Arguments.of(
                        Named.of("닉네임 중복", new DuplicateNicknameException()), "2001", "사용 중인 닉네임입니다."
                ),
                Arguments.of(
                        Named.of("아이디 중복", new DuplicateUsernameException()), "2002", "사용 중인 아이디입니다."
                )
        );
    }

}