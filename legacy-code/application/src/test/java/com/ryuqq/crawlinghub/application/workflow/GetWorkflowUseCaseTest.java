package com.ryuqq.crawlinghub.application.workflow;

import com.ryuqq.crawlinghub.application.workflow.port.out.LoadWorkflowPort;
import com.ryuqq.crawlinghub.application.workflow.usecase.GetWorkflowUseCase;
import com.ryuqq.crawlinghub.application.workflow.usecase.WorkflowNotFoundException;
import com.ryuqq.crawlinghub.domain.common.SiteType;
import com.ryuqq.crawlinghub.domain.site.CrawlSite;
import com.ryuqq.crawlinghub.domain.site.SiteId;
import com.ryuqq.crawlinghub.domain.workflow.CrawlWorkflow;
import com.ryuqq.crawlinghub.domain.workflow.WorkflowId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test for GetWorkflowUseCase
 * Tests workflow retrieval operations (CQRS Query pattern)
 */
@ExtendWith(MockitoExtension.class)
class GetWorkflowUseCaseTest {

    @Mock
    private LoadWorkflowPort loadWorkflowPort;

    private GetWorkflowUseCase getWorkflowUseCase;

    @BeforeEach
    void setUp() {
        getWorkflowUseCase = new GetWorkflowUseCase(loadWorkflowPort);
    }

    @Test
    @DisplayName("워크플로우 상세 정보를 조회한다")
    void shouldGetWorkflowDetail() {
        // given
        Long workflowId = 1L;
        CrawlWorkflow workflow = createTestWorkflow(workflowId, 1L, "Test Workflow", true);

        when(loadWorkflowPort.findById(any(WorkflowId.class))).thenReturn(Optional.of(workflow));

        // when
        CrawlWorkflow result = getWorkflowUseCase.getDetail(WorkflowId.of(workflowId));

        // then
        assertThat(result).isNotNull();
        assertThat(result.getWorkflowId().value()).isEqualTo(workflowId);
        assertThat(result.getWorkflowName()).isEqualTo("Test Workflow");
        verify(loadWorkflowPort).findById(WorkflowId.of(workflowId));
    }

    @Test
    @DisplayName("존재하지 않는 워크플로우 조회 시 예외 발생")
    void shouldThrowExceptionWhenWorkflowNotFound() {
        // given
        Long workflowId = 999L;
        when(loadWorkflowPort.findById(any(WorkflowId.class))).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> getWorkflowUseCase.getDetail(WorkflowId.of(workflowId)))
                .isInstanceOf(WorkflowNotFoundException.class)
                .hasMessageContaining("Workflow not found");

        verify(loadWorkflowPort).findById(WorkflowId.of(workflowId));
    }

    @Test
    @DisplayName("사이트별 워크플로우 목록을 조회한다")
    void shouldGetWorkflowsBySite() {
        // given
        Long siteId = 1L;
        List<CrawlWorkflow> workflows = List.of(
                createTestWorkflow(1L, siteId, "Workflow 1", true),
                createTestWorkflow(2L, siteId, "Workflow 2", true)
        );

        when(loadWorkflowPort.findBySiteId(any(SiteId.class))).thenReturn(workflows);

        // when
        List<CrawlWorkflow> result = getWorkflowUseCase.getWorkflowsBySite(SiteId.of(siteId));

        // then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(w -> w.getSiteId().value().equals(siteId));
        verify(loadWorkflowPort).findBySiteId(SiteId.of(siteId));
    }

    @Test
    @DisplayName("활성화된 워크플로우 목록을 조회한다")
    void shouldGetAllActiveWorkflows() {
        // given
        List<CrawlWorkflow> activeWorkflows = List.of(
                createTestWorkflow(1L, 1L, "Active 1", true),
                createTestWorkflow(2L, 2L, "Active 2", true)
        );

        when(loadWorkflowPort.findActiveWorkflows()).thenReturn(activeWorkflows);

        // when
        List<CrawlWorkflow> result = getWorkflowUseCase.getAllActiveWorkflows();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(CrawlWorkflow::isActive);
        verify(loadWorkflowPort).findActiveWorkflows();
    }

    @Test
    @DisplayName("사이트에 워크플로우가 없으면 빈 목록을 반환한다")
    void shouldReturnEmptyListWhenNoWorkflowsForSite() {
        // given
        Long siteId = 999L;
        when(loadWorkflowPort.findBySiteId(any(SiteId.class))).thenReturn(List.of());

        // when
        List<CrawlWorkflow> result = getWorkflowUseCase.getWorkflowsBySite(SiteId.of(siteId));

        // then
        assertThat(result).isEmpty();
        verify(loadWorkflowPort).findBySiteId(SiteId.of(siteId));
    }

    // ========== Helper Methods ==========

    private CrawlWorkflow createTestWorkflow(Long workflowId, Long siteId, String name, boolean isActive) {
        return CrawlWorkflow.reconstitute(
                WorkflowId.of(workflowId),
                SiteId.of(siteId),
                name,
                "Test description",
                isActive
        );
    }
}
