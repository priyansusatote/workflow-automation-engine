package com.priyansu.workflow.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;

    public void blacklist(String jti, long expiryMillis) {
        redisTemplate.opsForValue().set(jti, "blacklisted", Duration.ofMillis(expiryMillis));
    }

    public boolean isBlacklisted(String jti) {
        return redisTemplate.hasKey(jti);
    }
}