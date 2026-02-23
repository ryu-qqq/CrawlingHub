package com.ryuqq.crawlinghub.adapter.out.eventbridge.adapter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ryuqq.crawlinghub.adapter.out.eventbridge.exception.EventBridgePublishException;
import com.ryuqq.crawlinghub.adapter.out.eventbridge.mapper.EventBridgeScheduleMapper;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerOutBox;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerHistoryId;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerOutBoxId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerOubBoxStatus;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.scheduler.SchedulerClient;
import software.amazon.awssdk.services.scheduler.model.CreateScheduleRequest;
import software.amazon.awssdk.services.scheduler.model.CreateScheduleResponse;
import software.amazon.awssdk.services.scheduler.model.ScheduleState;
import software.amazon.awssdk.services.scheduler.model.Target;
import software.amazon.awssdk.services.scheduler.model.UpdateScheduleRequest;
import software.amazon.awssdk.services.scheduler.model.UpdateScheduleResponse;

/**
 * EventBridgeClientAdapter 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EventBridgeClientAdapter 테스트")
class EventBridgeClientAdapterTest {

    @Mock private SchedulerClient schedulerClient;

    @Mock private EventBridgeScheduleMapper mapper;

    private EventBridgeClientAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new EventBridgeClientAdapter(schedulerClient, mapper);
    }

    // ===== 테스트용 OutBox 헬퍼 =====

    private CrawlSchedulerOutBox createActiveOutBox() {
        return CrawlSchedulerOutBox.reconstitute(
                CrawlSchedulerOutBoxId.of(10L),
                CrawlSchedulerHistoryId.of(1L),
                CrawlSchedulerOubBoxStatus.PENDING,
                100L,
                200L,
                "test-scheduler",
                "0 9 * * ? *",
                SchedulerStatus.ACTIVE,
                null,
                0L,
                Instant.now(),
                null);
    }

    private CrawlSchedulerOutBox createInactiveOutBox() {
        return CrawlSchedulerOutBox.reconstitute(
                CrawlSchedulerOutBoxId.of(11L),
                CrawlSchedulerHistoryId.of(2L),
                CrawlSchedulerOubBoxStatus.PENDING,
                101L,
                201L,
                "inactive-scheduler",
                "0 10 * * ? *",
                SchedulerStatus.INACTIVE,
                null,
                0L,
                Instant.now(),
                null);
    }

    @Nested
    @DisplayName("syncFromOutBox - ACTIVE 스케줄러 처리")
    class SyncFromOutBoxActiveTest {

        @Test
        @DisplayName("ACTIVE 상태 OutBox는 createSchedule을 먼저 시도한다")
        void syncFromOutBox_withActiveStatus_attemptsCreateSchedule() {
            // given
            CrawlSchedulerOutBox outBox = createActiveOutBox();
            Target mockTarget = mock(Target.class);

            when(mapper.toScheduleName(any())).thenReturn("crawler-100");
            when(mapper.toCronExpression(any())).thenReturn("cron(0 9 * * ? *)");
            when(mapper.toTarget(any(), any(), any())).thenReturn(mockTarget);
            when(mapper.toCreateRequest(any(), any(), any(), any()))
                    .thenReturn(CreateScheduleRequest.builder().build());
            when(schedulerClient.createSchedule(any(CreateScheduleRequest.class)))
                    .thenReturn(CreateScheduleResponse.builder().build());

            // when
            adapter.syncFromOutBox(outBox);

            // then
            verify(schedulerClient, times(1)).createSchedule(any(CreateScheduleRequest.class));
        }

        @Test
        @DisplayName("createSchedule 실패 시 updateSchedule을 호출한다")
        void syncFromOutBox_whenCreateFails_fallsBackToUpdate() {
            // given
            CrawlSchedulerOutBox outBox = createActiveOutBox();
            Target mockTarget = mock(Target.class);

            when(mapper.toScheduleName(any())).thenReturn("crawler-100");
            when(mapper.toCronExpression(any())).thenReturn("cron(0 9 * * ? *)");
            when(mapper.toTarget(any(), any(), any())).thenReturn(mockTarget);
            when(mapper.toCreateRequest(any(), any(), any(), any()))
                    .thenReturn(CreateScheduleRequest.builder().build());
            when(mapper.toUpdateRequest(any(), any(), any(), any(), any()))
                    .thenReturn(UpdateScheduleRequest.builder().build());

            // createSchedule 실패 → updateSchedule fallback
            when(schedulerClient.createSchedule(any(CreateScheduleRequest.class)))
                    .thenThrow(new RuntimeException("스케줄이 이미 존재합니다"));
            when(schedulerClient.updateSchedule(any(UpdateScheduleRequest.class)))
                    .thenReturn(UpdateScheduleResponse.builder().build());

            // when
            adapter.syncFromOutBox(outBox);

            // then
            verify(schedulerClient, times(1)).createSchedule(any(CreateScheduleRequest.class));
            verify(schedulerClient, times(1)).updateSchedule(any(UpdateScheduleRequest.class));
        }
    }

    @Nested
    @DisplayName("syncFromOutBox - INACTIVE 스케줄러 처리")
    class SyncFromOutBoxInactiveTest {

        @Test
        @DisplayName("INACTIVE 상태 OutBox는 DISABLED 상태로 updateSchedule을 호출한다")
        void syncFromOutBox_withInactiveStatus_updatesWithDisabledState() {
            // given
            CrawlSchedulerOutBox outBox = createInactiveOutBox();
            Target mockTarget = mock(Target.class);
            UpdateScheduleRequest mockUpdateRequest =
                    UpdateScheduleRequest.builder().name("crawler-101").build();

            when(mapper.toScheduleName(any())).thenReturn("crawler-101");
            when(mapper.toCronExpression(any())).thenReturn("cron(0 10 * * ? *)");
            when(mapper.toTarget(any(), any(), any())).thenReturn(mockTarget);
            when(mapper.toUpdateRequest(any(), any(), any(), any(), any()))
                    .thenReturn(mockUpdateRequest);
            when(schedulerClient.updateSchedule(any(UpdateScheduleRequest.class)))
                    .thenReturn(UpdateScheduleResponse.builder().build());

            // when
            adapter.syncFromOutBox(outBox);

            // then
            // INACTIVE → DISABLED 상태로 updateSchedule 호출
            verify(mapper).toUpdateRequest(any(), any(), any(), any(ScheduleState.class), any());
            verify(schedulerClient, times(1)).updateSchedule(any(UpdateScheduleRequest.class));
        }
    }

    @Nested
    @DisplayName("syncFromOutBox - 예외 처리")
    class SyncFromOutBoxExceptionTest {

        @Test
        @DisplayName("EventBridgePublishException은 그대로 재전파된다")
        void syncFromOutBox_whenEventBridgePublishException_rethrowsAsIs() {
            // given
            CrawlSchedulerOutBox outBox = createActiveOutBox();
            Target mockTarget = mock(Target.class);

            when(mapper.toScheduleName(any())).thenReturn("crawler-100");
            when(mapper.toCronExpression(any())).thenReturn("cron(0 9 * * ? *)");
            when(mapper.toTarget(any(), any(), any())).thenReturn(mockTarget);
            when(mapper.toCreateRequest(any(), any(), any(), any()))
                    .thenReturn(CreateScheduleRequest.builder().build());
            when(mapper.toUpdateRequest(any(), any(), any(), any(), any()))
                    .thenReturn(UpdateScheduleRequest.builder().build());

            // createSchedule 실패 → fallback updateSchedule도 EventBridgePublishException
            when(schedulerClient.createSchedule(any(CreateScheduleRequest.class)))
                    .thenThrow(new RuntimeException("스케줄이 이미 존재합니다"));
            when(schedulerClient.updateSchedule(any(UpdateScheduleRequest.class)))
                    .thenThrow(new EventBridgePublishException("EventBridge 오류"));

            // when & then
            assertThatThrownBy(() -> adapter.syncFromOutBox(outBox))
                    .isInstanceOf(EventBridgePublishException.class)
                    .hasMessageContaining("EventBridge 오류");
        }

        @Test
        @DisplayName("try 블록 내에서 일반 예외 발생 시 EventBridgePublishException으로 래핑된다")
        void syncFromOutBox_whenGenericExceptionInsideTry_wrapsInEventBridgePublishException() {
            // given
            CrawlSchedulerOutBox outBox = createActiveOutBox();
            Target mockTarget = mock(Target.class);

            when(mapper.toScheduleName(any())).thenReturn("crawler-100");
            when(mapper.toCronExpression(any())).thenReturn("cron(0 9 * * ? *)");
            when(mapper.toTarget(any(), any(), any())).thenReturn(mockTarget);
            when(mapper.toCreateRequest(any(), any(), any(), any()))
                    .thenReturn(CreateScheduleRequest.builder().build());
            when(mapper.toUpdateRequest(any(), any(), any(), any(), any()))
                    .thenReturn(UpdateScheduleRequest.builder().build());

            // createSchedule 예외 → fallback updateSchedule도 일반 예외
            when(schedulerClient.createSchedule(any(CreateScheduleRequest.class)))
                    .thenThrow(new RuntimeException("createSchedule 실패"));
            when(schedulerClient.updateSchedule(any(UpdateScheduleRequest.class)))
                    .thenThrow(new RuntimeException("updateSchedule도 실패"));

            // when & then
            assertThatThrownBy(() -> adapter.syncFromOutBox(outBox))
                    .isInstanceOf(EventBridgePublishException.class)
                    .hasMessageContaining("OutBox 동기화 실패");
        }
    }
}
