package com.ryuqq.crawlinghub.domain.schedule;

import com.ryuqq.crawlinghub.domain.schedule.event.ScheduleCreatedEvent;
import com.ryuqq.crawlinghub.domain.schedule.event.ScheduleUpdatedEvent;
import com.ryuqq.crawlinghub.domain.seller.MustitSellerId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CrawlSchedule 테스트")
class CrawlScheduleTest {

    private Clock fixedClock;
    private LocalDateTime fixedNow;

    @BeforeEach
    void setUp() {
        fixedNow = LocalDateTime.of(2025, 1, 10, 12, 0, 0);
        fixedClock = Clock.fixed(
            fixedNow.atZone(ZoneId.systemDefault()).toInstant(),
            ZoneId.systemDefault()
        );
    }

    @Nested
    @DisplayName("forNew() 팩토리 메서드 테스트")
    class ForNewFactoryMethodTests {

        @Test
        @DisplayName("유효한 값으로 새 CrawlSchedule 생성")
        void shouldCreateNewSchedule() {
            // Given
            MustitSellerId sellerId = MustitSellerId.of(100L);
            CronExpression cronExpression = CronExpression.of("0 0 * * * *");

            // When
            CrawlSchedule schedule = CrawlSchedule.forNew(sellerId, cronExpression);

            // Then
            assertThat(schedule).isNotNull();
            assertThat(schedule.getIdValue()).isNull();  // 새 객체는 ID 없음
            assertThat(schedule.getSellerIdValue()).isEqualTo(sellerId.value());
            assertThat(schedule.getCronExpressionValue()).isEqualTo(cronExpression.getValue());
            assertThat(schedule.getStatus()).isEqualTo(ScheduleStatus.ACTIVE);
        }

        @Test
        @DisplayName("sellerId가 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionForNullSellerId() {
            // Given
            CronExpression cronExpression = CronExpression.of("0 0 * * * *");

            // When & Then
            assertThatThrownBy(() -> CrawlSchedule.forNew(null, cronExpression))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("셀러 ID는 필수입니다");
        }

