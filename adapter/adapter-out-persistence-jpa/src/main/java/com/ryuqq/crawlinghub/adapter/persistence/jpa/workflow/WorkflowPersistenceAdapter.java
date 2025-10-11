package com.ryuqq.crawlinghub.adapter.persistence.jpa.workflow;

import com.ryuqq.crawlinghub.application.workflow.port.out.LoadWorkflowPort;
import com.ryuqq.crawlinghub.application.workflow.port.out.SaveWorkflowPort;
import com.ryuqq.crawlinghub.domain.site.SiteId;
import com.ryuqq.crawlinghub.domain.workflow.CrawlWorkflow;
import com.ryuqq.crawlinghub.domain.workflow.WorkflowId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Persistence Adapter for CrawlWorkflow
 * Implements both Command (Save) and Query (Load) ports
 * Follows CQRS pattern by delegating to appropriate repositories
 */
@Component
public class WorkflowPersistenceAdapter implements SaveWorkflowPort, LoadWorkflowPort {

    private final CrawlWorkflowJpaRepository jpaRepository;
    private final CrawlWorkflowQueryRepository queryRepository;
    private final WorkflowMapper mapper;

    public WorkflowPersistenceAdapter(CrawlWorkflowJpaRepository jpaRepository,
                                       CrawlWorkflowQueryRepository queryRepository,
                                       WorkflowMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.queryRepository = queryRepository;
        this.mapper = mapper;
    }

    // ========================================
    // Command Port Implementation (SaveWorkflowPort)
    // Uses JpaRepository for CUD operations
    // ========================================

    @Override
    public CrawlWorkflow save(CrawlWorkflow workflow) {
        CrawlWorkflowEntity entity = mapper.toEntity(workflow);
        CrawlWorkflowEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public void delete(WorkflowId workflowId) {
        jpaRepository.deleteById(workflowId.value());
    }

    // ========================================
    // Query Port Implementation (LoadWorkflowPort)
    // Uses JpaRepository for simple find, QueryRepository for complex queries
    // ========================================

    @Override
    public Optional<CrawlWorkflow> findById(WorkflowId workflowId) {
        return jpaRepository.findById(workflowId.value())
                .map(mapper::toDomain);
    }

    @Override
    public List<CrawlWorkflow> findBySiteId(SiteId siteId) {
        return queryRepository.findBySiteId(siteId.value()).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<CrawlWorkflow> findActiveWorkflows() {
        return queryRepository.findActiveWorkflows().stream()
                .map(mapper::toDomain)
                .toList();
    }

}
