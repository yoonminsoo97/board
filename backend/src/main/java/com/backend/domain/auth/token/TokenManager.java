package com.backend.domain.auth.token;

import com.backend.domain.auth.exception.ExpiredTokenException;
import com.backend.domain.auth.exception.InvalidTokenException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class TokenManager {

    private final SecretKey secretKey;
    private final long accessTokenExpire;
    private final long refreshTokenExpire;

    public TokenManager(TokenProperties properties) {
        this.secretKey = Keys.hmacShaKeyFor(properties.getSecretKey().getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpire = properties.getAccessTokenExpire();
        this.refreshTokenExpire = properties.getRefreshTokenExpire();
    }

    public String createAccessToken(String username, String authority) {
        Date iat = new Date(System.currentTimeMillis());
        Date exp = new Date(iat.getTime() + accessTokenExpire);
        return Jwts.builder()
                .subject(username)
                .claim("authority", authority)
                .issuedAt(iat)
                .expiration(exp)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public String createRefreshToken(String username) {
        Date iat = new Date(System.currentTimeMillis());
        Date exp = new Date(iat.getTime() + refreshTokenExpire);
        return Jwts.builder()
                .subject(username)
                .issuedAt(iat)
                .expiration(exp)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public long getAccessTokenExpire() {
        return this.accessTokenExpire;
    }

    public long getRefreshTokenExpire() {
        return this.refreshTokenExpire;
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public void validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
        } catch (ExpiredJwtException ex) {
            throw new ExpiredTokenException();
        } catch (JwtException | IllegalArgumentException ex) {
            throw new InvalidTokenException();
        }
    }

}
