package com.board.domain.token.support;

import com.board.domain.member.entity.Member;
import com.board.domain.member.repository.MemberRepository;
import com.board.global.security.dto.LoginMember;
import com.board.global.security.exception.ExpiredTokenException;
import com.board.global.security.exception.InvalidTokenException;
import com.board.global.security.support.JwtManager;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest
class JwtManagerTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JwtManager jwtManager;

    @Value("${jwt.secret-key}")
    private String secretKey;

    private LoginMember loginMember;

    @BeforeEach
    void setUp() {
        Member member = Member.builder()
                .nickname("yoonkun")
                .username("yoon1234")
                .password(new BCryptPasswordEncoder().encode("12345678"))
                .build();
        memberRepository.save(member);
        loginMember = new LoginMember(member);
    }

    @Nested
    @DisplayName("토큰 생성")
    class CreateTokenTest {

        @Test
        @DisplayName("액세스 토큰을 생성한다")
        void createAccessToken() {
            String accessToken = jwtManager.createAccessToken(loginMember);

            assertThat(accessToken).isNotNull();
        }

        @Test
        @DisplayName("리프레시 토큰을 생성한다")
        void createRefreshToken() {
            String refreshToken = jwtManager.createRefreshToken();

            assertThat(refreshToken).isNotNull();
        }

    }

    @Nested
    @DisplayName("페이로드 조회")
    class TokenPayloadTest {

        @Test
        @DisplayName("액세스 토큰에서 페이로드를 조회한다")
        void getPayloadFromAccessToken() {
            String accessToken = jwtManager.createAccessToken(loginMember);

            Claims payload = jwtManager.getPayload(accessToken);

            assertThat(Long.valueOf(payload.getSubject())).isEqualTo(loginMember.getMemberId());
            assertThat(payload.get("nickname", String.class)).isEqualTo("yoonkun");
            assertThat(payload.get("authority", String.class)).isEqualTo("ROLE_MEMBER");
        }

        @Test
        @DisplayName("Null인 토큰에서 페이로드를 조회하면 예외가 발생한다")
        void getPayloadFromNullToken() {
            assertThatThrownBy(() -> jwtManager.getPayload(null))
                    .isInstanceOf(InvalidTokenException.class);
        }

        @Test
        @DisplayName("유효하지 토큰에서 페이로드를 조회하면 예외가 발생한다")
        void getPayloadFromInvalidToken() {
            assertThatThrownBy(() -> jwtManager.getPayload("invalidAccessToken"))
                    .isInstanceOf(InvalidTokenException.class);
        }

        @Test
        @DisplayName("만료된 토큰에서 페이로드를 조회하면 예외가 발생한다")
        void getPayloadFromExpiredToken() {
            String expiredToken = createExpiredToken(loginMember);

            assertThatThrownBy(() -> jwtManager.getPayload(expiredToken))
                    .isInstanceOf(ExpiredTokenException.class);
        }

        @Test
        @DisplayName("잘못된 시크릿 키를 가진 토큰에서 페이로드를 조회하면 예외가 발생한다")
        void getPayloadWrongSecretKeyToken() {
            String wrongSecretKeyToken = createWrongSecretKeyToken(loginMember);

            assertThatThrownBy(() -> jwtManager.getPayload(wrongSecretKeyToken))
                    .isInstanceOf(InvalidTokenException.class);
        }

        private String createExpiredToken(LoginMember loginMember) {
            Date iat = new Date();
            Date exp = new Date(iat.getTime() - 1);
            return Jwts.builder()
                    .subject(String.valueOf(loginMember.getMemberId()))
                    .claim("nickname", loginMember.getNickname())
                    .claim("authority", loginMember.getAuthority())
                    .issuedAt(iat)
                    .expiration(exp)
                    .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
                    .compact();
        }

        private String createWrongSecretKeyToken(LoginMember loginMember) {
            String wrongSecretKey = "wrongSecretKeywrongSecretKeywrongSecretKey";
            Date iat = new Date();
            Date exp = new Date(iat.getTime() + 300000);
            return Jwts.builder()
                    .subject(String.valueOf(loginMember.getMemberId()))
                    .claim("nickname", loginMember.getNickname())
                    .claim("authority", loginMember.getAuthority())
                    .issuedAt(iat)
                    .expiration(exp)
                    .signWith(Keys.hmacShaKeyFor(wrongSecretKey.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
                    .compact();
        }

    }

}