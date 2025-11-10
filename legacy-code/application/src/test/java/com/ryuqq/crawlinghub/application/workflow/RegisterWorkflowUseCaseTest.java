package com.ryuqq.crawlinghub.application.workflow;

import com.ryuqq.crawlinghub.application.site.port.out.LoadSitePort;
import com.ryuqq.crawlinghub.application.site.usecase.SiteNotFoundException;
import com.ryuqq.crawlinghub.application.workflow.port.out.SaveWorkflowPort;
import com.ryuqq.crawlinghub.application.workflow.usecase.InvalidWorkflowException;
import com.ryuqq.crawlinghub.application.workflow.usecase.RegisterWorkflowCommand;
import com.ryuqq.crawlinghub.application.workflow.usecase.RegisterWorkflowUseCase;
import com.ryuqq.crawlinghub.domain.common.SiteType;
import com.ryuqq.crawlinghub.domain.site.CrawlSite;
import com.ryuqq.crawlinghub.domain.site.SiteId;
import com.ryuqq.crawlinghub.domain.workflow.CrawlWorkflow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test for RegisterWorkflowUseCase
 * Tests workflow registration with validation logic
 */
@ExtendWith(MockitoExtension.class)
class RegisterWorkflowUseCaseTest {

    @Mock
    private SaveWorkflowPort saveWorkflowPort;

    @Mock
    private LoadSitePort loadSitePort;

    private RegisterWorkflowUseCase registerWorkflowUseCase;

    @BeforeEach
    void setUp() {
        registerWorkflowUseCase = new RegisterWorkflowUseCase(
                saveWorkflowPort,
                loadSitePort
        );
    }

    @Test
    @DisplayName("워크플로우를 성공적으로 등록한다")
    void shouldRegisterWorkflowSuccessfully() {
        // given
        Long siteId = 1L;
        CrawlSite site = createTestSite(siteId);
        RegisterWorkflowCommand command = createValidCommand(siteId);

        CrawlWorkflow savedWorkflow = CrawlWorkflow.create(
                SiteId.of(siteId),
                command.workflowName(),
                command.workflowDescription()
        );

        when(loadSitePort.findById(any(SiteId.class))).thenReturn(Optional.of(site));
        when(saveWorkflowPort.save(any(CrawlWorkflow.class))).thenReturn(savedWorkflow);

        // when
        CrawlWorkflow result = registerWorkflowUseCase.execute(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getSiteId().value()).isEqualTo(siteId);
        assertThat(result.getWorkflowName()).isEqualTo(command.workflowName());
        assertThat(result.isActive()).isTrue();

        // Verify interactions
        verify(loadSitePort).findById(SiteId.of(siteId));
        verify(saveWorkflowPort).save(any(CrawlWorkflow.class));
    }

    @Test
    @DisplayName("존재하지 않는 사이트로 워크플로우 등록 시 예외 발생")
    void shouldThrowExceptionWhenSiteNotFound() {
        // given
        Long siteId = 999L;
        RegisterWorkflowCommand command = createValidCommand(siteId);

        when(loadSitePort.findById(any(SiteId.class))).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> registerWorkflowUseCase.execute(command))
                .isInstanceOf(SiteNotFoundException.class)
                .hasMessageContaining("Site not found");

