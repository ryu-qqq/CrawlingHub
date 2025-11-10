package com.ryuqq.crawlinghub.application.schedule.usecase;

import com.ryuqq.crawlinghub.application.schedule.port.CrawlScheduleCommandPort;
import com.ryuqq.crawlinghub.domain.schedule.CrawlSchedule;
import com.ryuqq.crawlinghub.domain.schedule.ScheduleId;
import com.ryuqq.crawlinghub.domain.schedule.event.ScheduleDisabledEvent;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Integration test for DisableScheduleUseCase.
 * Verifies event publishing flow and transaction boundaries.
 *
 * @author crawlinghub (noreply@crawlinghub.com)
 */
@ExtendWith(MockitoExtension.class)
class DisableScheduleUseCaseTest {

    @Mock
    private CrawlScheduleCommandPort scheduleCommandPort;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private DisableScheduleUseCase useCase;

    private CrawlSchedule schedule;

    @BeforeEach
    void setUp() {
        schedule = CrawlSchedule.reconstitute(
                ScheduleId.of(1L),
                WorkflowId.of(1L),
                "Test Schedule",
                "0 0 0 * * *",
                "Asia/Seoul",
                true, // Initially enabled
                "schedule-1-rule",
                null
        );
    }

    @Test
    @DisplayName("스케줄 비활성화 성공 - ScheduleDisabledEvent 발행")
    void disableScheduleSuccessWithEventPublishing() {
        // given
        Long scheduleId = 1L;
        when(scheduleCommandPort.findById(ScheduleId.of(scheduleId)))
                .thenReturn(Optional.of(schedule));

        // when
        useCase.execute(scheduleId);

        // then
        assertThat(schedule.isEnabled()).isFalse();
        verify(scheduleCommandPort).save(schedule);

        ArgumentCaptor<ScheduleDisabledEvent> eventCaptor = ArgumentCaptor.forClass(ScheduleDisabledEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        ScheduleDisabledEvent event = eventCaptor.getValue();
        assertThat(event.scheduleId()).isEqualTo(scheduleId);
        assertThat(event.ruleName()).isNotBlank();
    }

    @Test
    @DisplayName("스케줄 비활성화 실패 - 스케줄을 찾을 수 없음")
    void disableScheduleFailureWhenScheduleNotFound() {
        // given
        Long scheduleId = 999L;
        when(scheduleCommandPort.findById(ScheduleId.of(scheduleId)))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> useCase.execute(scheduleId))
                .isInstanceOf(ScheduleNotFoundException.class);
    }

    @Test
    @DisplayName("스케줄 비활성화 실패 - 이미 비활성화된 스케줄")
    void disableScheduleFailureWhenAlreadyDisabled() {
        // given
        Long scheduleId = 1L;
        schedule.disable(); // Already disabled
        when(scheduleCommandPort.findById(ScheduleId.of(scheduleId)))
                .thenReturn(Optional.of(schedule));

        // when & then
        assertThatThrownBy(() -> useCase.execute(scheduleId))
                .isInstanceOf(InvalidScheduleException.class)
                .hasMessageContaining("already disabled");
    }
}
