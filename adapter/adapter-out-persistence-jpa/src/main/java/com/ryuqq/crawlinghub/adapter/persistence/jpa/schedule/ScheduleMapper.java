package com.ryuqq.crawlinghub.adapter.persistence.jpa.schedule;

import com.ryuqq.crawlinghub.domain.schedule.CrawlSchedule;
import com.ryuqq.crawlinghub.domain.schedule.ScheduleId;
import com.ryuqq.crawlinghub.domain.schedule.ScheduleInputParam;
import com.ryuqq.crawlinghub.domain.workflow.WorkflowId;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper for Schedule Domain and Entity conversion
 * Handles bidirectional mapping between domain objects and JPA entities
 */
@Component
public class ScheduleMapper {

    /**
     * Converts Schedule domain to JPA entity
     */
    public CrawlScheduleEntity toEntity(CrawlSchedule schedule) {
        return CrawlScheduleEntity.builder()
                .scheduleId(schedule.getScheduleId() != null ? schedule.getScheduleId().value() : null)
                .workflowId(schedule.getWorkflowId().value())
                .scheduleName(schedule.getScheduleName())
                .cronExpression(schedule.getCronExpression())
                .timezone(schedule.getTimezone())
                .isEnabled(schedule.isEnabled())
                .eventbridgeRuleName(schedule.getEventbridgeRuleName())
                .nextExecutionTime(schedule.getNextExecutionTime())
                .build();
    }

    /**
     * Converts JPA entity to Schedule domain
     */
    public CrawlSchedule toDomain(CrawlScheduleEntity entity) {
        return CrawlSchedule.reconstitute(
                new ScheduleId(entity.getScheduleId()),
                new WorkflowId(entity.getWorkflowId()),
                entity.getScheduleName(),
                entity.getCronExpression(),
                entity.getTimezone(),
                entity.getIsEnabled(),
                entity.getEventbridgeRuleName(),
                entity.getNextExecutionTime()
        );
    }

    /**
     * Converts multiple entities to domain objects
     */
    public List<CrawlSchedule> toDomains(List<CrawlScheduleEntity> entities) {
        return entities.stream()
                .map(this::toDomain)
                .toList();
    }

    /**
     * Converts ScheduleInputParam domain to JPA entity
     */
    public ScheduleInputParamEntity toInputParamEntity(ScheduleInputParam param) {
        return ScheduleInputParamEntity.builder()
                .inputParamId(param.getInputParamId())
                .scheduleId(param.getScheduleId())
                .paramKey(param.getParamKey())
                .paramValue(param.getParamValue())
                .paramType(param.getParamType())
                .build();
    }

    /**
     * Converts JPA entity to ScheduleInputParam domain
     */
    public ScheduleInputParam toInputParamDomain(ScheduleInputParamEntity entity) {
        return ScheduleInputParam.reconstitute(
                entity.getInputParamId(),
                entity.getScheduleId(),
                entity.getParamKey(),
                entity.getParamValue(),
                entity.getParamType()
        );
    }

    /**
     * Converts multiple param entities to domain objects
     */
    public List<ScheduleInputParam> toInputParamDomains(List<ScheduleInputParamEntity> entities) {
        return entities.stream()
                .map(this::toInputParamDomain)
                .toList();
    }
}
