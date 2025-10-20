package com.ryuqq.crawlinghub.application.schedule.usecase;

import com.ryuqq.crawlinghub.application.schedule.port.CrawlScheduleCommandPort;
import com.ryuqq.crawlinghub.application.schedule.port.CrawlScheduleQueryPort;
import com.ryuqq.crawlinghub.domain.common.ParamType;
import com.ryuqq.crawlinghub.domain.schedule.CrawlSchedule;
import com.ryuqq.crawlinghub.domain.schedule.ScheduleId;
import com.ryuqq.crawlinghub.domain.schedule.ScheduleInputParam;
import com.ryuqq.crawlinghub.domain.workflow.WorkflowId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GetScheduleUseCase
 *
 * @author Sangwon Ryu (ryuqq@company.com)
 * @since 2025-10-14
 */
@ExtendWith(MockitoExtension.class)
class GetScheduleUseCaseTest {

    @Mock
    private CrawlScheduleCommandPort scheduleCommandPort;

    @Mock
    private CrawlScheduleQueryPort scheduleQueryPort;

    @InjectMocks
    private GetScheduleUseCase useCase;

    private CrawlSchedule schedule1;
    private CrawlSchedule schedule2;
    private List<ScheduleInputParam> inputParams;

    @BeforeEach
    void setUp() {
        schedule1 = CrawlSchedule.reconstitute(
                ScheduleId.of(1L),
                WorkflowId.of(100L),
                "Daily Schedule",
                "0 0 * * *",
                "Asia/Seoul",
                true,
                "crawl-schedule-daily-schedule",
                LocalDateTime.now().plusHours(1)
        );

        schedule2 = CrawlSchedule.reconstitute(
                ScheduleId.of(2L),
                WorkflowId.of(100L),
                "Hourly Schedule",
                "0 * * * *",
                "Asia/Seoul",
                false,
                "crawl-schedule-hourly-schedule",
                LocalDateTime.now().plusMinutes(30)
        );

        inputParams = Arrays.asList(
                ScheduleInputParam.create(1L, "param1", "value1", ParamType.STATIC),
                ScheduleInputParam.create(1L, "param2", "value2", ParamType.STATIC)
        );
    }

    @Nested
    @DisplayName("getById Tests")
    class GetByIdTests {

        @Test
        @DisplayName("successfully gets schedule by ID")
        void getById_Success() {
            // given
            Long scheduleId = 1L;
            when(scheduleCommandPort.findById(ScheduleId.of(scheduleId)))
                    .thenReturn(Optional.of(schedule1));

            // when
            CrawlSchedule result = useCase.getById(scheduleId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getScheduleId()).isEqualTo(ScheduleId.of(scheduleId));
            assertThat(result.getScheduleName()).isEqualTo("Daily Schedule");
            verify(scheduleCommandPort).findById(ScheduleId.of(scheduleId));
        }

