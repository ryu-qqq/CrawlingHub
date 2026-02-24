package com.ryuqq.crawlinghub.domain.task.aggregate;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.cralwinghub.domain.fixture.common.FixedClock;
import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskIdFixture;
import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskOutboxFixture;
import com.ryuqq.crawlinghub.domain.task.id.CrawlTaskId;
import com.ryuqq.crawlinghub.domain.task.vo.OutboxStatus;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@DisplayName("CrawlTaskOutbox Aggregate 단위 테스트")
class CrawlTaskOutboxTest {

    private static final Instant NOW = FixedClock.aDefaultClock().instant();

    @Nested
    @DisplayName("forNew() 팩토리 메서드 테스트")
    class ForNewTest {

        @Test
        @DisplayName("신규 Outbox를 PENDING 상태로 생성한다")
        void createNewOutbox() {
            // given
            CrawlTaskId crawlTaskId = CrawlTaskIdFixture.anAssignedId();
            String payload = "{\"taskId\": 1, \"sellerId\": 100}";

            // when
            CrawlTaskOutbox outbox = CrawlTaskOutbox.forNew(crawlTaskId, payload, NOW);

            // then
            assertThat(outbox.getCrawlTaskId()).isEqualTo(crawlTaskId);
            assertThat(outbox.getCrawlTaskIdValue()).isEqualTo(1L);
            assertThat(outbox.getPayload()).isEqualTo(payload);
            assertThat(outbox.getStatus()).isEqualTo(OutboxStatus.PENDING);
            assertThat(outbox.getRetryCount()).isEqualTo(0);
            assertThat(outbox.getCreatedAt()).isEqualTo(NOW);
            assertThat(outbox.getProcessedAt()).isNull();
            assertThat(outbox.isPending()).isTrue();
        }

        @Test
        @DisplayName("idempotencyKey는 'outbox-{taskId}' 형식으로 생성된다")
        void idempotencyKeyFormat() {
            // given
            CrawlTaskId crawlTaskId = CrawlTaskId.of(42L);

            // when
            CrawlTaskOutbox outbox = CrawlTaskOutbox.forNew(crawlTaskId, "{}", NOW);

            // then
            assertThat(outbox.getIdempotencyKey()).isEqualTo("outbox-42");
        }
    }

    @Nested
    @DisplayName("reconstitute() 팩토리 메서드 테스트")
    class ReconstituteTest {

