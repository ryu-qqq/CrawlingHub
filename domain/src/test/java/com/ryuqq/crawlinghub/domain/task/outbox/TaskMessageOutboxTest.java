package com.ryuqq.crawlinghub.domain.task.outbox;

import com.ryuqq.crawlinghub.domain.task.TaskId;
import com.ryuqq.crawlinghub.domain.task.TaskType;
import com.ryuqq.crawlinghub.domain.task.event.TaskMessageCreatedEvent;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TaskMessageOutbox Domain Aggregate 단위 테스트
 *
 * <p>Outbox 패턴 구현체로 SQS 발행 보장을 위한 상태 관리 검증
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
@DisplayName("TaskMessageOutbox Domain Aggregate 단위 테스트")
class TaskMessageOutboxTest {

    @Nested
    @DisplayName("생성 테스트 (Happy Path)")
    class CreateTests {

        @Test
        @DisplayName("PENDING 상태로 새로운 Outbox 메시지를 생성할 수 있다")
        void shouldCreatePendingOutbox() {
            // Given
            TaskId taskId = TaskId.of(1L);
            TaskType taskType = TaskType.MINI_SHOP;

            // When
            TaskMessageOutbox outbox = TaskMessageOutbox.createPending(taskId, taskType);

            // Then
            assertThat(outbox).isNotNull();
            assertThat(outbox.getOutboxId()).isNull(); // 아직 저장되지 않음
            assertThat(outbox.getTaskId()).isEqualTo(taskId);
            assertThat(outbox.getTaskType()).isEqualTo(taskType);
            assertThat(outbox.getStatus()).isEqualTo(TaskMessageStatus.PENDING);
            assertThat(outbox.getRetryCount()).isZero();
            assertThat(outbox.getErrorMessage()).isNull();
            assertThat(outbox.getCreatedAt()).isNotNull();
            assertThat(outbox.getSentAt()).isNull();
        }

        @Test
        @DisplayName("reconstitute()로 기존 Outbox를 재구성할 수 있다")
        void shouldReconstituteOutbox() {
            // Given
            Long outboxId = 100L;
            TaskId taskId = TaskId.of(1L);
            TaskType taskType = TaskType.PRODUCT_DETAIL;
            TaskMessageStatus status = TaskMessageStatus.SENT;
            int retryCount = 2;
            String errorMessage = "발행 실패";
            LocalDateTime createdAt = LocalDateTime.now().minusHours(1);
            LocalDateTime sentAt = LocalDateTime.now();

            // When
            TaskMessageOutbox outbox = TaskMessageOutbox.reconstitute(
                outboxId, taskId, taskType, status, retryCount, errorMessage, createdAt, sentAt
            );

            // Then
            assertThat(outbox.getOutboxId()).isEqualTo(outboxId);
            assertThat(outbox.getTaskId()).isEqualTo(taskId);
            assertThat(outbox.getTaskType()).isEqualTo(taskType);
            assertThat(outbox.getStatus()).isEqualTo(status);
            assertThat(outbox.getRetryCount()).isEqualTo(retryCount);
            assertThat(outbox.getErrorMessage()).isEqualTo(errorMessage);
            assertThat(outbox.getCreatedAt()).isEqualTo(createdAt);
            assertThat(outbox.getSentAt()).isEqualTo(sentAt);
        }
    }

    @Nested
    @DisplayName("예외 케이스 테스트")
    class ExceptionTests {

        @Test
        @DisplayName("taskId가 null이면 예외 발생")
        void shouldThrowExceptionWhenTaskIdIsNull() {
            // When & Then
            assertThatThrownBy(() -> TaskMessageOutbox.createPending(null, TaskType.MINI_SHOP))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("taskId는 필수입니다");
        }

