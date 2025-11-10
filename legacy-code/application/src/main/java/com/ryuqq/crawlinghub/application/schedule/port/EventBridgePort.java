package com.ryuqq.crawlinghub.application.schedule.port;

/**
 * Port interface for AWS EventBridge integration
 * Follows hexagonal architecture - adapter implements this interface
 */
public interface EventBridgePort {

    /**
     * Creates an EventBridge rule for a schedule
     *
     * @param ruleName the rule name
     * @param cronExpression the AWS-compatible cron expression
     * @param description the rule description
     * @return the created rule ARN
     */
    String createRule(String ruleName, String cronExpression, String description);

    /**
     * Updates an EventBridge rule
     *
     * @param ruleName the rule name
     * @param cronExpression the new cron expression
     * @param description the new description
     */
    void updateRule(String ruleName, String cronExpression, String description);

    /**
     * Adds a target to an EventBridge rule
     * Target ARN is configured in adapter implementation
     *
     * @param ruleName the rule name
     * @param targetInput the input JSON string for the target
     */
    void addTarget(String ruleName, String targetInput);

    /**
     * Removes all targets from an EventBridge rule
     *
     * @param ruleName the rule name
     */
    void removeTargets(String ruleName);

    /**
     * Deletes an EventBridge rule
     * Note: All targets must be removed before deletion
     *
     * @param ruleName the rule name
     */
    void deleteRule(String ruleName);

    /**
     * Enables an EventBridge rule
     *
     * @param ruleName the rule name
     */
    void enableRule(String ruleName);

    /**
     * Disables an EventBridge rule
     *
     * @param ruleName the rule name
     */
    void disableRule(String ruleName);

    /**
     * Checks if a rule exists
     *
     * @param ruleName the rule name
     * @return true if the rule exists, false otherwise
     */
    boolean ruleExists(String ruleName);
}
