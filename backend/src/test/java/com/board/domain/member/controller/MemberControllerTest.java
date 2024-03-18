package com.board.domain.member.controller;

import com.board.domain.member.dto.MemberSignupRequest;
import com.board.domain.member.exception.DuplicateNicknameException;
import com.board.domain.member.exception.DuplicateUsernameException;
import com.board.domain.member.exception.PasswordMismatchException;
import com.board.domain.member.service.MemberService;
import com.board.global.security.config.SecurityConfig;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MemberController.class)
@Import(SecurityConfig.class)
class MemberControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberService memberService;

    @Test
    @DisplayName("닉네임 중복 확인을 한다")
    void memberNicknameExists() throws Exception {
        String targetNickname = "yoonkun";

        willDoNothing().given(memberService).memberNicknameExists(anyString());

        mockMvc.perform(get("/api/members/nickname/" + targetNickname))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"))
                .andDo(print());
    }

    @Test
    @DisplayName("닉네임 중복 시 예외가 발생한다")
    void memberNicknameExists_duplicateNickname() throws Exception {
        String targetNickname = "yoonkun";

        willThrow(new DuplicateNicknameException()).given(memberService).memberNicknameExists(anyString());

        mockMvc.perform(get("/api/members/nickname/" + targetNickname))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("E409001"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("사용 중인 닉네임입니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("아이디 중복 확인을 한다")
    void memberUsernameExists() throws Exception {
        String targetUsername = "yoon1234";

        willDoNothing().given(memberService).memberUsernameExists(anyString());

        mockMvc.perform(get("/api/members/username/" + targetUsername))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"))
                .andDo(print());
    }

    @Test
    @DisplayName("아이디 중복 시 예외가 발생한다")
    void memberUsernameExists_duplicateUsername() throws Exception {
        String targetUsername = "yoon1234";

        willThrow(new DuplicateUsernameException()).given(memberService).memberUsernameExists(anyString());

        mockMvc.perform(get("/api/members/username/" + targetUsername))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("E409002"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("사용 중인 아이디입니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("회원가입을 한다")
    void memberSignup() throws Exception {
        MemberSignupRequest memberSignupRequest = new MemberSignupRequest("yoonkun", "yoon1234", "12345678", "12345678");

        willDoNothing().given(memberService).memberSignup(any(MemberSignupRequest.class));

        mockMvc.perform(post("/api/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberSignupRequest))
                )
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("회원가입 시 입력값이 잘못되면 예외가 발생한다")
    void memberSignup_invalidInputValue() throws Exception {
        MemberSignupRequest invalidMemberSignupRequest = new MemberSignupRequest("", "", "12345678", "12345678");

        willDoNothing().given(memberService).memberSignup(any(MemberSignupRequest.class));

        mockMvc.perform(post("/api/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidMemberSignupRequest))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("E400001"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("입력값이 잘못되었습니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("회원가입 시 비밀번호가 일치하지 않으면 예외가 발생한다")
    void memberSignup_passwordMismatch() throws Exception {
        MemberSignupRequest memberSignupRequest = new MemberSignupRequest("yoonkun", "yoon1234", "12345678", "12345679");

        willThrow(new PasswordMismatchException()).given(memberService).memberSignup(any(MemberSignupRequest.class));

        mockMvc.perform(post("/api/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberSignupRequest))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("E400002"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("비밀번호가 일치하지 않습니다."))
                .andDo(print());
    }

}