        @Test
        @DisplayName("taskType이 null이면 예외 발생")
        void shouldThrowExceptionWhenTaskTypeIsNull() {
            // Given
            TaskId taskId = TaskId.of(1L);

            // When & Then
            assertThatThrownBy(() -> TaskMessageOutbox.createPending(taskId, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("taskType은 필수입니다");
        }

        @Test
        @DisplayName("reconstitute() 시 status가 null이면 예외 발생")
        void shouldThrowExceptionWhenStatusIsNull() {
            // When & Then
            assertThatThrownBy(() -> TaskMessageOutbox.reconstitute(
                1L, TaskId.of(1L), TaskType.MINI_SHOP, null, 0, null, LocalDateTime.now(), null
            ))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("status는 필수입니다");
        }

        @Test
        @DisplayName("reconstitute() 시 createdAt가 null이면 예외 발생")
        void shouldThrowExceptionWhenCreatedAtIsNull() {
            // When & Then
            assertThatThrownBy(() -> TaskMessageOutbox.reconstitute(
                1L, TaskId.of(1L), TaskType.MINI_SHOP, TaskMessageStatus.PENDING, 0, null, null, null
            ))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("createdAt는 필수입니다");
        }
    }

    @Nested
    @DisplayName("상태 전이 테스트")
    class StatusTransitionTests {

        @Test
        @DisplayName("PENDING → SENT: markSent() 호출 시 상태가 SENT로 변경된다")
        void shouldTransitionFromPendingToSent() {
            // Given: PENDING 상태의 Outbox
            TaskMessageOutbox outbox = TaskMessageOutbox.createPending(
                TaskId.of(1L),
                TaskType.MINI_SHOP
            );
            assertThat(outbox.getStatus()).isEqualTo(TaskMessageStatus.PENDING);

            // When: SQS 발행 성공
            outbox.markSent();

            // Then: SENT 상태로 전이
            assertThat(outbox.getStatus()).isEqualTo(TaskMessageStatus.SENT);
            assertThat(outbox.getSentAt()).isNotNull();
            assertThat(outbox.getErrorMessage()).isNull();
        }

        @Test
        @DisplayName("이미 SENT 상태인 메시지에 markSent() 호출 시 예외 발생")
        void shouldThrowExceptionWhenMarkSentOnAlreadySent() {
            // Given: SENT 상태의 Outbox
            TaskMessageOutbox outbox = TaskMessageOutbox.reconstitute(
                1L, TaskId.of(1L), TaskType.MINI_SHOP,
                TaskMessageStatus.SENT, 0, null, LocalDateTime.now(), LocalDateTime.now()
            );

            // When & Then
            assertThatThrownBy(outbox::markSent)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 발행된 메시지입니다");
        }

        @Test
        @DisplayName("PENDING 상태에서 markFailed() 호출 시 재시도 카운트 증가")
        void shouldIncrementRetryCountWhenMarkFailed() {
            // Given: PENDING 상태의 Outbox
            TaskMessageOutbox outbox = TaskMessageOutbox.createPending(
                TaskId.of(1L),
                TaskType.PRODUCT_DETAIL
            );
            assertThat(outbox.getRetryCount()).isZero();

            // When: SQS 발행 실패 (1회)
            outbox.markFailed("SQS 연결 실패");

            // Then: 재시도 카운트 1 증가, 에러 메시지 저장
            assertThat(outbox.getRetryCount()).isEqualTo(1);
            assertThat(outbox.getErrorMessage()).isEqualTo("SQS 연결 실패");
            assertThat(outbox.getStatus()).isEqualTo(TaskMessageStatus.PENDING); // 여전히 PENDING

            // When: SQS 발행 실패 (2회)
            outbox.markFailed("SQS 타임아웃");

            // Then: 재시도 카운트 2, 에러 메시지 업데이트
            assertThat(outbox.getRetryCount()).isEqualTo(2);
            assertThat(outbox.getErrorMessage()).isEqualTo("SQS 타임아웃");
        }

        @Test
        @DisplayName("이미 SENT 상태인 메시지에 markFailed() 호출 시 예외 발생")
        void shouldThrowExceptionWhenMarkFailedOnAlreadySent() {
            // Given: SENT 상태의 Outbox
            TaskMessageOutbox outbox = TaskMessageOutbox.reconstitute(
                1L, TaskId.of(1L), TaskType.MINI_SHOP,
                TaskMessageStatus.SENT, 0, null, LocalDateTime.now(), LocalDateTime.now()
            );

            // When & Then
            assertThatThrownBy(() -> outbox.markFailed("에러"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 발행된 메시지입니다");
        }
    }

    @Nested
    @DisplayName("재시도 로직 테스트")
    class RetryTests {

        @Test
        @DisplayName("PENDING 상태이고 재시도 횟수가 3회 미만이면 재시도 가능")
        void shouldRetryWhenPendingAndRetryCountLessThan3() {
            // Given: PENDING 상태, retryCount = 0
            TaskMessageOutbox outbox = TaskMessageOutbox.createPending(
                TaskId.of(1L),
                TaskType.MINI_SHOP
            );

            // When & Then: 재시도 가능
            assertThat(outbox.canRetry()).isTrue();

            // When: 실패 1회
            outbox.markFailed("실패");
            assertThat(outbox.canRetry()).isTrue();

            // When: 실패 2회
            outbox.markFailed("실패");
            assertThat(outbox.canRetry()).isTrue();

            // When: 실패 3회
            outbox.markFailed("실패");
            assertThat(outbox.canRetry()).isFalse(); // 최대 3회 초과
        }

        @Test
        @DisplayName("SENT 상태이면 재시도 불가")
        void shouldNotRetryWhenSent() {
            // Given: SENT 상태의 Outbox
            TaskMessageOutbox outbox = TaskMessageOutbox.reconstitute(
                1L, TaskId.of(1L), TaskType.MINI_SHOP,
                TaskMessageStatus.SENT, 0, null, LocalDateTime.now(), LocalDateTime.now()
            );

            // When & Then: 재시도 불가
            assertThat(outbox.canRetry()).isFalse();
        }
    }

    @Nested
    @DisplayName("Event 생성 테스트")
    class EventTests {

        @Test
        @DisplayName("PENDING 상태이지만 outboxId가 null이면 Event 생성 시 예외 발생")
        void shouldThrowExceptionWhenCreateEventWithNullOutboxId() {
            // Given: PENDING 상태의 Outbox (outboxId = null)
            TaskMessageOutbox outbox = TaskMessageOutbox.createPending(
                TaskId.of(1L),
                TaskType.MINI_SHOP
            );

            // When & Then: Event 생성 시 예외 발생 (outboxId가 null이므로)
            assertThatThrownBy(outbox::createEvent)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("outboxId는 필수이며 양수여야 합니다");
        }

        @Test
        @DisplayName("PENDING 상태의 저장된 Outbox는 outboxId를 포함한 Event를 생성한다")
        void shouldCreateEventWithOutboxId() {
            // Given: PENDING 상태의 저장된 Outbox (outboxId = 100L)
            TaskMessageOutbox outbox = TaskMessageOutbox.reconstitute(
                100L, TaskId.of(1L), TaskType.MINI_SHOP,
                TaskMessageStatus.PENDING, 0, null, LocalDateTime.now(), null
            );

            // When: Event 생성
            TaskMessageCreatedEvent event = outbox.createEvent();

            // Then: Event에 outboxId 포함
            assertThat(event).isNotNull();
            assertThat(event.outboxId()).isEqualTo(100L);
        }

        @Test
        @DisplayName("SENT 상태일 때는 Event를 생성하지 않는다 (null 반환)")
        void shouldNotCreateEventWhenSent() {
            // Given: SENT 상태의 Outbox
            TaskMessageOutbox outbox = TaskMessageOutbox.reconstitute(
                1L, TaskId.of(1L), TaskType.MINI_SHOP,
                TaskMessageStatus.SENT, 0, null, LocalDateTime.now(), LocalDateTime.now()
            );

            // When: Event 생성 시도
            TaskMessageCreatedEvent event = outbox.createEvent();

            // Then: null 반환 (Event 생성 안 함)
            assertThat(event).isNull();
        }
    }

    @Nested
    @DisplayName("equals/hashCode 테스트")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("같은 outboxId를 가진 두 Outbox는 동등하다")
        void shouldBeEqualWhenSameOutboxId() {
            // Given
            Long outboxId = 1L;
            TaskMessageOutbox outbox1 = TaskMessageOutbox.reconstitute(
                outboxId, TaskId.of(1L), TaskType.MINI_SHOP,
                TaskMessageStatus.PENDING, 0, null, LocalDateTime.now(), null
            );
            TaskMessageOutbox outbox2 = TaskMessageOutbox.reconstitute(
                outboxId, TaskId.of(2L), TaskType.PRODUCT_DETAIL,
                TaskMessageStatus.SENT, 1, null, LocalDateTime.now(), LocalDateTime.now()
            );

            // When & Then
            assertThat(outbox1).isEqualTo(outbox2);
            assertThat(outbox1.hashCode()).isEqualTo(outbox2.hashCode());
        }

        @Test
        @DisplayName("다른 outboxId를 가진 두 Outbox는 동등하지 않다")
        void shouldNotBeEqualWhenDifferentOutboxId() {
            // Given
            TaskMessageOutbox outbox1 = TaskMessageOutbox.reconstitute(
                1L, TaskId.of(1L), TaskType.MINI_SHOP,
                TaskMessageStatus.PENDING, 0, null, LocalDateTime.now(), null
            );
            TaskMessageOutbox outbox2 = TaskMessageOutbox.reconstitute(
                2L, TaskId.of(1L), TaskType.MINI_SHOP,
                TaskMessageStatus.PENDING, 0, null, LocalDateTime.now(), null
            );

            // When & Then
            assertThat(outbox1).isNotEqualTo(outbox2);
        }

        @Test
        @DisplayName("outboxId가 null인 두 Outbox는 동등하다")
        void shouldBeEqualWhenBothOutboxIdNull() {
            // Given
            TaskMessageOutbox outbox1 = TaskMessageOutbox.createPending(
                TaskId.of(1L), TaskType.MINI_SHOP
            );
            TaskMessageOutbox outbox2 = TaskMessageOutbox.createPending(
                TaskId.of(1L), TaskType.MINI_SHOP
            );

            // When & Then: 두 객체 모두 outboxId가 null이므로 동등
            assertThat(outbox1).isEqualTo(outbox2);
            assertThat(outbox1.hashCode()).isEqualTo(outbox2.hashCode());
        }
    }

    @Nested
    @DisplayName("toString() 테스트")
    class ToStringTests {

        @Test
        @DisplayName("toString()은 주요 필드를 포함한다")
        void shouldIncludeMainFieldsInToString() {
            // Given
            TaskMessageOutbox outbox = TaskMessageOutbox.reconstitute(
                100L, TaskId.of(1L), TaskType.MINI_SHOP,
                TaskMessageStatus.PENDING, 2, null, LocalDateTime.now(), null
            );

            // When
            String result = outbox.toString();

            // Then
            assertThat(result).contains("outboxId=100");
            assertThat(result).contains("taskId=TaskId[value=1]"); // Record 형식
            assertThat(result).contains("taskType=MINI_SHOP");
            assertThat(result).contains("status=PENDING");
            assertThat(result).contains("retryCount=2");
        }
    }

    @Nested
    @DisplayName("통합 시나리오: Outbox 생성 → 실패 → 재시도 → 성공")
    class IntegratedScenario {

        @Test
        @DisplayName("Outbox를 생성하고 SQS 발행 실패 후 재시도하여 성공한다")
        void shouldCreateOutboxRetryAndSucceed() {
            // Given: Outbox 생성 (PENDING)
            TaskMessageOutbox outbox = TaskMessageOutbox.createPending(
                TaskId.of(1L),
                TaskType.PRODUCT_DETAIL
            );
            assertThat(outbox.getStatus()).isEqualTo(TaskMessageStatus.PENDING);
            assertThat(outbox.canRetry()).isTrue();

            // When: SQS 발행 실패 (1회)
            outbox.markFailed("네트워크 오류");

            // Then: 재시도 가능
            assertThat(outbox.getStatus()).isEqualTo(TaskMessageStatus.PENDING);
            assertThat(outbox.getRetryCount()).isEqualTo(1);
            assertThat(outbox.canRetry()).isTrue();

            // When: SQS 발행 실패 (2회)
            outbox.markFailed("타임아웃");

            // Then: 여전히 재시도 가능
            assertThat(outbox.getStatus()).isEqualTo(TaskMessageStatus.PENDING);
            assertThat(outbox.getRetryCount()).isEqualTo(2);
            assertThat(outbox.canRetry()).isTrue();

            // When: SQS 발행 성공
            outbox.markSent();

            // Then: SENT 상태로 전이, 재시도 불가
            assertThat(outbox.getStatus()).isEqualTo(TaskMessageStatus.SENT);
            assertThat(outbox.getSentAt()).isNotNull();
            assertThat(outbox.canRetry()).isFalse();
            assertThat(outbox.getErrorMessage()).isNull(); // 에러 메시지 클리어
        }
    }
}
