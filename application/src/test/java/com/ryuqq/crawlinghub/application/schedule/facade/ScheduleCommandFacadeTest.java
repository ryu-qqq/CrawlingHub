package com.ryuqq.crawlinghub.application.schedule.facade;

import com.ryuqq.crawlinghub.application.crawl.schedule.dto.command.CreateScheduleCommandFixture;
import com.ryuqq.crawlinghub.application.crawl.schedule.dto.command.UpdateScheduleCommandFixture;
import com.ryuqq.crawlinghub.application.schedule.dto.command.CreateScheduleCommand;
import com.ryuqq.crawlinghub.application.schedule.dto.command.UpdateScheduleCommand;
import com.ryuqq.crawlinghub.application.schedule.dto.response.ScheduleResponse;
import com.ryuqq.crawlinghub.application.schedule.component.ScheduleOutboxStateManager;
import com.ryuqq.crawlinghub.application.schedule.component.ScheduleStateManager;
import com.ryuqq.crawlinghub.application.schedule.validator.CronExpressionValidator;
import com.ryuqq.crawlinghub.domain.crawl.schedule.CrawlScheduleFixture;
import com.ryuqq.crawlinghub.domain.schedule.CrawlSchedule;
import com.ryuqq.crawlinghub.domain.schedule.CrawlScheduleId;
import com.ryuqq.crawlinghub.domain.schedule.CronExpression;
import com.ryuqq.crawlinghub.domain.seller.MustItSellerId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

