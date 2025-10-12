package com.ryuqq.crawlinghub.domain.schedule;

import com.ryuqq.crawlinghub.domain.workflow.WorkflowId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CrawlScheduleTest {

    @Test
    @DisplayName("스케줄 생성 시 필수 값 검증")
    void shouldCreateScheduleWithRequiredFields() {
        // given
        WorkflowId workflowId = WorkflowId.of(1L);
        String scheduleName = "Daily Product Crawl";
        String cronExpression = "0 0 * * * *";
        String timezone = "Asia/Seoul";

        // when
        CrawlSchedule schedule = CrawlSchedule.create(workflowId, scheduleName, cronExpression, timezone);

        // then
        assertThat(schedule.getWorkflowId()).isEqualTo(workflowId);
        assertThat(schedule.getScheduleName()).isEqualTo(scheduleName);
        assertThat(schedule.getCronExpression()).isEqualTo(cronExpression);
        assertThat(schedule.getTimezone()).isEqualTo(timezone);
        assertThat(schedule.isEnabled()).isTrue();
        assertThat(schedule.getScheduleId()).isNull();
        assertThat(schedule.getEventbridgeRuleName()).startsWith("crawl-schedule-");
    }

    @Test
    @DisplayName("Workflow ID가 null이면 예외 발생")
    void shouldThrowExceptionWhenWorkflowIdIsNull() {
        // given
        WorkflowId workflowId = null;
        String scheduleName = "Daily Product Crawl";
        String cronExpression = "0 0 * * * *";
        String timezone = "Asia/Seoul";

        // when & then
        assertThatThrownBy(() -> CrawlSchedule.create(workflowId, scheduleName, cronExpression, timezone))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Workflow ID cannot be null");
    }

    @Test
    @DisplayName("스케줄 이름이 null이면 예외 발생")
    void shouldThrowExceptionWhenScheduleNameIsNull() {
        // given
        WorkflowId workflowId = WorkflowId.of(1L);
        String scheduleName = null;
        String cronExpression = "0 0 * * * *";
        String timezone = "Asia/Seoul";

        // when & then
        assertThatThrownBy(() -> CrawlSchedule.create(workflowId, scheduleName, cronExpression, timezone))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Schedule name cannot be null or blank");
    }

    @Test
    @DisplayName("Cron expression이 null이면 예외 발생")
    void shouldThrowExceptionWhenCronExpressionIsNull() {
        // given
        WorkflowId workflowId = WorkflowId.of(1L);
        String scheduleName = "Daily Product Crawl";
        String cronExpression = null;
        String timezone = "Asia/Seoul";

        // when & then
        assertThatThrownBy(() -> CrawlSchedule.create(workflowId, scheduleName, cronExpression, timezone))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cron expression cannot be null or blank");
    }

    @Test
    @DisplayName("Timezone이 null이면 예외 발생")
    void shouldThrowExceptionWhenTimezoneIsNull() {
        // given
        WorkflowId workflowId = WorkflowId.of(1L);
        String scheduleName = "Daily Product Crawl";
        String cronExpression = "0 0 * * * *";
        String timezone = null;

        // when & then
        assertThatThrownBy(() -> CrawlSchedule.create(workflowId, scheduleName, cronExpression, timezone))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Timezone cannot be null or blank");
    }

    @Test
    @DisplayName("스케줄 비활성화 가능")
    void shouldDisableSchedule() {
        // given
        CrawlSchedule schedule = CrawlSchedule.create(
                WorkflowId.of(1L),
                "Test Schedule",
                "0 0 * * * *",
                "Asia/Seoul"
        );

        // when
        schedule.disable();

        // then
        assertThat(schedule.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("스케줄 활성화 가능")
    void shouldEnableSchedule() {
        // given
        CrawlSchedule schedule = CrawlSchedule.create(
                WorkflowId.of(1L),
                "Test Schedule",
                "0 0 * * * *",
                "Asia/Seoul"
        );
        schedule.disable();

        // when
        schedule.enable();

        // then
        assertThat(schedule.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("다음 실행 시간 업데이트 가능")
    void shouldUpdateNextExecutionTime() {
        // given
        CrawlSchedule schedule = CrawlSchedule.create(
                WorkflowId.of(1L),
                "Test Schedule",
                "0 0 * * * *",
                "Asia/Seoul"
        );
        LocalDateTime nextTime = LocalDateTime.of(2025, 10, 14, 15, 0);

        // when
        schedule.updateNextExecutionTime(nextTime);

        // then
        assertThat(schedule.getNextExecutionTime()).isEqualTo(nextTime);
    }

    @Test
    @DisplayName("Cron expression 업데이트 가능")
    void shouldUpdateCronExpression() {
        // given
        CrawlSchedule schedule = CrawlSchedule.create(
                WorkflowId.of(1L),
                "Test Schedule",
                "0 0 * * * *",
                "Asia/Seoul"
        );

        // when
        schedule.updateCronExpression("0 30 * * * *");

        // then
        assertThat(schedule.getCronExpression()).isEqualTo("0 30 * * * *");
    }

    @Test
    @DisplayName("Cron expression이 null이면 업데이트 실패")
    void shouldThrowExceptionWhenUpdatingWithNullCronExpression() {
        // given
        CrawlSchedule schedule = CrawlSchedule.create(
                WorkflowId.of(1L),
                "Test Schedule",
                "0 0 * * * *",
                "Asia/Seoul"
        );

        // when & then
        assertThatThrownBy(() -> schedule.updateCronExpression(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cron expression cannot be null or blank");
    }

    @Test
    @DisplayName("EventBridge 규칙 이름이 올바르게 생성됨")
    void shouldGenerateEventbridgeRuleNameCorrectly() {
        // given
        String scheduleName = "Daily Product Crawl Test";

        // when
        CrawlSchedule schedule = CrawlSchedule.create(
                WorkflowId.of(1L),
                scheduleName,
                "0 0 * * * *",
                "Asia/Seoul"
        );

        // then
        assertThat(schedule.getEventbridgeRuleName())
                .isEqualTo("crawl-schedule-daily-product-crawl-test");
    }

    @Test
    @DisplayName("reconstitute로 DB에서 스케줄 복원 가능")
    void shouldReconstituteScheduleFromDatabase() {
        // given
        ScheduleId scheduleId = ScheduleId.of(100L);
        WorkflowId workflowId = WorkflowId.of(1L);
        String scheduleName = "Existing Schedule";
        String cronExpression = "0 0 * * * *";
        String timezone = "Asia/Seoul";
        boolean isEnabled = false;
        String eventbridgeRuleName = "crawl-schedule-existing-schedule";
        LocalDateTime nextExecutionTime = LocalDateTime.of(2025, 10, 14, 15, 0);

        // when
        CrawlSchedule schedule = CrawlSchedule.reconstitute(
                scheduleId, workflowId, scheduleName, cronExpression,
                timezone, isEnabled, eventbridgeRuleName, nextExecutionTime
        );

        // then
        assertThat(schedule.getScheduleId()).isEqualTo(scheduleId);
        assertThat(schedule.getWorkflowId()).isEqualTo(workflowId);
        assertThat(schedule.getScheduleName()).isEqualTo(scheduleName);
        assertThat(schedule.getCronExpression()).isEqualTo(cronExpression);
        assertThat(schedule.getTimezone()).isEqualTo(timezone);
        assertThat(schedule.isEnabled()).isFalse();
        assertThat(schedule.getEventbridgeRuleName()).isEqualTo(eventbridgeRuleName);
        assertThat(schedule.getNextExecutionTime()).isEqualTo(nextExecutionTime);
    }

}