        @Test
        @DisplayName("기존 데이터로 Outbox를 복원한다")
        void reconstituteOutbox() {
            // when
            CrawlTaskOutbox outbox = CrawlTaskOutboxFixture.aPendingOutbox();

            // then
            assertThat(outbox.getCrawlTaskId()).isEqualTo(CrawlTaskId.of(1L));
            assertThat(outbox.getIdempotencyKey()).isEqualTo("outbox-1-abcd1234");
            assertThat(outbox.getStatus()).isEqualTo(OutboxStatus.PENDING);
            assertThat(outbox.getRetryCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("상태 전환 메서드 테스트")
    class StateTransitionTest {

        @Test
        @DisplayName("markAsProcessing - PROCESSING 상태로 전환한다")
        void markAsProcessing() {
            // given
            CrawlTaskOutbox outbox = CrawlTaskOutboxFixture.aPendingOutbox();

            // when
            outbox.markAsProcessing(NOW);

            // then
            assertThat(outbox.getStatus()).isEqualTo(OutboxStatus.PROCESSING);
            assertThat(outbox.isProcessing()).isTrue();
            assertThat(outbox.getProcessedAt()).isEqualTo(NOW);
        }

        @Test
        @DisplayName("markAsSent - SENT 상태로 전환한다")
        void markAsSent() {
            // given
            CrawlTaskOutbox outbox = CrawlTaskOutboxFixture.aPendingOutbox();

            // when
            outbox.markAsSent(NOW);

            // then
            assertThat(outbox.isSent()).isTrue();
            assertThat(outbox.getProcessedAt()).isEqualTo(NOW);
        }

        @Test
        @DisplayName("markAsFailed - FAILED 상태로 전환하고 retryCount를 증가시킨다")
        void markAsFailed() {
            // given
            CrawlTaskOutbox outbox = CrawlTaskOutboxFixture.aPendingOutbox();
            int initialRetryCount = outbox.getRetryCount();

            // when
            outbox.markAsFailed(NOW);

            // then
            assertThat(outbox.getStatus()).isEqualTo(OutboxStatus.FAILED);
            assertThat(outbox.getRetryCount()).isEqualTo(initialRetryCount + 1);
            assertThat(outbox.getProcessedAt()).isEqualTo(NOW);
        }

        @Test
        @DisplayName("resetToPending - 재시도 가능하면 PENDING으로 복귀한다")
        void resetToPendingWhenCanRetry() {
            // given
            CrawlTaskOutbox outbox = CrawlTaskOutboxFixture.aRetryableFailedOutbox();
            assertThat(outbox.canRetry()).isTrue();

            // when
            outbox.resetToPending();

            // then
            assertThat(outbox.isPending()).isTrue();
        }

        @Test
        @DisplayName("resetToPending - 최대 재시도 횟수 도달 시 PENDING으로 복귀하지 않는다")
        void resetToPendingWhenMaxRetryReached() {
            // given
            CrawlTaskOutbox outbox = CrawlTaskOutboxFixture.aMaxRetriedFailedOutbox();
            assertThat(outbox.canRetry()).isFalse();
            OutboxStatus statusBefore = outbox.getStatus();

            // when
            outbox.resetToPending();

            // then
            assertThat(outbox.getStatus()).isEqualTo(statusBefore);
        }
    }

    @Nested
    @DisplayName("비즈니스 규칙 테스트")
    class BusinessRuleTest {

        @Test
        @DisplayName("canRetry - retryCount < 3이면 true")
        void canRetryWhenBelowLimit() {
            // given
            CrawlTaskOutbox outbox = CrawlTaskOutboxFixture.aRetryableFailedOutbox();

            // then
            assertThat(outbox.canRetry()).isTrue();
        }

        @Test
        @DisplayName("canRetry - retryCount == 3이면 false")
        void canRetryWhenAtLimit() {
            // given
            CrawlTaskOutbox outbox = CrawlTaskOutboxFixture.aMaxRetriedFailedOutbox();

            // then
            assertThat(outbox.canRetry()).isFalse();
        }

        @Test
        @DisplayName("isPending - PENDING 상태이면 true")
        void isPendingWhenPending() {
            // given
            CrawlTaskOutbox outbox = CrawlTaskOutboxFixture.aPendingOutbox();

            // then
            assertThat(outbox.isPending()).isTrue();
            assertThat(outbox.isProcessing()).isFalse();
            assertThat(outbox.isSent()).isFalse();
        }

        @Test
        @DisplayName("isProcessing - PROCESSING 상태이면 true")
        void isProcessingWhenProcessing() {
            // given
            CrawlTaskOutbox outbox = CrawlTaskOutboxFixture.aProcessingOutbox();

            // then
            assertThat(outbox.isProcessing()).isTrue();
            assertThat(outbox.isPending()).isFalse();
            assertThat(outbox.isSent()).isFalse();
        }

        @Test
        @DisplayName("isSent - SENT 상태이면 true")
        void isSentWhenSent() {
            // given
            CrawlTaskOutbox outbox = CrawlTaskOutboxFixture.aSentOutbox();

            // then
            assertThat(outbox.isSent()).isTrue();
            assertThat(outbox.isPending()).isFalse();
            assertThat(outbox.isProcessing()).isFalse();
        }

        @Test
        @DisplayName("forNew으로 생성된 outbox의 idempotencyKey는 결정적(deterministic)이다")
        void idempotencyKeyIsDeterministic() {
            // given
            CrawlTaskId crawlTaskId = CrawlTaskId.of(1L);

            // when
            CrawlTaskOutbox outbox1 = CrawlTaskOutbox.forNew(crawlTaskId, "{}", NOW);
            CrawlTaskOutbox outbox2 = CrawlTaskOutbox.forNew(crawlTaskId, "{}", NOW);

            // then
            assertThat(outbox1.getIdempotencyKey()).isEqualTo(outbox2.getIdempotencyKey());
        }
    }
}
