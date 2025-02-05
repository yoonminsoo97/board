package com.board.domain.member.dto;

import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberSignupRequest {

    @NotBlank(message = "닉네임을 입력해 주세요.")
    @Pattern(regexp = "(^$)|^[a-zA-Z가-힣0-9]{6,10}$", message = "닉네임은 6~10자의 영문 대소문자, 한글, 숫자만 사용할 수 있습니다.")
    private String nickname;

    @NotBlank(message = "아이디를 입력해 주세요.")
    @Pattern(regexp = "(^$)|^[a-z0-9]{8,16}$", message = "아이디는 8~16자의 영문 소문자, 숫자만 사용할 수 있습니다.")
    private String username;

    @NotBlank(message = "비밀번호를 입력해 주세요.")
    @Pattern(regexp = "(^$)|^[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};:|./?]{8,16}$", message = "비밀번호는 8~16자의 영문 대소문자, 숫자, 특수문자만 사용할 수 있습니다.")
    private String password;

}
