package com.board.global.security.dto;

import com.board.domain.member.entity.Member;

import lombok.Getter;

import org.springframework.security.core.userdetails.User;

import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;

@Getter
public class LoginMember extends User {

    private final Member member;

    public LoginMember(Member member) {
        super(member.getUsername(), member.getPassword(), createAuthorityList(member.getAuthority()));
        this.member = member;
    }

    public Long getMemberId() {
        return member.getId();
    }

    public String getNickname() {
        return member.getNickname();
    }

    public String getAuthority() {
        return member.getAuthority();
    }

}
