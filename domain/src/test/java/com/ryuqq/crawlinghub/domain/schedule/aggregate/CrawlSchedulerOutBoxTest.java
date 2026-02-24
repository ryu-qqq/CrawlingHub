package com.ryuqq.crawlinghub.domain.schedule.aggregate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.cralwinghub.domain.fixture.common.FixedClock;
import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerOutBoxFixture;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerHistoryId;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerOutBoxId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerOubBoxStatus;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@DisplayName("CrawlSchedulerOutBox Aggregate 단위 테스트")
class CrawlSchedulerOutBoxTest {

    private static final Instant NOW = FixedClock.aDefaultClock().instant();

    @Nested
    @DisplayName("forNew() 팩토리 메서드 테스트")
    class ForNewTest {

        @Test
        @DisplayName("신규 CrawlSchedulerOutBox를 PENDING 상태로 생성한다")
        void createNewOutbox() {
            // given
            CrawlSchedulerHistoryId historyId = CrawlSchedulerHistoryId.of(1L);

            // when
            CrawlSchedulerOutBox outbox =
                    CrawlSchedulerOutBox.forNew(
                            historyId,
                            10L,
                            100L,
                            "test-scheduler",
                            "cron(0 0 * * ? *)",
                            SchedulerStatus.ACTIVE,
                            NOW);

            // then
            assertThat(outbox.getOutBoxId()).isNull();
            assertThat(outbox.getOutBoxIdValue()).isNull();
            assertThat(outbox.getHistoryId()).isEqualTo(historyId);
            assertThat(outbox.getStatus()).isEqualTo(CrawlSchedulerOubBoxStatus.PENDING);
            assertThat(outbox.getSchedulerId()).isEqualTo(10L);
            assertThat(outbox.getSellerId()).isEqualTo(100L);
            assertThat(outbox.getSchedulerName()).isEqualTo("test-scheduler");
            assertThat(outbox.getCronExpression()).isEqualTo("cron(0 0 * * ? *)");
            assertThat(outbox.getSchedulerStatus()).isEqualTo(SchedulerStatus.ACTIVE);
            assertThat(outbox.getCreatedAt()).isEqualTo(NOW);
            assertThat(outbox.isPending()).isTrue();
        }
    }

    @Nested
    @DisplayName("reconstitute() 팩토리 메서드 테스트")
    class ReconstituteTest {

