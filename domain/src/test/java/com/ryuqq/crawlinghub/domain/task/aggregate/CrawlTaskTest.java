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
import com.ryuqq.crawlinghub.domain.task.exception.InvalidCrawlTaskStateException;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import java.time.Instant;
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
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("CrawlTask Aggregate Root 테스트")
class CrawlTaskTest {

    private static final Instant FIXED_INSTANT = FixedClock.aDefaultClock().instant();

    @Nested
    @DisplayName("forNew() 신규 생성 테스트")
    class ForNew {

        @Test
        @DisplayName("유효한 파라미터로 신규 태스크 생성 시 WAITING 상태, ID 미할당")
        void shouldCreateNewTaskWithWaitingStatusAndUnassignedId() {
            // when
            CrawlTask task =
                    CrawlTask.forNew(
                            CrawlSchedulerIdFixture.anAssignedId(),
                            SellerIdFixture.anAssignedId(),
                            CrawlTaskTypeFixture.defaultType(),
                            CrawlEndpointFixture.aMiniShopListEndpoint(),
                            FIXED_INSTANT);

            // then
            assertThat(task.getId()).isNotNull();
            assertThat(task.getId().isNew()).isTrue();
            assertThat(task.getStatus()).isEqualTo(CrawlTaskStatus.WAITING);
            assertThat(task.getRetryCount().value()).isZero();
            assertThat(task.getOutbox()).isNull();
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

                // when
                task.markAsPublished(FIXED_INSTANT);

                // then
                assertThat(task.getStatus()).isEqualTo(CrawlTaskStatus.PUBLISHED);
            }

            @Test
            @DisplayName("WAITING이 아닌 상태에서 PUBLISHED 전환 시 예외 발생")
            void shouldThrowExceptionWhenNotInWaitingStatus() {
                // given
                CrawlTask task = CrawlTaskFixture.aRunningTask();

                // when & then
                assertThatThrownBy(() -> task.markAsPublished(FIXED_INSTANT))
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

                // when
                task.markAsRunning(FIXED_INSTANT);

                // then
                assertThat(task.getStatus()).isEqualTo(CrawlTaskStatus.RUNNING);
            }

            @Test
            @DisplayName("PUBLISHED가 아닌 상태에서 RUNNING 전환 시 예외 발생")
            void shouldThrowExceptionWhenNotInPublishedStatus() {
                // given
                CrawlTask task = CrawlTaskFixture.aWaitingTask();

                // when & then
                assertThatThrownBy(() -> task.markAsRunning(FIXED_INSTANT))
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

                // when
                task.markAsSuccess(FIXED_INSTANT);

                // then
                assertThat(task.getStatus()).isEqualTo(CrawlTaskStatus.SUCCESS);
            }

            @Test
            @DisplayName("RUNNING이 아닌 상태에서 SUCCESS 전환 시 예외 발생")
            void shouldThrowExceptionWhenNotInRunningStatus() {
                // given
                CrawlTask task = CrawlTaskFixture.aPublishedTask();

                // when & then
                assertThatThrownBy(() -> task.markAsSuccess(FIXED_INSTANT))
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

                // when
                task.markAsFailed(FIXED_INSTANT);

                // then
                assertThat(task.getStatus()).isEqualTo(CrawlTaskStatus.FAILED);
            }

            @Test
            @DisplayName("RUNNING이 아닌 상태에서 FAILED 전환 시 예외 발생")
            void shouldThrowExceptionWhenNotInRunningStatus() {
                // given
                CrawlTask task = CrawlTaskFixture.aWaitingTask();

                // when & then
                assertThatThrownBy(() -> task.markAsFailed(FIXED_INSTANT))
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

                // when
                task.markAsTimeout(FIXED_INSTANT);

                // then
                assertThat(task.getStatus()).isEqualTo(CrawlTaskStatus.TIMEOUT);
            }

            @Test
            @DisplayName("RUNNING이 아닌 상태에서 TIMEOUT 전환 시 예외 발생")
            void shouldThrowExceptionWhenNotInRunningStatus() {
                // given
                CrawlTask task = CrawlTaskFixture.aPublishedTask();

                // when & then
                assertThatThrownBy(() -> task.markAsTimeout(FIXED_INSTANT))
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

                // when
                boolean result = task.attemptRetry(FIXED_INSTANT);

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

                // when
                boolean result = task.attemptRetry(FIXED_INSTANT);

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

                // when
                task.markAsPublishedAfterRetry(FIXED_INSTANT);

                // then
                assertThat(task.getStatus()).isEqualTo(CrawlTaskStatus.PUBLISHED);
            }

            @Test
            @DisplayName("RETRY가 아닌 상태에서 PUBLISHED 전환 시 예외 발생")
            void shouldThrowExceptionWhenNotInRetryStatus() {
                // given
                CrawlTask task = CrawlTaskFixture.aFailedTask();

                // when & then
                assertThatThrownBy(() -> task.markAsPublishedAfterRetry(FIXED_INSTANT))
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

                // when
                task.initializeOutbox(payload, FIXED_INSTANT);

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
                task.initializeOutbox("{\"taskId\":1}", FIXED_INSTANT);

                // when & then
                assertThatThrownBy(() -> task.initializeOutbox("{\"taskId\":2}", FIXED_INSTANT))
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
                task.initializeOutbox("{\"taskId\":1}", FIXED_INSTANT);

                // when
                task.markOutboxAsSent(FIXED_INSTANT);

                // then
                assertThat(task.getOutbox().isSent()).isTrue();
                assertThat(task.hasOutboxPending()).isFalse();
            }

