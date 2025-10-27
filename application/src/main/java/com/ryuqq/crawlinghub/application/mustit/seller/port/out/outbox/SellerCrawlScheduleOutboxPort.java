package com.ryuqq.crawlinghub.application.mustit.seller.port.out.outbox;

import com.ryuqq.crawlinghub.domain.mustit.seller.outbox.SellerCrawlScheduleOutbox;

import java.util.List;

/**
 * 셀러 크롤링 스케줄 Outbox Port (Outbound Port)
 * <p>
 * 헥사고날 아키텍처의 Outbound Port로서,
 * Application Layer가 특정 영속성 기술(JPA)에 의존하지 않도록
 * 추상화합니다.
 * </p>
 * <p>
 * 이 인터페이스는 Application Layer에 정의되며,
 * Adapter-Persistence Layer에서 구현됩니다.
 * </p>
 * <p>
 * 주요 책임:
 * <ul>
 *   <li>Outbox 생명주기 관리 (생성, 진행 중, 완료)</li>
 *   <li>Outbox 조회 (OpId, IdemKey, Seller)</li>
 *   <li>작업 상태 추적 및 복구 지원</li>
 * </ul>
 * </p>
 * <p>
 * 리팩토링 이력:
 * <ul>
 *   <li>2025-01-27: Orchestrator Store 통합을 위한 도메인 용어 메서드 추가</li>
 *   <li>ApplicationOrchestratorStore가 이 Port를 통해 Adapter에 접근</li>
 *   <li>Store SPI 의존성을 Application Layer에 격리</li>
 * </ul>
 * </p>
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
public interface SellerCrawlScheduleOutboxPort {

    /**
     * Outbox 저장
     * <p>
     * 새로운 Outbox를 PENDING 상태로 저장합니다.
     * </p>
     *
     * @param outbox Outbox 도메인 모델
     * @return 저장된 Outbox (ID 포함)
     */
    SellerCrawlScheduleOutbox save(SellerCrawlScheduleOutbox outbox);

    /**
     * OpId로 Outbox 조회
     * <p>
     * Orchestrator OpId로 Outbox를 조회합니다.
     * </p>
     *
     * @param opId Orchestrator OpId (UUID String)
     * @return Outbox 도메인 모델 (존재하지 않으면 null)
     */
    SellerCrawlScheduleOutbox findByOpId(String opId);

    /**
     * Idempotency Key로 Outbox 조회
     * <p>
     * 중복 실행 방지를 위한 멱등성 검증에 사용됩니다.
     * </p>
     *
     * @param idemKey Idempotency Key
     * @return Outbox 도메인 모델 (존재하지 않으면 null)
     */
    SellerCrawlScheduleOutbox findByIdemKey(String idemKey);

    /**
     * Seller ID로 최신 Outbox 조회
     * <p>
     * 특정 Seller의 가장 최근 스케줄 등록 건을 조회합니다.
     * </p>
     *
     * @param sellerId Seller PK (Long FK)
     * @return Outbox 도메인 모델 (존재하지 않으면 null)
     */
    SellerCrawlScheduleOutbox findLatestBySellerId(Long sellerId);

    /**
     * OpId 업데이트
     * <p>
     * Orchestrator 시작 후 OpId를 Outbox에 설정합니다.
     * </p>
     *
     * @param outboxId Outbox PK
     * @param opId     Orchestrator OpId
     */
    void updateOpId(Long outboxId, String opId);

    // ========================================
    // Orchestrator Store 통합 메서드 (도메인 용어)
    // ========================================

    /**
     * 작업 진행 중 상태로 표시
     * <p>
     * Orchestrator Executor 실행 전 Write-Ahead Log 기록에 사용됩니다.
     * ApplicationOrchestratorStore가 Store.writeAhead()를 처리할 때 호출합니다.
     * </p>
     *
     * @param opId        Orchestrator OpId (UUID String)
     * @param outcomeJson 실행 결과 JSON (Ok, Retry, Fail)
     */
    void markInProgress(String opId, String outcomeJson);

    /**
     * 작업 완료 상태로 표시
     * <p>
     * Orchestrator Executor 실행 완료 후 최종 상태 기록에 사용됩니다.
     * ApplicationOrchestratorStore가 Store.finalize()를 처리할 때 호출합니다.
     * </p>
     *
     * @param opId       Orchestrator OpId (UUID String)
     * @param finalState 최종 상태 (COMPLETED 또는 FAILED)
     */
    void markCompleted(String opId, String finalState);

    /**
     * 진행 중 작업의 결과 조회
     * <p>
     * Write-Ahead Log에 기록된 Outcome을 조회합니다.
     * ApplicationOrchestratorStore가 Store.getWriteAheadOutcome()를 처리할 때 호출합니다.
     * </p>
     *
     * @param opId Orchestrator OpId (UUID String)
     * @return Outcome JSON (존재하지 않으면 null)
     */
    String getInProgressOutcome(String opId);

    /**
     * 대기 중인 작업 목록 조회
     * <p>
     * Write-Ahead Log 기록 후 완료 처리가 안 된 작업들을 찾습니다.
     * Orchestrator Finalizer가 복구할 작업들을 스캔하는 데 사용됩니다.
     * ApplicationOrchestratorStore가 Store.scanWA()를 처리할 때 호출합니다.
     * </p>
     *
     * @param limit 조회 제한 (배치 크기)
     * @return OpId 목록 (UUID String)
     */
    List<String> findPendingOperations(int limit);

    /**
     * 타임아웃된 작업 목록 조회
     * <p>
     * IN_PROGRESS 상태이지만 타임아웃 시간이 지난 작업들을 찾습니다.
     * Orchestrator Reaper가 재시도하거나 실패 처리할 작업들을 스캔하는 데 사용됩니다.
     * ApplicationOrchestratorStore가 Store.scanInProgress()를 처리할 때 호출합니다.
     * </p>
     *
     * @param timeoutMillis 타임아웃 시간 (밀리초)
     * @param limit         조회 제한 (배치 크기)
     * @return OpId 목록 (UUID String)
     */
    List<String> findTimeoutOperations(long timeoutMillis, int limit);

    /**
     * 작업 Command 정보 조회
     * <p>
     * OpId에 해당하는 Orchestrator Command를 재구성하는 데 필요한 정보를 반환합니다.
     * Finalizer/Reaper가 작업을 재실행할 때 사용됩니다.
     * ApplicationOrchestratorStore가 Store.getEnvelope()를 처리할 때 호출합니다.
     * </p>
     *
     * @param opId Orchestrator OpId (UUID String)
     * @return Outbox 도메인 모델 (Command 재구성용)
     */
    SellerCrawlScheduleOutbox getOperationEnvelope(String opId);

    /**
     * 작업 상태 조회
     * <p>
     * OpId에 해당하는 작업의 현재 상태를 반환합니다.
     * ApplicationOrchestratorStore가 Store.getState()를 처리할 때 호출합니다.
     * </p>
     *
     * @param opId Orchestrator OpId (UUID String)
     * @return 작업 상태 (PENDING, IN_PROGRESS, COMPLETED, FAILED)
     */
    String getOperationState(String opId);
}
