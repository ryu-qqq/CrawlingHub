package com.ryuqq.crawlinghub.domain.crawl.schedule;

import com.ryuqq.crawlinghub.domain.schedule.CrawlSchedule;
import com.ryuqq.crawlinghub.domain.schedule.CrawlScheduleId;
import com.ryuqq.crawlinghub.domain.schedule.CronExpression;
import com.ryuqq.crawlinghub.domain.schedule.ScheduleStatus;
import com.ryuqq.crawlinghub.domain.seller.MustItSellerId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * CrawlSchedule Domain Aggregate 단위 테스트
 *
 * <p>테스트 범위:</p>
 * <ul>
 *   <li>Happy Path: 정상 생성 및 비즈니스 메서드</li>
 *   <li>Edge Cases: 경계값 테스트</li>
 *   <li>Exception Cases: 예외 상황 처리</li>
 *   <li>Invariant Validation: 불변식 검증</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-01-30
 */
@DisplayName("CrawlSchedule Domain 단위 테스트")
class CrawlScheduleTest {

    // ========================================
    // Happy Path Tests
    // ========================================

    @Nested
    @DisplayName("생성 테스트")
    class CreateTests {

        @Test
        @DisplayName("유효한 입력으로 신규 CrawlSchedule 생성 성공")
        void shouldCreateNewScheduleWithValidInputs() {
            // Given
            MustItSellerId sellerId = MustItSellerId.of(100L);
            CronExpression cronExpression = CronExpressionFixture.create();

            // When
            CrawlSchedule schedule = CrawlSchedule.forNew(sellerId, cronExpression);

            // Then
            assertThat(schedule).isNotNull();
            assertThat(schedule.getIdValue()).isNull(); // 신규 생성이므로 ID 없음
            assertThat(schedule.getSellerIdValue()).isEqualTo(100L);
            assertThat(schedule.getCronExpressionValue()).isEqualTo(cronExpression.getValue());
            assertThat(schedule.getStatus()).isEqualTo(ScheduleStatus.ACTIVE);
        }

        @Test
        @DisplayName("유효한 입력으로 기존 CrawlSchedule 생성 성공")
        void shouldCreateExistingScheduleWithValidInputs() {
            // Given
            CrawlScheduleId id = CrawlScheduleId.of(1L);
            MustItSellerId sellerId = MustItSellerId.of(100L);
            CronExpression cronExpression = CronExpressionFixture.create();
            ScheduleStatus status = ScheduleStatus.ACTIVE;

            // When
            CrawlSchedule schedule = CrawlSchedule.of(id, sellerId, cronExpression, status);

            // Then
            assertThat(schedule).isNotNull();
            assertThat(schedule.getIdValue()).isEqualTo(1L);
            assertThat(schedule.getStatus()).isEqualTo(status);
        }

        @Test
        @DisplayName("매시간 실행 스케줄 생성 성공")
        void shouldCreateHourlySchedule() {
            // When
            CrawlSchedule schedule = CrawlScheduleFixture.createHourlySchedule();

            // Then
            assertThat(schedule).isNotNull();
            assertThat(schedule.getCronExpressionValue()).contains("0 * * * *");
        }

        @Test
        @DisplayName("매일 실행 스케줄 생성 성공")
        void shouldCreateDailySchedule() {
            // When
            CrawlSchedule schedule = CrawlScheduleFixture.createDailySchedule();

            // Then
            assertThat(schedule).isNotNull();
            assertThat(schedule.getCronExpressionValue()).contains("0 0 * * *");
        }
    }

    @Nested
    @DisplayName("스케줄 업데이트 테스트")
    class UpdateScheduleTests {

        @Test
        @DisplayName("유효한 Cron 표현식으로 스케줄 업데이트 성공")
        void shouldUpdateScheduleWithValidCronExpression() {
            // Given
            CrawlSchedule schedule = CrawlScheduleFixture.createActive();
            CronExpression newExpression = CronExpressionFixture.createDaily();

            // When
            schedule.updateSchedule(newExpression, "test-idem-key");

            // Then
            assertThat(schedule.getCronExpressionValue()).isEqualTo(newExpression.getValue());
            assertThat(schedule.getNextExecutionTime()).isNull(); // 재계산 필요
        }

        @Test
        @DisplayName("스케줄 업데이트 시 다음 실행 시간 초기화")
        void shouldResetNextExecutionTimeWhenScheduleUpdated() {
            // Given
            LocalDateTime nextTime = LocalDateTime.now().plusHours(1);
            CrawlSchedule schedule = CrawlScheduleFixture.createWithNextExecution(nextTime);

            // When
            schedule.updateSchedule(CronExpressionFixture.createHourly(), "test-idem-key");

            // Then
            assertThat(schedule.getNextExecutionTime()).isNull();
        }
    }

