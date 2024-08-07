package com.board.domain.member.dto;

import com.board.domain.member.validator.annotation.Nickname;
import com.board.domain.member.validator.annotation.Password;
import com.board.domain.member.validator.annotation.Username;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberSignupRequest {

    @Nickname
    private String nickname;

    @Username
    private String username;

    @Password
    private String password;

    @Password(requiredMessage = "비밀번호 확인을 입력해 주세요.")
    private String passwordConfirm;

}