/**
 * ScheduleCommandFacade 단위 테스트
 *
 * <p>스케줄 생성 및 수정 UseCase의 비즈니스 로직을 검증합니다.
 * 기존 ScheduleCommandFacadeEventTest와 분리하여 추가 시나리오를 커버합니다.
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ScheduleCommandFacade 단위 테스트")
class ScheduleCommandFacadeTest {

    @Mock
    private CronExpressionValidator cronValidator;

    @Mock
    private ScheduleStateManager scheduleStateManager;

    @Mock
    private ScheduleOutboxStateManager outboxStateManager;

    @InjectMocks
    private ScheduleCommandFacade sut;

    @Nested
    @DisplayName("createSchedule 메서드는")
    class Describe_createSchedule {

        @Nested
        @DisplayName("유효한 Cron 표현식이 주어지면")
        class Context_with_valid_cron_expression {

            private CreateScheduleCommand command;
            private CrawlSchedule savedSchedule;

            @BeforeEach
            void setUp() {
                // Given: 유효한 생성 Command
                command = CreateScheduleCommandFixture.create();
                savedSchedule = CrawlScheduleFixture.createWithId(1L);

                // Mock: Idempotency 체크 실패 (신규 생성)
                given(outboxStateManager.existsByIdemKey(anyString())).willReturn(false);

                // Mock: Cron 검증 성공
                given(cronValidator.isValid(anyString())).willReturn(true);
                given(cronValidator.calculateNextExecution(anyString(), any(LocalDateTime.class)))
                    .willReturn(LocalDateTime.now().plusHours(1));

                // Mock: ScheduleStateManager.createSchedule() 호출 결과
                given(scheduleStateManager.createSchedule(
                    any(MustItSellerId.class),
                    any(CronExpression.class),
                    any(LocalDateTime.class),
                    anyString()
                )).willReturn(savedSchedule);
            }

            @Test
            @DisplayName("스케줄을 생성하고 응답을 반환한다")
            void it_creates_schedule_and_returns_response() {
                // When: 스케줄 생성 실행
                ScheduleResponse response = sut.createSchedule(command);

                // Then: Cron 검증이 수행됨
                then(cronValidator).should().isValid(command.cronExpression());

                // And: ScheduleStateManager가 호출됨
                then(scheduleStateManager).should().createSchedule(
                    any(MustItSellerId.class),
                    any(CronExpression.class),
                    any(LocalDateTime.class),
                    anyString()
                );

                // And: Outbox가 생성됨
                then(outboxStateManager).should().createOutbox(
                    any(Long.class),
                    anyString(),
                    anyString(),
                    anyString()
                );

                // And: 생성된 스케줄 정보가 응답으로 반환됨
                assertThat(response).isNotNull();
                assertThat(response.scheduleId()).isEqualTo(savedSchedule.getIdValue());
                assertThat(response.sellerId()).isEqualTo(savedSchedule.getSellerIdValue());
                assertThat(response.cronExpression()).isEqualTo(savedSchedule.getCronExpressionValue());
            }
        }

        @Nested
        @DisplayName("유효하지 않은 Cron 표현식이 주어지면")
        class Context_with_invalid_cron_expression {

            private CreateScheduleCommand command;

            @BeforeEach
            void setUp() {
                // Given: 유효하지 않은 Cron 표현식
                command = CreateScheduleCommandFixture.create();

                // Mock: Idempotency 체크 실패 (신규 생성)
                given(outboxStateManager.existsByIdemKey(anyString())).willReturn(false);

                // Mock: Cron 검증 실패
                given(cronValidator.isValid(anyString())).willReturn(false);
            }

            @Test
            @DisplayName("IllegalArgumentException을 발생시킨다")
            void it_throws_illegal_argument_exception() {
                // When & Then: 스케줄 생성 시도 시 예외 발생
                assertThatThrownBy(() -> sut.createSchedule(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("유효하지 않은 Cron 표현식입니다");

                // And: Cron 검증은 수행됨
                then(cronValidator).should().isValid(command.cronExpression());

                // And: ScheduleStateManager는 호출되지 않음
                then(scheduleStateManager).should(never()).createSchedule(
                    any(MustItSellerId.class),
                    any(CronExpression.class),
                    any(LocalDateTime.class),
                    anyString()
                );
            }
        }

        @Nested
        @DisplayName("Idempotency 키가 이미 존재하면")
        class Context_with_existing_idempotency_key {

            private CreateScheduleCommand command;
            private CrawlSchedule existingSchedule;

            @BeforeEach
            void setUp() {
                // Given: 생성 Command
                command = CreateScheduleCommandFixture.create();
                existingSchedule = CrawlScheduleFixture.createWithId(1L);

                // Mock: Idempotency 체크 성공 (이미 존재)
                given(outboxStateManager.existsByIdemKey(anyString())).willReturn(true);

                // Mock: 기존 스케줄 조회
                given(scheduleStateManager.getSchedule(any(CrawlScheduleId.class)))
                    .willReturn(existingSchedule);
            }

            @Test
            @DisplayName("기존 스케줄을 반환하고 새로 생성하지 않는다")
            void it_returns_existing_schedule_without_creating_new_one() {
                // When: 스케줄 생성 실행
                ScheduleResponse response = sut.createSchedule(command);

                // Then: 기존 스케줄이 반환됨
                assertThat(response).isNotNull();
                assertThat(response.scheduleId()).isEqualTo(existingSchedule.getIdValue());

                // And: ScheduleStateManager.createSchedule()는 호출되지 않음
                then(scheduleStateManager).should(never()).createSchedule(
                    any(MustItSellerId.class),
                    any(CronExpression.class),
                    any(LocalDateTime.class),
                    anyString()
                );

                // And: Outbox는 새로 생성되지 않음
                then(outboxStateManager).should(never()).createOutbox(
                    any(Long.class),
                    anyString(),
                    anyString(),
                    anyString()
                );
            }
        }

        @Nested
        @DisplayName("비즈니스 규칙 검증")
        class Context_business_rules {

            @Test
            @DisplayName("Cron 검증은 Schedule 생성보다 먼저 수행된다")
            void cron_validation_happens_before_schedule_creation() {
                // Given
                CreateScheduleCommand command = CreateScheduleCommandFixture.create();
                CrawlSchedule savedSchedule = CrawlScheduleFixture.createWithId(1L);

                given(outboxStateManager.existsByIdemKey(anyString())).willReturn(false);
                given(cronValidator.isValid(anyString())).willReturn(false); // Cron 검증 실패

                // When & Then: Cron 검증 실패로 예외 발생
                assertThatThrownBy(() -> sut.createSchedule(command))
                    .isInstanceOf(IllegalArgumentException.class);

                // And: ScheduleStateManager.createSchedule()는 호출되지 않음
                then(scheduleStateManager).should(never()).createSchedule(
                    any(MustItSellerId.class),
                    any(CronExpression.class),
                    any(LocalDateTime.class),
                    anyString()
                );
            }
        }
    }

    @Nested
    @DisplayName("updateSchedule 메서드는")
    class Describe_updateSchedule {

        @Nested
        @DisplayName("유효한 Cron 표현식이 주어지면")
        class Context_with_valid_cron_expression {

            private UpdateScheduleCommand command;
            private CrawlSchedule updatedSchedule;

            @BeforeEach
            void setUp() {
                // Given: 유효한 수정 Command
                command = UpdateScheduleCommandFixture.create();
                updatedSchedule = CrawlScheduleFixture.createWithId(command.scheduleId());

                // Mock: Idempotency 체크 실패 (신규 업데이트)
                given(outboxStateManager.existsByIdemKey(anyString())).willReturn(false);

                // Mock: Cron 검증 성공
                given(cronValidator.isValid(anyString())).willReturn(true);
                given(cronValidator.calculateNextExecution(anyString(), any(LocalDateTime.class)))
                    .willReturn(LocalDateTime.now().plusHours(1));

                // Mock: ScheduleStateManager.updateSchedule() 호출 결과
                given(scheduleStateManager.updateSchedule(
                    any(CrawlScheduleId.class),
                    any(CronExpression.class),
                    any(LocalDateTime.class),
                    anyString()
                )).willReturn(updatedSchedule);
            }

            @Test
            @DisplayName("스케줄을 수정하고 응답을 반환한다")
            void it_updates_schedule_and_returns_response() {
                // When: 스케줄 수정 실행
                ScheduleResponse response = sut.updateSchedule(command);

                // Then: Cron 검증이 수행됨
                then(cronValidator).should().isValid(command.cronExpression());

                // And: ScheduleStateManager가 호출됨
                then(scheduleStateManager).should().updateSchedule(
                    any(CrawlScheduleId.class),
                    any(CronExpression.class),
                    any(LocalDateTime.class),
                    anyString()
                );

                // And: Outbox가 생성됨
                then(outboxStateManager).should().createOutbox(
                    any(Long.class),
                    anyString(),
                    anyString(),
                    anyString()
                );

                // And: 수정된 스케줄 정보가 응답으로 반환됨
                assertThat(response).isNotNull();
                assertThat(response.scheduleId()).isEqualTo(updatedSchedule.getIdValue());
                assertThat(response.sellerId()).isEqualTo(updatedSchedule.getSellerIdValue());
                assertThat(response.cronExpression()).isEqualTo(updatedSchedule.getCronExpressionValue());
            }
        }

        @Nested
        @DisplayName("유효하지 않은 Cron 표현식이 주어지면")
        class Context_with_invalid_cron_expression {

            private UpdateScheduleCommand command;

            @BeforeEach
            void setUp() {
                // Given: 유효하지 않은 Cron 표현식
                command = UpdateScheduleCommandFixture.create();

                // Mock: Idempotency 체크 실패 (신규 업데이트)
                given(outboxStateManager.existsByIdemKey(anyString())).willReturn(false);

                // Mock: Cron 검증 실패
                given(cronValidator.isValid(anyString())).willReturn(false);
            }

            @Test
            @DisplayName("IllegalArgumentException을 발생시킨다")
            void it_throws_illegal_argument_exception() {
                // When & Then: 스케줄 수정 시도 시 예외 발생
                assertThatThrownBy(() -> sut.updateSchedule(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("유효하지 않은 Cron 표현식입니다");

                // And: Cron 검증은 수행됨
                then(cronValidator).should().isValid(command.cronExpression());

                // And: ScheduleStateManager는 호출되지 않음
                then(scheduleStateManager).should(never()).updateSchedule(
                    any(CrawlScheduleId.class),
                    any(CronExpression.class),
                    any(LocalDateTime.class),
                    anyString()
                );
            }
        }

        @Nested
        @DisplayName("Idempotency 키가 이미 존재하면")
        class Context_with_existing_idempotency_key {

            private UpdateScheduleCommand command;
            private CrawlSchedule existingSchedule;

            @BeforeEach
            void setUp() {
                // Given: 수정 Command
                command = UpdateScheduleCommandFixture.create();
                existingSchedule = CrawlScheduleFixture.createWithId(command.scheduleId());

                // Mock: Idempotency 체크 성공 (이미 존재)
                given(outboxStateManager.existsByIdemKey(anyString())).willReturn(true);

                // Mock: 기존 스케줄 조회
                given(scheduleStateManager.getSchedule(any(CrawlScheduleId.class)))
                    .willReturn(existingSchedule);
            }

            @Test
            @DisplayName("기존 스케줄을 반환하고 새로 수정하지 않는다")
            void it_returns_existing_schedule_without_updating() {
                // When: 스케줄 수정 실행
                ScheduleResponse response = sut.updateSchedule(command);

                // Then: 기존 스케줄이 반환됨
                assertThat(response).isNotNull();
                assertThat(response.scheduleId()).isEqualTo(existingSchedule.getIdValue());

                // And: ScheduleStateManager.updateSchedule()는 호출되지 않음
                then(scheduleStateManager).should(never()).updateSchedule(
                    any(CrawlScheduleId.class),
                    any(CronExpression.class),
                    any(LocalDateTime.class),
                    anyString()
                );

                // And: Outbox는 새로 생성되지 않음
                then(outboxStateManager).should(never()).createOutbox(
                    any(Long.class),
                    anyString(),
                    anyString(),
                    anyString()
                );
            }
        }

        @Nested
        @DisplayName("비즈니스 규칙 검증")
        class Context_business_rules {

            @Test
            @DisplayName("Cron 검증은 Schedule 수정보다 먼저 수행된다")
            void cron_validation_happens_before_schedule_update() {
                // Given
                UpdateScheduleCommand command = UpdateScheduleCommandFixture.create();

                given(outboxStateManager.existsByIdemKey(anyString())).willReturn(false);
                given(cronValidator.isValid(anyString())).willReturn(false); // Cron 검증 실패

                // When & Then: Cron 검증 실패로 예외 발생
                assertThatThrownBy(() -> sut.updateSchedule(command))
                    .isInstanceOf(IllegalArgumentException.class);

                // And: ScheduleStateManager.updateSchedule()는 호출되지 않음
                then(scheduleStateManager).should(never()).updateSchedule(
                    any(CrawlScheduleId.class),
                    any(CronExpression.class),
                    any(LocalDateTime.class),
                    anyString()
                );
            }
        }
    }
}