    @Nested
    @DisplayName("다음 실행 시간 계산 테스트")
    class NextExecutionTests {

        @Test
        @DisplayName("유효한 다음 실행 시간으로 계산 성공")
        void shouldCalculateNextExecutionTime() {
            // Given
            CrawlSchedule schedule = CrawlScheduleFixture.createActive();
            LocalDateTime nextTime = LocalDateTime.now().plusHours(1);

            // When
            schedule.calculateNextExecution(nextTime);

            // Then
            assertThat(schedule.getNextExecutionTime()).isEqualTo(nextTime);
        }

        @Test
        @DisplayName("현재 시간이 다음 실행 시간과 같으면 실행 가능")
        void shouldBeTimeToExecuteWhenCurrentTimeEqualsNextExecutionTime() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            CrawlSchedule schedule = CrawlScheduleFixture.createWithNextExecution(now);

            // When
            boolean isTimeToExecute = schedule.isTimeToExecute();

            // Then
            assertThat(isTimeToExecute).isTrue();
        }

        @Test
        @DisplayName("현재 시간이 다음 실행 시간 이후면 실행 가능")
        void shouldBeTimeToExecuteWhenCurrentTimeAfterNextExecutionTime() {
            // Given
            LocalDateTime pastTime = LocalDateTime.now().minusHours(1);
            CrawlSchedule schedule = CrawlScheduleFixture.createWithNextExecution(pastTime);

            // When
            boolean isTimeToExecute = schedule.isTimeToExecute();

            // Then
            assertThat(isTimeToExecute).isTrue();
        }

        @Test
        @DisplayName("현재 시간이 다음 실행 시간 이전이면 실행 불가")
        void shouldNotBeTimeToExecuteWhenCurrentTimeBeforeNextExecutionTime() {
            // Given
            LocalDateTime futureTime = LocalDateTime.now().plusHours(1);
            CrawlSchedule schedule = CrawlScheduleFixture.createWithNextExecution(futureTime);

            // When
            boolean isTimeToExecute = schedule.isTimeToExecute();

            // Then
            assertThat(isTimeToExecute).isFalse();
        }

