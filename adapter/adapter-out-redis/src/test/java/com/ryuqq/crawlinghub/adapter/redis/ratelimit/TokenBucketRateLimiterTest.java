package com.ryuqq.crawlinghub.adapter.redis.ratelimit;

import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@DisplayName("TokenBucketRateLimiter 테스트")
class TokenBucketRateLimiterTest {

    @Container
    static RedisContainer redis = new RedisContainer(DockerImageName.parse("redis:7.0-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @Autowired
    private TokenBucketRateLimiter rateLimiter;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @BeforeEach
    void setUp() {
        // Redis 초기화
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Test
    @DisplayName("첫 요청은 항상 허용된다")
    void firstRequestIsAlwaysAllowed() {
        // Given
        Long userAgentId = 1L;

        // When
        TokenBucketRateLimiter.RateLimitResult result = rateLimiter.tryConsumeDefault(userAgentId);

        // Then
        assertThat(result.isAllowed()).isTrue();
        assertThat(result.getCurrentTokens()).isLessThan(80.0);
        assertThat(result.getRetryAfterMs()).isEqualTo(0);
    }

    @Test
    @DisplayName("토큰이 충분하면 요청이 허용된다")
    void requestAllowedWhenTokensAvailable() {
        // Given
        Long userAgentId = 2L;
        int maxTokens = 10;
        double refillRate = 1.0; // 1 token/sec
        int tokensToConsume = 5;

        // When
        TokenBucketRateLimiter.RateLimitResult result =
                rateLimiter.tryConsume(userAgentId, tokensToConsume, refillRate, maxTokens);

        // Then
        assertThat(result.isAllowed()).isTrue();
        assertThat(result.getCurrentTokens()).isEqualTo(5.0);
    }

    @Test
    @DisplayName("토큰이 부족하면 요청이 거부된다")
    void requestDeniedWhenTokensInsufficient() {
        // Given
        Long userAgentId = 3L;
        int maxTokens = 10;
        double refillRate = 1.0;

        // 10개 토큰 모두 소비
        rateLimiter.tryConsume(userAgentId, 10, refillRate, maxTokens);

        // When - 추가 요청
        TokenBucketRateLimiter.RateLimitResult result =
                rateLimiter.tryConsume(userAgentId, 5, refillRate, maxTokens);

        // Then
        assertThat(result.isAllowed()).isFalse();
        assertThat(result.getCurrentTokens()).isEqualTo(0.0);
        assertThat(result.getRetryAfterMs()).isGreaterThan(0);
    }

    @Test
    @DisplayName("시간 경과 시 토큰이 재충전된다")
    void tokensRefillOverTime() throws InterruptedException {
        // Given
        Long userAgentId = 4L;
        int maxTokens = 10;
        double refillRate = 2.0; // 2 tokens/sec

        // 10개 토큰 모두 소비
        rateLimiter.tryConsume(userAgentId, 10, refillRate, maxTokens);

        // When - 1초 대기 (2 tokens 재충전)
        Thread.sleep(1000);

        TokenBucketRateLimiter.RateLimitResult result =
                rateLimiter.tryConsume(userAgentId, 2, refillRate, maxTokens);

        // Then
        assertThat(result.isAllowed()).isTrue();
        assertThat(result.getCurrentTokens()).isGreaterThanOrEqualTo(0.0);
    }

    @Test
    @DisplayName("최대 토큰 수를 초과하지 않는다")
    void tokensDoNotExceedMaximum() throws InterruptedException {
        // Given
        Long userAgentId = 5L;
        int maxTokens = 10;
        double refillRate = 100.0; // 빠른 재충전

        // When - 충분한 시간 대기
        Thread.sleep(200);

        TokenBucketRateLimiter.RateLimitResult result =
                rateLimiter.tryConsume(userAgentId, 1, refillRate, maxTokens);

        // Then
        assertThat(result.isAllowed()).isTrue();
        assertThat(result.getCurrentTokens()).isLessThanOrEqualTo(10.0);
    }

    @Test
    @DisplayName("Bucket 상태를 조회할 수 있다")
    void canQueryBucketStatus() {
        // Given
        Long userAgentId = 6L;
        int maxTokens = 80;
        double refillRate = 0.1333;

        rateLimiter.tryConsume(userAgentId, 10, refillRate, maxTokens);

        // When
        TokenBucketRateLimiter.BucketStatus status = rateLimiter.getBucketStatus(userAgentId);

        // Then
        assertThat(status).isNotNull();
        assertThat(status.getCurrentTokens()).isEqualTo(70.0);
        assertThat(status.getMaxTokens()).isEqualTo(80);
        assertThat(status.getRefillRate()).isEqualTo(0.1333);
    }

    @Test
    @DisplayName("존재하지 않는 Bucket 조회 시 null 반환")
    void returnsNullForNonExistentBucket() {
        // Given
        Long userAgentId = 999L;

        // When
        TokenBucketRateLimiter.BucketStatus status = rateLimiter.getBucketStatus(userAgentId);

        // Then
        assertThat(status).isNull();
    }

    @Test
    @DisplayName("retry_after_ms가 정확하게 계산된다")
    void retryAfterMsCalculatedCorrectly() {
        // Given
        Long userAgentId = 7L;
        int maxTokens = 10;
        double refillRate = 1.0; // 1 token/sec

        // 모든 토큰 소비
        rateLimiter.tryConsume(userAgentId, 10, refillRate, maxTokens);

        // When - 5개 토큰 필요 (5초 대기 필요)
        TokenBucketRateLimiter.RateLimitResult result =
                rateLimiter.tryConsume(userAgentId, 5, refillRate, maxTokens);

        // Then
        assertThat(result.isAllowed()).isFalse();
        assertThat(result.getRetryAfterMs()).isGreaterThanOrEqualTo(4500); // ~5000ms
        assertThat(result.getRetryAfterMs()).isLessThanOrEqualTo(5500);
    }
}