        verify(loadSitePort).findById(SiteId.of(siteId));
        verify(saveWorkflowPort, never()).save(any());
    }

    @Test
    @DisplayName("스텝 순서가 1부터 시작하지 않으면 예외 발생")
    void shouldThrowExceptionWhenStepOrderNotStartFromOne() {
        // given
        Long siteId = 1L;
        CrawlSite site = createTestSite(siteId);

        // Step order starts from 2 (invalid)
        RegisterWorkflowCommand.WorkflowStepCommand invalidStep =
                new RegisterWorkflowCommand.WorkflowStepCommand(
                        "Step 2",
                        2, // Invalid: should start from 1
                        "API_CALL",
                        "endpoint1",
                        false,
                        List.of(),
                        List.of()
                );

        RegisterWorkflowCommand command = new RegisterWorkflowCommand(
                siteId,
                "Invalid Workflow",
                "Description",
                List.of(invalidStep)
        );

        when(loadSitePort.findById(any(SiteId.class))).thenReturn(Optional.of(site));

        // when & then
        assertThatThrownBy(() -> registerWorkflowUseCase.execute(command))
                .isInstanceOf(InvalidWorkflowException.class)
                .hasMessageContaining("Step orders must be sequential starting from 1");

        verify(saveWorkflowPort, never()).save(any());
    }

    @Test
    @DisplayName("스텝 순서가 연속적이지 않으면 예외 발생")
    void shouldThrowExceptionWhenStepOrderNotSequential() {
        // given
        Long siteId = 1L;
        CrawlSite site = createTestSite(siteId);

        // Steps with gap in order (1 → 3, missing 2)
        RegisterWorkflowCommand.WorkflowStepCommand step1 =
                createStepCommand("Step 1", 1, "endpoint1");
        RegisterWorkflowCommand.WorkflowStepCommand step3 =
                createStepCommand("Step 3", 3, "endpoint3"); // Gap: missing step 2

        RegisterWorkflowCommand command = new RegisterWorkflowCommand(
                siteId,
                "Invalid Workflow",
                "Description",
                List.of(step1, step3)
        );

        when(loadSitePort.findById(any(SiteId.class))).thenReturn(Optional.of(site));

        // when & then
        assertThatThrownBy(() -> registerWorkflowUseCase.execute(command))
                .isInstanceOf(InvalidWorkflowException.class)
                .hasMessageContaining("Step orders must be sequential");

        verify(saveWorkflowPort, never()).save(any());
    }

    @Test
    @DisplayName("출력 참조가 이전 스텝을 참조하면 성공")
    void shouldAllowBackwardOutputReference() {
        // given
        Long siteId = 1L;
        CrawlSite site = createTestSite(siteId);

        // Step 1 with output
        RegisterWorkflowCommand.StepOutputCommand output1 =
                new RegisterWorkflowCommand.StepOutputCommand(
                        "productId",
                        "$.data.id",
                        "STRING"
                );
        RegisterWorkflowCommand.WorkflowStepCommand step1 =
                createStepCommandWithOutput("Step 1", 1, "endpoint1", List.of(output1));

        // Step 2 referencing Step 1's output (valid backward reference)
        RegisterWorkflowCommand.StepParamCommand param2 =
                new RegisterWorkflowCommand.StepParamCommand(
                        "id",
                        "{{step1.productId}}", // Valid: referencing previous step
                        "OUTPUT_REF",
                        true
                );
        RegisterWorkflowCommand.WorkflowStepCommand step2 =
                createStepCommandWithParam("Step 2", 2, "endpoint2", List.of(param2));

        RegisterWorkflowCommand command = new RegisterWorkflowCommand(
                siteId,
                "Valid Workflow",
                "Description",
                List.of(step1, step2)
        );

        CrawlWorkflow savedWorkflow = CrawlWorkflow.create(
                SiteId.of(siteId),
                command.workflowName(),
                command.workflowDescription()
        );

        when(loadSitePort.findById(any(SiteId.class))).thenReturn(Optional.of(site));
        when(saveWorkflowPort.save(any(CrawlWorkflow.class))).thenReturn(savedWorkflow);

        // when
        CrawlWorkflow result = registerWorkflowUseCase.execute(command);

        // then
        assertThat(result).isNotNull();
        verify(saveWorkflowPort).save(any(CrawlWorkflow.class));
    }

    @Test
    @DisplayName("출력 참조가 이후 스텝을 참조하면 예외 발생 (순환 참조 방지)")
    void shouldThrowExceptionWhenForwardOutputReference() {
        // given
        Long siteId = 1L;
        CrawlSite site = createTestSite(siteId);

        // Step 1 referencing Step 2's output (invalid forward reference)
        RegisterWorkflowCommand.StepParamCommand param1 =
                new RegisterWorkflowCommand.StepParamCommand(
                        "id",
                        "{{step2.productId}}", // Invalid: forward reference
                        "OUTPUT_REF",
                        true
                );
        RegisterWorkflowCommand.WorkflowStepCommand step1 =
                createStepCommandWithParam("Step 1", 1, "endpoint1", List.of(param1));

        // Step 2 with output
        RegisterWorkflowCommand.StepOutputCommand output2 =
                new RegisterWorkflowCommand.StepOutputCommand(
                        "productId",
                        "$.data.id",
                        "STRING"
                );
        RegisterWorkflowCommand.WorkflowStepCommand step2 =
                createStepCommandWithOutput("Step 2", 2, "endpoint2", List.of(output2));

        RegisterWorkflowCommand command = new RegisterWorkflowCommand(
                siteId,
                "Invalid Workflow",
                "Description",
                List.of(step1, step2)
        );

        when(loadSitePort.findById(any(SiteId.class))).thenReturn(Optional.of(site));

        // when & then
        assertThatThrownBy(() -> registerWorkflowUseCase.execute(command))
                .isInstanceOf(InvalidWorkflowException.class)
                .hasMessageContaining("cannot reference output from");

        verify(saveWorkflowPort, never()).save(any());
    }

    @Test
    @DisplayName("출력 참조가 존재하지 않는 출력 키를 참조하면 예외 발생")
    void shouldThrowExceptionWhenReferencingNonExistentOutputKey() {
        // given
        Long siteId = 1L;
        CrawlSite site = createTestSite(siteId);

        // Step 1 with output "productId"
        RegisterWorkflowCommand.StepOutputCommand output1 =
                new RegisterWorkflowCommand.StepOutputCommand(
                        "productId",
                        "$.data.id",
                        "STRING"
                );
        RegisterWorkflowCommand.WorkflowStepCommand step1 =
                createStepCommandWithOutput("Step 1", 1, "endpoint1", List.of(output1));

        // Step 2 referencing non-existent output key "wrongKey"
        RegisterWorkflowCommand.StepParamCommand param2 =
                new RegisterWorkflowCommand.StepParamCommand(
                        "id",
                        "{{step1.wrongKey}}", // Invalid: output key doesn't exist
                        "OUTPUT_REF",
                        true
                );
        RegisterWorkflowCommand.WorkflowStepCommand step2 =
                createStepCommandWithParam("Step 2", 2, "endpoint2", List.of(param2));

        RegisterWorkflowCommand command = new RegisterWorkflowCommand(
                siteId,
                "Invalid Workflow",
                "Description",
                List.of(step1, step2)
        );

        when(loadSitePort.findById(any(SiteId.class))).thenReturn(Optional.of(site));

        // when & then
        assertThatThrownBy(() -> registerWorkflowUseCase.execute(command))
                .isInstanceOf(InvalidWorkflowException.class)
                .hasMessageContaining("Output key 'wrongKey' not found");

        verify(saveWorkflowPort, never()).save(any());
    }

    @Test
    @DisplayName("잘못된 JSONPath 표현식이 있으면 예외 발생")
    void shouldThrowExceptionWhenInvalidJsonPath() {
        // given
        Long siteId = 1L;
        CrawlSite site = createTestSite(siteId);

        // Invalid JSONPath (doesn't start with $)
        RegisterWorkflowCommand.StepOutputCommand invalidOutput =
                new RegisterWorkflowCommand.StepOutputCommand(
                        "productId",
                        "data.id", // Invalid: should start with $
                        "STRING"
                );
        RegisterWorkflowCommand.WorkflowStepCommand step =
                createStepCommandWithOutput("Step 1", 1, "endpoint1", List.of(invalidOutput));

        RegisterWorkflowCommand command = new RegisterWorkflowCommand(
                siteId,
                "Invalid Workflow",
                "Description",
                List.of(step)
        );

        when(loadSitePort.findById(any(SiteId.class))).thenReturn(Optional.of(site));

        // when & then
        assertThatThrownBy(() -> registerWorkflowUseCase.execute(command))
                .isInstanceOf(InvalidWorkflowException.class)
                .hasMessageContaining("Invalid JSONPath expression");

        verify(saveWorkflowPort, never()).save(any());
    }

    // ========== Helper Methods ==========

    private CrawlSite createTestSite(Long siteId) {
        return CrawlSite.reconstitute(
                new SiteId(siteId),
                "Test Site",
                "https://api.test.com",
                SiteType.REST_API,
                true
        );
    }

    private RegisterWorkflowCommand createValidCommand(Long siteId) {
        RegisterWorkflowCommand.WorkflowStepCommand step = createStepCommand(
                "Step 1",
                1,
                "endpoint1"
        );

        return new RegisterWorkflowCommand(
                siteId,
                "Test Workflow",
                "Test workflow description",
                List.of(step)
        );
    }

    private RegisterWorkflowCommand.WorkflowStepCommand createStepCommand(
            String stepName, Integer stepOrder, String endpointKey) {
        return new RegisterWorkflowCommand.WorkflowStepCommand(
                stepName,
                stepOrder,
                "API_CALL",
                endpointKey,
                false,
                List.of(),
                List.of()
        );
    }

    private RegisterWorkflowCommand.WorkflowStepCommand createStepCommandWithOutput(
            String stepName, Integer stepOrder, String endpointKey,
            List<RegisterWorkflowCommand.StepOutputCommand> outputs) {
        return new RegisterWorkflowCommand.WorkflowStepCommand(
                stepName,
                stepOrder,
                "API_CALL",
                endpointKey,
                false,
                List.of(),
                outputs
        );
    }

    private RegisterWorkflowCommand.WorkflowStepCommand createStepCommandWithParam(
            String stepName, Integer stepOrder, String endpointKey,
            List<RegisterWorkflowCommand.StepParamCommand> params) {
        return new RegisterWorkflowCommand.WorkflowStepCommand(
                stepName,
                stepOrder,
                "API_CALL",
                endpointKey,
                false,
                params,
                List.of()
        );
    }

}
