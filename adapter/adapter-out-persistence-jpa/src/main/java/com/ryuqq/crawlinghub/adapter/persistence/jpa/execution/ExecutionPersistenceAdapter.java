package com.ryuqq.crawlinghub.adapter.persistence.jpa.execution;

import com.ryuqq.crawlinghub.application.execution.port.out.LoadExecutionPort;
import com.ryuqq.crawlinghub.domain.common.ExecutionStatus;
import com.ryuqq.crawlinghub.domain.execution.CrawlExecution;
import com.ryuqq.crawlinghub.domain.execution.ExecutionId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Persistence Adapter for CrawlExecution
 * Implements Query (Load) port only
 * Follows CQRS pattern - read operations using QueryDSL
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

}
