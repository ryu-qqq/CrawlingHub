package com.ryuqq.crawlinghub.application.task.dto.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * EventBridgeTriggerPayload 단위 테스트
 *
 * <p>EventBridge 트리거 SQS 메시지 페이로드 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("EventBridgeTriggerPayload 테스트")
class EventBridgeTriggerPayloadTest {

    @Nested
    @DisplayName("레코드 필드 테스트")
    class RecordFields {

        @Test
        @DisplayName("[성공] 모든 필드 접근 가능")
        void shouldAccessAllFields() {
            // Given
            EventBridgeTriggerPayload payload =
                    new EventBridgeTriggerPayload(1L, 2L, "test-scheduler", "2024-01-01T00:00:00Z");

            // Then
            assertThat(payload.schedulerId()).isEqualTo(1L);
            assertThat(payload.sellerId()).isEqualTo(2L);
            assertThat(payload.schedulerName()).isEqualTo("test-scheduler");
            assertThat(payload.triggerTime()).isEqualTo("2024-01-01T00:00:00Z");
        }

        @Test
        @DisplayName("[성공] null 필드 허용")
        void shouldAllowNullFields() {
            // Given
            EventBridgeTriggerPayload payload = new EventBridgeTriggerPayload(1L, 2L, null, null);

            // Then
            assertThat(payload.schedulerName()).isNull();
            assertThat(payload.triggerTime()).isNull();
        }
    }

    @Nested
    @DisplayName("레코드 동등성 테스트")
    class Equality {

        @Test
        @DisplayName("[성공] 같은 필드를 가진 두 레코드는 동등")
        void shouldBeEqualWhenSameFields() {
            // Given
            EventBridgeTriggerPayload payload1 =
                    new EventBridgeTriggerPayload(1L, 2L, "scheduler", "2024-01-01T00:00:00Z");
            EventBridgeTriggerPayload payload2 =
                    new EventBridgeTriggerPayload(1L, 2L, "scheduler", "2024-01-01T00:00:00Z");

            // Then
            assertThat(payload1).isEqualTo(payload2);
        }
    }
}
