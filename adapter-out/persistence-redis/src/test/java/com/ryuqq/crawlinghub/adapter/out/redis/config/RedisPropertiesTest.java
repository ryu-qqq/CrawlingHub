package com.ryuqq.crawlinghub.adapter.out.redis.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * RedisProperties 단위 테스트
 *
 * <p>Redis 분산 락 설정 Properties의 기본값과 동작을 검증합니다.
 *
 * <p>테스트 범위:
 *
 * <ul>
 *   <li>기본값 검증 - defaultWaitTimeMs, defaultLeaseTimeMs, keyPrefix
 *   <li>CrawlTriggerLock 설정
 *   <li>CrawlTaskLock 설정
 *   <li>Setter/Getter 동작
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("redis")
@Tag("config")
@DisplayName("RedisProperties 단위 테스트")
class RedisPropertiesTest {

    private RedisProperties properties;

    @BeforeEach
    void setUp() {
        properties = new RedisProperties();
    }

    @Nested
    @DisplayName("기본값 검증")
    class DefaultValueTests {

        @Test
        @DisplayName("성공 - 기본 대기 시간 5000ms")
        void shouldHaveDefaultWaitTimeOf5000ms() {
            assertThat(properties.getDefaultWaitTimeMs()).isEqualTo(5000L);
        }

        @Test
        @DisplayName("성공 - 기본 유지 시간 30000ms")
        void shouldHaveDefaultLeaseTimeOf30000ms() {
            assertThat(properties.getDefaultLeaseTimeMs()).isEqualTo(30000L);
        }

        @Test
        @DisplayName("성공 - 기본 키 접두사 'crawlinghub:lock:'")
        void shouldHaveDefaultKeyPrefix() {
            assertThat(properties.getKeyPrefix()).isEqualTo("crawlinghub:lock:");
        }
    }

    @Nested
    @DisplayName("CrawlTriggerLock 설정")
    class CrawlTriggerLockTests {

        @Test
        @DisplayName("성공 - CrawlTrigger 기본 대기 시간 3000ms")
        void shouldHaveCrawlTriggerDefaultWaitTime() {
            assertThat(properties.getCrawlTrigger().getWaitTimeMs()).isEqualTo(3000L);
        }

        @Test
        @DisplayName("성공 - CrawlTrigger 기본 유지 시간 60000ms")
        void shouldHaveCrawlTriggerDefaultLeaseTime() {
            assertThat(properties.getCrawlTrigger().getLeaseTimeMs()).isEqualTo(60000L);
        }

        @Test
        @DisplayName("성공 - CrawlTrigger 설정 변경")
        void shouldAllowCrawlTriggerSettingsChange() {
            // Given
            RedisProperties.CrawlTriggerLock triggerLock = new RedisProperties.CrawlTriggerLock();
            triggerLock.setWaitTimeMs(10000L);
            triggerLock.setLeaseTimeMs(120000L);

            // When
            properties.setCrawlTrigger(triggerLock);

            // Then
            assertThat(properties.getCrawlTrigger().getWaitTimeMs()).isEqualTo(10000L);
            assertThat(properties.getCrawlTrigger().getLeaseTimeMs()).isEqualTo(120000L);
        }
    }

    @Nested
    @DisplayName("CrawlTaskLock 설정")
    class CrawlTaskLockTests {

        @Test
        @DisplayName("성공 - CrawlTask 기본 대기 시간 5000ms")
        void shouldHaveCrawlTaskDefaultWaitTime() {
            assertThat(properties.getCrawlTask().getWaitTimeMs()).isEqualTo(5000L);
        }

        @Test
        @DisplayName("성공 - CrawlTask 기본 유지 시간 300000ms (5분)")
        void shouldHaveCrawlTaskDefaultLeaseTime() {
            assertThat(properties.getCrawlTask().getLeaseTimeMs()).isEqualTo(300000L);
        }

        @Test
        @DisplayName("성공 - CrawlTask 설정 변경")
        void shouldAllowCrawlTaskSettingsChange() {
            // Given
            RedisProperties.CrawlTaskLock taskLock = new RedisProperties.CrawlTaskLock();
            taskLock.setWaitTimeMs(8000L);
            taskLock.setLeaseTimeMs(600000L);

            // When
            properties.setCrawlTask(taskLock);

            // Then
            assertThat(properties.getCrawlTask().getWaitTimeMs()).isEqualTo(8000L);
            assertThat(properties.getCrawlTask().getLeaseTimeMs()).isEqualTo(600000L);
        }
    }

    @Nested
    @DisplayName("Setter/Getter 동작")
    class SetterGetterTests {

        @Test
        @DisplayName("성공 - defaultWaitTimeMs 설정 변경")
        void shouldChangeDefaultWaitTime() {
            // When
            properties.setDefaultWaitTimeMs(10000L);

            // Then
            assertThat(properties.getDefaultWaitTimeMs()).isEqualTo(10000L);
        }

        @Test
        @DisplayName("성공 - defaultLeaseTimeMs 설정 변경")
        void shouldChangeDefaultLeaseTime() {
            // When
            properties.setDefaultLeaseTimeMs(60000L);

            // Then
            assertThat(properties.getDefaultLeaseTimeMs()).isEqualTo(60000L);
        }

        @Test
        @DisplayName("성공 - keyPrefix 설정 변경")
        void shouldChangeKeyPrefix() {
            // When
            properties.setKeyPrefix("custom:prefix:");

            // Then
            assertThat(properties.getKeyPrefix()).isEqualTo("custom:prefix:");
        }
    }
}
