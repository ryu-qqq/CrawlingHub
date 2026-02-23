package com.ryuqq.crawlinghub.adapter.in.sqs.scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.application.task.dto.command.TriggerCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.task.dto.messaging.EventBridgeTriggerPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * EventBridgeTriggerListenerMapper 단위 테스트
 *
 * <p>EventBridgeTriggerPayload → TriggerCrawlTaskCommand 변환을 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@DisplayName("EventBridgeTriggerListenerMapper 단위 테스트")
class EventBridgeTriggerListenerMapperTest {

    private EventBridgeTriggerListenerMapper sut;

    @BeforeEach
    void setUp() {
        sut = new EventBridgeTriggerListenerMapper();
    }

    @Nested
    @DisplayName("toCommand 메서드 테스트")
    class ToCommandTest {

        @Test
        @DisplayName("[성공] payload의 schedulerId가 TriggerCrawlTaskCommand에 올바르게 매핑된다")
        void shouldMapSchedulerIdToCommand() {
            // Given
            EventBridgeTriggerPayload payload =
                    new EventBridgeTriggerPayload(
                            42L, 100L, "crawl-scheduler-v1", "2026-02-23T10:00:00Z");

            // When
            TriggerCrawlTaskCommand command = sut.toCommand(payload);

            // Then
            assertThat(command.crawlSchedulerId()).isEqualTo(42L);
        }

        @Test
        @DisplayName(
                "[성공] sellerId, schedulerName, triggerTime은 Command에 포함되지 않는다 (schedulerId만 전달)")
        void shouldOnlyMapSchedulerIdNotOtherFields() {
            // Given
            EventBridgeTriggerPayload payload =
                    new EventBridgeTriggerPayload(
                            99L, 999L, "my-scheduler", "2026-02-23T12:00:00Z");

            // When
            TriggerCrawlTaskCommand command = sut.toCommand(payload);

            // Then: TriggerCrawlTaskCommand는 schedulerId(crawlSchedulerId)만 가짐
            assertThat(command.crawlSchedulerId()).isEqualTo(99L);
        }

        @Test
        @DisplayName("[성공] 동일 payload로 여러 번 변환해도 같은 결과를 반환한다 (멱등성)")
        void shouldReturnSameResultForSamePayload() {
            // Given
            EventBridgeTriggerPayload payload =
                    new EventBridgeTriggerPayload(
                            7L, 77L, "repeated-scheduler", "2026-02-23T00:00:00Z");

            // When
            TriggerCrawlTaskCommand command1 = sut.toCommand(payload);
            TriggerCrawlTaskCommand command2 = sut.toCommand(payload);

            // Then
            assertThat(command1.crawlSchedulerId()).isEqualTo(command2.crawlSchedulerId());
        }
    }
}
