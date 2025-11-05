package com.ryuqq.crawlinghub.adapter.out.eventbridge.adapter;

import com.ryuqq.crawlinghub.adapter.out.eventbridge.converter.CronExpressionConverter;
import com.ryuqq.crawlinghub.application.schedule.port.out.EventBridgeSchedulerPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.DeleteRuleRequest;
import software.amazon.awssdk.services.eventbridge.model.EventBridgeException;
import software.amazon.awssdk.services.eventbridge.model.PutRuleRequest;
import software.amazon.awssdk.services.eventbridge.model.PutRuleResponse;
import software.amazon.awssdk.services.eventbridge.model.PutTargetsRequest;
import software.amazon.awssdk.services.eventbridge.model.RemoveTargetsRequest;
import software.amazon.awssdk.services.eventbridge.model.RuleState;
import software.amazon.awssdk.services.eventbridge.model.Target;

/**
 * AWS EventBridge Scheduler Adapter
 *
 * <p>Outbox Pattern의 S2 Phase(Execute)에서 호출되는 EventBridge Adapter입니다.
 * {@link EventBridgeSchedulerPort} 인터페이스를 구현하여 Application Layer와 통신합니다.
 *
 * <p>주요 책임:
 * <ul>
 *   <li>scheduleName 생성: "seller-crawl-schedule-{scheduleId}"</li>
 *   <li>targetArn 내부 관리: @Value로 주입받아 Target 설정</li>
 *   <li>AWS EventBridge SDK 호출: PutRule, PutTargets, DeleteRule 등</li>
 *   <li>Circuit Breaker 적용: EventBridge API 장애 격리</li>
 * </ul>
 *
 * <p>설계 원칙:
 * <ul>
 *   <li>Constructor Injection: @Value 필드 주입 대신 생성자 주입 사용</li>
 *   <li>Law of Demeter: 내부 로직 캡슐화, Port 계약만 노출</li>
 *   <li>Hexagonal Architecture: Application Layer는 이 Adapter의 구현을 알지 못함</li>
 * </ul>
 *
 * @author Claude Code
 * @since 2025-10-31
 */
@Component
public class EventBridgeSchedulerAdapter implements EventBridgeSchedulerPort {

    private static final Logger log = LoggerFactory.getLogger(EventBridgeSchedulerAdapter.class);

    private final EventBridgeClient eventBridgeClient;
    private final CronExpressionConverter cronConverter;
    private final String targetArn;

    /**
     * 생성자 주입 (Zero-Tolerance Rule #5)
     *
     * <p>⚠️ @Value 필드 주입은 Anti-Pattern입니다.
     * 생성자 파라미터에서 @Value를 사용하여 주입받습니다.
     *
     * @param eventBridgeClient EventBridge Client (Bean 주입)
     * @param cronConverter     Cron 변환기 (Bean 주입)
     * @param targetArn         EventBridge Target ARN (application.yml에서 주입)
     */
    public EventBridgeSchedulerAdapter(
        EventBridgeClient eventBridgeClient,
        CronExpressionConverter cronConverter,
        @Value("${aws.eventbridge.target.arn}") String targetArn
    ) {
        this.eventBridgeClient = eventBridgeClient;
        this.cronConverter = cronConverter;
        this.targetArn = targetArn;
    }

