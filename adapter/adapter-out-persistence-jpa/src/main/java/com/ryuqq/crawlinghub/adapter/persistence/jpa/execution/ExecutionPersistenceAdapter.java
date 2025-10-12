package com.ryuqq.crawlinghub.adapter.persistence.jpa.execution;

import com.ryuqq.crawlinghub.application.execution.port.out.LoadExecutionPort;
import com.ryuqq.crawlinghub.domain.common.ExecutionStatus;
import com.ryuqq.crawlinghub.domain.execution.CrawlExecution;
import com.ryuqq.crawlinghub.domain.execution.ExecutionId;
import com.ryuqq.crawlinghub.domain.schedule.ScheduleId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Persistence Adapter for CrawlExecution
 * Implements Query (Load) port only
 * Follows CQRS pattern - read operations using QueryDSL
 * Supports both Offset-Based and No-Offset pagination strategies
 *
 * Note: For this implementation, we only implement LoadExecutionPort
 * as execution creation/modification might be handled by other services
 */
@Component
public class ExecutionPersistenceAdapter implements LoadExecutionPort {

    private final CrawlExecutionJpaRepository jpaRepository;
    private final CrawlExecutionQueryRepository queryRepository;
    private final ExecutionMapper mapper;

    public ExecutionPersistenceAdapter(CrawlExecutionJpaRepository jpaRepository,
                                        CrawlExecutionQueryRepository queryRepository,
                                        ExecutionMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.queryRepository = queryRepository;
        this.mapper = mapper;
    }

    // ========================================
    // Query Port Implementation (LoadExecutionPort)
    // Uses JpaRepository for simple find, QueryRepository for complex queries
    // ========================================

    @Override
    public Optional<CrawlExecution> findById(ExecutionId executionId) {
        return jpaRepository.findById(executionId.value())
                .map(mapper::toDomain);
    }

    @Override
    public List<CrawlExecution> findByStatus(ExecutionStatus status) {
        return queryRepository.findByStatus(status).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Page<CrawlExecution> findByStatus(ExecutionStatus status, Pageable pageable) {
        return queryRepository.findByStatus(status, pageable)
                .map(mapper::toDomain);
    }

    @Override
    public List<CrawlExecution> findByStatus(ExecutionStatus status, Long lastExecutionId, int pageSize) {
        return queryRepository.findByStatus(status, lastExecutionId, pageSize).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Page<CrawlExecution> findWithFilters(
            ScheduleId scheduleId,
            ExecutionStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    ) {
        Long scheduleIdValue = scheduleId != null ? scheduleId.value() : null;
        return queryRepository.findWithFilters(scheduleIdValue, status, startDate, endDate, pageable)
                .map(mapper::toDomain);
    }

    @Override
    public List<CrawlExecution> findWithFilters(
            ScheduleId scheduleId,
            ExecutionStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Long lastExecutionId,
            int pageSize
    ) {
        Long scheduleIdValue = scheduleId != null ? scheduleId.value() : null;
        return queryRepository.findWithFilters(scheduleIdValue, status, startDate, endDate, lastExecutionId, pageSize).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<CrawlExecution> findAll() {
        return queryRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Page<CrawlExecution> findAll(Pageable pageable) {
        return queryRepository.findAll(pageable)
                .map(mapper::toDomain);
    }

    @Override
    public List<CrawlExecution> findAll(Long lastExecutionId, int pageSize) {
        return queryRepository.findAll(lastExecutionId, pageSize).stream()
                .map(mapper::toDomain)
                .toList();
    }

}
