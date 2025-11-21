package com.ryuqq.crawlinghub.adapter.out.eventbridge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AWS EventBridge Scheduler 설정 Properties
 *
 * <p><strong>설정 예시 (application.yml)</strong>:
 *
 * <pre>{@code
 * aws:
 *   eventbridge:
 *     region: ap-northeast-2
 *     schedule-group-name: crawlinghub-schedules
 *     target-arn: arn:aws:lambda:ap-northeast-2:123456789012:function:crawler
 *     role-arn: arn:aws:iam::123456789012:role/EventBridgeSchedulerRole
 * }</pre>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
@ConfigurationProperties(prefix = "aws.eventbridge")
public class EventBridgeProperties {

    /** AWS 리전 (기본값: ap-northeast-2) */
    private String region = "ap-northeast-2";

    /** EventBridge Schedule Group 이름 */
    private String scheduleGroupName = "crawlinghub-schedules";

    /** 스케줄러가 호출할 Target ARN (Lambda, SQS 등) */
    private String targetArn;

    /** EventBridge가 Target을 호출할 때 사용할 IAM Role ARN */
    private String roleArn;

    /** 스케줄 이름 prefix */
    private String scheduleNamePrefix = "crawler-";

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
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
