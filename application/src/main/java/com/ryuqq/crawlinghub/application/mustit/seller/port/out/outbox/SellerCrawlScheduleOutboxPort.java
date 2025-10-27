package com.ryuqq.crawlinghub.application.mustit.seller.port.out.outbox;

import com.ryuqq.crawlinghub.domain.mustit.seller.outbox.SellerCrawlScheduleOutbox;

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
 *   <li>Outbox 저장</li>
 *   <li>Outbox 조회 (OpId, IdemKey)</li>
 *   <li>OpId 업데이트</li>
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
}
