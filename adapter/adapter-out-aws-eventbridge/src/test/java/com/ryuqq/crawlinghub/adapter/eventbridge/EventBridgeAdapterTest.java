package com.ryuqq.crawlinghub.adapter.eventbridge;

import com.ryuqq.crawlinghub.adapter.config.EventBridgeProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.DeleteRuleRequest;
import software.amazon.awssdk.services.eventbridge.model.DescribeRuleRequest;
import software.amazon.awssdk.services.eventbridge.model.DescribeRuleResponse;
import software.amazon.awssdk.services.eventbridge.model.DisableRuleRequest;
import software.amazon.awssdk.services.eventbridge.model.EnableRuleRequest;
import software.amazon.awssdk.services.eventbridge.model.EventBridgeException;
import software.amazon.awssdk.services.eventbridge.model.PutRuleRequest;
import software.amazon.awssdk.services.eventbridge.model.PutRuleResponse;
import software.amazon.awssdk.services.eventbridge.model.PutTargetsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutTargetsResponse;
import software.amazon.awssdk.services.eventbridge.model.RemoveTargetsRequest;
import software.amazon.awssdk.services.eventbridge.model.RemoveTargetsResponse;
import software.amazon.awssdk.services.eventbridge.model.ResourceNotFoundException;
import software.amazon.awssdk.services.eventbridge.model.RuleState;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test for EventBridgeAdapter.
 *
 * @author crawlinghub (noreply@crawlinghub.com)
 */
@ExtendWith(MockitoExtension.class)
class EventBridgeAdapterTest {

    @Mock
    private EventBridgeClient eventBridgeClient;

    private EventBridgeProperties properties;
    private EventBridgeAdapter adapter;

    @BeforeEach
    void setUp() {
        properties = new EventBridgeProperties();
        properties.setEnabled(true);
        properties.setEventBusName("test-bus");
        properties.setTargetArn("arn:aws:lambda:ap-northeast-2:123456789012:function:test");

        adapter = new EventBridgeAdapter(eventBridgeClient, properties);
    }

    @Test
    @DisplayName("createRule 성공 - EventBridge 규칙 생성")
    void createRuleSuccess() {
        // given
        String ruleName = "test-rule";
        String cronExpression = "0 0 * * *";
        String description = "Test rule";
        String expectedArn = "arn:aws:events:ap-northeast-2:123456789012:rule/test-rule";

        when(eventBridgeClient.putRule(any(PutRuleRequest.class)))
                .thenReturn(PutRuleResponse.builder().ruleArn(expectedArn).build());

        // when
        String ruleArn = adapter.createRule(ruleName, cronExpression, description);

        // then
        assertThat(ruleArn).isEqualTo(expectedArn);

        ArgumentCaptor<PutRuleRequest> captor = ArgumentCaptor.forClass(PutRuleRequest.class);
        verify(eventBridgeClient).putRule(captor.capture());

        PutRuleRequest request = captor.getValue();
        assertThat(request.name()).isEqualTo(ruleName);
        assertThat(request.scheduleExpression()).isEqualTo("cron(" + cronExpression + ")");
        assertThat(request.description()).isEqualTo(description);
        assertThat(request.state()).isEqualTo(RuleState.DISABLED);
        assertThat(request.eventBusName()).isEqualTo("test-bus");
    }

    @Test
    @DisplayName("createRule 실패 - EventBridge 예외 발생")
    void createRuleFailure() {
        // given
        when(eventBridgeClient.putRule(any(PutRuleRequest.class)))
                .thenThrow(EventBridgeException.builder().message("AWS Error").build());

        // when & then
        assertThatThrownBy(() -> adapter.createRule("test", "0 0 * * *", "desc"))
                .isInstanceOf(EventBridgeAdapter.EventBridgeOperationException.class)
                .hasMessageContaining("Failed to create rule");
    }

    @Test
    @DisplayName("createRule 스킵 - EventBridge가 disabled 상태")
    void createRuleSkippedWhenDisabled() {
        // given
        properties.setEnabled(false);

        // when
        String result = adapter.createRule("test", "0 0 * * *", "desc");

        // then
        assertThat(result).isEqualTo("test");
        verify(eventBridgeClient, never()).putRule(any(PutRuleRequest.class));
    }

    @Test
    @DisplayName("updateRule 성공 - 기존 규칙 업데이트")
    void updateRuleSuccess() {
        // given
        String ruleName = "test-rule";
        String cronExpression = "0 0 * * *";
        String description = "Updated description";

        // when
        adapter.updateRule(ruleName, cronExpression, description);

        // then
        ArgumentCaptor<PutRuleRequest> captor = ArgumentCaptor.forClass(PutRuleRequest.class);
        verify(eventBridgeClient).putRule(captor.capture());

        PutRuleRequest request = captor.getValue();
        assertThat(request.name()).isEqualTo(ruleName);
        assertThat(request.scheduleExpression()).isEqualTo("cron(" + cronExpression + ")");
        assertThat(request.description()).isEqualTo(description);
    }

