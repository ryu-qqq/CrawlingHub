package com.ryuqq.crawlinghub.adapter.eventbridge;

import com.ryuqq.crawlinghub.adapter.config.EventBridgeProperties;
import com.ryuqq.crawlinghub.application.schedule.port.EventBridgePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.DeleteRuleRequest;
import software.amazon.awssdk.services.eventbridge.model.DescribeRuleRequest;
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
import software.amazon.awssdk.services.eventbridge.model.Target;

/**
 * EventBridge Adapter Implementation.
 * Implements EventBridgePort interface for AWS EventBridge integration
 * Handles rule lifecycle and target management
 *
 * <p>IMPORTANT: This adapter is called from @TransactionalEventListener handlers
 * which execute AFTER database transaction commits, ensuring data consistency
 *
 * @author crawlinghub (noreply@crawlinghub.com)
 */
@Component
public class EventBridgeAdapter implements EventBridgePort {

    private static final Logger LOG = LoggerFactory.getLogger(EventBridgeAdapter.class);
    private static final String TARGET_ID_PREFIX = "Target-";

    private final EventBridgeClient eventBridgeClient;
    private final EventBridgeProperties properties;

    public EventBridgeAdapter(
            EventBridgeClient eventBridgeClient,
            EventBridgeProperties properties) {
        this.eventBridgeClient = eventBridgeClient;
        this.properties = properties;
    }

    @Override
    public String createRule(String ruleName, String cronExpression, String description) {
        if (!properties.isEnabled()) {
            LOG.info("EventBridge is disabled. Skipping rule creation: {}", ruleName);
            return ruleName;
        }

        try {
            PutRuleRequest request = PutRuleRequest.builder()
                    .name(ruleName)
                    .description(description)
                    .scheduleExpression("cron(" + cronExpression + ")")
                    .state(RuleState.DISABLED) // Initially disabled
                    .eventBusName(properties.getEventBusName())
                    .build();

            PutRuleResponse response = eventBridgeClient.putRule(request);
            LOG.info("EventBridge rule created successfully: {} (ARN: {})", ruleName, response.ruleArn());
            return response.ruleArn();

        } catch (EventBridgeException e) {
            LOG.error("Failed to create EventBridge rule: {}", ruleName, e);
            throw new EventBridgeOperationException("Failed to create rule: "
                    + ruleName, e);
        }
    }

    @Override
    public void updateRule(String ruleName, String cronExpression, String description) {
        if (!properties.isEnabled()) {
            LOG.info("EventBridge is disabled. Skipping rule update: {}", ruleName);
            return;
        }

        try {
            // PutRule with same name updates the existing rule
            PutRuleRequest request = PutRuleRequest.builder()
                    .name(ruleName)
                    .description(description)
                    .scheduleExpression("cron(" + cronExpression + ")")
                    .eventBusName(properties.getEventBusName())
                    .build();

            eventBridgeClient.putRule(request);
            LOG.info("EventBridge rule updated successfully: {}", ruleName);

        } catch (EventBridgeException e) {
            LOG.error("Failed to update EventBridge rule: {}", ruleName, e);
            throw new EventBridgeOperationException("Failed to update rule: "
                    + ruleName, e);
        }
    }

    @Override
    public void addTarget(String ruleName, String targetInput) {
        if (!properties.isEnabled()) {
            LOG.info("EventBridge is disabled. Skipping target addition: {}", ruleName);
            return;
        }

        String targetArn = properties.getTargetArn();
        if (targetArn == null || targetArn.isBlank()) {
            throw new IllegalStateException("EventBridge target ARN is not configured. "
                    + "Please set aws.eventbridge.target-arn property.");
        }

        try {
            Target target = Target.builder()
                    .id(TARGET_ID_PREFIX + ruleName)
                    .arn(targetArn)
                    .input(targetInput)
                    .build();

            PutTargetsRequest request = PutTargetsRequest.builder()
                    .rule(ruleName)
                    .eventBusName(properties.getEventBusName())
                    .targets(target)
                    .build();

            PutTargetsResponse response = eventBridgeClient.putTargets(request);

            if (response.failedEntryCount() > 0) {
                LOG.error("Failed to add target to rule: {}. Failed entries: {}",
                         ruleName, response.failedEntries());
                throw new EventBridgeOperationException(
                    "Failed to add target to rule: " + ruleName
                    + ". Failed entries: " + response.failedEntries(), null);
            }

            LOG.info("Target added successfully to rule: {} with ARN: {}", ruleName, targetArn);

        } catch (EventBridgeException e) {
            LOG.error("Failed to add target to rule: {}", ruleName, e);
            throw new EventBridgeOperationException("Failed to add target to rule: "
                    + ruleName, e);
        }
    }

