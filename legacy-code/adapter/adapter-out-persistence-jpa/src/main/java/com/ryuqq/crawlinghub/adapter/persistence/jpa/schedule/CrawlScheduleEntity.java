package com.ryuqq.crawlinghub.adapter.persistence.jpa.schedule;

import com.ryuqq.crawlinghub.adapter.persistence.jpa.common.BaseTimeEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "crawl_schedule")
public class CrawlScheduleEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long scheduleId;

    @Column(name = "workflow_id", nullable = false)
    private Long workflowId;

    @Column(name = "schedule_name", nullable = false, length = 200)
    private String scheduleName;

    @Column(name = "cron_expression", nullable = false, length = 100)
    private String cronExpression;

    @Column(name = "timezone", length = 50)
    private String timezone;

    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled;

    @Column(name = "eventbridge_rule_name", length = 200)
    private String eventbridgeRuleName;

    @Column(name = "next_execution_time")
    private LocalDateTime nextExecutionTime;

    protected CrawlScheduleEntity() {
    }

    private CrawlScheduleEntity(Long scheduleId, Long workflowId, String scheduleName, String cronExpression,
                         String timezone, Boolean isEnabled, String eventbridgeRuleName, LocalDateTime nextExecutionTime) {
        this.scheduleId = scheduleId;
        this.workflowId = workflowId;
        this.scheduleName = scheduleName;
        this.cronExpression = cronExpression;
        this.timezone = timezone;
        this.isEnabled = isEnabled;
        this.eventbridgeRuleName = eventbridgeRuleName;
        this.nextExecutionTime = nextExecutionTime;
    }

    public Long getScheduleId() {
        return scheduleId;
    }

    public Long getWorkflowId() {
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

    public Boolean getIsEnabled() {
        return isEnabled;
    }

    public String getEventbridgeRuleName() {
        return eventbridgeRuleName;
    }

    public LocalDateTime getNextExecutionTime() {
        return nextExecutionTime;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long scheduleId;
        private Long workflowId;
        private String scheduleName;
        private String cronExpression;
        private String timezone;
        private Boolean isEnabled;
        private String eventbridgeRuleName;
        private LocalDateTime nextExecutionTime;

        public Builder scheduleId(Long scheduleId) {
            this.scheduleId = scheduleId;
            return this;
        }

        public Builder workflowId(Long workflowId) {
            this.workflowId = workflowId;
            return this;
        }

        public Builder scheduleName(String scheduleName) {
            this.scheduleName = scheduleName;
            return this;
        }

        public Builder cronExpression(String cronExpression) {
            this.cronExpression = cronExpression;
            return this;
        }

        public Builder timezone(String timezone) {
            this.timezone = timezone;
            return this;
        }

        public Builder isEnabled(Boolean isEnabled) {
            this.isEnabled = isEnabled;
            return this;
        }

        public Builder eventbridgeRuleName(String eventbridgeRuleName) {
            this.eventbridgeRuleName = eventbridgeRuleName;
            return this;
        }

        public Builder nextExecutionTime(LocalDateTime nextExecutionTime) {
            this.nextExecutionTime = nextExecutionTime;
            return this;
        }

        public CrawlScheduleEntity build() {
            return new CrawlScheduleEntity(scheduleId, workflowId, scheduleName, cronExpression,
                    timezone, isEnabled, eventbridgeRuleName, nextExecutionTime);
        }
    }

}
