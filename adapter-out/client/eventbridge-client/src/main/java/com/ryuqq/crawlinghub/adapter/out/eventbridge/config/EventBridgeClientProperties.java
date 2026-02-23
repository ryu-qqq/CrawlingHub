package com.ryuqq.crawlinghub.adapter.out.eventbridge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AWS EventBridge Scheduler Properties
 *
 * <p>eventbridge.yml에서 설정을 읽어옵니다.
 *
 * <p><strong>설정 예시 (eventbridge.yml)</strong>:
 *
 * <pre>{@code
 * eventbridge:
 *   region: ap-northeast-2
 *   schedule-group-name: crawlinghub-schedules
 *   target-arn: arn:aws:lambda:...
 *   role-arn: arn:aws:iam::...
 * }</pre>
 */
@ConfigurationProperties(prefix = "eventbridge")
public class EventBridgeClientProperties {

    private String region = "ap-northeast-2";
    private String endpoint;
    private String scheduleGroupName = "crawlinghub-schedules";
    private String targetArn;
    private String roleArn;
    private String scheduleNamePrefix = "crawler-";

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getScheduleGroupName() {
        return scheduleGroupName;
    }

    public void setScheduleGroupName(String scheduleGroupName) {
        this.scheduleGroupName = scheduleGroupName;
    }

    public String getTargetArn() {
        return targetArn;
    }

    public void setTargetArn(String targetArn) {
        this.targetArn = targetArn;
    }

    public String getRoleArn() {
        return roleArn;
    }

    public void setRoleArn(String roleArn) {
        this.roleArn = roleArn;
    }

    public String getScheduleNamePrefix() {
        return scheduleNamePrefix;
    }

    public void setScheduleNamePrefix(String scheduleNamePrefix) {
        this.scheduleNamePrefix = scheduleNamePrefix;
    }
}
