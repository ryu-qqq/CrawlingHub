package com.ryuqq.crawlinghub.adapter.out.eventbridge.executor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.ryuqq.crawlinghub.adapter.out.eventbridge.converter.CronExpressionConverter;
import com.ryuqq.crawlinghub.adapter.out.eventbridge.model.ScheduleRequest;
import com.ryuqq.orchestrator.core.contract.Envelope;
import com.ryuqq.orchestrator.core.executor.Executor;
import com.ryuqq.orchestrator.core.model.OpId;
import com.ryuqq.orchestrator.core.outcome.Fail;
import com.ryuqq.orchestrator.core.outcome.Ok;
import com.ryuqq.orchestrator.core.outcome.Outcome;
import com.ryuqq.orchestrator.core.outcome.Retry;
import com.ryuqq.orchestrator.core.statemachine.OperationState;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import java.util.concurrent.TimeUnit;

/**
 * AWS EventBridge Executor
 * <p>
 * Orchestrator SDK의 {@link Executor} 인터페이스를 구현하여
 * AWS EventBridge Scheduler Rule을 생성/수정/삭제합니다.
 * </p>
 * <p>
 * 주요 책임:
 * <ul>
 *   <li>CREATE: EventBridge Rule 생성 + Target 설정</li>
 *   <li>UPDATE: 기존 Rule의 Cron 표현식 수정</li>
 *   <li>DELETE: Rule 및 Target 삭제</li>
 *   <li>상태 관리: IN_PROGRESS → COMPLETED/FAILED</li>
 * </ul>
 * </p>
 * <p>
 * Thread Safety:
 * <ul>
 *   <li>Guava Cache 사용으로 thread-safe 보장</li>
 *   <li>getState/getOutcome 동시 호출 가능</li>
 *   <li>TTL 기반 자동 메모리 정리 (메모리 누수 방지)</li>
 * </ul>
 * </p>
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
@Component
public class EventBridgeExecutor implements Executor {

    private static final Logger LOG = LoggerFactory.getLogger(EventBridgeExecutor.class);

    private final EventBridgeClient eventBridgeClient;
    private final CronExpressionConverter cronConverter;
    private final ObjectMapper objectMapper;

    /**
     * Operation 실행 결과 저장소 (Guava Cache)
     * <p>
     * Key: OpId, Value: ExecutionResult (state + outcome)
     * </p>
     * <p>
     * TTL: 30분 (expireAfterWrite)
     * 최대 크기: 10,000개
     * 이유: 단일 인스턴스 환경, 1-2명 관리자, EventBridge API 빠름
     * </p>
     */
    private final Cache<OpId, ExecutionResult> executionResults =
            CacheBuilder.newBuilder()
                    .expireAfterWrite(30, TimeUnit.MINUTES)
                    .maximumSize(10000)
                    .build();

    /**
     * EventBridge Target ARN (Lambda 또는 SQS 등)
     * <p>
     * 실제 환경에서는 application.yml에서 주입받아야 합니다.
     * </p>
     */
    private static final String TARGET_ARN = "arn:aws:lambda:ap-northeast-2:123456789012:function:seller-crawl-function";

    /**
     * 생성자
     *
     * @param eventBridgeClient EventBridge Client
     * @param cronConverter     Cron 변환기
     * @param objectMapper      JSON 직렬화/역직렬화
     */
    public EventBridgeExecutor(
            EventBridgeClient eventBridgeClient,
            CronExpressionConverter cronConverter,
            ObjectMapper objectMapper
    ) {
        this.eventBridgeClient = eventBridgeClient;
        this.cronConverter = cronConverter;
        this.objectMapper = objectMapper;
    }

    /**
     * EventBridge 스케줄 작업 실행
     * <p>
     * 비블로킹으로 즉시 반환되며, 실제 실행은 별도 스레드에서 수행됩니다.
     * EventBridge API 호출은 빠르므로 동기 실행 후 즉시 완료 상태로 전환합니다.
     * </p>
     *
     * @param envelope Orchestrator Envelope (OpId + Command)
     * @throws IllegalArgumentException envelope가 null인 경우
     */
    @Override
    public void execute(Envelope envelope) {
        if (envelope == null) {
            throw new IllegalArgumentException("envelope cannot be null");
        }

        OpId opId = envelope.opId();
        LOG.info("Executing EventBridge operation: opId={}", opId);

        // 초기 상태: IN_PROGRESS
        executionResults.put(opId, ExecutionResult.inProgress());

        // EventBridge API 호출 (동기) 후 즉시 완료 상태로 전환
        try {
            Outcome outcome = executeEventBridgeOperation(envelope);
            executionResults.put(opId, ExecutionResult.completed(outcome));
            LOG.info("EventBridge operation completed: opId={}, outcome={}", opId, outcome.getClass().getSimpleName());

        } catch (Exception e) {
            Fail failOutcome = Fail.of("EXEC-500", "Unexpected execution error: " + e.getMessage(), e.getClass().getName());
            executionResults.put(opId, ExecutionResult.failed(failOutcome));
            LOG.error("EventBridge operation failed: opId={}", opId, e);
        }
    }

    /**
     * Operation 상태 조회
     *
     * @param opId Operation ID
     * @return 현재 상태 (PENDING, IN_PROGRESS, COMPLETED, FAILED)
     * @throws IllegalArgumentException opId가 null인 경우
     * @throws IllegalStateException    opId에 해당하는 Operation이 존재하지 않거나 TTL 만료된 경우
     */
    @Override
    public OperationState getState(OpId opId) {
        if (opId == null) {
            throw new IllegalArgumentException("opId cannot be null");
        }

        ExecutionResult result = executionResults.getIfPresent(opId);
        if (result == null) {
            throw new IllegalStateException("Operation not found for OpId: " + opId);
        }

        return result.state();
    }

    /**
     * Operation 결과 조회
     * <p>
     * 주의: Operation이 종료 상태(COMPLETED, FAILED)일 때만 호출해야 합니다.
     * </p>
     *
     * @param opId Operation ID
     * @return 실행 결과 (Ok, Retry, Fail)
     * @throws IllegalArgumentException opId가 null인 경우
     * @throws IllegalStateException    Operation이 존재하지 않거나 아직 종료되지 않은 경우, TTL 만료된 경우
     */
    @Override
    public Outcome getOutcome(OpId opId) {
        if (opId == null) {
            throw new IllegalArgumentException("opId cannot be null");
        }

        ExecutionResult result = executionResults.getIfPresent(opId);
        if (result == null) {
            throw new IllegalStateException("Operation not found for OpId: " + opId);
        }

        if (!result.state().isTerminal()) {
            throw new IllegalStateException("Operation is not completed yet: " + opId);
        }

        Outcome outcome = result.outcome();
        if (outcome == null) {
            throw new IllegalStateException("Outcome not available for OpId: " + opId);
        }

        return outcome;
    }

    /**
     * EventBridge 작업 실행 (CREATE/UPDATE/DELETE)
     *
     * @param envelope Envelope
     * @return Outcome
     */
    @CircuitBreaker(name = "eventbridge")
    private Outcome executeEventBridgeOperation(Envelope envelope) {
        try {
            ScheduleRequest request = parsePayload(envelope);

            return switch (request.operation().toUpperCase()) {
                case "CREATE" -> createSchedule(request, envelope.opId());
                case "UPDATE" -> updateSchedule(request, envelope.opId());
                case "DELETE" -> deleteSchedule(request, envelope.opId());
                default -> Fail.of(
                        "EB-400",
                        "Unknown operation: " + request.operation()
                );
            };

        } catch (JsonProcessingException e) {
            return Fail.of("EB-400", "Failed to parse payload: " + e.getMessage());
        } catch (Exception e) {
            return Fail.of("EB-500", "Unexpected error: " + e.getMessage());
        }
    }

    /**
     * CREATE: EventBridge Rule 생성
     *
     * @param request ScheduleRequest
     * @param opId    Operation ID
     * @return Outcome
     */
    private Outcome createSchedule(ScheduleRequest request, OpId opId) {
        try {
            String ruleName = request.getRuleName();
            String awsCronExpression = cronConverter.toAwsCron(request.cronExpression());

            // 1. Rule 생성
            PutRuleRequest putRuleRequest = PutRuleRequest.builder()
                    .name(ruleName)
                    .scheduleExpression(awsCronExpression)
                    .state(RuleState.ENABLED)
                    .description("Seller crawl schedule for seller " + request.sellerId())
                    .build();

            PutRuleResponse putRuleResponse = eventBridgeClient.putRule(putRuleRequest);

            // 2. Target 설정
            setTarget(ruleName, request.sellerId());

            LOG.info("EventBridge Rule created: ruleName={}, arn={}", ruleName, putRuleResponse.ruleArn());
            return Ok.of(opId, "Created EventBridge Rule: " + ruleName);

        } catch (EventBridgeException e) {
            if (e.isThrottlingException()) {
                return Retry.of("EventBridge throttling", 1, 30000);
            }
            return Fail.of("EB-" + e.statusCode(), e.awsErrorDetails().errorMessage());
        }
    }

    /**
     * UPDATE: 기존 Rule의 Cron 표현식 수정
     *
     * @param request ScheduleRequest
     * @param opId    Operation ID
     * @return Outcome
     */
    private Outcome updateSchedule(ScheduleRequest request, OpId opId) {
        try {
            String ruleName = request.getRuleName();
            String awsCronExpression = cronConverter.toAwsCron(request.cronExpression());

            // Rule 업데이트 (PutRule은 존재하면 업데이트)
            PutRuleRequest putRuleRequest = PutRuleRequest.builder()
                    .name(ruleName)
                    .scheduleExpression(awsCronExpression)
                    .state(RuleState.ENABLED)
                    .description("Seller crawl schedule for seller " + request.sellerId())
                    .build();

            PutRuleResponse putRuleResponse = eventBridgeClient.putRule(putRuleRequest);

            LOG.info("EventBridge Rule updated: ruleName={}, arn={}", ruleName, putRuleResponse.ruleArn());
            return Ok.of(opId, "Updated EventBridge Rule: " + ruleName);

        } catch (EventBridgeException e) {
            if (e.isThrottlingException()) {
                return Retry.of("EventBridge throttling", 1, 30000);
            }
            if (e.statusCode() == 404) {
                return Fail.of("EB-404", "Schedule not found: " + request.getRuleName());
            }
            return Fail.of("EB-" + e.statusCode(), e.awsErrorDetails().errorMessage());
        }
    }

    /**
     * DELETE: Rule 및 Target 삭제
     *
     * @param request ScheduleRequest
     * @param opId    Operation ID
     * @return Outcome
     */
    private Outcome deleteSchedule(ScheduleRequest request, OpId opId) {
        try {
            String ruleName = request.getRuleName();

            // 1. Target 제거
            removeTarget(ruleName, request.sellerId());

            // 2. Rule 삭제
            DeleteRuleRequest deleteRuleRequest = DeleteRuleRequest.builder()
                    .name(ruleName)
                    .build();

            eventBridgeClient.deleteRule(deleteRuleRequest);

            LOG.info("EventBridge Rule deleted: ruleName={}", ruleName);
            return Ok.of(opId, "Deleted EventBridge Rule: " + ruleName);

        } catch (EventBridgeException e) {
            if (e.isThrottlingException()) {
                return Retry.of("EventBridge throttling", 1, 30000);
            }
            if (e.statusCode() == 404) {
                // 이미 삭제된 경우 성공으로 처리 (멱등성)
                LOG.info("EventBridge Rule already deleted: ruleName={}", request.getRuleName());
                return Ok.of(opId, "EventBridge Rule already deleted: " + request.getRuleName());
            }
            return Fail.of("EB-" + e.statusCode(), e.awsErrorDetails().errorMessage());
        }
    }

    /**
     * Target 설정
     *
     * @param ruleName Rule 이름
     * @param sellerId Seller ID
     */
    private void setTarget(String ruleName, Long sellerId) {
        Target target = Target.builder()
                .id("seller-crawl-target-" + sellerId)
                .arn(TARGET_ARN)
                .input(String.format("{\"sellerId\": %d}", sellerId))
                .build();

        PutTargetsRequest putTargetsRequest = PutTargetsRequest.builder()
                .rule(ruleName)
                .targets(target)
                .build();

        eventBridgeClient.putTargets(putTargetsRequest);
    }

    /**
     * Target 제거
     *
     * @param ruleName Rule 이름
     * @param sellerId Seller ID
     */
    private void removeTarget(String ruleName, Long sellerId) {
        RemoveTargetsRequest removeTargetsRequest = RemoveTargetsRequest.builder()
                .rule(ruleName)
                .ids("seller-crawl-target-" + sellerId)
                .build();

        eventBridgeClient.removeTargets(removeTargetsRequest);
    }

    /**
     * Payload 파싱
     *
     * @param envelope Envelope
     * @return ScheduleRequest
     * @throws JsonProcessingException JSON 파싱 실패 시
     */
    private ScheduleRequest parsePayload(Envelope envelope) throws JsonProcessingException {
        String payloadValue = envelope.command().payload().getValue();
        return objectMapper.readValue(payloadValue, ScheduleRequest.class);
    }

    /**
     * Operation 실행 결과 내부 클래스
     *
     * @param state   Operation 상태
     * @param outcome Operation 결과 (nullable)
     */
    private record ExecutionResult(
            OperationState state,
            Outcome outcome
    ) {
        static ExecutionResult inProgress() {
            return new ExecutionResult(OperationState.IN_PROGRESS, null);
        }

        static ExecutionResult completed(Outcome outcome) {
            return new ExecutionResult(OperationState.COMPLETED, outcome);
        }

        static ExecutionResult failed(Outcome outcome) {
            return new ExecutionResult(OperationState.FAILED, outcome);
        }
    }
}
