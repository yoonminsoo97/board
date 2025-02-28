package com.backend.domain.auth.util;

import com.backend.domain.member.entity.Member;

import com.backend.global.error.exception.ErrorType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationServiceException;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${jwt.secret-key}")
    private String secretKey;

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

    @DisplayName("access token에서 claims을 추출한다.")
    @Test
    void extractClaims() {
        Member member = Member.builder()
                .nickname("yoonkun")
                .username("yoon1234")
                .password("12345678")
                .build();
        String accesStoken = jwtUtil.createAccesStoken(member);

        Claims claims = jwtUtil.extractClaims(accesStoken);

        assertThat(claims.get("username", String.class)).isEqualTo("yoon1234");
        assertThat(claims.get("authority", String.class)).isEqualTo("ROLE_MEMBER");
    }

    @DisplayName("토큰이 만료되면 예외가 발생한다.")
    @Test
    void expiredToken() {
        Date iat = new Date();
        Date exp = new Date(iat.getTime() - 1);
        String expiredToken = Jwts.builder()
                .issuedAt(iat)
                .expiration(exp)
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
                .compact();

        assertThatThrownBy(() -> jwtUtil.validateToken(expiredToken))
                .isInstanceOf(AuthenticationServiceException.class)
                .hasMessage(ErrorType.EXPIRED_TOKEN.getErrorCode());
    }

    @DisplayName("토큰 형식이 잘못되면 예외가 발생한다.")
    @Test
    void invalidToken() {
        assertThatThrownBy(() -> jwtUtil.validateToken("wrong-token"))
                .isInstanceOf(AuthenticationServiceException.class)
                .hasMessage(ErrorType.INVALID_TOKEN.getErrorCode());
    }

}