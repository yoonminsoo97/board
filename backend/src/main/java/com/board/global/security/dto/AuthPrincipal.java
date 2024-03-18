package com.board.global.security.dto;

import com.board.domain.member.entity.Member;

import lombok.Getter;

import org.springframework.security.core.userdetails.User;

import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;

@Getter
public class AuthPrincipal extends User {

    private final Member member;

    public AuthPrincipal(Member member) {
        super(member.getUsername(), member.getPassword(), createAuthorityList(member.getRole().getAuthority()));
        this.member = member;
    }

}
