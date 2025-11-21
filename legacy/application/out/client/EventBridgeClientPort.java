package com.ryuqq.crawlinghub.application.port.out.client;

/** AWS EventBridge Client Port */
public interface EventBridgeClientPort {

    void createRule(String ruleName, String cronExpression, String target);

    void updateRule(String ruleName, String cronExpression);

    void disableRule(String ruleName);
}
