package com.ryuqq.crawlinghub.adapter.persistence.jpa.schedule;

import com.ryuqq.crawlinghub.application.schedule.port.CrawlScheduleCommandPort;
import com.ryuqq.crawlinghub.application.schedule.port.CrawlScheduleQueryPort;
import com.ryuqq.crawlinghub.domain.schedule.CrawlSchedule;
import com.ryuqq.crawlinghub.domain.schedule.ScheduleId;
import com.ryuqq.crawlinghub.domain.schedule.ScheduleInputParam;
import com.ryuqq.crawlinghub.domain.workflow.WorkflowId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Persistence Adapter for CrawlSchedule
 * Implements both Command and Query ports for Schedule operations
 * Follows CQRS pattern by delegating to appropriate repositories
 */
@Component
public class SchedulePersistenceAdapter implements CrawlScheduleCommandPort, CrawlScheduleQueryPort {

    private final CrawlScheduleJpaRepository jpaRepository;
    private final CrawlScheduleQueryRepository queryRepository;
    private final ScheduleInputParamJpaRepository inputParamRepository;
    private final ScheduleMapper mapper;

    public SchedulePersistenceAdapter(
            CrawlScheduleJpaRepository jpaRepository,
            CrawlScheduleQueryRepository queryRepository,
            ScheduleInputParamJpaRepository inputParamRepository,
            ScheduleMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.queryRepository = queryRepository;
        this.inputParamRepository = inputParamRepository;
        this.mapper = mapper;
    }

    // ========================================
    // Command Port Implementation (CrawlScheduleCommandPort)
    // ========================================

    @Override
    public CrawlSchedule save(CrawlSchedule schedule) {
        CrawlScheduleEntity entity = mapper.toEntity(schedule);
        CrawlScheduleEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<CrawlSchedule> findById(ScheduleId scheduleId) {
        return jpaRepository.findById(scheduleId.value())
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsById(ScheduleId scheduleId) {
        return jpaRepository.existsById(scheduleId.value());
    }

    @Override
    public void deleteById(ScheduleId scheduleId) {
        jpaRepository.deleteById(scheduleId.value());
    }

    @Override
    public List<ScheduleInputParam> saveInputParams(List<ScheduleInputParam> params) {
        List<ScheduleInputParamEntity> entities = params.stream()
                .map(mapper::toInputParamEntity)
                .toList();

        List<ScheduleInputParamEntity> savedEntities = inputParamRepository.saveAll(entities);
        return mapper.toInputParamDomains(savedEntities);
    }

    @Override
    public void deleteInputParamsByScheduleId(Long scheduleId) {
        inputParamRepository.deleteByScheduleId(scheduleId);
    }

    @Override
    public List<ScheduleInputParam> findInputParamsByScheduleId(Long scheduleId) {
        List<ScheduleInputParamEntity> entities = inputParamRepository.findByScheduleId(scheduleId);
        return mapper.toInputParamDomains(entities);
    }

    // ========================================
    // Query Port Implementation (CrawlScheduleQueryPort)
    // ========================================

    @Override
    public Optional<CrawlSchedule> findByIdWithInputParams(ScheduleId scheduleId) {
        CrawlScheduleEntity scheduleEntity = queryRepository.findByIdWithInputParams(scheduleId.value());
        if (scheduleEntity == null) {
            return Optional.empty();
        }

        // Convert to domain
        CrawlSchedule schedule = mapper.toDomain(scheduleEntity);

        // Note: Input params are loaded separately via findInputParamsByScheduleId if needed
        // This keeps the domain model clean and avoids unnecessary data loading
        return Optional.of(schedule);
    }

    @Override
    public List<CrawlSchedule> findByWorkflowId(WorkflowId workflowId) {
        List<CrawlScheduleEntity> entities = queryRepository.findByWorkflowId(workflowId.value());
        return mapper.toDomains(entities);
    }

    @Override
    public List<CrawlSchedule> findByIsEnabled(boolean isEnabled) {
        List<CrawlScheduleEntity> entities = queryRepository.findByIsEnabled(isEnabled);
        return mapper.toDomains(entities);
    }

    @Override
    public List<CrawlSchedule> findByWorkflowIdAndIsEnabled(WorkflowId workflowId, boolean isEnabled) {
        List<CrawlScheduleEntity> entities = queryRepository.findByWorkflowIdAndIsEnabled(
                workflowId.value(),
                isEnabled
        );
        return mapper.toDomains(entities);
    }

    @Override
    public List<CrawlSchedule> findAll() {
        List<CrawlScheduleEntity> entities = queryRepository.findAll();
        return mapper.toDomains(entities);
    }

    @Override
    public long countByWorkflowId(WorkflowId workflowId) {
        return queryRepository.countByWorkflowId(workflowId.value());
    }
}
