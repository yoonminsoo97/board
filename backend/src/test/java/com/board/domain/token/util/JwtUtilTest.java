package com.board.domain.token.util;

import com.board.domain.token.exception.ExpiredTokenException;
import com.board.domain.token.exception.InvalidTokenException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

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

    @Test
    @DisplayName("Access Token에서 Payload를 조회한다")
    void getPayloadAccessToken() {
        String accessToken = jwtUtil.createAccessToken("yoon1234", "yoonKun", "ROLE_MEMBER");

        Claims payload = jwtUtil.getPayload(accessToken);

        assertThat(payload.getSubject()).isEqualTo("yoon1234");
        assertThat(payload.get("nickname", String.class)).isEqualTo("yoonKun");
        assertThat(payload.get("authority", String.class)).isEqualTo("ROLE_MEMBER");
    }

    @Test
    @DisplayName("Refresh Token에서 Payload를 조회한다")
    void getPayloadRefreshToken() {
        String refreshToken = jwtUtil.createRefreshToken("yoon1234");

        Claims payload = jwtUtil.getPayload(refreshToken);

        assertThat(payload.getSubject()).isEqualTo("yoon1234");
    }

    @Test
    @DisplayName("유효하지 않은 Token에서 Payload를 조회하면 예외가 발생한다")
    void getPayloadToken_invalidToken() {
        assertThatThrownBy(() -> jwtUtil.getPayload(null))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    @DisplayName("만료된 Token에서 Payload를 조회하면 예외가 발생한다")
    void getPayloadToken_expiredToken() {
        Date iat = new Date();
        Date exp = new Date(iat.getTime() - 1);
        String expiredToken = Jwts.builder()
                .subject("yoon1234")
                .issuedAt(iat)
                .expiration(exp)
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
                .compact();

        assertThatThrownBy(() -> jwtUtil.getPayload(expiredToken))
                .isInstanceOf(ExpiredTokenException.class);
    }

    @Test
    @DisplayName("잘못된 시크릿 키를 가진 Token에서 Payload를 조회하면 예외가 발생한다")
    void getPayloadToken_wrongSecretKey() {
        Date iat = new Date();
        Date exp = new Date(iat.getTime() + 360000);
        String wrongSecretKeyToken = Jwts.builder()
                .subject("yoon1234")
                .issuedAt(iat)
                .expiration(exp)
                .signWith(Keys.hmacShaKeyFor("wrongSecretKeywrongSecretKeywrongSecretKey".getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
                .compact();

        assertThatThrownBy(() -> jwtUtil.getPayload(wrongSecretKeyToken))
                .isInstanceOf(InvalidTokenException.class);
    }

}