package com.board.domain.member.entity;

public enum Role {

    MEMBER("ROLE_MEMBER");

    private final String authority;

    Role(String authority) {
        this.authority = authority;
    }

    public String authority() {
        return authority;
    }

}
