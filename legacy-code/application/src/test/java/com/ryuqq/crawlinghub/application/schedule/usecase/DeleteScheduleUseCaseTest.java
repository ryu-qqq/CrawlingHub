package com.ryuqq.crawlinghub.application.schedule.usecase;

import com.ryuqq.crawlinghub.application.schedule.port.CrawlScheduleCommandPort;
import com.ryuqq.crawlinghub.domain.schedule.CrawlSchedule;
import com.ryuqq.crawlinghub.domain.schedule.ScheduleId;
import com.ryuqq.crawlinghub.domain.schedule.event.ScheduleDeletedEvent;
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
 * Integration test for DeleteScheduleUseCase.
 * Verifies event publishing flow for enabled schedules.
 *
 * @author crawlinghub (noreply@crawlinghub.com)
 */
@ExtendWith(MockitoExtension.class)
class DeleteScheduleUseCaseTest {

    @Mock
    private CrawlScheduleCommandPort scheduleCommandPort;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private DeleteScheduleUseCase useCase;

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
    @DisplayName("스케줄 삭제 성공 - Enabled 상태였으면 ScheduleDeletedEvent 발행")
    void deleteEnabledSchedulePublishesEvent() {
        // given
        Long scheduleId = 1L;
        schedule.enable(); // Enabled schedule

        when(scheduleCommandPort.findById(ScheduleId.of(scheduleId)))
                .thenReturn(Optional.of(schedule));

        // when
        useCase.execute(scheduleId);

        // then
        verify(scheduleCommandPort).deleteInputParamsByScheduleId(scheduleId);
        verify(scheduleCommandPort).deleteById(ScheduleId.of(scheduleId));

        ArgumentCaptor<ScheduleDeletedEvent> eventCaptor = ArgumentCaptor.forClass(ScheduleDeletedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        ScheduleDeletedEvent event = eventCaptor.getValue();
        assertThat(event.scheduleId()).isEqualTo(scheduleId);
        assertThat(event.ruleName()).isNotBlank();
        assertThat(event.wasEnabled()).isTrue();
    }

    @Test
    @DisplayName("스케줄 삭제 성공 - Disabled 상태였으면 이벤트 발행하지 않음")
    void deleteDisabledScheduleDoesNotPublishEvent() {
        // given
        Long scheduleId = 1L;
        schedule.disable(); // Disabled schedule

        when(scheduleCommandPort.findById(ScheduleId.of(scheduleId)))
                .thenReturn(Optional.of(schedule));

        // when
        useCase.execute(scheduleId);

        // then
        verify(scheduleCommandPort).deleteInputParamsByScheduleId(scheduleId);
        verify(scheduleCommandPort).deleteById(ScheduleId.of(scheduleId));
        verify(eventPublisher, never()).publishEvent(any(ScheduleDeletedEvent.class));
    }

    @Test
    @DisplayName("스케줄 삭제 실패 - 스케줄을 찾을 수 없음")
    void deleteScheduleFailureWhenScheduleNotFound() {
        // given
        Long scheduleId = 999L;
        when(scheduleCommandPort.findById(ScheduleId.of(scheduleId)))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> useCase.execute(scheduleId))
                .isInstanceOf(ScheduleNotFoundException.class);
    }

    @Test
    @DisplayName("스케줄 삭제 성공 - InputParams도 함께 삭제됨")
    void deleteScheduleDeletesInputParamsToo() {
        // given
        Long scheduleId = 1L;
        schedule.enable();

        when(scheduleCommandPort.findById(ScheduleId.of(scheduleId)))
                .thenReturn(Optional.of(schedule));

        // when
        useCase.execute(scheduleId);

        // then
        verify(scheduleCommandPort).deleteInputParamsByScheduleId(scheduleId);
        verify(scheduleCommandPort).deleteById(ScheduleId.of(scheduleId));
    }
}
