package com.board.domain.member.dto;

import com.board.domain.member.entity.Member;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberProfileResponse {

    private String nickname;
    private String username;

    public static MemberProfileResponse of(Member member) {
        return MemberProfileResponse.builder()
                .nickname(member.getNickname())
                .username(member.getUsername())
                .build();
    }

}