            @Test
            @DisplayName("Outbox가 초기화되지 않으면 예외 발생")
            void shouldThrowExceptionWhenOutboxNotInitialized() {
                // given
                CrawlTask task = CrawlTaskFixture.aWaitingTask();

                // when & then
                assertThatThrownBy(() -> task.markOutboxAsSent(FIXED_INSTANT))
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
                task.initializeOutbox("{\"taskId\":1}", FIXED_INSTANT);

                // when
                task.markOutboxAsFailed(FIXED_INSTANT);

                // then
                assertThat(task.getOutbox().getRetryCount()).isEqualTo(1);
            }

            @Test
            @DisplayName("Outbox가 초기화되지 않으면 예외 발생")
            void shouldThrowExceptionWhenOutboxNotInitialized() {
                // given
                CrawlTask task = CrawlTaskFixture.aWaitingTask();

                // when & then
                assertThatThrownBy(() -> task.markOutboxAsFailed(FIXED_INSTANT))
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
    @DisplayName("generateIdempotencyKey() 테스트")
    class GenerateIdempotencyKey {

        @Test
        @DisplayName("멱등성 키 생성 시 Deterministic 형식 (schedulerId-taskId-uuid8)")
        void shouldGenerateDeterministicKey() {
            // given
            CrawlTask task = CrawlTaskFixture.aWaitingTask();

            // when
            String key = task.generateIdempotencyKey();

            // then
            // Deterministic 형식: {schedulerId}-{taskId}-{uuid8}
            assertThat(key).matches("\\d+-\\d+-[a-f0-9]{8}");
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

        @Test
        @DisplayName("getCrawlSchedulerId()는 스케줄러 ID를 반환한다")
        void shouldReturnCrawlSchedulerId() {
            CrawlTask task = CrawlTaskFixture.aWaitingTask();
            assertThat(task.getCrawlSchedulerId()).isNotNull();
        }

        @Test
        @DisplayName("getSellerId()는 셀러 ID를 반환한다")
        void shouldReturnSellerId() {
            CrawlTask task = CrawlTaskFixture.aWaitingTask();
            assertThat(task.getSellerId()).isNotNull();
        }

        @Test
        @DisplayName("getMustItSellerName()은 머스잇 셀러명을 반환한다")
        void shouldReturnMustItSellerName() {
            CrawlTask task = CrawlTaskFixture.aWaitingTask();
            assertThat(task.getMustItSellerName()).isNotNull();
        }

        @Test
        @DisplayName("getRetryCountValue()는 재시도 횟수를 반환한다")
        void shouldReturnRetryCountValue() {
            CrawlTask task = CrawlTaskFixture.aWaitingTask();
            assertThat(task.getRetryCountValue()).isGreaterThanOrEqualTo(0);
        }
    }

    @Nested
    @DisplayName("failDirectly() 직접 실패 처리 테스트")
    class FailDirectly {

        @Test
        @DisplayName("비종료 상태에서 FAILED로 직접 전환한다")
        void shouldFailDirectlyFromNonTerminalState() {
            CrawlTask task = CrawlTaskFixture.aWaitingTask();

            task.failDirectly(FIXED_INSTANT);

            assertThat(task.getStatus()).isEqualTo(CrawlTaskStatus.FAILED);
        }

        @Test
        @DisplayName("PUBLISHED 상태에서 FAILED로 직접 전환한다")
        void shouldFailDirectlyFromPublishedState() {
            CrawlTask task = CrawlTaskFixture.aPublishedTask();

            task.failDirectly(FIXED_INSTANT);

            assertThat(task.getStatus()).isEqualTo(CrawlTaskStatus.FAILED);
        }

        @Test
        @DisplayName("이미 종료 상태이면 예외가 발생한다")
        void shouldThrowWhenAlreadyTerminal() {
            CrawlTask task = CrawlTaskFixture.aSuccessTask();

            assertThatThrownBy(() -> task.failDirectly(FIXED_INSTANT))
                    .isInstanceOf(InvalidCrawlTaskStateException.class);
        }
    }

    @Nested
    @DisplayName("isTerminal() 테스트")
    class IsTerminal {

        @Test
        @DisplayName("WAITING 상태는 종료 상태가 아니다")
        void waitingIsNotTerminal() {
            CrawlTask task = CrawlTaskFixture.aWaitingTask();
            assertThat(task.isTerminal()).isFalse();
        }

        @Test
        @DisplayName("SUCCESS 상태는 종료 상태이다")
        void successIsTerminal() {
            CrawlTask task = CrawlTaskFixture.aSuccessTask();
            assertThat(task.isTerminal()).isTrue();
        }
    }
}