    @Override
    public void removeTargets(String ruleName) {
        if (!properties.isEnabled()) {
            LOG.info("EventBridge is disabled. Skipping target removal: {}", ruleName);
            return;
        }

        try {
            String targetId = TARGET_ID_PREFIX + ruleName;

            RemoveTargetsRequest request = RemoveTargetsRequest.builder()
                    .rule(ruleName)
                    .eventBusName(properties.getEventBusName())
                    .ids(targetId)
                    .build();

            RemoveTargetsResponse response = eventBridgeClient.removeTargets(request);

            if (response.failedEntryCount() > 0) {
                LOG.error("Failed to remove targets from rule: {}. Failed entries: {}",
                         ruleName, response.failedEntries());
                throw new EventBridgeOperationException(
                    "Failed to remove targets from rule: " + ruleName
                    + ". Failed entries: " + response.failedEntries(), null);
            }

            LOG.info("Targets removed successfully from rule: {}", ruleName);

        } catch (EventBridgeException e) {
            LOG.error("Failed to remove targets from rule: {}", ruleName, e);
            throw new EventBridgeOperationException("Failed to remove targets from rule: "
                    + ruleName, e);
        }
    }

    @Override
    public void deleteRule(String ruleName) {
        if (!properties.isEnabled()) {
            LOG.info("EventBridge is disabled. Skipping rule deletion: {}", ruleName);
            return;
        }

        try {
            DeleteRuleRequest request = DeleteRuleRequest.builder()
                    .name(ruleName)
                    .eventBusName(properties.getEventBusName())
                    .build();

            eventBridgeClient.deleteRule(request);
            LOG.info("EventBridge rule deleted successfully: {}", ruleName);

        } catch (EventBridgeException e) {
            LOG.error("Failed to delete EventBridge rule: {}", ruleName, e);
            throw new EventBridgeOperationException("Failed to delete rule: "
                    + ruleName, e);
        }
    }

    @Override
    public void enableRule(String ruleName) {
        if (!properties.isEnabled()) {
            LOG.info("EventBridge is disabled. Skipping rule enable: {}", ruleName);
            return;
        }

        try {
            EnableRuleRequest request = EnableRuleRequest.builder()
                    .name(ruleName)
                    .eventBusName(properties.getEventBusName())
                    .build();

            eventBridgeClient.enableRule(request);
            LOG.info("EventBridge rule enabled successfully: {}", ruleName);

        } catch (EventBridgeException e) {
            LOG.error("Failed to enable EventBridge rule: {}", ruleName, e);
            throw new EventBridgeOperationException("Failed to enable rule: "
                    + ruleName, e);
        }
    }

    @Override
    public void disableRule(String ruleName) {
        if (!properties.isEnabled()) {
            LOG.info("EventBridge is disabled. Skipping rule disable: {}", ruleName);
            return;
        }

        try {
            DisableRuleRequest request = DisableRuleRequest.builder()
                    .name(ruleName)
                    .eventBusName(properties.getEventBusName())
                    .build();

            eventBridgeClient.disableRule(request);
            LOG.info("EventBridge rule disabled successfully: {}", ruleName);

        } catch (EventBridgeException e) {
            LOG.error("Failed to disable EventBridge rule: {}", ruleName, e);
            throw new EventBridgeOperationException("Failed to disable rule: "
                    + ruleName, e);
        }
    }

    @Override
    public boolean ruleExists(String ruleName) {
        if (!properties.isEnabled()) {
            LOG.debug("EventBridge is disabled. Returning false for rule existence check: {}", ruleName);
            return false;
        }

        try {
            DescribeRuleRequest request = DescribeRuleRequest.builder()
                    .name(ruleName)
                    .eventBusName(properties.getEventBusName())
                    .build();

            eventBridgeClient.describeRule(request);
            return true;

        } catch (ResourceNotFoundException e) {
            LOG.debug("Rule not found: {}", ruleName);
            return false;
        } catch (EventBridgeException e) {
            LOG.error("Failed to check rule existence: {}", ruleName, e);
            throw new EventBridgeOperationException("Failed to check rule existence: "
                    + ruleName, e);
        }
    }

    /**
     * Custom exception for EventBridge operations.
     */
    public static class EventBridgeOperationException extends RuntimeException {
        public EventBridgeOperationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
