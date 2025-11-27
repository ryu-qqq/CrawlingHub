package com.ryuqq.crawlinghub.application.product.manager;

/**
 * 외부 동기화 Outbox 트랜잭션 관리자
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>SyncOutbox 영속성 관리
 *   <li>Outbox 상태 전환 관리
 *   <li>트랜잭션 경계 관리
 * </ul>
 *
 * <p><strong>상태 전환</strong>:
 *
 * <pre>
 * PENDING → PROCESSING (외부 서버 API 호출 시작)
 * PROCESSING → COMPLETED (API 호출 성공)
 * PROCESSING → FAILED (API 호출 실패)
 * FAILED → PENDING (재시도 가능 시)
 * </pre>
 *
 * @author development-team
 * @since 1.0.0
 */
public class SyncOutboxManager {

    // private final SyncOutboxPersistencePort syncOutboxPersistencePort;
    //
    // public SyncOutboxManager(SyncOutboxPersistencePort syncOutboxPersistencePort) {
    //     this.syncOutboxPersistencePort = syncOutboxPersistencePort;
    // }
    //
    // // === 생성 ===
    //
    // /**
    //  * 신규 등록용 SyncOutbox 생성 및 저장
    //  *
    //  * @param crawledProductId CrawledProduct ID
    //  * @param sellerId 판매자 ID
    //  * @param itemNo 상품 번호
    //  * @return 저장된 SyncOutbox
    //  */
    // @Transactional
    // public CrawledProductSyncOutbox createForCreate(
    //         CrawledProductId crawledProductId,
    //         SellerId sellerId,
    //         long itemNo) {
    //     CrawledProductSyncOutbox outbox = CrawledProductSyncOutbox.forCreate(
    //             crawledProductId, sellerId, itemNo);
    //     syncOutboxPersistencePort.persist(outbox);
    //     return outbox;
    // }
    //
    // /**
    //  * 갱신용 SyncOutbox 생성 및 저장
    //  *
    //  * @param crawledProductId CrawledProduct ID
    //  * @param sellerId 판매자 ID
    //  * @param itemNo 상품 번호
    //  * @param externalProductId 외부 상품 ID
    //  * @return 저장된 SyncOutbox
    //  */
    // @Transactional
    // public CrawledProductSyncOutbox createForUpdate(
    //         CrawledProductId crawledProductId,
    //         SellerId sellerId,
    //         long itemNo,
    //         Long externalProductId) {
    //     CrawledProductSyncOutbox outbox = CrawledProductSyncOutbox.forUpdate(
    //             crawledProductId, sellerId, itemNo, externalProductId);
    //     syncOutboxPersistencePort.persist(outbox);
    //     return outbox;
    // }
    //
    // // === 상태 전환 ===
    //
    // /**
    //  * 처리 시작 (외부 서버 API 호출 시작)
    //  *
    //  * @param outbox 처리 시작할 Outbox
    //  */
    // @Transactional
    // public void markAsProcessing(CrawledProductSyncOutbox outbox) {
    //     outbox.markAsProcessing();
    //     syncOutboxPersistencePort.update(outbox);
    // }
    //
    // /**
    //  * 동기화 완료 (신규 등록 시 외부 ID 저장)
    //  *
    //  * @param outbox 완료할 Outbox
    //  * @param externalProductId 외부 상품 ID (신규 등록 시)
    //  */
    // @Transactional
    // public void markAsCompleted(CrawledProductSyncOutbox outbox, Long externalProductId) {
    //     outbox.markAsCompleted(externalProductId);
    //     syncOutboxPersistencePort.update(outbox);
    // }
    //
    // /**
    //  * 처리 실패
    //  *
    //  * @param outbox 실패한 Outbox
    //  * @param errorMessage 오류 메시지
    //  */
    // @Transactional
    // public void markAsFailed(CrawledProductSyncOutbox outbox, String errorMessage) {
    //     outbox.markAsFailed(errorMessage);
    //     syncOutboxPersistencePort.update(outbox);
    // }
    //
    // /**
    //  * 재시도를 위해 PENDING으로 복귀
    //  *
    //  * @param outbox 재시도할 Outbox
    //  */
    // @Transactional
    // public void resetToPending(CrawledProductSyncOutbox outbox) {
    //     if (outbox.canRetry()) {
    //         outbox.resetToPending();
    //         syncOutboxPersistencePort.update(outbox);
    //     }
    // }
}
