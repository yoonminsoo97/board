package com.board.domain.member.dto;

import com.board.domain.member.validator.annotation.Nickname;

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

    @Nickname
    private String nickname;

}
