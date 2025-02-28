package com.backend.domain.member.controller;

import com.backend.domain.member.dto.MemberSignupRequest;
import com.backend.domain.member.exception.DuplicateNicknameException;
import com.backend.domain.member.exception.DuplicateUsernameException;
import com.backend.domain.member.service.MemberService;

import com.backend.domain.support.ControllerTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MemberController.class)
class MemberControllerTest extends ControllerTest {

    @MockitoBean
    private MemberService memberService;

    @DisplayName("회원가입에 성공하면 200을 응답한다.")
    @Test
    void memberSignup() throws Exception {
        MemberSignupRequest memberSignupRequest = new MemberSignupRequest("yoonkun", "yoon1234", "12345678");

        willDoNothing().given(memberService).memberSignup(any(MemberSignupRequest.class));

        mockMvc.perform(post("/api/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberSignupRequest))
                )
                .andExpect(status().isOk())
                .andDo(restdocs)
                .andDo(restdocs.document(
                        requestFields(
                                fieldWithPath("nickname").type(JsonFieldType.STRING).description("닉네임"),
                                fieldWithPath("username").type(JsonFieldType.STRING).description("아이디"),
                                fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호")
                        )
                ));
    }

    @DisplayName("회원가입 시 입력값이 유효하지 않으면 400을 응답한다.")
    @ParameterizedTest
    @MethodSource("invalidInputMemberSignupRequest")
    void memberSignupInvalidInput(MemberSignupRequest memberSignupRequest) throws Exception {
        mockMvc.perform(post("/api/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberSignupRequest))
                )
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.status").value(400),
                        jsonPath("$.errorCode").value("E400001"),
                        jsonPath("$.message").value("입력값이 잘못 되었습니다."),
                        jsonPath("$.errors").isArray(),
                        jsonPath("$.errors[0].field").isNotEmpty(),
                        jsonPath("$.errors[0].message").isNotEmpty()
                );
    }

    private static Stream<Object> invalidInputMemberSignupRequest() {
        return Stream.of(
                Arguments.of(Named.of("닉네임 공백", new MemberSignupRequest("", "yoon1234", "12345678"))),
                Arguments.of(Named.of("아이디 공백", new MemberSignupRequest("yoonkun", "", "12345678"))),
                Arguments.of(Named.of("비밀번호 공백", new MemberSignupRequest("yoonkun", "yoon1234", "")))
        );
    }

    @DisplayName("회원가입 시 닉네임이 중복되면 409를 응답한다.")
    @Test
    void memberSignupDuplicateNickname() throws Exception {
        MemberSignupRequest memberSignupRequest = new MemberSignupRequest("yoonkun", "yoon1234", "12345678");

        willThrow(new DuplicateNicknameException()).given(memberService).memberSignup(any(MemberSignupRequest.class));

        mockMvc.perform(post("/api/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberSignupRequest))
                )
                .andExpectAll(
                        status().isConflict(),
                        jsonPath("$.status").value(409),
                        jsonPath("$.errorCode").value("E409001"),
                        jsonPath("$.message").value("사용 중인 닉네임입니다.")
                );
    }

    @DisplayName("회원가입 시 아이디가 중복되면 409를 응답한다.")
    @Test
    void memberSignupDuplicateUsername() throws Exception {
        MemberSignupRequest memberSignupRequest = new MemberSignupRequest("yoonkun", "yoon1234", "12345678");

        willThrow(new DuplicateUsernameException()).given(memberService).memberSignup(any(MemberSignupRequest.class));

        mockMvc.perform(post("/api/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberSignupRequest))
                )
                .andExpectAll(
                        status().isConflict(),
                        jsonPath("$.status").value(409),
                        jsonPath("$.errorCode").value("E409002"),
                        jsonPath("$.message").value("사용 중인 아이디입니다.")
                );
    }

}