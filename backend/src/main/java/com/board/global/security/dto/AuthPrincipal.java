package com.board.global.security.dto;

import com.board.domain.member.entity.Member;

import org.springframework.security.core.userdetails.User;

import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;

public class AuthPrincipal extends User {

    private final Member member;

    public AuthPrincipal(Member member) {
        super(member.getUsername(), member.getPassword(), createAuthorityList(member.getRole().authority()));
        this.member = member;
    }

    public Member getMember() {
        return member;
    }

}
