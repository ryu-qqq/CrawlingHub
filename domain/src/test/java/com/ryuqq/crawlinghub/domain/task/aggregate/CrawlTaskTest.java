package com.ryuqq.crawlinghub.domain.task.aggregate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.cralwinghub.domain.fixture.common.FixedClock;
import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlEndpointFixture;
import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskFixture;
import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskIdFixture;
import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskTypeFixture;
import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerIdFixture;
import com.ryuqq.cralwinghub.domain.fixture.seller.SellerIdFixture;
import com.ryuqq.crawlinghub.domain.common.event.DomainEvent;
import com.ryuqq.crawlinghub.domain.task.event.CrawlTaskRegisteredEvent;
import com.ryuqq.crawlinghub.domain.task.exception.InvalidCrawlTaskStateException;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * CrawlTask Aggregate Root 단위 테스트
 *
 * <p>테스트 대상:
 *
 * <ul>
 *   <li>신규 생성 {@code forNew()} - WAITING 상태, ID 미할당
 *   <li>상태 전환 메서드 - markAsPublished, markAsRunning, markAsSuccess, markAsFailed, markAsTimeout
 *   <li>재시도 로직 - canRetry(), attemptRetry(), markAsPublishedAfterRetry()
 *   <li>Outbox 관리 - initializeOutbox, markOutboxAsSent, markOutboxAsFailed
 *   <li>도메인 이벤트 - addRegisteredEvent, clearDomainEvents
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("CrawlTask Aggregate Root 테스트")
class CrawlTaskTest {

    @Nested
    @DisplayName("forNew() 신규 생성 테스트")
    class ForNew {

        @Test
        @DisplayName("유효한 파라미터로 신규 태스크 생성 시 WAITING 상태, ID 미할당")
        void shouldCreateNewTaskWithWaitingStatusAndUnassignedId() {
            // given
            FixedClock clock = FixedClock.aDefaultClock();

            // when
            CrawlTask task =
                    CrawlTask.forNew(
                            CrawlSchedulerIdFixture.anAssignedId(),
                            SellerIdFixture.anAssignedId(),
                            CrawlTaskTypeFixture.defaultType(),
                            CrawlEndpointFixture.aMiniShopListEndpoint(),
                            clock);

            // then
            assertThat(task.getId()).isNotNull();
            assertThat(task.getId().isAssigned()).isFalse();
            assertThat(task.getStatus()).isEqualTo(CrawlTaskStatus.WAITING);
            assertThat(task.getRetryCount().value()).isZero();
            assertThat(task.getOutbox()).isNull();
            assertThat(task.getDomainEvents()).isEmpty();
        }

