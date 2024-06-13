package com.board.domain.member.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberNicknameRequest {

    @NotBlank(message = "닉네임을 입력해 주세요.")
    private String nickname;

}
