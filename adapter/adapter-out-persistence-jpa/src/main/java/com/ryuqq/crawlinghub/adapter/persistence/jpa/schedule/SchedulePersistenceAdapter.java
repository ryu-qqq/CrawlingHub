package com.ryuqq.crawlinghub.adapter.persistence.jpa.schedule;

import com.ryuqq.crawlinghub.application.schedule.port.CrawlScheduleCommandPort;
import com.ryuqq.crawlinghub.application.schedule.port.CrawlScheduleQueryPort;
import com.ryuqq.crawlinghub.domain.schedule.CrawlSchedule;
import com.ryuqq.crawlinghub.domain.schedule.ScheduleId;
import com.ryuqq.crawlinghub.domain.schedule.ScheduleInputParam;
import com.ryuqq.crawlinghub.domain.workflow.WorkflowId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public CrawlSchedule save(CrawlSchedule schedule) {
        CrawlScheduleEntity entity = mapper.toEntity(schedule);
        CrawlScheduleEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CrawlSchedule> findById(ScheduleId scheduleId) {
        return jpaRepository.findById(scheduleId.value())
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(ScheduleId scheduleId) {
        return jpaRepository.existsById(scheduleId.value());
    }

    @Override
    @Transactional
    public void deleteById(ScheduleId scheduleId) {
        jpaRepository.deleteById(scheduleId.value());
    }

    @Override
    @Transactional
    public List<ScheduleInputParam> saveInputParams(List<ScheduleInputParam> params) {
        List<ScheduleInputParamEntity> entities = params.stream()
                .map(mapper::toInputParamEntity)
                .toList();

        List<ScheduleInputParamEntity> savedEntities = inputParamRepository.saveAll(entities);
        return mapper.toInputParamDomains(savedEntities);
    }

    @Override
    @Transactional
    public void deleteInputParamsByScheduleId(Long scheduleId) {
        inputParamRepository.deleteByScheduleId(scheduleId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleInputParam> findInputParamsByScheduleId(Long scheduleId) {
        List<ScheduleInputParamEntity> entities = inputParamRepository.findByScheduleId(scheduleId);
        return mapper.toInputParamDomains(entities);
    }

    // ========================================
    // Query Port Implementation (CrawlScheduleQueryPort)
    // ========================================

    @Override
    @Transactional(readOnly = true)
    public Optional<CrawlSchedule> findByIdWithInputParams(ScheduleId scheduleId) {
        CrawlScheduleEntity scheduleEntity = queryRepository.findByIdWithInputParams(scheduleId.value());
        if (scheduleEntity == null) {
            return Optional.empty();
        }

        // Load input parameters
        List<ScheduleInputParamEntity> paramEntities = inputParamRepository.findByScheduleId(scheduleId.value());

        // Convert to domain
        CrawlSchedule schedule = mapper.toDomain(scheduleEntity);

        // If there are input params, we could enhance the domain model to include them
        // For now, we just return the schedule
        // The caller can use findInputParamsByScheduleId if needed
        return Optional.of(schedule);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CrawlSchedule> findByWorkflowId(WorkflowId workflowId) {
        List<CrawlScheduleEntity> entities = queryRepository.findByWorkflowId(workflowId.value());
        return mapper.toDomains(entities);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CrawlSchedule> findByIsEnabled(boolean isEnabled) {
        List<CrawlScheduleEntity> entities = queryRepository.findByIsEnabled(isEnabled);
        return mapper.toDomains(entities);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CrawlSchedule> findByWorkflowIdAndIsEnabled(WorkflowId workflowId, boolean isEnabled) {
        List<CrawlScheduleEntity> entities = queryRepository.findByWorkflowIdAndIsEnabled(
                workflowId.value(),
                isEnabled
        );
        return mapper.toDomains(entities);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CrawlSchedule> findAll() {
        List<CrawlScheduleEntity> entities = queryRepository.findAll();
        return mapper.toDomains(entities);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByWorkflowId(WorkflowId workflowId) {
        return queryRepository.countByWorkflowId(workflowId.value());
    }
}
