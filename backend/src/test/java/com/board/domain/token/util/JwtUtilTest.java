package com.board.domain.token.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("Access Token을 생성한다")
    void createAccessToken() {
        String accessToken = jwtUtil.createAccessToken("yoon1234", "yoonKun", "ROLE_MEMBER");

        assertThat(accessToken).isNotNull();
    }

    @Test
    @DisplayName("Refresh Token을 생성한다")
    void createRefreshToken() {
        String refreshToken = jwtUtil.createRefreshToken("yoon1234");

        assertThat(refreshToken).isNotNull();
    }

}