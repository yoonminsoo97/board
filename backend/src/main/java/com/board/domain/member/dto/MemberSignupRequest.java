package com.board.domain.member.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberSignupRequest {

    @NotBlank(message = "닉네임을 입력해 주세요.")
    private String nickname;

    @NotBlank(message = "아이디를 입력해 주세요.")
    private String username;

    @NotBlank(message = "비밀번호를 입력해 주세요.")
    private String password;

    @NotBlank(message = "비밀번호를 한 번 더 입력해 주세요.")
    private String passwordConfirm;

}