        @Test
        @DisplayName("throws ScheduleNotFoundException when schedule not found")
        void getById_ThrowsExceptionWhenNotFound() {
            // given
            Long scheduleId = 999L;
            when(scheduleCommandPort.findById(ScheduleId.of(scheduleId)))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> useCase.getById(scheduleId))
                    .isInstanceOf(ScheduleNotFoundException.class);
            verify(scheduleCommandPort).findById(ScheduleId.of(scheduleId));
        }
    }

    @Nested
    @DisplayName("getByIdWithInputParams Tests")
    class GetByIdWithInputParamsTests {

        @Test
        @DisplayName("successfully gets schedule with input parameters")
        void getByIdWithInputParams_Success() {
            // given
            Long scheduleId = 1L;
            when(scheduleQueryPort.findByIdWithInputParams(ScheduleId.of(scheduleId)))
                    .thenReturn(Optional.of(schedule1));

            // when
            CrawlSchedule result = useCase.getByIdWithInputParams(scheduleId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getScheduleId()).isEqualTo(ScheduleId.of(scheduleId));
            verify(scheduleQueryPort).findByIdWithInputParams(ScheduleId.of(scheduleId));
        }

        @Test
        @DisplayName("throws ScheduleNotFoundException when schedule not found")
        void getByIdWithInputParams_ThrowsExceptionWhenNotFound() {
            // given
            Long scheduleId = 999L;
            when(scheduleQueryPort.findByIdWithInputParams(ScheduleId.of(scheduleId)))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> useCase.getByIdWithInputParams(scheduleId))
                    .isInstanceOf(ScheduleNotFoundException.class);
            verify(scheduleQueryPort).findByIdWithInputParams(ScheduleId.of(scheduleId));
        }
    }

    @Nested
    @DisplayName("getInputParams Tests")
    class GetInputParamsTests {

        @Test
        @DisplayName("successfully gets input parameters")
        void getInputParams_Success() {
            // given
            Long scheduleId = 1L;
            when(scheduleCommandPort.findInputParamsByScheduleId(scheduleId))
                    .thenReturn(inputParams);

            // when
            List<ScheduleInputParam> result = useCase.getInputParams(scheduleId);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getParamKey()).isEqualTo("param1");
            assertThat(result.get(1).getParamKey()).isEqualTo("param2");
            verify(scheduleCommandPort).findInputParamsByScheduleId(scheduleId);
        }

        @Test
        @DisplayName("returns empty list when no input parameters found")
        void getInputParams_ReturnsEmptyList() {
            // given
            Long scheduleId = 1L;
            when(scheduleCommandPort.findInputParamsByScheduleId(scheduleId))
                    .thenReturn(Collections.emptyList());

            // when
            List<ScheduleInputParam> result = useCase.getInputParams(scheduleId);

            // then
            assertThat(result).isEmpty();
            verify(scheduleCommandPort).findInputParamsByScheduleId(scheduleId);
        }
    }

    @Nested
    @DisplayName("getByWorkflowId Tests")
    class GetByWorkflowIdTests {

        @Test
        @DisplayName("successfully gets schedules by workflow ID")
        void getByWorkflowId_Success() {
            // given
            Long workflowId = 100L;
            List<CrawlSchedule> schedules = Arrays.asList(schedule1, schedule2);
            when(scheduleQueryPort.findByWorkflowId(WorkflowId.of(workflowId)))
                    .thenReturn(schedules);

            // when
            List<CrawlSchedule> result = useCase.getByWorkflowId(workflowId);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).containsExactly(schedule1, schedule2);
            verify(scheduleQueryPort).findByWorkflowId(WorkflowId.of(workflowId));
        }

        @Test
        @DisplayName("returns empty list when no schedules found")
        void getByWorkflowId_ReturnsEmptyList() {
            // given
            Long workflowId = 999L;
            when(scheduleQueryPort.findByWorkflowId(WorkflowId.of(workflowId)))
                    .thenReturn(Collections.emptyList());

            // when
            List<CrawlSchedule> result = useCase.getByWorkflowId(workflowId);

            // then
            assertThat(result).isEmpty();
            verify(scheduleQueryPort).findByWorkflowId(WorkflowId.of(workflowId));
        }
    }

    @Nested
    @DisplayName("getByIsEnabled Tests")
    class GetByIsEnabledTests {

        @Test
        @DisplayName("successfully gets enabled schedules")
        void getByIsEnabled_Enabled_Success() {
            // given
            when(scheduleQueryPort.findByIsEnabled(true))
                    .thenReturn(Collections.singletonList(schedule1));

            // when
            List<CrawlSchedule> result = useCase.getByIsEnabled(true);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).isEnabled()).isTrue();
            verify(scheduleQueryPort).findByIsEnabled(true);
        }

        @Test
        @DisplayName("successfully gets disabled schedules")
        void getByIsEnabled_Disabled_Success() {
            // given
            when(scheduleQueryPort.findByIsEnabled(false))
                    .thenReturn(Collections.singletonList(schedule2));

            // when
            List<CrawlSchedule> result = useCase.getByIsEnabled(false);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).isEnabled()).isFalse();
            verify(scheduleQueryPort).findByIsEnabled(false);
        }
    }

    @Nested
    @DisplayName("getByWorkflowIdAndIsEnabled Tests")
    class GetByWorkflowIdAndIsEnabledTests {

        @Test
        @DisplayName("successfully gets schedules by workflow ID and enabled status")
        void getByWorkflowIdAndIsEnabled_Success() {
            // given
            Long workflowId = 100L;
            boolean isEnabled = true;
            when(scheduleQueryPort.findByWorkflowIdAndIsEnabled(
                    WorkflowId.of(workflowId), isEnabled))
                    .thenReturn(Collections.singletonList(schedule1));

            // when
            List<CrawlSchedule> result = useCase.getByWorkflowIdAndIsEnabled(workflowId, isEnabled);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getWorkflowId()).isEqualTo(WorkflowId.of(workflowId));
            assertThat(result.get(0).isEnabled()).isTrue();
            verify(scheduleQueryPort).findByWorkflowIdAndIsEnabled(
                    WorkflowId.of(workflowId), isEnabled);
        }
    }

    @Nested
    @DisplayName("getAll Tests")
    class GetAllTests {

        @Test
        @DisplayName("successfully gets all schedules")
        void getAll_Success() {
            // given
            List<CrawlSchedule> allSchedules = Arrays.asList(schedule1, schedule2);
            when(scheduleQueryPort.findAll()).thenReturn(allSchedules);

            // when
            List<CrawlSchedule> result = useCase.getAll();

            // then
            assertThat(result).hasSize(2);
            assertThat(result).containsExactly(schedule1, schedule2);
            verify(scheduleQueryPort).findAll();
        }

        @Test
        @DisplayName("returns empty list when no schedules exist")
        void getAll_ReturnsEmptyList() {
            // given
            when(scheduleQueryPort.findAll()).thenReturn(Collections.emptyList());

            // when
            List<CrawlSchedule> result = useCase.getAll();

            // then
            assertThat(result).isEmpty();
            verify(scheduleQueryPort).findAll();
        }
    }

    @Nested
    @DisplayName("getByFilter Tests")
    class GetByFilterTests {

        @Test
        @DisplayName("calls findAll when filter is empty")
        void getByFilter_EmptyFilter_CallsFindAll() {
            // given
            ScheduleFilter filter = ScheduleFilter.empty();
            List<CrawlSchedule> allSchedules = Arrays.asList(schedule1, schedule2);
            when(scheduleQueryPort.findAll()).thenReturn(allSchedules);

            // when
            List<CrawlSchedule> result = useCase.getByFilter(filter);

            // then
            assertThat(result).hasSize(2);
            verify(scheduleQueryPort).findAll();
            verify(scheduleQueryPort, never()).findByWorkflowId(any());
            verify(scheduleQueryPort, never()).findByIsEnabled(anyBoolean());
            verify(scheduleQueryPort, never()).findByWorkflowIdAndIsEnabled(any(), anyBoolean());
        }

        @Test
        @DisplayName("calls findByWorkflowId when only workflow ID is set")
        void getByFilter_WorkflowIdOnly_CallsFindByWorkflowId() {
            // given
            Long workflowId = 100L;
            ScheduleFilter filter = ScheduleFilter.byWorkflowId(workflowId);
            List<CrawlSchedule> schedules = Arrays.asList(schedule1, schedule2);
            when(scheduleQueryPort.findByWorkflowId(WorkflowId.of(workflowId)))
                    .thenReturn(schedules);

            // when
            List<CrawlSchedule> result = useCase.getByFilter(filter);

            // then
            assertThat(result).hasSize(2);
            verify(scheduleQueryPort).findByWorkflowId(WorkflowId.of(workflowId));
            verify(scheduleQueryPort, never()).findAll();
            verify(scheduleQueryPort, never()).findByIsEnabled(anyBoolean());
            verify(scheduleQueryPort, never()).findByWorkflowIdAndIsEnabled(any(), anyBoolean());
        }

        @Test
        @DisplayName("calls findByIsEnabled when only enabled status is set")
        void getByFilter_IsEnabledOnly_CallsFindByIsEnabled() {
            // given
            Boolean isEnabled = true;
            ScheduleFilter filter = ScheduleFilter.byIsEnabled(isEnabled);
            when(scheduleQueryPort.findByIsEnabled(isEnabled))
                    .thenReturn(Collections.singletonList(schedule1));

            // when
            List<CrawlSchedule> result = useCase.getByFilter(filter);

            // then
            assertThat(result).hasSize(1);
            verify(scheduleQueryPort).findByIsEnabled(isEnabled);
            verify(scheduleQueryPort, never()).findAll();
            verify(scheduleQueryPort, never()).findByWorkflowId(any());
            verify(scheduleQueryPort, never()).findByWorkflowIdAndIsEnabled(any(), anyBoolean());
        }

        @Test
        @DisplayName("calls findByWorkflowIdAndIsEnabled when both filters are set")
        void getByFilter_BothFilters_CallsFindByWorkflowIdAndIsEnabled() {
            // given
            Long workflowId = 100L;
            Boolean isEnabled = true;
            ScheduleFilter filter = new ScheduleFilter(workflowId, isEnabled);
            when(scheduleQueryPort.findByWorkflowIdAndIsEnabled(
                    WorkflowId.of(workflowId), isEnabled))
                    .thenReturn(Collections.singletonList(schedule1));

            // when
            List<CrawlSchedule> result = useCase.getByFilter(filter);

            // then
            assertThat(result).hasSize(1);
            verify(scheduleQueryPort).findByWorkflowIdAndIsEnabled(
                    WorkflowId.of(workflowId), isEnabled);
            verify(scheduleQueryPort, never()).findAll();
            verify(scheduleQueryPort, never()).findByWorkflowId(any());
            verify(scheduleQueryPort, never()).findByIsEnabled(anyBoolean());
        }

        @Test
        @DisplayName("handles disabled schedules filter correctly")
        void getByFilter_DisabledSchedules() {
            // given
            Boolean isEnabled = false;
            ScheduleFilter filter = ScheduleFilter.byIsEnabled(isEnabled);
            when(scheduleQueryPort.findByIsEnabled(false))
                    .thenReturn(Collections.singletonList(schedule2));

            // when
            List<CrawlSchedule> result = useCase.getByFilter(filter);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).isEnabled()).isFalse();
            verify(scheduleQueryPort).findByIsEnabled(false);
        }
    }
}
