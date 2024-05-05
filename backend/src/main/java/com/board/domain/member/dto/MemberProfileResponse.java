package com.board.domain.member.dto;

import com.board.domain.member.entity.Member;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberProfileResponse {

    private final String nickname;
    private final String username;

    public MemberProfileResponse(Member member) {
        this.nickname = member.getNickname();
        this.username = member.getUsername();
    }

}