    /**
     * EventBridge 스케줄 등록
     *
     * <p>처리 흐름:
     * <ol>
     *   <li>scheduleName 생성: "seller-crawl-schedule-{scheduleId}"</li>
     *   <li>Cron 표현식 변환: Standard → AWS EventBridge 형식</li>
     *   <li>PutRule: EventBridge Rule 생성</li>
     *   <li>PutTargets: Lambda/SQS Target 설정 (sellerId 포함)</li>
     * </ol>
     *
     * @param scheduleId     스케줄 ID (Rule 이름 생성용)
     * @param sellerId       셀러 ID (Target Input 전달용)
     * @param cronExpression Cron 표현식
     * @return EventBridge Schedule Name
     * @throws EventBridgeException EventBridge API 호출 실패 시
     */
    @Override
    @CircuitBreaker(name = "eventbridge")
    public String registerSchedule(Long scheduleId, Long sellerId, String cronExpression) {
        String scheduleName = generateScheduleName(scheduleId);
        String awsCronExpression = cronConverter.toAwsCron(cronExpression);

        log.info("📋 EventBridge 스케줄 등록 시작: scheduleName={}, sellerId={}, cron={}",
            scheduleName, sellerId, awsCronExpression);

        try {
            // 1. Rule 생성
            PutRuleRequest putRuleRequest = PutRuleRequest.builder()
                .name(scheduleName)
                .scheduleExpression(awsCronExpression)
                .state(RuleState.ENABLED)
                .description("Seller crawl schedule for seller " + sellerId)
                .build();

            PutRuleResponse putRuleResponse = eventBridgeClient.putRule(putRuleRequest);

            // 2. Target 설정 (sellerId를 Lambda에 전달)
            setTarget(scheduleName, scheduleId, sellerId);

            log.info("✅ EventBridge Rule 생성 완료: scheduleName={}, ruleArn={}",
                scheduleName, putRuleResponse.ruleArn());

            return scheduleName;

        } catch (EventBridgeException e) {
            log.error("❌ EventBridge Rule 생성 실패: scheduleName={}, error={}",
                scheduleName, e.awsErrorDetails().errorMessage(), e);
            throw e;
        }
    }

    /**
     * EventBridge 스케줄 업데이트
     *
     * <p>처리 흐름:
     * <ol>
     *   <li>scheduleName 생성: "seller-crawl-schedule-{scheduleId}"</li>
     *   <li>Cron 표현식 변환: Standard → AWS EventBridge 형식</li>
     *   <li>PutRule: 기존 Rule 업데이트 (같은 이름이면 자동 업데이트)</li>
     * </ol>
     *
     * <p>⚠️ Target은 업데이트하지 않습니다. sellerId가 변경되지 않는다고 가정합니다.
     *
     * @param scheduleId     스케줄 ID (Rule 이름 조회용)
     * @param sellerId       셀러 ID (Target Input 전달용, 로깅용)
     * @param cronExpression 새로운 Cron 표현식
     * @throws EventBridgeException EventBridge API 호출 실패 시
     */
    @Override
    @CircuitBreaker(name = "eventbridge")
    public void updateSchedule(Long scheduleId, Long sellerId, String cronExpression) {
        String scheduleName = generateScheduleName(scheduleId);
        String awsCronExpression = cronConverter.toAwsCron(cronExpression);

        log.info("🔄 EventBridge 스케줄 업데이트 시작: scheduleName={}, sellerId={}, cron={}",
            scheduleName, sellerId, awsCronExpression);

        try {
            // Rule 업데이트 (PutRule은 존재하면 업데이트, 없으면 생성)
            PutRuleRequest putRuleRequest = PutRuleRequest.builder()
                .name(scheduleName)
                .scheduleExpression(awsCronExpression)
                .state(RuleState.ENABLED)
                .description("Seller crawl schedule for seller " + sellerId)
                .build();

            PutRuleResponse putRuleResponse = eventBridgeClient.putRule(putRuleRequest);

            log.info("✅ EventBridge Rule 업데이트 완료: scheduleName={}, ruleArn={}",
                scheduleName, putRuleResponse.ruleArn());

        } catch (EventBridgeException e) {
            log.error("❌ EventBridge Rule 업데이트 실패: scheduleName={}, error={}",
                scheduleName, e.awsErrorDetails().errorMessage(), e);
            throw e;
        }
    }

