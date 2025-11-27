package com.ryuqq.crawlinghub.adapter.out.redis.config;

import org.redisson.api.RedissonClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Redis 설정
 *
 * <p><strong>용도</strong>: Redisson 클라이언트 및 분산 락 설정
 *
 * <p><strong>Redisson Spring Boot Starter 자동 설정</strong>:
 *
 * <ul>
 *   <li>RedissonClient Bean 자동 생성 (application.yml/redis.yml 기반)
 *   <li>Single Server / Cluster / Sentinel 모드 지원
 *   <li>Watchdog 기능으로 락 자동 갱신
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(RedisProperties.class)
public class RedisConfig {

    private final RedissonClient redissonClient;
    private final RedisProperties redisProperties;

    public RedisConfig(RedissonClient redissonClient, RedisProperties redisProperties) {
        this.redissonClient = redissonClient;
        this.redisProperties = redisProperties;
    }

    // Redisson Spring Boot Starter가 RedissonClient Bean을 자동으로 생성
    // 추가 설정이 필요한 경우 여기에 Bean 정의
}
