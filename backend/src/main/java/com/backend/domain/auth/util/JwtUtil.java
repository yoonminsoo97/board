package com.backend.domain.auth.util;

import com.backend.domain.member.entity.Member;
import com.backend.global.error.exception.ErrorType;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long accessTokenExpire;
    private final long refreshTokenExpire;

    public JwtUtil(@Value("${jwt.secret-key}") String secretKey,
                   @Value("${jwt.access-token.expire}") long accessTokenExpire,
                   @Value("${jwt.refresh-token.expire}") long refreshTokenExpire) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpire = accessTokenExpire;
        this.refreshTokenExpire = refreshTokenExpire;
    }

    public String createAccesStoken(Member member) {
        Date iat = new Date();
        Date exp = new Date(iat.getTime() + accessTokenExpire);
        return Jwts.builder()
                .issuedAt(iat)
                .expiration(exp)
                .claim("username", member.getUsername())
                .claim("authority", member.getRole().getAuthority())
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public String createRefreshToken() {
        Date iat = new Date();
        Date exp = new Date(iat.getTime() + refreshTokenExpire);
        return Jwts.builder()
                .issuedAt(iat)
                .expiration(exp)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token).getPayload();
    }

    public void validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
        } catch (ExpiredJwtException ex) {
            throw new AuthenticationServiceException(ErrorType.EXPIRED_TOKEN.getErrorCode());
        } catch (JwtException e) {
            throw new AuthenticationServiceException(ErrorType.INVALID_TOKEN.getErrorCode());
        }
    }

}
