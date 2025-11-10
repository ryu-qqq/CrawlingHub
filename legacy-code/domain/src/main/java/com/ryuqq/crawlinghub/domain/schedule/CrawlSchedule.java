package com.ryuqq.crawlinghub.domain.schedule;

import com.ryuqq.crawlinghub.domain.workflow.WorkflowId;

import java.time.LocalDateTime;

public class CrawlSchedule {

    private final ScheduleId scheduleId;
    private final WorkflowId workflowId;
    private final String scheduleName;
    private String cronExpression;
    private final String timezone;
    private boolean isEnabled;
    private final String eventbridgeRuleName;
    private LocalDateTime nextExecutionTime;

    private CrawlSchedule(ScheduleId scheduleId, WorkflowId workflowId, String scheduleName, String cronExpression,
                         String timezone, boolean isEnabled, String eventbridgeRuleName, LocalDateTime nextExecutionTime) {
        this.scheduleId = scheduleId;
        this.workflowId = workflowId;
        this.scheduleName = scheduleName;
        this.cronExpression = cronExpression;
        this.timezone = timezone;
        this.isEnabled = isEnabled;
        this.eventbridgeRuleName = eventbridgeRuleName;
        this.nextExecutionTime = nextExecutionTime;
    }

    public static CrawlSchedule create(WorkflowId workflowId, String scheduleName, String cronExpression, String timezone) {
        validateCreate(workflowId, scheduleName, cronExpression, timezone);
        String eventbridgeRuleName = generateEventbridgeRuleName(scheduleName);
        return new CrawlSchedule(null, workflowId, scheduleName, cronExpression, timezone, true, eventbridgeRuleName, null);
    }

    public static CrawlSchedule reconstitute(ScheduleId scheduleId, WorkflowId workflowId, String scheduleName,
                                            String cronExpression, String timezone, boolean isEnabled,
                                            String eventbridgeRuleName, LocalDateTime nextExecutionTime) {
        return new CrawlSchedule(scheduleId, workflowId, scheduleName, cronExpression,
                timezone, isEnabled, eventbridgeRuleName, nextExecutionTime);
    }

    private static void validateCreate(WorkflowId workflowId, String scheduleName, String cronExpression, String timezone) {
        if (workflowId == null) {
            throw new IllegalArgumentException("Workflow ID cannot be null");
        }
        if (scheduleName == null || scheduleName.isBlank()) {
            throw new IllegalArgumentException("Schedule name cannot be null or blank");
        }
        if (cronExpression == null || cronExpression.isBlank()) {
            throw new IllegalArgumentException("Cron expression cannot be null or blank");
        }
        if (timezone == null || timezone.isBlank()) {
            throw new IllegalArgumentException("Timezone cannot be null or blank");
        }
    }

    private static String generateEventbridgeRuleName(String scheduleName) {
        return "crawl-schedule-" + scheduleName.toLowerCase().replaceAll("[^a-z0-9-]", "-");
    }

    public void enable() {
        this.isEnabled = true;
    }

    public void disable() {
        this.isEnabled = false;
    }

    public void updateNextExecutionTime(LocalDateTime nextTime) {
        this.nextExecutionTime = nextTime;
    }

    public void updateCronExpression(String newExpression) {
        if (newExpression == null || newExpression.isBlank()) {
            throw new IllegalArgumentException("Cron expression cannot be null or blank");
        }
        this.cronExpression = newExpression;
    }

    public ScheduleId getScheduleId() {
        return scheduleId;
    }

    public WorkflowId getWorkflowId() {
        return workflowId;
    }

    public String getScheduleName() {
        return scheduleName;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public String getTimezone() {
        return timezone;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public String getEventbridgeRuleName() {
        return eventbridgeRuleName;
    }

    public LocalDateTime getNextExecutionTime() {
        return nextExecutionTime;
    }

}
