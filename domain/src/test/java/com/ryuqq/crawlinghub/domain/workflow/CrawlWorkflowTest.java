package com.ryuqq.crawlinghub.domain.workflow;

import com.ryuqq.crawlinghub.domain.common.StepType;
import com.ryuqq.crawlinghub.domain.site.SiteId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CrawlWorkflowTest {

    @Test
    @DisplayName("워크플로우 생성 시 필수 값 검증")
    void shouldCreateWorkflowWithRequiredFields() {
        // given
        SiteId siteId = SiteId.of(1L);
        String workflowName = "Product Crawl Workflow";
        String workflowDescription = "상품 크롤링 워크플로우";

        // when
        CrawlWorkflow workflow = CrawlWorkflow.create(siteId, workflowName, workflowDescription);

        // then
        assertThat(workflow.getSiteId()).isEqualTo(siteId);
        assertThat(workflow.getWorkflowName()).isEqualTo(workflowName);
        assertThat(workflow.getWorkflowDescription()).isEqualTo(workflowDescription);
        assertThat(workflow.isActive()).isTrue();
        assertThat(workflow.getWorkflowId()).isNull();
        assertThat(workflow.getSteps()).isEmpty();
        assertThat(workflow.getStepsCount()).isZero();
    }

    @Test
    @DisplayName("Site ID가 null이면 예외 발생")
    void shouldThrowExceptionWhenSiteIdIsNull() {
        // given
        SiteId siteId = null;
        String workflowName = "Product Workflow";
        String workflowDescription = "설명";

        // when & then
        assertThatThrownBy(() -> CrawlWorkflow.create(siteId, workflowName, workflowDescription))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Site ID cannot be null");
    }

    @Test
    @DisplayName("워크플로우 이름이 null이면 예외 발생")
    void shouldThrowExceptionWhenWorkflowNameIsNull() {
        // given
        SiteId siteId = SiteId.of(1L);
        String workflowName = null;
        String workflowDescription = "설명";

        // when & then
        assertThatThrownBy(() -> CrawlWorkflow.create(siteId, workflowName, workflowDescription))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Workflow name cannot be null or blank");
    }

    @Test
    @DisplayName("워크플로우 이름이 blank면 예외 발생")
    void shouldThrowExceptionWhenWorkflowNameIsBlank() {
        // given
        SiteId siteId = SiteId.of(1L);
        String workflowName = "   ";
        String workflowDescription = "설명";

        // when & then
        assertThatThrownBy(() -> CrawlWorkflow.create(siteId, workflowName, workflowDescription))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Workflow name cannot be null or blank");
    }

    @Test
    @DisplayName("워크플로우 활성화/비활성화 가능")
    void shouldActivateAndDeactivateWorkflow() {
        // given
        CrawlWorkflow workflow = CrawlWorkflow.create(
                SiteId.of(1L),
                "Test Workflow",
                "설명"
        );

        // when & then - 비활성화
        workflow.deactivate();
        assertThat(workflow.isActive()).isFalse();

        // when & then - 다시 활성화
        workflow.activate();
        assertThat(workflow.isActive()).isTrue();
    }

    @Test
    @DisplayName("워크플로우 설명 업데이트 가능")
    void shouldUpdateWorkflowDescription() {
        // given
        CrawlWorkflow workflow = CrawlWorkflow.create(
                SiteId.of(1L),
                "Test Workflow",
                "원래 설명"
        );

        // when
        workflow.updateDescription("업데이트된 설명");

        // then
        assertThat(workflow.getWorkflowDescription()).isEqualTo("업데이트된 설명");
    }

    @Test
    @DisplayName("워크플로우에 스텝 추가 가능")
    void shouldAddStepToWorkflow() {
        // given
        CrawlWorkflow workflow = CrawlWorkflow.create(
                SiteId.of(1L),
                "Test Workflow",
                "설명"
        );

        WorkflowStep step = WorkflowStep.create(
                null,
                "API 호출",
                0,
                StepType.API_CALL,
                "product-api",
                false,
                null,
                null
        );

        // when
        workflow.addStep(step);

        // then
        assertThat(workflow.getSteps()).hasSize(1);
        assertThat(workflow.getStepsCount()).isEqualTo(1);
        assertThat(workflow.getSteps().get(0).getStepName()).isEqualTo("API 호출");
    }

    @Test
    @DisplayName("null 스텝은 추가할 수 없음")
    void shouldThrowExceptionWhenAddingNullStep() {
        // given
        CrawlWorkflow workflow = CrawlWorkflow.create(
                SiteId.of(1L),
                "Test Workflow",
                "설명"
        );

        // when & then
        assertThatThrownBy(() -> workflow.addStep(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Step cannot be null");
    }

    @Test
    @DisplayName("워크플로우 스텝 일괄 교체 가능")
    void shouldReplaceAllSteps() {
        // given
        CrawlWorkflow workflow = CrawlWorkflow.create(
                SiteId.of(1L),
                "Test Workflow",
                "설명"
        );

        WorkflowStep step1 = WorkflowStep.create(
                null, "Step 1", 0, StepType.API_CALL, "endpoint1", false, null, null
        );
        workflow.addStep(step1);

        List<WorkflowStep> newSteps = new ArrayList<>();
        newSteps.add(WorkflowStep.create(
                null, "New Step 1", 0, StepType.DATA_TRANSFORM, "endpoint2", false, null, null
        ));
        newSteps.add(WorkflowStep.create(
                null, "New Step 2", 1, StepType.DATA_VALIDATE, "endpoint3", false, null, null
        ));

        // when
        workflow.replaceSteps(newSteps);

        // then
        assertThat(workflow.getSteps()).hasSize(2);
        assertThat(workflow.getSteps().get(0).getStepName()).isEqualTo("New Step 1");
        assertThat(workflow.getSteps().get(1).getStepName()).isEqualTo("New Step 2");
    }

    @Test
    @DisplayName("워크플로우 스텝 전체 삭제 가능")
    void shouldClearAllSteps() {
        // given
        CrawlWorkflow workflow = CrawlWorkflow.create(
                SiteId.of(1L),
                "Test Workflow",
                "설명"
        );

        workflow.addStep(WorkflowStep.create(
                null, "Step 1", 0, StepType.API_CALL, "endpoint1", false, null, null
        ));
        workflow.addStep(WorkflowStep.create(
                null, "Step 2", 1, StepType.DATA_TRANSFORM, "endpoint2", false, null, null
        ));

        // when
        workflow.clearSteps();

        // then
        assertThat(workflow.getSteps()).isEmpty();
        assertThat(workflow.getStepsCount()).isZero();
    }

    @Test
    @DisplayName("reconstitute로 DB에서 워크플로우 복원 가능")
    void shouldReconstituteWorkflowFromDatabase() {
        // given
        WorkflowId workflowId = WorkflowId.of(100L);
        SiteId siteId = SiteId.of(1L);
        String workflowName = "Existing Workflow";
        String workflowDescription = "기존 워크플로우";
        boolean isActive = false;

        // when
        CrawlWorkflow workflow = CrawlWorkflow.reconstitute(
                workflowId, siteId, workflowName, workflowDescription, isActive
        );

        // then
        assertThat(workflow.getWorkflowId()).isEqualTo(workflowId);
        assertThat(workflow.getSiteId()).isEqualTo(siteId);
        assertThat(workflow.getWorkflowName()).isEqualTo(workflowName);
        assertThat(workflow.getWorkflowDescription()).isEqualTo(workflowDescription);
        assertThat(workflow.isActive()).isFalse();
        assertThat(workflow.getSteps()).isEmpty();
    }

    @Test
    @DisplayName("reconstitute로 스텝과 함께 워크플로우 복원 가능")
    void shouldReconstituteWorkflowWithStepsFromDatabase() {
        // given
        WorkflowId workflowId = WorkflowId.of(100L);
        SiteId siteId = SiteId.of(1L);
        String workflowName = "Existing Workflow";
        String workflowDescription = "기존 워크플로우";
        boolean isActive = true;

        List<WorkflowStep> steps = new ArrayList<>();
        steps.add(WorkflowStep.create(
                workflowId, "Step 1", 0, StepType.API_CALL, "endpoint1", false, null, null
        ));
        steps.add(WorkflowStep.create(
                workflowId, "Step 2", 1, StepType.DATA_TRANSFORM, "endpoint2", true, null, null
        ));

        // when
        CrawlWorkflow workflow = CrawlWorkflow.reconstituteWithSteps(
                workflowId, siteId, workflowName, workflowDescription, isActive, steps
        );

        // then
        assertThat(workflow.getWorkflowId()).isEqualTo(workflowId);
        assertThat(workflow.getSteps()).hasSize(2);
        assertThat(workflow.getSteps().get(0).getStepName()).isEqualTo("Step 1");
        assertThat(workflow.getSteps().get(1).getStepName()).isEqualTo("Step 2");
    }

    @Test
    @DisplayName("워크플로우 스텝 목록은 불변 리스트로 반환")
    void shouldReturnUnmodifiableListOfSteps() {
        // given
        CrawlWorkflow workflow = CrawlWorkflow.create(
                SiteId.of(1L),
                "Test Workflow",
                "설명"
        );

        workflow.addStep(WorkflowStep.create(
                null, "Step 1", 0, StepType.API_CALL, "endpoint1", false, null, null
        ));

        // when
        List<WorkflowStep> steps = workflow.getSteps();

        // then
        assertThatThrownBy(() -> steps.add(WorkflowStep.create(
                null, "Step 2", 1, StepType.DATA_TRANSFORM, "endpoint2", false, null, null
        ))).isInstanceOf(UnsupportedOperationException.class);
    }

}