        @Test
        @DisplayName("신규 생성 시 createdAt과 updatedAt이 설정됨")
        void shouldSetCreatedAtAndUpdatedAt() {
            // when
            CrawlTask task = CrawlTaskFixture.forNew();

            // then
            assertThat(task.getCreatedAt()).isNotNull();
            assertThat(task.getUpdatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("reconstitute() 영속성 복원 테스트")
    class Reconstitute {

        @Test
        @DisplayName("기존 데이터로 CrawlTask 복원 성공")
        void shouldRestoreTaskFromExistingData() {
            // when
            CrawlTask task = CrawlTaskFixture.aRunningTask();

            // then
            assertThat(task.getId()).isEqualTo(CrawlTaskIdFixture.anAssignedId());
            assertThat(task.getStatus()).isEqualTo(CrawlTaskStatus.RUNNING);
        }
    }

    @Nested
    @DisplayName("상태 전환 테스트")
    class StatusTransition {

        @Nested
        @DisplayName("markAsPublished() 테스트")
        class MarkAsPublished {

            @Test
            @DisplayName("WAITING → PUBLISHED 전환 성공")
            void shouldTransitionFromWaitingToPublished() {
                // given
                CrawlTask task = CrawlTaskFixture.aWaitingTask();
                FixedClock clock = FixedClock.aDefaultClock();

                // when
                task.markAsPublished(clock);

                // then
                assertThat(task.getStatus()).isEqualTo(CrawlTaskStatus.PUBLISHED);
            }

            @Test
            @DisplayName("WAITING이 아닌 상태에서 PUBLISHED 전환 시 예외 발생")
            void shouldThrowExceptionWhenNotInWaitingStatus() {
                // given
                CrawlTask task = CrawlTaskFixture.aRunningTask();
                FixedClock clock = FixedClock.aDefaultClock();

                // when & then
                assertThatThrownBy(() -> task.markAsPublished(clock))
                        .isInstanceOf(InvalidCrawlTaskStateException.class);
            }
        }

        @Nested
        @DisplayName("markAsRunning() 테스트")
        class MarkAsRunning {

            @Test
            @DisplayName("PUBLISHED → RUNNING 전환 성공")
            void shouldTransitionFromPublishedToRunning() {
                // given
                CrawlTask task = CrawlTaskFixture.aPublishedTask();
                FixedClock clock = FixedClock.aDefaultClock();

                // when
                task.markAsRunning(clock);

                // then
                assertThat(task.getStatus()).isEqualTo(CrawlTaskStatus.RUNNING);
            }

            @Test
            @DisplayName("PUBLISHED가 아닌 상태에서 RUNNING 전환 시 예외 발생")
            void shouldThrowExceptionWhenNotInPublishedStatus() {
                // given
                CrawlTask task = CrawlTaskFixture.aWaitingTask();
                FixedClock clock = FixedClock.aDefaultClock();

                // when & then
                assertThatThrownBy(() -> task.markAsRunning(clock))
                        .isInstanceOf(InvalidCrawlTaskStateException.class);
            }
        }

        @Nested
        @DisplayName("markAsSuccess() 테스트")
        class MarkAsSuccess {

            @Test
            @DisplayName("RUNNING → SUCCESS 전환 성공")
            void shouldTransitionFromRunningToSuccess() {
                // given
                CrawlTask task = CrawlTaskFixture.aRunningTask();
                FixedClock clock = FixedClock.aDefaultClock();

                // when
                task.markAsSuccess(clock);

                // then
                assertThat(task.getStatus()).isEqualTo(CrawlTaskStatus.SUCCESS);
            }

            @Test
            @DisplayName("RUNNING이 아닌 상태에서 SUCCESS 전환 시 예외 발생")
            void shouldThrowExceptionWhenNotInRunningStatus() {
                // given
                CrawlTask task = CrawlTaskFixture.aPublishedTask();
                FixedClock clock = FixedClock.aDefaultClock();

                // when & then
                assertThatThrownBy(() -> task.markAsSuccess(clock))
                        .isInstanceOf(InvalidCrawlTaskStateException.class);
            }
        }

        @Nested
        @DisplayName("markAsFailed() 테스트")
        class MarkAsFailed {

            @Test
            @DisplayName("RUNNING → FAILED 전환 성공")
            void shouldTransitionFromRunningToFailed() {
                // given
                CrawlTask task = CrawlTaskFixture.aRunningTask();
                FixedClock clock = FixedClock.aDefaultClock();

                // when
                task.markAsFailed(clock);

                // then
                assertThat(task.getStatus()).isEqualTo(CrawlTaskStatus.FAILED);
            }

            @Test
            @DisplayName("RUNNING이 아닌 상태에서 FAILED 전환 시 예외 발생")
            void shouldThrowExceptionWhenNotInRunningStatus() {
                // given
                CrawlTask task = CrawlTaskFixture.aWaitingTask();
                FixedClock clock = FixedClock.aDefaultClock();

                // when & then
                assertThatThrownBy(() -> task.markAsFailed(clock))
                        .isInstanceOf(InvalidCrawlTaskStateException.class);
            }
        }

        @Nested
        @DisplayName("markAsTimeout() 테스트")
        class MarkAsTimeout {

            @Test
            @DisplayName("RUNNING → TIMEOUT 전환 성공")
            void shouldTransitionFromRunningToTimeout() {
                // given
                CrawlTask task = CrawlTaskFixture.aRunningTask();
                FixedClock clock = FixedClock.aDefaultClock();

                // when
                task.markAsTimeout(clock);

                // then
                assertThat(task.getStatus()).isEqualTo(CrawlTaskStatus.TIMEOUT);
            }

            @Test
            @DisplayName("RUNNING이 아닌 상태에서 TIMEOUT 전환 시 예외 발생")
            void shouldThrowExceptionWhenNotInRunningStatus() {
                // given
                CrawlTask task = CrawlTaskFixture.aPublishedTask();
                FixedClock clock = FixedClock.aDefaultClock();

                // when & then
                assertThatThrownBy(() -> task.markAsTimeout(clock))
                        .isInstanceOf(InvalidCrawlTaskStateException.class);
            }
        }
    }

    @Nested
    @DisplayName("재시도 로직 테스트")
    class RetryLogic {

        @Nested
        @DisplayName("canRetry() 테스트")
        class CanRetry {

            @Test
            @DisplayName("FAILED 상태이고 재시도 횟수가 남아있으면 true 반환")
            void shouldReturnTrueWhenFailedAndCanRetry() {
                // given
                CrawlTask task = CrawlTaskFixture.aFailedTask();

                // then
                assertThat(task.canRetry()).isTrue();
            }

            @Test
            @DisplayName("TIMEOUT 상태이고 재시도 횟수가 남아있으면 true 반환")
            void shouldReturnTrueWhenTimeoutAndCanRetry() {
                // given
                CrawlTask task = CrawlTaskFixture.aTimeoutTask();

                // then
                assertThat(task.canRetry()).isTrue();
            }

            @Test
            @DisplayName("최대 재시도 횟수에 도달하면 false 반환")
            void shouldReturnFalseWhenMaxRetryReached() {
                // given
                CrawlTask task = CrawlTaskFixture.aFailedTaskWithMaxRetry();

                // then
                assertThat(task.canRetry()).isFalse();
            }

            @Test
            @DisplayName("WAITING 상태에서는 false 반환")
            void shouldReturnFalseWhenInWaitingStatus() {
                // given
                CrawlTask task = CrawlTaskFixture.aWaitingTask();

                // then
                assertThat(task.canRetry()).isFalse();
            }
        }

        @Nested
        @DisplayName("attemptRetry() 테스트")
        class AttemptRetry {

            @Test
            @DisplayName("재시도 가능한 상태에서 재시도 성공")
            void shouldRetryWhenCanRetry() {
                // given
                CrawlTask task = CrawlTaskFixture.aFailedTask();
                int originalRetryCount = task.getRetryCount().value();
                FixedClock clock = FixedClock.aDefaultClock();

                // when
                boolean result = task.attemptRetry(clock);

                // then
                assertThat(result).isTrue();
                assertThat(task.getStatus()).isEqualTo(CrawlTaskStatus.RETRY);
                assertThat(task.getRetryCount().value()).isEqualTo(originalRetryCount + 1);
            }

            @Test
            @DisplayName("재시도 불가능한 상태에서 재시도 실패")
            void shouldFailWhenCannotRetry() {
                // given
                CrawlTask task = CrawlTaskFixture.aFailedTaskWithMaxRetry();
                FixedClock clock = FixedClock.aDefaultClock();

                // when
                boolean result = task.attemptRetry(clock);

                // then
                assertThat(result).isFalse();
                assertThat(task.getStatus()).isEqualTo(CrawlTaskStatus.FAILED);
            }
        }

        @Nested
        @DisplayName("markAsPublishedAfterRetry() 테스트")
        class MarkAsPublishedAfterRetry {

            @Test
            @DisplayName("RETRY → PUBLISHED 전환 성공")
            void shouldTransitionFromRetryToPublished() {
                // given
                CrawlTask task = CrawlTaskFixture.aRetryTask();
                FixedClock clock = FixedClock.aDefaultClock();

                // when
                task.markAsPublishedAfterRetry(clock);

                // then
                assertThat(task.getStatus()).isEqualTo(CrawlTaskStatus.PUBLISHED);
            }

            @Test
            @DisplayName("RETRY가 아닌 상태에서 PUBLISHED 전환 시 예외 발생")
            void shouldThrowExceptionWhenNotInRetryStatus() {
                // given
                CrawlTask task = CrawlTaskFixture.aFailedTask();
                FixedClock clock = FixedClock.aDefaultClock();

                // when & then
                assertThatThrownBy(() -> task.markAsPublishedAfterRetry(clock))
                        .isInstanceOf(InvalidCrawlTaskStateException.class);
            }
        }
    }

    @Nested
    @DisplayName("isInProgress() 테스트")
    class IsInProgress {

        @Test
        @DisplayName("WAITING 상태면 true 반환")
        void shouldReturnTrueWhenWaiting() {
            // given
            CrawlTask task = CrawlTaskFixture.aWaitingTask();

            // then
            assertThat(task.isInProgress()).isTrue();
        }

        @Test
        @DisplayName("PUBLISHED 상태면 true 반환")
        void shouldReturnTrueWhenPublished() {
            // given
            CrawlTask task = CrawlTaskFixture.aPublishedTask();

            // then
            assertThat(task.isInProgress()).isTrue();
        }

        @Test
        @DisplayName("RUNNING 상태면 true 반환")
        void shouldReturnTrueWhenRunning() {
            // given
            CrawlTask task = CrawlTaskFixture.aRunningTask();

            // then
            assertThat(task.isInProgress()).isTrue();
        }

        @Test
        @DisplayName("SUCCESS 상태면 false 반환")
        void shouldReturnFalseWhenSuccess() {
            // given
            CrawlTask task = CrawlTaskFixture.aSuccessTask();

            // then
            assertThat(task.isInProgress()).isFalse();
        }

        @Test
        @DisplayName("FAILED 상태면 false 반환")
        void shouldReturnFalseWhenFailed() {
            // given
            CrawlTask task = CrawlTaskFixture.aFailedTask();

            // then
            assertThat(task.isInProgress()).isFalse();
        }
    }

    @Nested
    @DisplayName("Outbox 관리 테스트")
    class OutboxManagement {

        @Nested
        @DisplayName("initializeOutbox() 테스트")
        class InitializeOutbox {

            @Test
            @DisplayName("Outbox 초기화 성공")
            void shouldInitializeOutbox() {
                // given
                CrawlTask task = CrawlTaskFixture.aWaitingTask();
                String payload = "{\"taskId\":1}";
                FixedClock clock = FixedClock.aDefaultClock();

                // when
                task.initializeOutbox(payload, clock);

                // then
                assertThat(task.hasOutbox()).isTrue();
                assertThat(task.hasOutboxPending()).isTrue();
                assertThat(task.getOutbox().getPayload()).isEqualTo(payload);
            }

            @Test
            @DisplayName("Outbox가 이미 초기화되어 있으면 예외 발생")
            void shouldThrowExceptionWhenOutboxAlreadyInitialized() {
                // given
                CrawlTask task = CrawlTaskFixture.aWaitingTask();
                FixedClock clock = FixedClock.aDefaultClock();
                task.initializeOutbox("{\"taskId\":1}", clock);

                // when & then
                assertThatThrownBy(() -> task.initializeOutbox("{\"taskId\":2}", clock))
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessage("Outbox가 이미 초기화되었습니다.");
            }
        }

        @Nested
        @DisplayName("markOutboxAsSent() 테스트")
        class MarkOutboxAsSent {

            @Test
            @DisplayName("Outbox 발행 성공 처리")
            void shouldMarkOutboxAsSent() {
                // given
                CrawlTask task = CrawlTaskFixture.aWaitingTask();
                FixedClock clock = FixedClock.aDefaultClock();
                task.initializeOutbox("{\"taskId\":1}", clock);

                // when
                task.markOutboxAsSent(clock);

                // then
                assertThat(task.getOutbox().isSent()).isTrue();
                assertThat(task.hasOutboxPending()).isFalse();
            }

            @Test
            @DisplayName("Outbox가 초기화되지 않으면 예외 발생")
            void shouldThrowExceptionWhenOutboxNotInitialized() {
                // given
                CrawlTask task = CrawlTaskFixture.aWaitingTask();
                FixedClock clock = FixedClock.aDefaultClock();

                // when & then
                assertThatThrownBy(() -> task.markOutboxAsSent(clock))
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessage("Outbox가 초기화되지 않았습니다.");
            }
        }

        @Nested
        @DisplayName("markOutboxAsFailed() 테스트")
        class MarkOutboxAsFailed {

            @Test
            @DisplayName("Outbox 발행 실패 처리")
            void shouldMarkOutboxAsFailed() {
                // given
                CrawlTask task = CrawlTaskFixture.aWaitingTask();
                FixedClock clock = FixedClock.aDefaultClock();
                task.initializeOutbox("{\"taskId\":1}", clock);

                // when
                task.markOutboxAsFailed(clock);

                // then
                assertThat(task.getOutbox().getRetryCount()).isEqualTo(1);
            }

            @Test
            @DisplayName("Outbox가 초기화되지 않으면 예외 발생")
            void shouldThrowExceptionWhenOutboxNotInitialized() {
                // given
                CrawlTask task = CrawlTaskFixture.aWaitingTask();
                FixedClock clock = FixedClock.aDefaultClock();

                // when & then
                assertThatThrownBy(() -> task.markOutboxAsFailed(clock))
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessage("Outbox가 초기화되지 않았습니다.");
            }
        }

        @Nested
        @DisplayName("hasOutbox() / hasOutboxPending() 테스트")
        class OutboxQueries {

            @Test
            @DisplayName("Outbox가 없으면 hasOutbox() false 반환")
            void shouldReturnFalseWhenNoOutbox() {
                // given
                CrawlTask task = CrawlTaskFixture.aWaitingTask();

                // then
                assertThat(task.hasOutbox()).isFalse();
                assertThat(task.hasOutboxPending()).isFalse();
            }
        }
    }

    @Nested
    @DisplayName("도메인 이벤트 테스트")
    class DomainEvents {

        @Nested
        @DisplayName("addRegisteredEvent() 테스트")
        class AddRegisteredEvent {

            @Test
            @DisplayName("ID 할당 후 등록 이벤트 발행 성공")
            void shouldAddRegisteredEventWhenIdAssigned() {
                // given
                CrawlTask task = CrawlTaskFixture.aWaitingTask();
                String payload = "{\"taskId\":1}";
                FixedClock clock = FixedClock.aDefaultClock();

                // when
                task.addRegisteredEvent(payload, clock);

                // then
                List<DomainEvent> events = task.getDomainEvents();
                assertThat(events).hasSize(1);
                assertThat(events.get(0)).isInstanceOf(CrawlTaskRegisteredEvent.class);
            }

            @Test
            @DisplayName("ID 미할당 상태에서 등록 이벤트 발행 시 예외 발생")
            void shouldThrowExceptionWhenIdNotAssigned() {
                // given
                CrawlTask task = CrawlTaskFixture.forNew();
                FixedClock clock = FixedClock.aDefaultClock();

                // when & then
                assertThatThrownBy(() -> task.addRegisteredEvent("{\"taskId\":1}", clock))
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessage("등록 이벤트는 ID 할당 후 발행해야 합니다.");
            }
        }

        @Nested
        @DisplayName("clearDomainEvents() 테스트")
        class ClearDomainEvents {

            @Test
            @DisplayName("이벤트 초기화 성공")
            void shouldClearAllDomainEvents() {
                // given
                CrawlTask task = CrawlTaskFixture.aWaitingTask();
                FixedClock clock = FixedClock.aDefaultClock();
                task.addRegisteredEvent("{\"taskId\":1}", clock);
                assertThat(task.getDomainEvents()).isNotEmpty();

                // when
                task.clearDomainEvents();

                // then
                assertThat(task.getDomainEvents()).isEmpty();
            }
        }

        @Test
        @DisplayName("getDomainEvents()는 읽기 전용 목록 반환")
        void shouldReturnUnmodifiableEventList() {
            // given
            CrawlTask task = CrawlTaskFixture.aWaitingTask();
            FixedClock clock = FixedClock.aDefaultClock();
            task.addRegisteredEvent("{\"taskId\":1}", clock);

            // when
            List<DomainEvent> events = task.getDomainEvents();

            // then
            assertThatThrownBy(() -> events.add(null))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("generateIdempotencyKey() 테스트")
    class GenerateIdempotencyKey {

        @Test
        @DisplayName("멱등성 키 생성 시 schedulerId와 taskId 포함")
        void shouldGenerateKeyWithSchedulerIdAndTaskId() {
            // given
            CrawlTask task = CrawlTaskFixture.aWaitingTask();

            // when
            String key = task.generateIdempotencyKey();

            // then
            assertThat(key).startsWith("1-1-");
            assertThat(key).hasSize(12); // "1-1-xxxxxxxx" (8자리 UUID)
        }
    }

    @Nested
    @DisplayName("Getter 메서드 테스트")
    class GetterMethods {

        @Test
        @DisplayName("getIdValue()는 ID의 원시값 반환")
        void shouldReturnIdValue() {
            // given
            CrawlTask task = CrawlTaskFixture.aTaskWithId(123L);

            // then
            assertThat(task.getIdValue()).isEqualTo(123L);
        }

        @Test
        @DisplayName("getCrawlSchedulerIdValue()는 스케줄러 ID의 원시값 반환")
        void shouldReturnSchedulerIdValue() {
            // given
            CrawlTask task = CrawlTaskFixture.aWaitingTask();

            // then
            assertThat(task.getCrawlSchedulerIdValue()).isEqualTo(1L);
        }

        @Test
        @DisplayName("getSellerIdValue()는 셀러 ID의 원시값 반환")
        void shouldReturnSellerIdValue() {
            // given
            CrawlTask task = CrawlTaskFixture.aWaitingTask();

            // then
            assertThat(task.getSellerIdValue()).isEqualTo(1L);
        }

        @Test
        @DisplayName("getTaskType()은 태스크 유형 반환")
        void shouldReturnTaskType() {
            // given
            CrawlTask task = CrawlTaskFixture.aWaitingTask();

            // then
            assertThat(task.getTaskType()).isEqualTo(CrawlTaskTypeFixture.defaultType());
        }

        @Test
        @DisplayName("getEndpoint()는 엔드포인트 반환")
        void shouldReturnEndpoint() {
            // given
            CrawlTask task = CrawlTaskFixture.aWaitingTask();

            // then
            assertThat(task.getEndpoint()).isNotNull();
        }
    }
}