    @Test
    @DisplayName("addTarget 성공 - 규칙에 타겟 추가")
    void addTargetSuccess() {
        // given
        String ruleName = "test-rule";
        String targetInput = "{\"scheduleId\":1}";

        when(eventBridgeClient.putTargets(any(PutTargetsRequest.class)))
                .thenReturn(PutTargetsResponse.builder().failedEntryCount(0).build());

        // when
        adapter.addTarget(ruleName, targetInput);

        // then
        ArgumentCaptor<PutTargetsRequest> captor = ArgumentCaptor.forClass(PutTargetsRequest.class);
        verify(eventBridgeClient).putTargets(captor.capture());

        PutTargetsRequest request = captor.getValue();
        assertThat(request.rule()).isEqualTo(ruleName);
        assertThat(request.targets()).hasSize(1);
        assertThat(request.targets().get(0).id()).isEqualTo("Target-" + ruleName);
        assertThat(request.targets().get(0).arn()).isEqualTo(properties.getTargetArn());
        assertThat(request.targets().get(0).input()).isEqualTo(targetInput);
    }

    @Test
    @DisplayName("addTarget 실패 - targetArn이 설정되지 않음")
    void addTargetFailureNoTargetArn() {
        // given
        properties.setTargetArn(null);

        // when & then
        assertThatThrownBy(() -> adapter.addTarget("test", "{}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("target ARN is not configured");
    }

    @Test
    @DisplayName("addTarget 실패 - failed entries 존재")
    void addTargetFailureWithFailedEntries() {
        // given
        when(eventBridgeClient.putTargets(any(PutTargetsRequest.class)))
                .thenReturn(PutTargetsResponse.builder().failedEntryCount(1).build());

        // when & then
        assertThatThrownBy(() -> adapter.addTarget("test", "{}"))
                .isInstanceOf(EventBridgeAdapter.EventBridgeOperationException.class)
                .hasMessageContaining("Failed to add target");
    }

    @Test
    @DisplayName("removeTargets 성공 - 규칙에서 타겟 제거")
    void removeTargetsSuccess() {
        // given
        String ruleName = "test-rule";

        when(eventBridgeClient.removeTargets(any(RemoveTargetsRequest.class)))
                .thenReturn(RemoveTargetsResponse.builder().failedEntryCount(0).build());

        // when
        adapter.removeTargets(ruleName);

        // then
        ArgumentCaptor<RemoveTargetsRequest> captor = ArgumentCaptor.forClass(RemoveTargetsRequest.class);
        verify(eventBridgeClient).removeTargets(captor.capture());

        RemoveTargetsRequest request = captor.getValue();
        assertThat(request.rule()).isEqualTo(ruleName);
        assertThat(request.ids()).containsExactly("Target-" + ruleName);
    }

    @Test
    @DisplayName("deleteRule 성공 - 규칙 삭제")
    void deleteRuleSuccess() {
        // given
        String ruleName = "test-rule";

        // when
        adapter.deleteRule(ruleName);

        // then
        ArgumentCaptor<DeleteRuleRequest> captor = ArgumentCaptor.forClass(DeleteRuleRequest.class);
        verify(eventBridgeClient).deleteRule(captor.capture());

        DeleteRuleRequest request = captor.getValue();
        assertThat(request.name()).isEqualTo(ruleName);
    }

    @Test
    @DisplayName("enableRule 성공 - 규칙 활성화")
    void enableRuleSuccess() {
        // given
        String ruleName = "test-rule";

        // when
        adapter.enableRule(ruleName);

        // then
        ArgumentCaptor<EnableRuleRequest> captor = ArgumentCaptor.forClass(EnableRuleRequest.class);
        verify(eventBridgeClient).enableRule(captor.capture());

        EnableRuleRequest request = captor.getValue();
        assertThat(request.name()).isEqualTo(ruleName);
    }

    @Test
    @DisplayName("disableRule 성공 - 규칙 비활성화")
    void disableRuleSuccess() {
        // given
        String ruleName = "test-rule";

        // when
        adapter.disableRule(ruleName);

        // then
        ArgumentCaptor<DisableRuleRequest> captor = ArgumentCaptor.forClass(DisableRuleRequest.class);
        verify(eventBridgeClient).disableRule(captor.capture());

        DisableRuleRequest request = captor.getValue();
        assertThat(request.name()).isEqualTo(ruleName);
    }

    @Test
    @DisplayName("ruleExists 성공 - 규칙 존재함")
    void ruleExistsTrue() {
        // given
        String ruleName = "test-rule";
        when(eventBridgeClient.describeRule(any(DescribeRuleRequest.class)))
                .thenReturn(DescribeRuleResponse.builder().name(ruleName).build());

        // when
        boolean exists = adapter.ruleExists(ruleName);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("ruleExists 성공 - 규칙 존재하지 않음")
    void ruleExistsFalse() {
        // given
        String ruleName = "test-rule";
        when(eventBridgeClient.describeRule(any(DescribeRuleRequest.class)))
                .thenThrow(ResourceNotFoundException.builder().message("Not found").build());

        // when
        boolean exists = adapter.ruleExists(ruleName);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("ruleExists false 반환 - EventBridge가 disabled 상태")
    void ruleExistsReturnsFalseWhenDisabled() {
        // given
        properties.setEnabled(false);

        // when
        boolean exists = adapter.ruleExists("test");

        // then
        assertThat(exists).isFalse();
        verify(eventBridgeClient, never()).describeRule(any(DescribeRuleRequest.class));
    }
}
