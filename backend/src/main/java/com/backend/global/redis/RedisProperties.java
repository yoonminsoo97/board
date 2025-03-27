package com.backend.global.redis;

import lombok.Getter;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.data.redis")
@Getter
public class RedisProperties {

    private final String host;
    private final int port;

    public RedisProperties(String host, int port) {
        this.host = host;
        this.port = port;
    }

}
