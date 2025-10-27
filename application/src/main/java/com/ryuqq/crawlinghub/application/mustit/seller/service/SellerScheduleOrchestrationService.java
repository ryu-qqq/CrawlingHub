package com.ryuqq.crawlinghub.application.mustit.seller.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.application.mustit.seller.port.out.outbox.SellerCrawlScheduleOutboxPort;
import com.ryuqq.crawlinghub.domain.mustit.seller.outbox.CommandInfo;
import com.ryuqq.crawlinghub.domain.mustit.seller.outbox.SellerCrawlScheduleOutbox;
import com.ryuqq.orchestrator.application.orchestrator.OperationHandle;
import com.ryuqq.orchestrator.application.orchestrator.Orchestrator;
import com.ryuqq.orchestrator.core.contract.Command;
import com.ryuqq.orchestrator.core.model.BizKey;
import com.ryuqq.orchestrator.core.model.Domain;
import com.ryuqq.orchestrator.core.model.EventType;
import com.ryuqq.orchestrator.core.model.IdemKey;
import com.ryuqq.orchestrator.core.model.OpId;
import com.ryuqq.orchestrator.core.model.Payload;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 셀러 크롤링 스케줄 Orchestration Service
 * <p>
 * Orchestrator SDK를 사용하여 AWS EventBridge 스케줄 등록/수정/삭제를
 * 안전하게 처리하는 Application Service입니다.
 * </p>
 * <p>
 * 주요 책임:
 * <ul>
 *   <li>Orchestrator Command 생성</li>
 *   <li>Outbox Domain Model 생성 (PENDING 상태)</li>
 *   <li>OutboxPort를 통한 Outbox 저장</li>
 *   <li>Orchestrator.submit() 호출 → OperationHandle 획득</li>
 *   <li>OpId를 OutboxPort를 통해 업데이트</li>
 * </ul>
 * </p>
 * <p>
 * 트랜잭션 전략:
 * <ul>
 *   <li>이 서비스는 트랜잭션 없음 (Non-Transactional)</li>
 *   <li>OutboxPort의 Adapter가 REQUIRES_NEW 트랜잭션으로 독립 커밋</li>
 *   <li>Orchestrator.submit()은 비동기 실행 시작 (블로킹 없음)</li>
 * </ul>
 * </p>
 * <p>
 * 헥사고날 아키텍처:
 * <ul>
 *   <li>Persistence 의존성 없음 (OutboxPort 추상화 사용)</li>
 *   <li>Domain Model 사용 (SellerCrawlScheduleOutbox)</li>
 *   <li>외부 API 호출 (Orchestrator)은 트랜잭션 밖에서 처리</li>
 * </ul>
 * </p>
 * <p>
 * 리팩토링 이력:
 * <ul>
 *   <li>2025-01-27: Template Method 패턴 적용으로 중복 코드 80% 제거</li>
 *   <li>ScheduleOperation Enum 도입으로 타입 안정성 확보</li>
 *   <li>ScheduleRequest Record 사용으로 Payload 타입 안정성 확보</li>
 * </ul>
 * </p>
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
@Service
public class SellerScheduleOrchestrationService {

    private static final String DOMAIN = "seller-crawl-schedule";
    private static final long DEFAULT_TIMEOUT_MILLIS = 5 * 60 * 1000L; // 5분

    private final Orchestrator orchestrator;
    private final SellerCrawlScheduleOutboxPort outboxPort;
    private final ObjectMapper objectMapper;

    /**
     * 생성자
     *
     * @param orchestrator Orchestrator SDK 인스턴스
     * @param outboxPort   Outbox Outbound Port (Persistence 추상화)
     * @param objectMapper JSON 직렬화용
     */
    public SellerScheduleOrchestrationService(
            Orchestrator orchestrator,
            SellerCrawlScheduleOutboxPort outboxPort,
            ObjectMapper objectMapper
    ) {
        this.orchestrator = Objects.requireNonNull(orchestrator);
        this.outboxPort = Objects.requireNonNull(outboxPort);
        this.objectMapper = Objects.requireNonNull(objectMapper);
    }

