package com.board.domain.member.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberNicknameRequest {

    @NotBlank(message = "닉네임을 입력해 주세요.")
    private String nickname;

}