    /**
     * EventBridge 스케줄 삭제
     *
     * <p>처리 흐름:
     * <ol>
     *   <li>scheduleName 생성: "seller-crawl-schedule-{scheduleId}"</li>
     *   <li>RemoveTargets: Target 제거 (Rule 삭제 전 필수)</li>
     *   <li>DeleteRule: Rule 삭제</li>
     * </ol>
     *
     * <p>⚠️ 멱등성: Rule이 이미 삭제된 경우 404 에러는 무시하고 성공으로 처리
     *
     * @param scheduleId 스케줄 ID (Rule 이름 조회용)
     * @param sellerId   셀러 ID (Target ID 조회용)
     * @throws EventBridgeException EventBridge API 호출 실패 시 (404 제외)
     */
    @Override
    @CircuitBreaker(name = "eventbridge")
    public void deleteSchedule(Long scheduleId, Long sellerId) {
        String scheduleName = generateScheduleName(scheduleId);

        log.info("🗑️ EventBridge 스케줄 삭제 시작: scheduleName={}, sellerId={}",
            scheduleName, sellerId);

        try {
            // 1. Target 제거 (Rule 삭제 전 필수)
            removeTarget(scheduleName, scheduleId);

            // 2. Rule 삭제
            DeleteRuleRequest deleteRuleRequest = DeleteRuleRequest.builder()
                .name(scheduleName)
                .build();

            eventBridgeClient.deleteRule(deleteRuleRequest);

            log.info("✅ EventBridge Rule 삭제 완료: scheduleName={}", scheduleName);

        } catch (EventBridgeException e) {
            // 404는 멱등성 처리 (이미 삭제된 경우)
            if (e.statusCode() == 404) {
                log.info("ℹ️ EventBridge Rule 이미 삭제됨 (멱등성 처리): scheduleName={}", scheduleName);
                return;
            }

            log.error("❌ EventBridge Rule 삭제 실패: scheduleName={}, error={}",
                scheduleName, e.awsErrorDetails().errorMessage(), e);
            throw e;
        }
    }

    /**
     * Target 설정
     *
     * <p>Lambda/SQS에 sellerId를 전달하기 위해 Target Input을 설정합니다.
     *
     * @param scheduleName Schedule Name (Rule 이름)
     * @param scheduleId   스케줄 ID (Target ID 생성용)
     * @param sellerId     셀러 ID (Lambda에 전달할 데이터)
     */
    private void setTarget(String scheduleName, Long scheduleId, Long sellerId) {
        Target target = Target.builder()
            .id(generateTargetId(scheduleId))
            .arn(targetArn)
            .input(String.format("{\"sellerId\": %d}", sellerId))
            .build();

        PutTargetsRequest putTargetsRequest = PutTargetsRequest.builder()
            .rule(scheduleName)
            .targets(target)
            .build();

        eventBridgeClient.putTargets(putTargetsRequest);

        log.debug("🎯 Target 설정 완료: targetId={}, sellerId={}", generateTargetId(scheduleId), sellerId);
    }

    /**
     * Target 제거
     *
     * @param scheduleName Schedule Name (Rule 이름)
     * @param scheduleId   스케줄 ID (Target ID 생성용)
     */
    private void removeTarget(String scheduleName, Long scheduleId) {
        RemoveTargetsRequest removeTargetsRequest = RemoveTargetsRequest.builder()
            .rule(scheduleName)
            .ids(generateTargetId(scheduleId))
            .build();

        eventBridgeClient.removeTargets(removeTargetsRequest);

        log.debug("🗑️ Target 제거 완료: targetId={}", generateTargetId(scheduleId));
    }

    /**
     * Schedule Name 생성
     *
     * <p>형식: "seller-crawl-schedule-{scheduleId}"
     *
     * @param scheduleId 스케줄 ID
     * @return Schedule Name
     */
    private String generateScheduleName(Long scheduleId) {
        return "seller-crawl-schedule-" + scheduleId;
    }

    /**
     * Target ID 생성
     *
     * <p>형식: "seller-crawl-target-{scheduleId}"
     *
     * @param scheduleId 스케줄 ID
     * @return Target ID
     */
    private String generateTargetId(Long scheduleId) {
        return "seller-crawl-target-" + scheduleId;
    }
}