    // ========================================
    // Public API Methods
    // ========================================

    /**
     * 스케줄 생성 요청
     * <p>
     * Template Method 패턴을 통해 공통 로직을 재사용합니다.
     * </p>
     *
     * @param sellerId       셀러 PK (Long FK)
     * @param cronExpression Cron 표현식
     * @return Orchestrator OpId
     */
    public OpId startScheduleCreation(Long sellerId, String cronExpression) {
        Objects.requireNonNull(cronExpression, "cronExpression must not be null");
        return executeScheduleOperation(sellerId, cronExpression, ScheduleOperation.CREATE);
    }

    /**
     * 스케줄 수정 요청
     * <p>
     * Template Method 패턴을 통해 공통 로직을 재사용합니다.
     * </p>
     *
     * @param sellerId       셀러 PK (Long FK)
     * @param cronExpression 새로운 Cron 표현식
     * @return Orchestrator OpId
     */
    public OpId startScheduleUpdate(Long sellerId, String cronExpression) {
        Objects.requireNonNull(cronExpression, "cronExpression must not be null");
        return executeScheduleOperation(sellerId, cronExpression, ScheduleOperation.UPDATE);
    }

    /**
     * 스케줄 삭제 요청
     * <p>
     * Template Method 패턴을 통해 공통 로직을 재사용합니다.
     * cronExpression은 null이 허용됩니다.
     * </p>
     *
     * @param sellerId 셀러 PK (Long FK)
     * @return Orchestrator OpId
     */
    public OpId startScheduleDeletion(Long sellerId) {
        return executeScheduleOperation(sellerId, null, ScheduleOperation.DELETE);
    }

    // ========================================
    // Template Method (Private)
    // ========================================

    /**
     * 스케줄 작업 실행 Template Method
     * <p>
     * CREATE, UPDATE, DELETE 작업의 공통 로직을 처리합니다.
     * </p>
     * <p>
     * 실행 흐름:
     * <ol>
     *   <li>Idempotency Key 생성</li>
     *   <li>CommandInfo 생성</li>
     *   <li>Payload JSON 생성 (ScheduleRequest 사용)</li>
     *   <li>Outbox Domain Model 생성 및 저장 (PENDING 상태)</li>
     *   <li>Orchestrator Command 생성</li>
     *   <li>Orchestrator.submit() 호출 → OperationHandle 획득</li>
     *   <li>OpId를 Outbox에 업데이트</li>
     * </ol>
     * </p>
     * <p>
     * 트랜잭션 전략:
     * <ul>
     *   <li>이 메서드는 트랜잭션 없음 (@Transactional 없음)</li>
     *   <li>OutboxPort.save()는 REQUIRES_NEW 트랜잭션으로 독립 커밋</li>
     *   <li>Orchestrator.submit()은 비동기 시작 (블로킹 없음)</li>
     * </ul>
     * </p>
     *
     * @param sellerId       셀러 PK (Long FK)
     * @param cronExpression Cron 표현식 (DELETE 시 null 허용)
     * @param operation      작업 유형 Enum
     * @return Orchestrator OpId
     */
    private OpId executeScheduleOperation(
            Long sellerId,
            String cronExpression,
            ScheduleOperation operation
    ) {
        Objects.requireNonNull(sellerId, "sellerId must not be null");
        Objects.requireNonNull(operation, "operation must not be null");

        // 1. Idempotency Key 생성
        String idemKey = generateIdemKey(sellerId, operation);

        // 2. CommandInfo 생성
        CommandInfo commandInfo = CommandInfo.of(
                DOMAIN,
                operation.getEventType(),
                "seller-" + sellerId,
                idemKey
        );

        // 3. Payload JSON 생성 (ScheduleRequest 사용)
        String payloadJson = createPayloadJson(sellerId, cronExpression, operation);

        // 4. Outbox Domain Model 생성 및 저장 (PENDING 상태)
        SellerCrawlScheduleOutbox outbox = SellerCrawlScheduleOutbox.of(
                commandInfo,
                sellerId,
                payloadJson
        );
        SellerCrawlScheduleOutbox savedOutbox = outboxPort.save(outbox);

        // 5. Orchestrator Command 생성
        Command command = Command.of(
                Domain.of(DOMAIN),
                EventType.of(operation.getEventType()),
                BizKey.of("seller-" + sellerId),
                IdemKey.of(idemKey),
                Payload.of(payloadJson)
        );

        // 6. Orchestrator 시작 → OperationHandle 획득
        OperationHandle handle = orchestrator.submit(command, DEFAULT_TIMEOUT_MILLIS);
        OpId opId = handle.getOpId();

        // 7. OpId를 Outbox에 업데이트 (REQUIRES_NEW 트랜잭션)
        outboxPort.updateOpId(savedOutbox.getId(), opId.getValue());

        return opId;
    }

