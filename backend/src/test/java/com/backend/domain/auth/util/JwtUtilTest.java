package com.backend.domain.auth.util;

import com.backend.domain.member.entity.Member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    @DisplayName("access token을 생성한다.")
    @Test
    void createAccessToken() {
        Member member = Member.builder()
                .nickname("yoonkun")
                .username("yoon1234")
                .password("12345678")
                .build();

        String accesStoken = jwtUtil.createAccesStoken(member);

        assertThat(accesStoken).isNotNull();
    }

    @DisplayName("refresh token을 생성한다.")
    @Test
    void createRefreshToken() {
        String refreshToken = jwtUtil.createRefreshToken();

        assertThat(refreshToken).isNotNull();
    }

}