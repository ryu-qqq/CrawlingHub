package com.ryuqq.crawlinghub.application.schedule.usecase;

import com.ryuqq.crawlinghub.application.schedule.port.CrawlScheduleCommandPort;
import com.ryuqq.crawlinghub.domain.schedule.CrawlSchedule;
import com.ryuqq.crawlinghub.domain.schedule.ScheduleId;
import com.ryuqq.crawlinghub.domain.schedule.event.ScheduleUpdatedEvent;
import com.ryuqq.crawlinghub.domain.workflow.WorkflowId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Integration test for UpdateScheduleUseCase.
 * Verifies event publishing flow for enabled schedules with cron changes.
 *
 * @author crawlinghub (noreply@crawlinghub.com)
 */
@ExtendWith(MockitoExtension.class)
class UpdateScheduleUseCaseTest {

    @Mock
    private CrawlScheduleCommandPort scheduleCommandPort;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UpdateScheduleUseCase useCase;

    private CrawlSchedule schedule;

    @BeforeEach
    void setUp() {
        schedule = CrawlSchedule.reconstitute(
                ScheduleId.of(1L),
                WorkflowId.of(1L),
                "Test Schedule",
                "0 0 0 * * *",
                "Asia/Seoul",
                false, // Initially disabled
                "schedule-1-rule",
                null
        );
    }

    @Test
    @DisplayName("스케줄 업데이트 성공 - Enabled && Cron 변경 시 ScheduleUpdatedEvent 발행")
    void updateScheduleWithEnabledAndCronChangePublishesEvent() {
        // given
        Long scheduleId = 1L;
        schedule.enable(); // Enabled schedule

        UpdateScheduleCommand command = new UpdateScheduleCommand(
                scheduleId,
                null, // No name change
                "0 30 0 * * *", // New cron expression (second minute hour day month dayOfWeek)
                null, // No timezone change
                null // No input params change
        );

        when(scheduleCommandPort.findById(ScheduleId.of(scheduleId)))
                .thenReturn(Optional.of(schedule));

        // when
        useCase.execute(command);

        // then
        assertThat(schedule.getCronExpression()).isEqualTo("0 30 0 * * *");
        verify(scheduleCommandPort).save(schedule);

        ArgumentCaptor<ScheduleUpdatedEvent> eventCaptor = ArgumentCaptor.forClass(ScheduleUpdatedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        ScheduleUpdatedEvent event = eventCaptor.getValue();
        assertThat(event.scheduleId()).isEqualTo(scheduleId);
        assertThat(event.ruleName()).isNotBlank();
        assertThat(event.cronExpression()).isNotBlank();
        assertThat(event.description()).contains("Test Schedule");
    }

    @Test
    @DisplayName("스케줄 업데이트 - Disabled 상태에서는 이벤트 발행하지 않음")
    void updateScheduleWithDisabledDoesNotPublishEvent() {
        // given
        Long scheduleId = 1L;
        schedule.disable(); // Disabled schedule

        UpdateScheduleCommand command = new UpdateScheduleCommand(
                scheduleId,
                null,
                "0 30 0 * * *", // New cron expression (second minute hour day month dayOfWeek)
                null,
                null
        );

        when(scheduleCommandPort.findById(ScheduleId.of(scheduleId)))
                .thenReturn(Optional.of(schedule));

        // when
        useCase.execute(command);

        // then
        verify(scheduleCommandPort).save(schedule);
        verify(eventPublisher, never()).publishEvent(any(ScheduleUpdatedEvent.class));
    }

    @Test
    @DisplayName("스케줄 업데이트 - Cron 변경 없으면 이벤트 발행하지 않음")
    void updateScheduleWithoutCronChangeDoesNotPublishEvent() {
        // given
        Long scheduleId = 1L;
        schedule.enable(); // Enabled schedule

        UpdateScheduleCommand command = new UpdateScheduleCommand(
                scheduleId,
                null,
                null, // No cron change
                null,
                null
        );

        when(scheduleCommandPort.findById(ScheduleId.of(scheduleId)))
                .thenReturn(Optional.of(schedule));

        // when
        useCase.execute(command);

        // then
        verify(scheduleCommandPort).save(schedule);
        verify(eventPublisher, never()).publishEvent(any(ScheduleUpdatedEvent.class));
    }

    @Test
    @DisplayName("스케줄 업데이트 실패 - 스케줄을 찾을 수 없음")
    void updateScheduleFailureWhenScheduleNotFound() {
        // given
        Long scheduleId = 999L;
        UpdateScheduleCommand command = new UpdateScheduleCommand(
                scheduleId,
                null,
                "0 0 30 * * *",
                null,
                null
        );

        when(scheduleCommandPort.findById(ScheduleId.of(scheduleId)))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ScheduleNotFoundException.class);
    }

    @Test
    @DisplayName("스케줄 업데이트 실패 - 잘못된 Cron Expression")
    void updateScheduleFailureWithInvalidCronExpression() {
        // given
        Long scheduleId = 1L;
        UpdateScheduleCommand command = new UpdateScheduleCommand(
                scheduleId,
                null,
                "invalid cron", // Invalid cron
                null,
                null
        );

        when(scheduleCommandPort.findById(ScheduleId.of(scheduleId)))
                .thenReturn(Optional.of(schedule));

        // when & then
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(InvalidCronExpressionException.class);
    }
}
