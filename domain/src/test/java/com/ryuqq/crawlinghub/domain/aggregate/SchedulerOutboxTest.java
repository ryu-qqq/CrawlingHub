package com.ryuqq.crawlinghub.domain.aggregate;

import com.ryuqq.crawlinghub.domain.crawler.aggregate.outbox.SchedulerOutbox;
import com.ryuqq.crawlinghub.domain.crawler.vo.ScheduleId;
import com.ryuqq.crawlinghub.domain.crawler.vo.SchedulerOutboxEventType;
import com.ryuqq.crawlinghub.domain.crawler.vo.SchedulerOutboxStatus;
import com.ryuqq.crawlinghub.domain.fixture.SchedulerOutboxFixture;
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
 *   <li>✅ 상태 전환 (WAITING → SENDING → COMPLETED/FAILED)</li>
 *   <li>✅ 실패 시 errorMessage, retryCount 증가</li>
 *   <li>✅ 재시도 로직 (Tell Don't Ask: canRetry, retry)</li>
 *   <li>✅ 최대 재시도 횟수 제한 (MAX_RETRY_COUNT = 5)</li>
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

    @Test
    void shouldSendOutbox() {
        // Given
        SchedulerOutbox outbox = SchedulerOutboxFixture.waitingOutbox();

        // When
        outbox.send();

        // Then
        assertThat(outbox.getStatus()).isEqualTo(SchedulerOutboxStatus.SENDING);
    }

    @Test
    void shouldCompleteOutbox() {
        // Given
        SchedulerOutbox outbox = SchedulerOutboxFixture.sendingOutbox();

        // When
        outbox.complete();

        // Then
        assertThat(outbox.getStatus()).isEqualTo(SchedulerOutboxStatus.COMPLETED);
    }

    @Test
    void shouldFailOutbox() {
        // Given
        SchedulerOutbox outbox = SchedulerOutboxFixture.sendingOutbox();
        String errorMessage = "EventBridge API call failed: InvalidRuleName";

        // When
        outbox.fail(errorMessage);

        // Then
        assertThat(outbox.getStatus()).isEqualTo(SchedulerOutboxStatus.FAILED);
        assertThat(outbox.getErrorMessage()).isEqualTo(errorMessage);
        assertThat(outbox.getRetryCount()).isEqualTo(1);
    }

    @Test
    void shouldAllowRetryWhenCountLessThan5() {
        // Given
        SchedulerOutbox outbox = SchedulerOutboxFixture.failedOutboxWithRetryCount(3);

        // When
        boolean canRetry = outbox.canRetry();

        // Then
        assertThat(canRetry).isTrue();
    }

    @Test
    void shouldNotAllowRetryWhenCountExceeds5() {
        // Given
        SchedulerOutbox outbox = SchedulerOutboxFixture.failedOutboxWithRetryCount(5);

        // When
        boolean canRetry = outbox.canRetry();

        // Then
        assertThat(canRetry).isFalse();
    }

    @Test
    void shouldRetryFailedOutbox() {
        // Given
        SchedulerOutbox outbox = SchedulerOutboxFixture.failedOutboxWithRetryCount(2);

        // When
        outbox.retry();

        // Then
        assertThat(outbox.getStatus()).isEqualTo(SchedulerOutboxStatus.WAITING);
    }
}
