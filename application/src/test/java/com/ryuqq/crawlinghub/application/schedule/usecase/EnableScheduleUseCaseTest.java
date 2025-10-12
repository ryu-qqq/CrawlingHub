package com.ryuqq.crawlinghub.application.schedule.usecase;

import com.ryuqq.crawlinghub.application.schedule.port.CrawlScheduleCommandPort;
import com.ryuqq.crawlinghub.domain.schedule.CrawlSchedule;
import com.ryuqq.crawlinghub.domain.schedule.ScheduleId;
import com.ryuqq.crawlinghub.domain.schedule.ScheduleInputParam;
import com.ryuqq.crawlinghub.domain.schedule.event.ScheduleEnabledEvent;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Integration test for EnableScheduleUseCase.
 * Verifies event publishing flow and transaction boundaries.
 *
 * @author crawlinghub (noreply@crawlinghub.com)
 */
@ExtendWith(MockitoExtension.class)
class EnableScheduleUseCaseTest {

    @Mock
    private CrawlScheduleCommandPort scheduleCommandPort;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private EnableScheduleUseCase useCase;

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
    @DisplayName("스케줄 활성화 성공 - ScheduleEnabledEvent 발행")
    void enableScheduleSuccessWithEventPublishing() {
        // given
        Long scheduleId = 1L;
        when(scheduleCommandPort.findById(ScheduleId.of(scheduleId)))
                .thenReturn(Optional.of(schedule));
        when(scheduleCommandPort.findInputParamsByScheduleId(scheduleId))
                .thenReturn(List.of());

        // when
        useCase.execute(scheduleId);

        // then
        assertThat(schedule.isEnabled()).isTrue();
        verify(scheduleCommandPort).save(schedule);

        ArgumentCaptor<ScheduleEnabledEvent> eventCaptor = ArgumentCaptor.forClass(ScheduleEnabledEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        ScheduleEnabledEvent event = eventCaptor.getValue();
        assertThat(event.scheduleId()).isEqualTo(scheduleId);
        assertThat(event.ruleName()).isNotBlank();
        assertThat(event.cronExpression()).isNotBlank();
        assertThat(event.scheduleName()).isEqualTo("Test Schedule");
        assertThat(event.targetInput()).contains("scheduleId");
    }

    @Test
    @DisplayName("스케줄 활성화 실패 - 스케줄을 찾을 수 없음")
    void enableScheduleFailureWhenScheduleNotFound() {
        // given
        Long scheduleId = 999L;
        when(scheduleCommandPort.findById(ScheduleId.of(scheduleId)))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> useCase.execute(scheduleId))
                .isInstanceOf(ScheduleNotFoundException.class);
    }

    @Test
    @DisplayName("스케줄 활성화 실패 - 이미 활성화된 스케줄")
    void enableScheduleFailureWhenAlreadyEnabled() {
        // given
        Long scheduleId = 1L;
        schedule.enable(); // Already enabled
        when(scheduleCommandPort.findById(ScheduleId.of(scheduleId)))
                .thenReturn(Optional.of(schedule));

        // when & then
        assertThatThrownBy(() -> useCase.execute(scheduleId))
                .isInstanceOf(InvalidScheduleException.class)
                .hasMessageContaining("already enabled");
    }

    @Test
    @DisplayName("스케줄 활성화 성공 - InputParams 포함된 targetInput 생성")
    void enableScheduleWithInputParams() {
        // given
        Long scheduleId = 1L;
        List<ScheduleInputParam> inputParams = List.of(
                ScheduleInputParam.reconstitute(1L, scheduleId, "siteId", "100", null),
                ScheduleInputParam.reconstitute(2L, scheduleId, "brandId", "200", null)
        );

        when(scheduleCommandPort.findById(ScheduleId.of(scheduleId)))
                .thenReturn(Optional.of(schedule));
        when(scheduleCommandPort.findInputParamsByScheduleId(scheduleId))
                .thenReturn(inputParams);

        // when
        useCase.execute(scheduleId);

        // then
        ArgumentCaptor<ScheduleEnabledEvent> eventCaptor = ArgumentCaptor.forClass(ScheduleEnabledEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        ScheduleEnabledEvent event = eventCaptor.getValue();
        assertThat(event.targetInput()).contains("siteId");
        assertThat(event.targetInput()).contains("100");
        assertThat(event.targetInput()).contains("brandId");
        assertThat(event.targetInput()).contains("200");
    }
}
