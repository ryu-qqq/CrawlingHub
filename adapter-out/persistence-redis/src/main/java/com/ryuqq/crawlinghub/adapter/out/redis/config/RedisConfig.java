package com.ryuqq.crawlinghub.adapter.out.redis.config;

import org.redisson.config.Config;
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
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
 * <p><strong>RejectedExecutionException 방지</strong>:
 *
 * <ul>
 *   <li>keepAlive: TCP keepAlive로 연결 유지
 *   <li>pingConnectionInterval: 주기적 ping으로 유휴 연결 감지
 *   <li>retryAttempts/retryInterval: 일시적 연결 실패 시 재시도
 *   <li>connectionMinimumIdleSize: 최소 유휴 연결 유지
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(RedisProperties.class)
public class RedisConfig {

    /**
     * Redisson 자동 설정 커스터마이저
     *
     * <p>Spring Boot Starter의 자동 설정(host, port 등)을 유지하면서 추가 설정을 적용합니다.
     *
     * @return RedissonAutoConfigurationCustomizer
     */
    @Bean
    RedissonAutoConfigurationCustomizer redissonCustomizer() {
        return (Config config) -> {
            config.useSingleServer()
                    .setKeepAlive(true)
                    .setPingConnectionInterval(30_000)
                    .setConnectionMinimumIdleSize(4)
                    .setConnectionPoolSize(16)
                    .setRetryAttempts(3)
                    .setRetryInterval(1500);
        };
    }
}
