package com.ryuqq.crawlinghub.domain.aggregate;

import com.ryuqq.crawlinghub.domain.crawler.aggregate.outbox.SchedulerOutbox;
import com.ryuqq.crawlinghub.domain.crawler.vo.ScheduleId;
import com.ryuqq.crawlinghub.domain.crawler.vo.SchedulerOutboxEventType;
import com.ryuqq.crawlinghub.domain.crawler.vo.SchedulerOutboxStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * SchedulerOutbox Aggregate Root 테스트
 *
 * <p><strong>테스트 범위:</strong></p>
 * <ul>
 *   <li>✅ SchedulerOutbox 생성 (WAITING 상태)</li>
 *   <li>✅ Payload JSON 형식 검증</li>
 *   <li>✅ 초기 retryCount = 0</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
class SchedulerOutboxTest {

    @Test
    void shouldCreateSchedulerOutboxWithWaitingStatus() {
        // Given
        ScheduleId scheduleId = ScheduleId.generate();
        SchedulerOutboxEventType eventType = SchedulerOutboxEventType.SCHEDULE_REGISTERED;
        String payload = "{\"ruleName\":\"mustit-crawler-seller_12345\",\"scheduleExpression\":\"rate(1 day)\"}";

        // When
        SchedulerOutbox outbox = SchedulerOutbox.create(scheduleId, eventType, payload);

        // Then
        assertThat(outbox.getOutboxId()).isNotNull();
        assertThat(outbox.getScheduleId()).isEqualTo(scheduleId);
        assertThat(outbox.getEventType()).isEqualTo(eventType);
        assertThat(outbox.getPayload()).isEqualTo(payload);
        assertThat(outbox.getStatus()).isEqualTo(SchedulerOutboxStatus.WAITING);
        assertThat(outbox.getRetryCount()).isEqualTo(0);
    }

    @Test
    void shouldValidatePayloadFormat() {
        // Given
        ScheduleId scheduleId = ScheduleId.generate();
        SchedulerOutboxEventType eventType = SchedulerOutboxEventType.SCHEDULE_REGISTERED;

        // When & Then
        assertThatThrownBy(() -> SchedulerOutbox.create(scheduleId, eventType, "invalid-json"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Payload는 유효한 JSON 형식이어야 합니다");
    }
}
