package com.backend.domain.auth.repository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class TokenRepository {

    private static final String BLACK_LIST_TOKEN_PREFIX = "black:";
    private static final String BLOCKED = "blocked";

    private final StringRedisTemplate stringRedisTemplate;

    public void add(String key, String value, long expire) {
        stringRedisTemplate.opsForValue().set(key, value, expire, TimeUnit.MILLISECONDS);
    }

    public void addBlackList(String token, long expire) {
        stringRedisTemplate.opsForValue().set(BLACK_LIST_TOKEN_PREFIX + token, BLOCKED, expire, TimeUnit.MILLISECONDS);
    }

    public boolean isBlocked(String token) {
        return stringRedisTemplate.hasKey(BLACK_LIST_TOKEN_PREFIX + token);
    }

    public Optional<String> findByUsername(String username) {
        return Optional.of(stringRedisTemplate.opsForValue().get(username));
    }

    public void deleteByUsername(String username) {
        stringRedisTemplate.delete(username);
    }

}