        @Test
        @DisplayName("기존 데이터로 복원한다")
        void reconstituteOutbox() {
            // given
            CrawlSchedulerOutBox outbox = CrawlSchedulerOutBoxFixture.aPendingOutBox();

            // then
            assertThat(outbox.getOutBoxId()).isEqualTo(CrawlSchedulerOutBoxId.of(1L));
            assertThat(outbox.getOutBoxIdValue()).isEqualTo(1L);
            assertThat(outbox.getStatus()).isEqualTo(CrawlSchedulerOubBoxStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("상태 전환 메서드 테스트")
    class StateTransitionTest {

        @Test
        @DisplayName("markAsProcessing - PENDING에서 PROCESSING으로 전환한다")
        void markAsProcessing() {
            // given
            CrawlSchedulerOutBox outbox = CrawlSchedulerOutBoxFixture.aPendingOutBox();

            // when
            outbox.markAsProcessing(NOW);

            // then
            assertThat(outbox.getStatus()).isEqualTo(CrawlSchedulerOubBoxStatus.PROCESSING);
            assertThat(outbox.isProcessing()).isTrue();
            assertThat(outbox.getProcessedAt()).isEqualTo(NOW);
        }

        @Test
        @DisplayName("markAsProcessing - PENDING이 아닌 상태에서 전환 시 예외가 발생한다")
        void throwWhenNotPendingForProcessing() {
            // given
            CrawlSchedulerOutBox outbox = CrawlSchedulerOutBoxFixture.aCompletedOutBox();

            // then
            assertThatThrownBy(() -> outbox.markAsProcessing(NOW))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("PENDING");
        }

        @Test
        @DisplayName("markAsCompleted - COMPLETED 상태로 전환한다")
        void markAsCompleted() {
            // given
            CrawlSchedulerOutBox outbox = CrawlSchedulerOutBoxFixture.aPendingOutBox();

            // when
            outbox.markAsCompleted(NOW);

            // then
            assertThat(outbox.isCompleted()).isTrue();
            assertThat(outbox.getErrorMessage()).isNull();
            assertThat(outbox.getProcessedAt()).isEqualTo(NOW);
        }

        @Test
        @DisplayName("markAsCompleted - 이미 COMPLETED이면 무시된다")
        void ignoreWhenAlreadyCompleted() {
            // given
            CrawlSchedulerOutBox outbox = CrawlSchedulerOutBoxFixture.aCompletedOutBox();
            Instant originalProcessedAt = outbox.getProcessedAt();

            // when
            outbox.markAsCompleted(NOW);

            // then
            assertThat(outbox.isCompleted()).isTrue();
            assertThat(outbox.getProcessedAt()).isEqualTo(originalProcessedAt);
        }

        @Test
        @DisplayName("markAsFailed - FAILED 상태로 전환한다")
        void markAsFailed() {
            // given
            CrawlSchedulerOutBox outbox = CrawlSchedulerOutBoxFixture.aPendingOutBox();

            // when
            outbox.markAsFailed("EventBridge 오류", NOW);

            // then
            assertThat(outbox.isFailed()).isTrue();
            assertThat(outbox.getErrorMessage()).isEqualTo("EventBridge 오류");
            assertThat(outbox.getProcessedAt()).isEqualTo(NOW);
        }

        @Test
        @DisplayName("markAsFailed - 에러 메시지가 null이면 예외가 발생한다")
        void throwWhenErrorMessageIsNull() {
            // given
            CrawlSchedulerOutBox outbox = CrawlSchedulerOutBoxFixture.aPendingOutBox();

            assertThatThrownBy(() -> outbox.markAsFailed(null, NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("에러 메시지");
        }

        @Test
        @DisplayName("markAsFailed - 에러 메시지가 빈 문자열이면 예외가 발생한다")
        void throwWhenErrorMessageIsBlank() {
            // given
            CrawlSchedulerOutBox outbox = CrawlSchedulerOutBoxFixture.aPendingOutBox();

            assertThatThrownBy(() -> outbox.markAsFailed("  ", NOW))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("retry - FAILED 상태에서 PENDING으로 복원한다")
        void retry() {
            // given
            CrawlSchedulerOutBox outbox = CrawlSchedulerOutBoxFixture.aFailedOutBox();

            // when
            outbox.retry();

            // then
            assertThat(outbox.isPending()).isTrue();
            assertThat(outbox.getProcessedAt()).isNull();
            assertThat(outbox.getErrorMessage()).isNull();
        }

        @Test
        @DisplayName("retry - FAILED가 아닌 상태에서 재시도 시 예외가 발생한다")
        void throwWhenNotFailedForRetry() {
            // given
            CrawlSchedulerOutBox outbox = CrawlSchedulerOutBoxFixture.aPendingOutBox();

            assertThatThrownBy(() -> outbox.retry())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("FAILED");
        }

        @Test
        @DisplayName("resetToPending - PROCESSING 상태에서 PENDING으로 복원한다")
        void resetToPending() {
            // given
            CrawlSchedulerOutBox outbox = CrawlSchedulerOutBoxFixture.aPendingOutBox();
            outbox.markAsProcessing(NOW);
            assertThat(outbox.isProcessing()).isTrue();

            // when
            outbox.resetToPending();

            // then
            assertThat(outbox.isPending()).isTrue();
            assertThat(outbox.getProcessedAt()).isNull();
        }

        @Test
        @DisplayName("resetToPending - PROCESSING이 아닌 상태에서 예외가 발생한다")
        void throwWhenNotProcessingForReset() {
            // given
            CrawlSchedulerOutBox outbox = CrawlSchedulerOutBoxFixture.aPendingOutBox();

            assertThatThrownBy(() -> outbox.resetToPending())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("PROCESSING");
        }
    }

    @Nested
    @DisplayName("equals/hashCode 테스트")
    class EqualsHashCodeTest {

        @Test
        @DisplayName("같은 outBoxId이면 동일하다")
        void sameOutBoxIdAreEqual() {
            // given
            CrawlSchedulerOutBox outbox1 = CrawlSchedulerOutBoxFixture.aPendingOutBox();
            CrawlSchedulerOutBox outbox2 = CrawlSchedulerOutBoxFixture.aPendingOutBox();

            // then
            assertThat(outbox1).isEqualTo(outbox2);
            assertThat(outbox1.hashCode()).isEqualTo(outbox2.hashCode());
        }

        @Test
        @DisplayName("다른 outBoxId이면 다르다")
        void differentOutBoxIdAreNotEqual() {
            // given
            CrawlSchedulerOutBox outbox1 = CrawlSchedulerOutBoxFixture.aPendingOutBox();
            CrawlSchedulerOutBox outbox2 = CrawlSchedulerOutBoxFixture.aCompletedOutBox();

            // then
            assertThat(outbox1).isNotEqualTo(outbox2);
        }
    }
}
