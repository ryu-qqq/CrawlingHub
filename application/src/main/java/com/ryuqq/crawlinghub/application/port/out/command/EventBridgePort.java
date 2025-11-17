package com.ryuqq.crawlinghub.application.port.out.command;

/**
 * AWS EventBridge Rule 관리 Outbound Port
 *
 * <p>EventBridge Scheduler Rule을 생성/수정/삭제하여 크롤링 스케줄을 관리합니다.</p>
 *
 * <p><strong>Zero-Tolerance 규칙:</strong></p>
 * <ul>
 *   <li>✅ Application Layer는 AWS SDK 타입 사용 금지 (Infrastructure 책임)</li>
 *   <li>✅ Primitive 타입 사용 (sellerId: String, intervalDays: int)</li>
 *   <li>✅ @Transactional 내부 호출 절대 금지 (외부 API)</li>
 * </ul>
 *
 * <p><strong>사용 시나리오:</strong></p>
 * <ul>
 *   <li>createRule() - Seller 등록 시 크롤링 스케줄 Rule 생성</li>
 *   <li>updateRule() - Seller 크롤링 주기 변경 시 Rule 업데이트</li>
 *   <li>deleteRule() - Seller 비활성화 시 Rule 삭제</li>
 * </ul>
 *
 * <p><strong>Implementation 요구사항:</strong></p>
 * <ul>
 *   <li>Rule Name Convention: "crawling-schedule-{sellerId}"</li>
 *   <li>Cron Expression: rate({intervalDays} days)</li>
 *   <li>Target: Lambda 함수 (크롤링 트리거)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
public interface EventBridgePort {

    /**
     * EventBridge Rule 생성
     *
     * <p>새로운 Seller의 크롤링 스케줄 Rule을 생성합니다.</p>
     *
     * <p><strong>호출 위치:</strong></p>
     * <ul>
     *   <li>RegisterSellerUseCaseImpl.execute() 내부</li>
     *   <li>⚠️ 반드시 @Transactional 외부에서 호출</li>
     * </ul>
     *
     * @param sellerId Seller ID (String)
     * @param intervalDays 크롤링 주기 (일 단위, 1~365)
     * @throws IllegalArgumentException intervalDays가 유효하지 않은 경우
     * @throws EventBridgeException Rule 생성 실패 시
     */
    void createRule(String sellerId, int intervalDays);

    /**
     * EventBridge Rule 업데이트
     *
     * <p>기존 Seller의 크롤링 주기를 변경합니다.</p>
     *
     * <p><strong>호출 위치:</strong></p>
     * <ul>
     *   <li>UpdateSellerIntervalUseCaseImpl.execute() 내부</li>
     *   <li>⚠️ 반드시 @Transactional 외부에서 호출</li>
     * </ul>
     *
     * @param sellerId Seller ID (String)
     * @param newIntervalDays 새로운 크롤링 주기 (일 단위, 1~365)
     * @throws IllegalArgumentException newIntervalDays가 유효하지 않은 경우
     * @throws EventBridgeException Rule 업데이트 실패 시
     */
    void updateRule(String sellerId, int newIntervalDays);

    /**
     * EventBridge Rule 삭제
     *
     * <p>Seller 비활성화 시 크롤링 스케줄 Rule을 삭제합니다.</p>
     *
     * <p><strong>호출 위치:</strong></p>
     * <ul>
     *   <li>DeactivateSellerUseCaseImpl.execute() 내부</li>
     *   <li>⚠️ 반드시 @Transactional 외부에서 호출</li>
     * </ul>
     *
     * @param sellerId Seller ID (String)
     * @throws EventBridgeException Rule 삭제 실패 시
     */
    void deleteRule(String sellerId);
}