        @Test
        @DisplayName("cronExpression이 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionForNullCronExpression() {
            // Given
            MustitSellerId sellerId = MustitSellerId.of(100L);

            // When & Then
            assertThatThrownBy(() -> CrawlSchedule.forNew(sellerId, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cron 표현식은 필수입니다");
        }
    }

    @Nested
    @DisplayName("of() 팩토리 메서드 테스트")
    class OfFactoryMethodTests {

        @Test
        @DisplayName("유효한 값으로 기존 CrawlSchedule 생성")
        void shouldCreateExistingSchedule() {
            // Given
            CrawlScheduleId id = CrawlScheduleId.of(1L);
            MustitSellerId sellerId = MustitSellerId.of(100L);
            CronExpression cronExpression = CronExpression.of("0 0 * * * *");
            ScheduleStatus status = ScheduleStatus.ACTIVE;

            // When
            CrawlSchedule schedule = CrawlSchedule.of(id, sellerId, cronExpression, status);

            // Then
            assertThat(schedule).isNotNull();
            assertThat(schedule.getIdValue()).isEqualTo(id.value());
            assertThat(schedule.getSellerIdValue()).isEqualTo(sellerId.value());
            assertThat(schedule.getCronExpressionValue()).isEqualTo(cronExpression.getValue());
            assertThat(schedule.getStatus()).isEqualTo(status);
        }

        @Test
        @DisplayName("id가 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionForNullId() {
            // Given
            MustitSellerId sellerId = MustitSellerId.of(100L);
            CronExpression cronExpression = CronExpression.of("0 0 * * * *");
            ScheduleStatus status = ScheduleStatus.ACTIVE;

            // When & Then
            assertThatThrownBy(() -> CrawlSchedule.of(null, sellerId, cronExpression, status))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CrawlSchedule ID는 필수입니다");
        }

        @Test
        @DisplayName("sellerId가 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionForNullSellerId() {
            // Given
            CrawlScheduleId id = CrawlScheduleId.of(1L);
            CronExpression cronExpression = CronExpression.of("0 0 * * * *");
            ScheduleStatus status = ScheduleStatus.ACTIVE;

            // When & Then
            assertThatThrownBy(() -> CrawlSchedule.of(id, null, cronExpression, status))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("셀러 ID는 필수입니다");
        }

        @Test
        @DisplayName("cronExpression이 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionForNullCronExpression() {
            // Given
            CrawlScheduleId id = CrawlScheduleId.of(1L);
            MustitSellerId sellerId = MustitSellerId.of(100L);
            ScheduleStatus status = ScheduleStatus.ACTIVE;

            // When & Then
            assertThatThrownBy(() -> CrawlSchedule.of(id, sellerId, null, status))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cron 표현식은 필수입니다");
        }

        @Test
        @DisplayName("status가 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionForNullStatus() {
            // Given
            CrawlScheduleId id = CrawlScheduleId.of(1L);
            MustitSellerId sellerId = MustitSellerId.of(100L);
            CronExpression cronExpression = CronExpression.of("0 0 * * * *");

            // When & Then
            assertThatThrownBy(() -> CrawlSchedule.of(id, sellerId, cronExpression, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("스케줄 상태는 필수입니다");
        }
    }

    @Nested
    @DisplayName("reconstitute() 팩토리 메서드 테스트")
    class ReconstituteFactoryMethodTests {

        @Test
        @DisplayName("모든 필드로 CrawlSchedule 재구성")
        void shouldReconstituteSchedule() {
            // Given
            CrawlScheduleId id = CrawlScheduleId.of(1L);
            MustitSellerId sellerId = MustitSellerId.of(100L);
            CronExpression cronExpression = CronExpression.of("0 0 * * * *");
            ScheduleStatus status = ScheduleStatus.ACTIVE;
            LocalDateTime nextExecutionTime = fixedNow.plusHours(1);
            LocalDateTime lastExecutedAt = fixedNow.minusHours(1);
            LocalDateTime createdAt = fixedNow.minusDays(1);
            LocalDateTime updatedAt = fixedNow;

            // When
            CrawlSchedule schedule = CrawlSchedule.reconstitute(
                id, sellerId, cronExpression, status,
                nextExecutionTime, lastExecutedAt, createdAt, updatedAt
            );

            // Then
            assertThat(schedule).isNotNull();
            assertThat(schedule.getIdValue()).isEqualTo(id.value());
            assertThat(schedule.getSellerIdValue()).isEqualTo(sellerId.value());
            assertThat(schedule.getCronExpressionValue()).isEqualTo(cronExpression.getValue());
            assertThat(schedule.getStatus()).isEqualTo(status);
            assertThat(schedule.getNextExecutionTime()).isEqualTo(nextExecutionTime);
            assertThat(schedule.getLastExecutedAt()).isEqualTo(lastExecutedAt);
        }

        @Test
        @DisplayName("reconstitute는 ID 필수")
        void shouldRequireIdForReconstitute() {
            // Given
            MustitSellerId sellerId = MustitSellerId.of(100L);
            CronExpression cronExpression = CronExpression.of("0 0 * * * *");
            ScheduleStatus status = ScheduleStatus.ACTIVE;

            // When & Then
            assertThatThrownBy(() -> CrawlSchedule.reconstitute(
                null, sellerId, cronExpression, status, null, null, fixedNow, fixedNow
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DB reconstitute는 ID가 필수입니다");
        }
    }

    @Nested
    @DisplayName("Domain Events 관리 테스트")
    class DomainEventsManagementTests {

        @Test
        @DisplayName("getDomainEvents()는 초기에 빈 리스트 반환")
        void shouldReturnEmptyEventsInitially() {
            // Given
            CrawlSchedule schedule = CrawlSchedule.forNew(
                MustitSellerId.of(100L),
                CronExpression.of("0 0 * * * *")
            );

            // When
            var events = schedule.getDomainEvents();

            // Then
            assertThat(events).isEmpty();
        }

        @Test
        @DisplayName("clearDomainEvents()는 모든 이벤트 제거")
        void shouldClearAllEvents() {
            // Given
            CrawlSchedule schedule = CrawlSchedule.of(
                CrawlScheduleId.of(1L),
                MustitSellerId.of(100L),
                CronExpression.of("0 0 * * * *"),
                ScheduleStatus.ACTIVE
            );
            schedule.publishCreatedEvent("idem-key-123");

            // When
            schedule.clearDomainEvents();

            // Then
            assertThat(schedule.getDomainEvents()).isEmpty();
        }
    }

    @Nested
    @DisplayName("publishCreatedEvent() 테스트")
    class PublishCreatedEventTests {

        @Test
        @DisplayName("유효한 idemKey로 ScheduleCreatedEvent 발행")
        void shouldPublishCreatedEvent() {
            // Given
            CrawlSchedule schedule = CrawlSchedule.of(
                CrawlScheduleId.of(1L),
                MustitSellerId.of(100L),
                CronExpression.of("0 0 * * * *"),
                ScheduleStatus.ACTIVE
            );
            String idemKey = "idem-key-123";

            // When
            schedule.publishCreatedEvent(idemKey);

            // Then
            assertThat(schedule.getDomainEvents()).hasSize(1);
            assertThat(schedule.getDomainEvents().get(0)).isInstanceOf(ScheduleCreatedEvent.class);

            ScheduleCreatedEvent event = (ScheduleCreatedEvent) schedule.getDomainEvents().get(0);
            assertThat(event.scheduleId()).isEqualTo(1L);
            assertThat(event.sellerId()).isEqualTo(100L);
            assertThat(event.cronExpression()).isEqualTo("0 0 * * * *");
            assertThat(event.outboxIdemKey()).isEqualTo(idemKey);
        }

        @Test
        @DisplayName("ID 없이 publishCreatedEvent() 호출 시 IllegalStateException 발생")
        void shouldThrowExceptionWhenPublishingWithoutId() {
            // Given
            CrawlSchedule schedule = CrawlSchedule.forNew(
                MustitSellerId.of(100L),
                CronExpression.of("0 0 * * * *")
            );

            // When & Then
            assertThatThrownBy(() -> schedule.publishCreatedEvent("idem-key"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("스케줄 ID가 없어 이벤트를 발행할 수 없습니다");
        }
    }

    @Nested
    @DisplayName("updateSchedule() 테스트")
    class UpdateScheduleTests {

        @Test
        @DisplayName("새 Cron 표현식으로 스케줄 업데이트 및 이벤트 발행")
        void shouldUpdateScheduleAndPublishEvent() {
            // Given
            CrawlSchedule schedule = CrawlSchedule.of(
                CrawlScheduleId.of(1L),
                MustitSellerId.of(100L),
                CronExpression.of("0 0 * * * *"),
                ScheduleStatus.ACTIVE
            );
            CronExpression newExpression = CronExpression.of("0 */5 * * * ?");
            String idemKey = "update-idem-123";

            // When
            schedule.updateSchedule(newExpression, idemKey);

            // Then
            assertThat(schedule.getCronExpressionValue()).isEqualTo(newExpression.getValue());
            assertThat(schedule.getNextExecutionTime()).isNull();  // 재계산 필요
            assertThat(schedule.getDomainEvents()).hasSize(1);
            assertThat(schedule.getDomainEvents().get(0)).isInstanceOf(ScheduleUpdatedEvent.class);

            ScheduleUpdatedEvent event = (ScheduleUpdatedEvent) schedule.getDomainEvents().get(0);
            assertThat(event.scheduleId()).isEqualTo(1L);
            assertThat(event.sellerId()).isEqualTo(100L);
            assertThat(event.cronExpression()).isEqualTo("0 */5 * * * ?");
            assertThat(event.outboxIdemKey()).isEqualTo(idemKey);
        }

        @Test
        @DisplayName("null Cron 표현식으로 업데이트 시 IllegalArgumentException 발생")
        void shouldThrowExceptionForNullExpression() {
            // Given
            CrawlSchedule schedule = CrawlSchedule.of(
                CrawlScheduleId.of(1L),
                MustitSellerId.of(100L),
                CronExpression.of("0 0 * * * *"),
                ScheduleStatus.ACTIVE
            );

            // When & Then
            assertThatThrownBy(() -> schedule.updateSchedule(null, "idem-key"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cron 표현식은 null일 수 없습니다");
        }

        @Test
        @DisplayName("ID 없이 updateSchedule() 호출 시 IllegalStateException 발생")
        void shouldThrowExceptionWhenUpdatingWithoutId() {
            // Given
            CrawlSchedule schedule = CrawlSchedule.forNew(
                MustitSellerId.of(100L),
                CronExpression.of("0 0 * * * *")
            );
            CronExpression newExpression = CronExpression.of("0 */5 * * * ?");

            // When & Then
            assertThatThrownBy(() -> schedule.updateSchedule(newExpression, "idem-key"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("스케줄 ID가 없어 업데이트할 수 없습니다");
        }
    }

    @Nested
    @DisplayName("calculateNextExecution() 테스트")
    class CalculateNextExecutionTests {

        @Test
        @DisplayName("다음 실행 시간 계산")
        void shouldCalculateNextExecution() {
            // Given
            CrawlSchedule schedule = CrawlSchedule.of(
                CrawlScheduleId.of(1L),
                MustitSellerId.of(100L),
                CronExpression.of("0 0 * * * *"),
                ScheduleStatus.ACTIVE
            );
            LocalDateTime nextTime = fixedNow.plusHours(1);

            // When
            schedule.calculateNextExecution(nextTime);

            // Then
            assertThat(schedule.getNextExecutionTime()).isEqualTo(nextTime);
        }

        @Test
        @DisplayName("null 다음 실행 시간은 IllegalArgumentException 발생")
        void shouldThrowExceptionForNullNextTime() {
            // Given
            CrawlSchedule schedule = CrawlSchedule.of(
                CrawlScheduleId.of(1L),
                MustitSellerId.of(100L),
                CronExpression.of("0 0 * * * *"),
                ScheduleStatus.ACTIVE
            );

            // When & Then
            assertThatThrownBy(() -> schedule.calculateNextExecution(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("다음 실행 시간은 null일 수 없습니다");
        }
    }

    @Nested
    @DisplayName("markExecuted() 테스트")
    class MarkExecutedTests {

        @Test
        @DisplayName("실행 완료 표시 및 lastExecutedAt 업데이트")
        void shouldMarkAsExecuted() {
            // Given
            CrawlSchedule schedule = CrawlSchedule.of(
                CrawlScheduleId.of(1L),
                MustitSellerId.of(100L),
                CronExpression.of("0 0 * * * *"),
                ScheduleStatus.ACTIVE
            );

            // When
            schedule.markExecuted();

            // Then
            assertThat(schedule.getLastExecutedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("isTimeToExecute() 테스트")
    class IsTimeToExecuteTests {

        @Test
        @DisplayName("nextExecutionTime이 현재보다 이전이면 true 반환")
        void shouldReturnTrueWhenTimeHasCome() {
            // Given
            CrawlSchedule schedule = CrawlSchedule.of(
                CrawlScheduleId.of(1L),
                MustitSellerId.of(100L),
                CronExpression.of("0 0 * * * *"),
                ScheduleStatus.ACTIVE
            );
            schedule.calculateNextExecution(fixedNow.minusMinutes(1));

            // When
            boolean isTime = schedule.isTimeToExecute();

            // Then
            assertThat(isTime).isTrue();
        }

        @Test
        @DisplayName("nextExecutionTime이 현재보다 이후면 false 반환")
        void shouldReturnFalseWhenTimeHasNotCome() {
            // Given - nextExecutionTime을 충분히 미래(1년 후)로 설정
            LocalDateTime futureTime = LocalDateTime.now().plusYears(1);
            CrawlSchedule schedule = CrawlSchedule.reconstitute(
                CrawlScheduleId.of(1L),
                MustitSellerId.of(100L),
                CronExpression.of("0 0 * * * *"),
                ScheduleStatus.ACTIVE,
                futureTime,  // 1년 후
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
            );

            // When
            boolean isTime = schedule.isTimeToExecute();

            // Then
            assertThat(isTime).isFalse();
        }

        @Test
        @DisplayName("nextExecutionTime이 null이면 false 반환")
        void shouldReturnFalseWhenNextExecutionTimeIsNull() {
            // Given
            CrawlSchedule schedule = CrawlSchedule.of(
                CrawlScheduleId.of(1L),
                MustitSellerId.of(100L),
                CronExpression.of("0 0 * * * *"),
                ScheduleStatus.ACTIVE
            );

            // When
            boolean isTime = schedule.isTimeToExecute();

            // Then
            assertThat(isTime).isFalse();
        }
    }

    @Nested
    @DisplayName("activate() 및 suspend() 테스트")
    class ActivateAndSuspendTests {

        @Test
        @DisplayName("activate()는 상태를 ACTIVE로 변경")
        void shouldActivateSchedule() {
            // Given
            CrawlSchedule schedule = CrawlSchedule.of(
                CrawlScheduleId.of(1L),
                MustitSellerId.of(100L),
                CronExpression.of("0 0 * * * *"),
                ScheduleStatus.SUSPENDED
            );

            // When
            schedule.activate();

            // Then
            assertThat(schedule.getStatus()).isEqualTo(ScheduleStatus.ACTIVE);
            assertThat(schedule.isActive()).isTrue();
        }

        @Test
        @DisplayName("suspend()는 상태를 SUSPENDED로 변경")
        void shouldSuspendSchedule() {
            // Given
            CrawlSchedule schedule = CrawlSchedule.of(
                CrawlScheduleId.of(1L),
                MustitSellerId.of(100L),
                CronExpression.of("0 0 * * * *"),
                ScheduleStatus.ACTIVE
            );

            // When
            schedule.suspend();

            // Then
            assertThat(schedule.getStatus()).isEqualTo(ScheduleStatus.SUSPENDED);
            assertThat(schedule.isActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("isActive() 및 hasStatus() 테스트")
    class IsActiveAndHasStatusTests {

        @Test
        @DisplayName("ACTIVE 상태면 isActive() true 반환")
        void shouldReturnTrueForActiveStatus() {
            // Given
            CrawlSchedule schedule = CrawlSchedule.of(
                CrawlScheduleId.of(1L),
                MustitSellerId.of(100L),
                CronExpression.of("0 0 * * * *"),
                ScheduleStatus.ACTIVE
            );

            // When
            boolean isActive = schedule.isActive();

            // Then
            assertThat(isActive).isTrue();
        }

        @Test
        @DisplayName("SUSPENDED 상태면 isActive() false 반환")
        void shouldReturnFalseForSuspendedStatus() {
            // Given
            CrawlSchedule schedule = CrawlSchedule.of(
                CrawlScheduleId.of(1L),
                MustitSellerId.of(100L),
                CronExpression.of("0 0 * * * *"),
                ScheduleStatus.SUSPENDED
            );

            // When
            boolean isActive = schedule.isActive();

            // Then
            assertThat(isActive).isFalse();
        }

        @Test
        @DisplayName("hasStatus()는 주어진 상태와 일치하면 true 반환")
        void shouldReturnTrueWhenStatusMatches() {
            // Given
            CrawlSchedule schedule = CrawlSchedule.of(
                CrawlScheduleId.of(1L),
                MustitSellerId.of(100L),
                CronExpression.of("0 0 * * * *"),
                ScheduleStatus.ACTIVE
            );

            // When
            boolean hasStatus = schedule.hasStatus(ScheduleStatus.ACTIVE);

            // Then
            assertThat(hasStatus).isTrue();
        }

        @Test
        @DisplayName("hasStatus()는 주어진 상태와 다르면 false 반환")
        void shouldReturnFalseWhenStatusDoesNotMatch() {
            // Given
            CrawlSchedule schedule = CrawlSchedule.of(
                CrawlScheduleId.of(1L),
                MustitSellerId.of(100L),
                CronExpression.of("0 0 * * * *"),
                ScheduleStatus.ACTIVE
            );

            // When
            boolean hasStatus = schedule.hasStatus(ScheduleStatus.SUSPENDED);

            // Then
            assertThat(hasStatus).isFalse();
        }
    }

    @Nested
    @DisplayName("Law of Demeter 메서드 테스트")
    class LawOfDemeterTests {

        @Test
        @DisplayName("getIdValue()는 ID 값 반환")
        void shouldReturnIdValue() {
            // Given
            CrawlSchedule schedule = CrawlSchedule.of(
                CrawlScheduleId.of(123L),
                MustitSellerId.of(100L),
                CronExpression.of("0 0 * * * *"),
                ScheduleStatus.ACTIVE
            );

            // When
            Long idValue = schedule.getIdValue();

            // Then
            assertThat(idValue).isEqualTo(123L);
        }

        @Test
        @DisplayName("getIdValue()는 ID가 null이면 null 반환")
        void shouldReturnNullWhenIdIsNull() {
            // Given
            CrawlSchedule schedule = CrawlSchedule.forNew(
                MustitSellerId.of(100L),
                CronExpression.of("0 0 * * * *")
            );

            // When
            Long idValue = schedule.getIdValue();

            // Then
            assertThat(idValue).isNull();
        }

        @Test
        @DisplayName("getSellerIdValue()는 Seller ID 값 반환")
        void shouldReturnSellerIdValue() {
            // Given
            CrawlSchedule schedule = CrawlSchedule.of(
                CrawlScheduleId.of(1L),
                MustitSellerId.of(999L),
                CronExpression.of("0 0 * * * *"),
                ScheduleStatus.ACTIVE
            );

            // When
            Long sellerIdValue = schedule.getSellerIdValue();

            // Then
            assertThat(sellerIdValue).isEqualTo(999L);
        }

        @Test
        @DisplayName("getCronExpressionValue()는 Cron 표현식 값 반환")
        void shouldReturnCronExpressionValue() {
            // Given
            String expression = "0 0 12 * * ?";
            CrawlSchedule schedule = CrawlSchedule.of(
                CrawlScheduleId.of(1L),
                MustitSellerId.of(100L),
                CronExpression.of(expression),
                ScheduleStatus.ACTIVE
            );

            // When
            String cronValue = schedule.getCronExpressionValue();

            // Then
            assertThat(cronValue).isEqualTo(expression);
        }
    }

    @Nested
    @DisplayName("toEventBridgePayload() 테스트")
    class ToEventBridgePayloadTests {

        @Test
        @DisplayName("유효한 EventBridgePayload 생성")
        void shouldCreateEventBridgePayload() {
            // Given
            CrawlSchedule schedule = CrawlSchedule.of(
                CrawlScheduleId.of(1L),
                MustitSellerId.of(100L),
                CronExpression.of("0 0 * * * *"),
                ScheduleStatus.ACTIVE
            );

            // When
            CrawlSchedule.EventBridgePayload payload = schedule.toEventBridgePayload();

            // Then
            assertThat(payload).isNotNull();
            assertThat(payload.scheduleId()).isEqualTo(1L);
            assertThat(payload.sellerId()).isEqualTo(100L);
            assertThat(payload.cronExpression()).isEqualTo("0 0 * * * *");
        }

        @Test
        @DisplayName("ID 없이 toEventBridgePayload() 호출 시 IllegalStateException 발생")
        void shouldThrowExceptionWhenCreatingPayloadWithoutId() {
            // Given
            CrawlSchedule schedule = CrawlSchedule.forNew(
                MustitSellerId.of(100L),
                CronExpression.of("0 0 * * * *")
            );

            // When & Then
            assertThatThrownBy(() -> schedule.toEventBridgePayload())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("스케줄 ID가 없어 EventBridge 페이로드를 생성할 수 없습니다");
        }
    }

    @Nested
    @DisplayName("EventBridgePayload Record 테스트")
    class EventBridgePayloadRecordTests {

        @Test
        @DisplayName("유효한 값으로 EventBridgePayload 생성")
        void shouldCreatePayloadWithValidValues() {
            // When
            CrawlSchedule.EventBridgePayload payload = new CrawlSchedule.EventBridgePayload(
                1L, 100L, "0 0 * * * *"
            );

            // Then
            assertThat(payload.scheduleId()).isEqualTo(1L);
            assertThat(payload.sellerId()).isEqualTo(100L);
            assertThat(payload.cronExpression()).isEqualTo("0 0 * * * *");
        }

        @Test
        @DisplayName("scheduleId가 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionForNullScheduleId() {
            // When & Then
            assertThatThrownBy(() -> new CrawlSchedule.EventBridgePayload(null, 100L, "0 0 * * * *"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("스케줄 ID는 필수입니다");
        }

        @Test
        @DisplayName("sellerId가 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionForNullSellerId() {
            // When & Then
            assertThatThrownBy(() -> new CrawlSchedule.EventBridgePayload(1L, null, "0 0 * * * *"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("셀러 ID는 필수입니다");
        }

        @Test
        @DisplayName("cronExpression이 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionForNullCronExpression() {
            // When & Then
            assertThatThrownBy(() -> new CrawlSchedule.EventBridgePayload(1L, 100L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cron 표현식은 필수입니다");
        }

        @Test
        @DisplayName("cronExpression이 빈 문자열이면 IllegalArgumentException 발생")
        void shouldThrowExceptionForBlankCronExpression() {
            // When & Then
            assertThatThrownBy(() -> new CrawlSchedule.EventBridgePayload(1L, 100L, "  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cron 표현식은 필수입니다");
        }
    }

    @Nested
    @DisplayName("toResponse() 테스트")
    class ToResponseTests {

        @Test
        @DisplayName("유효한 ScheduleResponseData 생성")
        void shouldCreateScheduleResponse() {
            // Given
            LocalDateTime nextExecution = fixedNow.plusHours(1);
            LocalDateTime lastExecuted = fixedNow.minusHours(1);
            CrawlSchedule schedule = CrawlSchedule.reconstitute(
                CrawlScheduleId.of(1L),
                MustitSellerId.of(100L),
                CronExpression.of("0 0 * * * *"),
                ScheduleStatus.ACTIVE,
                nextExecution,
                lastExecuted,
                fixedNow.minusDays(1),
                fixedNow
            );

            // When
            CrawlSchedule.ScheduleResponseData response = schedule.toResponse();

            // Then
            assertThat(response).isNotNull();
            assertThat(response.scheduleId()).isEqualTo(1L);
            assertThat(response.sellerId()).isEqualTo(100L);
            assertThat(response.cronExpression()).isEqualTo("0 0 * * * *");
            assertThat(response.status()).isEqualTo(ScheduleStatus.ACTIVE);
            assertThat(response.nextExecutionTime()).isEqualTo(nextExecution);
            assertThat(response.lastExecutedAt()).isEqualTo(lastExecuted);
        }

        @Test
        @DisplayName("ID 없이 toResponse() 호출 시 IllegalStateException 발생")
        void shouldThrowExceptionWhenCreatingResponseWithoutId() {
            // Given
            CrawlSchedule schedule = CrawlSchedule.forNew(
                MustitSellerId.of(100L),
                CronExpression.of("0 0 * * * *")
            );

            // When & Then
            assertThatThrownBy(() -> schedule.toResponse())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("스케줄 ID가 없어 Response를 생성할 수 없습니다");
        }
    }

    @Nested
    @DisplayName("equals() 및 hashCode() 테스트")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("같은 ID를 가진 두 객체는 동일")
        void shouldBeEqualForSameId() {
            // Given
            CrawlScheduleId id = CrawlScheduleId.of(1L);
            CrawlSchedule schedule1 = CrawlSchedule.of(
                id, MustitSellerId.of(100L), CronExpression.of("0 0 * * * *"), ScheduleStatus.ACTIVE
            );
            CrawlSchedule schedule2 = CrawlSchedule.of(
                id, MustitSellerId.of(200L), CronExpression.of("0 */5 * * * ?"), ScheduleStatus.SUSPENDED
            );

            // Then
            assertThat(schedule1).isEqualTo(schedule2);
            assertThat(schedule1.hashCode()).isEqualTo(schedule2.hashCode());
        }

        @Test
        @DisplayName("다른 ID를 가진 두 객체는 다름")
        void shouldNotBeEqualForDifferentId() {
            // Given
            CrawlSchedule schedule1 = CrawlSchedule.of(
                CrawlScheduleId.of(1L), MustitSellerId.of(100L),
                CronExpression.of("0 0 * * * *"), ScheduleStatus.ACTIVE
            );
            CrawlSchedule schedule2 = CrawlSchedule.of(
                CrawlScheduleId.of(2L), MustitSellerId.of(100L),
                CronExpression.of("0 0 * * * *"), ScheduleStatus.ACTIVE
            );

            // Then
            assertThat(schedule1).isNotEqualTo(schedule2);
        }

        @Test
        @DisplayName("ID가 null인 객체는 동일함 (ID가 null이면 equals는 true)")
        void shouldBeEqualWhenIdIsNull() {
            // Given
            CrawlSchedule schedule1 = CrawlSchedule.forNew(
                MustitSellerId.of(100L), CronExpression.of("0 0 * * * *")
            );
            CrawlSchedule schedule2 = CrawlSchedule.forNew(
                MustitSellerId.of(100L), CronExpression.of("0 0 * * * *")
            );

            // Then - Objects.equals(null, null) = true
            assertThat(schedule1).isEqualTo(schedule2);
        }

        @Test
        @DisplayName("null과는 동일하지 않음")
        void shouldNotBeEqualToNull() {
            // Given
            CrawlSchedule schedule = CrawlSchedule.of(
                CrawlScheduleId.of(1L), MustitSellerId.of(100L),
                CronExpression.of("0 0 * * * *"), ScheduleStatus.ACTIVE
            );

            // Then
            assertThat(schedule).isNotEqualTo(null);
        }
    }

    @Nested
    @DisplayName("toString() 테스트")
    class ToStringTests {

        @Test
        @DisplayName("toString()은 null이 아님")
        void shouldHaveNonNullToString() {
            // Given
            CrawlSchedule schedule = CrawlSchedule.of(
                CrawlScheduleId.of(1L), MustitSellerId.of(100L),
                CronExpression.of("0 0 * * * *"), ScheduleStatus.ACTIVE
            );

            // When
            String result = schedule.toString();

            // Then
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("toString()은 비어있지 않음")
        void shouldHaveNonEmptyToString() {
            // Given
            CrawlSchedule schedule = CrawlSchedule.of(
                CrawlScheduleId.of(1L), MustitSellerId.of(100L),
                CronExpression.of("0 0 * * * *"), ScheduleStatus.ACTIVE
            );

            // When
            String result = schedule.toString();

            // Then
            assertThat(result).isNotEmpty();
        }

        @Test
        @DisplayName("toString()은 클래스명 포함")
        void shouldContainClassNameInToString() {
            // Given
            CrawlSchedule schedule = CrawlSchedule.of(
                CrawlScheduleId.of(1L), MustitSellerId.of(100L),
                CronExpression.of("0 0 * * * *"), ScheduleStatus.ACTIVE
            );

            // When
            String result = schedule.toString();

            // Then
            assertThat(result).contains("CrawlSchedule");
        }
    }

    @Nested
    @DisplayName("실제 사용 시나리오 테스트")
    class UsageScenarioTests {

        @Test
        @DisplayName("새 스케줄 생성 → 이벤트 발행 → 실행 시간 계산 시나리오")
        void shouldHandleNewScheduleCreationScenario() {
            // Given
            MustitSellerId sellerId = MustitSellerId.of(100L);
            CronExpression cronExpression = CronExpression.of("0 0 * * * *");

            // When
            CrawlSchedule schedule = CrawlSchedule.forNew(sellerId, cronExpression);

            // ID 할당 (영속화 후)
            schedule = CrawlSchedule.of(
                CrawlScheduleId.of(1L), sellerId, cronExpression, ScheduleStatus.ACTIVE
            );

            // 이벤트 발행
            schedule.publishCreatedEvent("create-idem-123");

            // 다음 실행 시간 계산
            schedule.calculateNextExecution(fixedNow.plusHours(1));

            // Then
            assertThat(schedule.getIdValue()).isNotNull();
            assertThat(schedule.getDomainEvents()).hasSize(1);
            assertThat(schedule.getNextExecutionTime()).isNotNull();
            assertThat(schedule.isActive()).isTrue();
        }

        @Test
        @DisplayName("스케줄 실행 → 완료 표시 → 다음 실행 시간 재계산 시나리오")
        void shouldHandleScheduleExecutionScenario() {
            // Given
            CrawlSchedule schedule = CrawlSchedule.of(
                CrawlScheduleId.of(1L),
                MustitSellerId.of(100L),
                CronExpression.of("0 0 * * * *"),
                ScheduleStatus.ACTIVE
            );
            schedule.calculateNextExecution(fixedNow.minusMinutes(1));

            // When
            boolean shouldExecute = schedule.isTimeToExecute();
            if (shouldExecute) {
                schedule.markExecuted();
                schedule.calculateNextExecution(fixedNow.plusHours(1));
            }

            // Then
            assertThat(shouldExecute).isTrue();
            assertThat(schedule.getLastExecutedAt()).isNotNull();
            assertThat(schedule.getNextExecutionTime()).isAfter(fixedNow);
        }

        @Test
        @DisplayName("스케줄 일시정지 → 재활성화 시나리오")
        void shouldHandleSuspendAndReactivateScenario() {
            // Given
            CrawlSchedule schedule = CrawlSchedule.of(
                CrawlScheduleId.of(1L),
                MustitSellerId.of(100L),
                CronExpression.of("0 0 * * * *"),
                ScheduleStatus.ACTIVE
            );

            // When
            schedule.suspend();
            assertThat(schedule.isActive()).isFalse();

            schedule.activate();

            // Then
            assertThat(schedule.isActive()).isTrue();
            assertThat(schedule.getStatus()).isEqualTo(ScheduleStatus.ACTIVE);
        }

        @Test
        @DisplayName("Cron 표현식 변경 → 이벤트 발행 → 다음 실행 시간 재계산 시나리오")
        void shouldHandleUpdateScheduleScenario() {
            // Given
            CrawlSchedule schedule = CrawlSchedule.of(
                CrawlScheduleId.of(1L),
                MustitSellerId.of(100L),
                CronExpression.of("0 0 * * * *"),
                ScheduleStatus.ACTIVE
            );
            schedule.calculateNextExecution(fixedNow.plusHours(1));

            // When
            CronExpression newExpression = CronExpression.of("0 */5 * * * ?");
            schedule.updateSchedule(newExpression, "update-idem-456");
            schedule.calculateNextExecution(fixedNow.plusMinutes(5));

            // Then
            assertThat(schedule.getCronExpressionValue()).isEqualTo(newExpression.getValue());
            assertThat(schedule.getDomainEvents()).hasSize(1);
            assertThat(schedule.getNextExecutionTime()).isEqualTo(fixedNow.plusMinutes(5));
        }
    }
}