        @Test
        @DisplayName("다음 실행 시간이 null이면 실행 불가")
        void shouldNotBeTimeToExecuteWhenNextExecutionTimeIsNull() {
            // Given
            CrawlSchedule schedule = CrawlScheduleFixture.createActive();
            // nextExecutionTime은 null

            // When
            boolean isTimeToExecute = schedule.isTimeToExecute();

            // Then
            assertThat(isTimeToExecute).isFalse();
        }
    }

    @Nested
    @DisplayName("실행 완료 표시 테스트")
    class MarkExecutedTests {

        @Test
        @DisplayName("실행 완료 표시 시 마지막 실행 시간 갱신")
        void shouldUpdateLastExecutedAtWhenMarkedExecuted() {
            // Given
            CrawlSchedule schedule = CrawlScheduleFixture.createActive();

            // When
            schedule.markExecuted();

            // Then
            assertThat(schedule.getLastExecutedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("활성화/일시정지 테스트")
    class StatusTransitionTests {

        @Test
        @DisplayName("SUSPENDED 상태의 스케줄을 활성화하면 ACTIVE 상태로 변경")
        void shouldActivateSuspendedSchedule() {
            // Given
            CrawlSchedule schedule = CrawlScheduleFixture.createSuspended();

            // When
            schedule.activate();

            // Then
            assertThat(schedule.getStatus()).isEqualTo(ScheduleStatus.ACTIVE);
        }

        @Test
        @DisplayName("ACTIVE 상태의 스케줄을 일시정지하면 SUSPENDED 상태로 변경")
        void shouldSuspendActiveSchedule() {
            // Given
            CrawlSchedule schedule = CrawlScheduleFixture.createActive();

            // When
            schedule.suspend();

            // Then
            assertThat(schedule.getStatus()).isEqualTo(ScheduleStatus.SUSPENDED);
        }

        @Test
        @DisplayName("ACTIVE 상태 확인 - isActive() 메서드")
        void shouldCheckIfScheduleIsActive() {
            // Given
            CrawlSchedule activeSchedule = CrawlScheduleFixture.createActive();
            CrawlSchedule suspendedSchedule = CrawlScheduleFixture.createSuspended();

            // When & Then
            assertThat(activeSchedule.isActive()).isTrue();
            assertThat(suspendedSchedule.isActive()).isFalse();
        }

        @Test
        @DisplayName("특정 상태 확인 - hasStatus() 메서드")
        void shouldCheckIfScheduleHasSpecificStatus() {
            // Given
            CrawlSchedule schedule = CrawlScheduleFixture.createActive();

            // When & Then
            assertThat(schedule.hasStatus(ScheduleStatus.ACTIVE)).isTrue();
            assertThat(schedule.hasStatus(ScheduleStatus.SUSPENDED)).isFalse();
        }
    }

    // ========================================
    // Edge Case Tests
    // ========================================

    @Nested
    @DisplayName("경계값 테스트")
    class EdgeCaseTests {

        @Test
        @DisplayName("최소 ID (1L)로 스케줄 생성 성공")
        void shouldCreateScheduleWithMinimumId() {
            // Given
            CrawlScheduleId id = CrawlScheduleId.of(1L);

            // When
            CrawlSchedule schedule = CrawlScheduleFixture.createWithId(1L);

            // Then
            assertThat(schedule.getIdValue()).isEqualTo(1L);
        }

        @Test
        @DisplayName("최대 ID (Long.MAX_VALUE)로 스케줄 생성 성공")
        void shouldCreateScheduleWithMaximumId() {
            // When
            CrawlSchedule schedule = CrawlScheduleFixture.createWithId(Long.MAX_VALUE);

            // Then
            assertThat(schedule.getIdValue()).isEqualTo(Long.MAX_VALUE);
        }

        @Test
        @DisplayName("과거 시간을 다음 실행 시간으로 설정 가능")
        void shouldAcceptPastTimeAsNextExecutionTime() {
            // Given
            CrawlSchedule schedule = CrawlScheduleFixture.createActive();
            LocalDateTime pastTime = LocalDateTime.now().minusDays(1);

            // When
            schedule.calculateNextExecution(pastTime);

            // Then
            assertThat(schedule.getNextExecutionTime()).isEqualTo(pastTime);
            assertThat(schedule.isTimeToExecute()).isTrue(); // 과거 시간이므로 즉시 실행 가능
        }

        @Test
        @DisplayName("먼 미래 시간을 다음 실행 시간으로 설정 가능")
        void shouldAcceptFarFutureTimeAsNextExecutionTime() {
            // Given
            CrawlSchedule schedule = CrawlScheduleFixture.createActive();
            LocalDateTime farFuture = LocalDateTime.now().plusYears(100);

            // When
            schedule.calculateNextExecution(farFuture);

            // Then
            assertThat(schedule.getNextExecutionTime()).isEqualTo(farFuture);
            assertThat(schedule.isTimeToExecute()).isFalse(); // 먼 미래이므로 실행 불가
        }
    }

    // ========================================
    // Exception Tests
    // ========================================

    @Nested
    @DisplayName("예외 케이스 테스트")
    class ExceptionTests {

        @Test
        @DisplayName("ID가 null인 채로 of() 호출하면 예외 발생")
        void shouldThrowExceptionWhenIdIsNullInOfMethod() {
            // When & Then
            assertThatThrownBy(() ->
                CrawlSchedule.of(
                    null,
                    MustItSellerId.of(100L),
                    CronExpressionFixture.create(),
                    ScheduleStatus.ACTIVE
                )
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CrawlSchedule ID는 필수입니다");
        }

        @Test
        @DisplayName("셀러 ID가 null이면 예외 발생")
        void shouldThrowExceptionWhenSellerIdIsNull() {
            // When & Then
            assertThatThrownBy(() ->
                CrawlSchedule.forNew(null, CronExpressionFixture.create())
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("셀러 ID는 필수입니다");
        }

        @Test
        @DisplayName("Cron 표현식이 null이면 예외 발생")
        void shouldThrowExceptionWhenCronExpressionIsNull() {
            // When & Then
            assertThatThrownBy(() ->
                CrawlSchedule.forNew(MustItSellerId.of(100L), null)
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cron 표현식은 필수입니다");
        }

        @Test
        @DisplayName("스케줄 상태가 null이면 예외 발생")
        void shouldThrowExceptionWhenStatusIsNull() {
            // When & Then
            assertThatThrownBy(() ->
                CrawlSchedule.of(
                    CrawlScheduleId.of(1L),
                    MustItSellerId.of(100L),
                    CronExpressionFixture.create(),
                    null
                )
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("스케줄 상태는 필수입니다");
        }

        @Test
        @DisplayName("스케줄 업데이트 시 null Cron 표현식이면 예외 발생")
        void shouldThrowExceptionWhenUpdatingWithNullCronExpression() {
            // Given
            CrawlSchedule schedule = CrawlScheduleFixture.createActive();

            // When & Then
            assertThatThrownBy(() -> schedule.updateSchedule(null, "test-idem-key"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cron 표현식은 null일 수 없습니다");
        }

        @Test
        @DisplayName("다음 실행 시간 계산 시 null이면 예외 발생")
        void shouldThrowExceptionWhenCalculatingWithNullNextTime() {
            // Given
            CrawlSchedule schedule = CrawlScheduleFixture.createActive();

            // When & Then
            assertThatThrownBy(() -> schedule.calculateNextExecution(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("다음 실행 시간은 null일 수 없습니다");
        }

        @Test
        @DisplayName("DB reconstitute 시 ID가 null이면 예외 발생")
        void shouldThrowExceptionWhenReconstitutingWithNullId() {
            // When & Then
            assertThatThrownBy(() ->
                CrawlSchedule.reconstitute(
                    null,
                    MustItSellerId.of(100L),
                    CronExpressionFixture.create(),
                    ScheduleStatus.ACTIVE,
                    null,
                    null,
                    null,
                    null
                )
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DB reconstitute는 ID가 필수입니다");
        }
    }

    // ========================================
    // Invariant Validation Tests
    // ========================================

    @Nested
    @DisplayName("불변식 검증 테스트")
    class InvariantTests {

        @Test
        @DisplayName("모든 상태 전환 후에도 필수 필드는 유지됨")
        void shouldMaintainRequiredFieldsAfterStatusTransitions() {
            // Given
            CrawlSchedule schedule = CrawlScheduleFixture.createActive();

            // When - 여러 상태 전환
            schedule.suspend();
            schedule.activate();
            schedule.suspend();

            // Then - 필수 필드 유지
            assertThat(schedule.getSellerIdValue()).isNotNull();
            assertThat(schedule.getCronExpressionValue()).isNotNull();
            assertThat(schedule.getStatus()).isNotNull();
        }

        @Test
        @DisplayName("스케줄 업데이트 후에도 필수 필드는 유지됨")
        void shouldMaintainRequiredFieldsAfterScheduleUpdate() {
            // Given
            CrawlSchedule schedule = CrawlScheduleFixture.createActive();

            // When
            schedule.updateSchedule(CronExpressionFixture.createDaily(), "test-idem-key-1");
            schedule.updateSchedule(CronExpressionFixture.createHourly(), "test-idem-key-2");

            // Then
            assertThat(schedule.getSellerIdValue()).isNotNull();
            assertThat(schedule.getCronExpressionValue()).isNotNull();
            assertThat(schedule.getStatus()).isNotNull();
        }

        @Test
        @DisplayName("상태는 항상 유효한 ScheduleStatus 값이어야 함")
        void shouldAlwaysHaveValidScheduleStatus() {
            // Given
            CrawlSchedule schedule = CrawlScheduleFixture.createActive();

            // When - 다양한 상태 전환
            schedule.suspend();
            assertThat(schedule.getStatus()).isIn(
                ScheduleStatus.ACTIVE,
                ScheduleStatus.SUSPENDED
            );

            schedule.activate();
            assertThat(schedule.getStatus()).isIn(
                ScheduleStatus.ACTIVE,
                ScheduleStatus.SUSPENDED
            );
        }
    }

    // ========================================
    // Law of Demeter Tests
    // ========================================

    @Nested
    @DisplayName("Law of Demeter 준수 테스트")
    class LawOfDemeterTests {

        @Test
        @DisplayName("getIdValue()는 ID를 직접 노출하지 않고 값만 반환")
        void shouldReturnIdValueWithoutExposingIdObject() {
            // Given
            CrawlSchedule schedule = CrawlScheduleFixture.createWithId(100L);

            // When
            Long idValue = schedule.getIdValue();

            // Then
            assertThat(idValue).isEqualTo(100L);
        }

        @Test
        @DisplayName("getSellerIdValue()는 셀러 ID를 직접 노출하지 않고 값만 반환")
        void shouldReturnSellerIdValueWithoutExposingSellerIdObject() {
            // Given
            CrawlSchedule schedule = CrawlScheduleFixture.createWithSellerId(200L);

            // When
            Long sellerIdValue = schedule.getSellerIdValue();

            // Then
            assertThat(sellerIdValue).isEqualTo(200L);
        }

        @Test
        @DisplayName("getCronExpressionValue()는 Cron 표현식을 직접 노출하지 않고 값만 반환")
        void shouldReturnCronExpressionValueWithoutExposingCronObject() {
            // Given
            CronExpression cronExpression = CronExpressionFixture.create();
            CrawlSchedule schedule = CrawlScheduleFixture.createWithCron(cronExpression);

            // When
            String cronValue = schedule.getCronExpressionValue();

            // Then
            assertThat(cronValue).isEqualTo(cronExpression.getValue());
        }
    }
}
