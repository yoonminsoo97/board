package com.board.domain.token.util;

import com.board.global.error.exception.ErrorType;

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

    public String createAccessToken(String username, String authority) {
        Date iat = new Date();
        Date exp = new Date(iat.getTime() + accessTokenExpire);
        return Jwts.builder()
                .subject(username)
                .claim("authority", authority)
                .signWith(secretKey, Jwts.SIG.HS256)
                .issuedAt(iat)
                .expiration(exp)
                .compact();
    }

    public String createRefreshToken() {
        Date iat = new Date();
        Date exp = new Date(iat.getTime() + refreshTokenExpire);
        return Jwts.builder()
                .signWith(secretKey, Jwts.SIG.HS256)
                .issuedAt(iat)
                .expiration(exp)
                .compact();
    }

    public Claims getClaims(String token) {
        return extractClaims(token);
    }

    private Claims extractClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException ex) {
            throw new AuthenticationServiceException(ErrorType.EXPIRED_TOKEN.getErrorCode());
        } catch (IllegalArgumentException | JwtException ex) {
            throw new AuthenticationServiceException(ErrorType.INVALID_TOKEN.getErrorCode());
        }
    }

}
