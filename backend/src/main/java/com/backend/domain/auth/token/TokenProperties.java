package com.backend.domain.auth.token;

import lombok.Getter;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
@Getter
public class TokenProperties {

    private final String secretKey;
    private final long accessTokenExpire;
    private final long refreshTokenExpire;

    public TokenProperties(String secretKey, long accessTokenExpire, long refreshTokenExpire) {
        this.secretKey = secretKey;
        this.accessTokenExpire = accessTokenExpire;
        this.refreshTokenExpire = refreshTokenExpire;
    }

}
