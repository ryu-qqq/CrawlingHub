package com.ryuqq.crawlinghub.application.schedule.facade;

import com.ryuqq.crawlinghub.application.crawl.schedule.dto.command.CreateScheduleCommandFixture;
import com.ryuqq.crawlinghub.application.crawl.schedule.dto.command.UpdateScheduleCommandFixture;
import com.ryuqq.crawlinghub.application.schedule.dto.command.CreateScheduleCommand;
import com.ryuqq.crawlinghub.application.schedule.dto.command.UpdateScheduleCommand;
import com.ryuqq.crawlinghub.application.schedule.manager.ScheduleOutboxStateManager;
import com.ryuqq.crawlinghub.application.schedule.manager.ScheduleStateManager;
import com.ryuqq.crawlinghub.application.schedule.validator.CronExpressionValidator;
import com.ryuqq.crawlinghub.domain.crawl.schedule.CrawlScheduleFixture;
import com.ryuqq.crawlinghub.domain.schedule.CrawlSchedule;
import com.ryuqq.crawlinghub.domain.schedule.CrawlScheduleId;
import com.ryuqq.crawlinghub.domain.schedule.CronExpression;
import com.ryuqq.crawlinghub.domain.seller.MustitSellerId;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

/**
 * ScheduleCommandFacade 이벤트 발행 테스트
 *
 * <p>Domain Event 등록 및 이벤트 발행 로직을 검증합니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ScheduleCommandFacade 이벤트 발행 테스트")
class ScheduleCommandFacadeEventTest {

    @Mock
    private CronExpressionValidator cronValidator;

    @Mock
    private ScheduleStateManager scheduleStateManager;

    @Mock
    private ScheduleOutboxStateManager outboxStateManager;

    @InjectMocks
    private ScheduleCommandFacade sut;

    @BeforeEach
    void setUp() {
        // 공통 설정: Cron 검증 성공
        given(cronValidator.isValid(anyString())).willReturn(true);
        given(cronValidator.calculateNextExecution(anyString(), any(LocalDateTime.class)))
            .willReturn(LocalDateTime.now().plusHours(1));
    }

    @Nested
    @DisplayName("createSchedule 메서드는")
    class Describe_create_schedule {

        private CreateScheduleCommand command;
        private CrawlSchedule savedSchedule;

        @BeforeEach
        void setUp() {
            command = CreateScheduleCommandFixture.create();
            savedSchedule = CrawlScheduleFixture.createWithId(1L);

            // Mock 설정: Idempotency 체크 실패 (신규 생성)
            given(outboxStateManager.existsByIdemKey(anyString())).willReturn(false);

            // Mock 설정: ScheduleStateManager.createSchedule() 호출 결과
            given(scheduleStateManager.createSchedule(
                any(MustitSellerId.class),
                any(CronExpression.class),
                any(LocalDateTime.class),
                anyString()
            )).willReturn(savedSchedule);
        }

        @Test
        @DisplayName("ScheduleStateManager.createSchedule()를 호출한다")
        void it_calls_schedule_state_manager_create() {
            // When
            sut.createSchedule(command);

            // Then: ScheduleStateManager.createSchedule() 호출
            then(scheduleStateManager).should(times(1)).createSchedule(
                any(MustitSellerId.class),
                any(CronExpression.class),
                any(LocalDateTime.class),
                anyString()
            );
        }

        @Test
        @DisplayName("ScheduleOutboxStateManager.createOutbox()를 호출한다")
        void it_calls_outbox_state_manager_create() {
            // When
            sut.createSchedule(command);

            // Then: ScheduleOutboxStateManager.createOutbox() 호출
            then(outboxStateManager).should(times(1)).createOutbox(
                any(Long.class),
                anyString(),
                anyString(),
                anyString()
            );
        }

        @Test
        @DisplayName("정상적으로 응답을 반환한다")
        void it_returns_response() {
            // When
            var response = sut.createSchedule(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.scheduleId()).isEqualTo(savedSchedule.getIdValue());
            assertThat(response.sellerId()).isEqualTo(savedSchedule.getSellerIdValue());
            assertThat(response.cronExpression()).isEqualTo(savedSchedule.getCronExpressionValue());
        }
    }

    @Nested
    @DisplayName("updateSchedule 메서드는")
    class Describe_update_schedule {

        private UpdateScheduleCommand command;
        private CrawlSchedule updatedSchedule;

        @BeforeEach
        void setUp() {
            command = UpdateScheduleCommandFixture.create();
            updatedSchedule = CrawlScheduleFixture.createWithId(command.scheduleId());

            // Mock 설정: Idempotency 체크 실패 (신규 업데이트)
            given(outboxStateManager.existsByIdemKey(anyString())).willReturn(false);

            // Mock 설정: ScheduleStateManager.updateSchedule() 호출 결과
            given(scheduleStateManager.updateSchedule(
                any(CrawlScheduleId.class),
                any(CronExpression.class),
                any(LocalDateTime.class),
                anyString()
            )).willReturn(updatedSchedule);
        }

        @Test
        @DisplayName("ScheduleStateManager.updateSchedule()를 호출한다")
        void it_calls_schedule_state_manager_update() {
            // When
            sut.updateSchedule(command);

            // Then: ScheduleStateManager.updateSchedule() 호출
            then(scheduleStateManager).should(times(1)).updateSchedule(
                any(CrawlScheduleId.class),
                any(CronExpression.class),
                any(LocalDateTime.class),
                anyString()
            );
        }

        @Test
        @DisplayName("ScheduleOutboxStateManager.createOutbox()를 호출한다")
        void it_calls_outbox_state_manager_create() {
            // When
            sut.updateSchedule(command);

            // Then: ScheduleOutboxStateManager.createOutbox() 호출
            then(outboxStateManager).should(times(1)).createOutbox(
                any(Long.class),
                anyString(),
                anyString(),
                anyString()
            );
        }

        @Test
        @DisplayName("정상적으로 응답을 반환한다")
        void it_returns_response() {
            // When
            var response = sut.updateSchedule(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.scheduleId()).isEqualTo(updatedSchedule.getIdValue());
            assertThat(response.sellerId()).isEqualTo(updatedSchedule.getSellerIdValue());
            assertThat(response.cronExpression()).isEqualTo(updatedSchedule.getCronExpressionValue());
        }
    }
}

