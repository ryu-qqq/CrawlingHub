package com.ryuqq.crawlinghub.adapter.persistence.jpa.workflow;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Command Repository for WorkflowStepParam (CQRS Pattern)
 * Handles only CUD operations using JpaRepository
 * - save(entity): Create or Update
 * - deleteById(id): Delete
 * - existsById(id): Existence check
 *
 * Complex queries should use QueryDSL in a separate QueryRepository if needed
 */
public interface WorkflowStepParamJpaRepository extends JpaRepository<WorkflowStepParamEntity, Long> {
    // Inherits basic CRUD methods from JpaRepository
    // - findById(Long id)
    // - save(WorkflowStepParamEntity entity)
    // - deleteById(Long id)
    // - existsById(Long id)

    /**
     * Find all params by step ID for retrieval operations
     * Used when loading step with params
     */
    List<WorkflowStepParamEntity> findByStepId(Long stepId);

    /**
     * Delete all params by step ID for cascade operations
     * Used when deleting a step or replacing all params
     */
    void deleteByStepId(Long stepId);
}
