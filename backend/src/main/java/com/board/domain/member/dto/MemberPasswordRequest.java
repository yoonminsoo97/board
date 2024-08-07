package com.board.domain.member.dto;

import com.board.domain.member.validator.annotation.Password;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberPasswordRequest {

    @Password(requiredMessage = "현재 비밀번호를 입력해 주세요.")
    private String curPassword;

    @Password(requiredMessage = "새로운 비밀번호를 입력해 주세요.")
    private String newPassword;

    @Password(requiredMessage = "새로운 비밀번호 확인을 입력해 주세요.")
    private String newPasswordConfirm;

}