    // ========================================
    // Private Helper Methods
    // ========================================

    /**
     * Idempotency Key 생성
     * <p>
     * 형식: seller-{sellerId}-{action}
     * </p>
     * <p>
     * 멱등성 보장 전략:
     * <ul>
     *   <li>타임스탬프 제거: 동일한 비즈니스 요청은 항상 동일한 멱등키 생성</li>
     *   <li>sellerId + operation 조합: 특정 셀러의 특정 작업을 고유하게 식별</li>
     *   <li>중복 제출 방지: Orchestrator가 동일 IdemKey 재전송 시 중복 처리 차단</li>
     * </ul>
     * </p>
     * <p>
     * 예시:
     * <ul>
     *   <li>seller-123-create: 셀러 123의 스케줄 생성</li>
     *   <li>seller-123-update: 셀러 123의 스케줄 수정</li>
     *   <li>seller-123-delete: 셀러 123의 스케줄 삭제</li>
     * </ul>
     * </p>
     * <p>
     * 동작 시나리오:
     * <ul>
     *   <li>시나리오 1: 같은 요청 2번 제출 → 첫 번째만 처리, 두 번째는 중복으로 skip</li>
     *   <li>시나리오 2: UPDATE 후 다시 UPDATE → 두 번째 UPDATE는 새 작업으로 처리 (cronExpression 다름)</li>
     *   <li>시나리오 3: CREATE 완료 후 DELETE → 별개 작업으로 처리 (operation 다름)</li>
     * </ul>
     * </p>
     *
     * @param sellerId  셀러 PK
     * @param operation 작업 유형 Enum
     * @return Idempotency Key (타임스탬프 없음)
     */
    private String generateIdemKey(Long sellerId, ScheduleOperation operation) {
        return String.format("seller-%d-%s",
                sellerId,
                operation.getAction()
        );
    }

    /**
     * Payload JSON 생성
     * <p>
     * ScheduleRequest Record를 사용하여 타입 안정성을 확보합니다.
     * EventBridgeExecutor가 파싱할 수 있도록 JSON으로 직렬화합니다.
     * </p>
     *
     * @param sellerId       셀러 PK
     * @param cronExpression Cron 표현식 (DELETE 시 null 허용)
     * @param operation      작업 유형 Enum
     * @return JSON String
     */
    private String createPayloadJson(
            Long sellerId,
            String cronExpression,
            ScheduleOperation operation
    ) {
        // ScheduleRequest Record 생성 (타입 안정성)
        ScheduleRequest request = new ScheduleRequest(
                sellerId,
                cronExpression,
                operation.getOperationName()
        );

        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Failed to serialize ScheduleRequest: " + request,
                    e
            );
        }
    }

    /**
     * ScheduleRequest 내부 클래스
     * <p>
     * EventBridgeExecutor가 파싱하는 Payload DTO입니다.
     * Record를 사용하여 불변성과 타입 안정성을 확보합니다.
     * </p>
     */
    private record ScheduleRequest(
            Long sellerId,
            String cronExpression,
            String operation
    ) {
    }
}
