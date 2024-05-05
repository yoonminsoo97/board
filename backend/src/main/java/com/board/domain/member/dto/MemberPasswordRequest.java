package com.board.domain.member.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberPasswordRequest {

    @NotBlank(message = "현재 비밀번호를 입력해 주세요.")
    private String curPassword;

    @NotBlank(message = "새로운 비밀번호를 입력해 주세요.")
    private String newPassword;

    @NotBlank(message = "새로운 비밀번호를 한 번 더 입력해 주세요.")
    private String newPasswordConfirm;

}
