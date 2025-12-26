package com.ryuqq.crawlinghub.application.common.component.lock;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * LockType 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("LockType 테스트")
class LockTypeTest {

    @Nested
    @DisplayName("CRAWL_TRIGGER 테스트")
    class CrawlTrigger {

        @Test
        @DisplayName("[성공] 키 프리픽스 확인")
        void shouldReturnCorrectKeyPrefix() {
            // Then
            assertThat(LockType.CRAWL_TRIGGER.getKeyPrefix()).isEqualTo("trigger:");
        }

        @Test
        @DisplayName("[성공] 기본 대기 시간 0ms")
        void shouldReturnZeroWaitTime() {
            // Then
            assertThat(LockType.CRAWL_TRIGGER.getDefaultWaitTimeMs()).isZero();
        }

        @Test
        @DisplayName("[성공] 기본 유지 시간 60초")
        void shouldReturnDefaultLeaseTime() {
            // Then
            assertThat(LockType.CRAWL_TRIGGER.getDefaultLeaseTimeMs()).isEqualTo(60000L);
        }

        @Test
        @DisplayName("[성공] 락 키 생성")
        void shouldBuildKeyWithIdentifier() {
            // Given
            Long schedulerId = 123L;

            // When
            String lockKey = LockType.CRAWL_TRIGGER.buildKey(schedulerId);

            // Then
            assertThat(lockKey).isEqualTo("trigger:123");
        }
    }

    @Nested
    @DisplayName("CRAWL_TASK 테스트")
    class CrawlTask {

        @Test
        @DisplayName("[성공] 키 프리픽스 확인")
        void shouldReturnCorrectKeyPrefix() {
            // Then
            assertThat(LockType.CRAWL_TASK.getKeyPrefix()).isEqualTo("task:");
        }

        @Test
        @DisplayName("[성공] 기본 대기 시간 0ms")
        void shouldReturnZeroWaitTime() {
            // Then
            assertThat(LockType.CRAWL_TASK.getDefaultWaitTimeMs()).isZero();
        }

        @Test
        @DisplayName("[성공] 기본 유지 시간 60초")
        void shouldReturnDefaultLeaseTime() {
            // Then
            assertThat(LockType.CRAWL_TASK.getDefaultLeaseTimeMs()).isEqualTo(60000L);
        }

        @Test
        @DisplayName("[성공] 락 키 생성")
        void shouldBuildKeyWithIdentifier() {
            // Given
            Long taskId = 456L;

            // When
            String lockKey = LockType.CRAWL_TASK.buildKey(taskId);

            // Then
            assertThat(lockKey).isEqualTo("task:456");
        }

        @Test
        @DisplayName("[성공] 문자열 식별자로 락 키 생성")
        void shouldBuildKeyWithStringIdentifier() {
            // Given
            String taskId = "abc-123";

            // When
            String lockKey = LockType.CRAWL_TASK.buildKey(taskId);

            // Then
            assertThat(lockKey).isEqualTo("task:abc-123");
        }
    }
}
