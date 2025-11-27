package com.ryuqq.crawlinghub.domain.product.aggregate;

import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 외부 상품 서버 동기화 Outbox
 *
 * <p>외부 상품 서버로 동기화 요청 상태를 관리합니다.
 *
 * <p><strong>Outbox 패턴 흐름</strong>:
 *
 * <pre>
 * 1. CrawledProduct 저장 시 SyncOutbox 함께 저장 (같은 트랜잭션)
 * 2. 이벤트 리스너 또는 스케줄러가 PENDING 상태 조회
 * 3. External Product API 호출 시작 시 PROCESSING으로 변경
 * 4. API 호출 성공 시 COMPLETED로 변경
 * 5. 실패 시 FAILED로 변경 후 재시도
 * </pre>
 *
 * @author development-team
 * @since 1.0.0
 */
public class CrawledProductSyncOutbox {

    private static final int MAX_RETRY_COUNT = 3;

    private final Long id;
    private final CrawledProductId crawledProductId;
    private final SellerId sellerId;
    private final long itemNo;
    private final SyncType syncType;
    private final String idempotencyKey;
    private Long externalProductId;
    private ProductOutboxStatus status;
    private int retryCount;
    private String errorMessage;
    private final LocalDateTime createdAt;
    private LocalDateTime processedAt;

    private CrawledProductSyncOutbox(
            Long id,
            CrawledProductId crawledProductId,
            SellerId sellerId,
            long itemNo,
            SyncType syncType,
            String idempotencyKey,
            Long externalProductId,
            ProductOutboxStatus status,
            int retryCount,
            String errorMessage,
            LocalDateTime createdAt,
            LocalDateTime processedAt) {
        this.id = id;
        this.crawledProductId = crawledProductId;
        this.sellerId = sellerId;
        this.itemNo = itemNo;
        this.syncType = syncType;
        this.idempotencyKey = idempotencyKey;
        this.externalProductId = externalProductId;
        this.status = status;
        this.retryCount = retryCount;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
    }

    /** 신규 등록용 Outbox 생성 */
    public static CrawledProductSyncOutbox forCreate(
            CrawledProductId crawledProductId, SellerId sellerId, long itemNo) {
        String idempotencyKey = generateIdempotencyKey(crawledProductId, SyncType.CREATE);
        return new CrawledProductSyncOutbox(
                null,
                crawledProductId,
                sellerId,
                itemNo,
                SyncType.CREATE,
                idempotencyKey,
                null,
                ProductOutboxStatus.PENDING,
                0,
                null,
                LocalDateTime.now(),
                null);
    }

    /** 갱신용 Outbox 생성 */
    public static CrawledProductSyncOutbox forUpdate(
            CrawledProductId crawledProductId,
            SellerId sellerId,
            long itemNo,
            Long externalProductId) {
        if (externalProductId == null) {
            throw new IllegalArgumentException("갱신 시 externalProductId는 필수입니다.");
        }
        String idempotencyKey = generateIdempotencyKey(crawledProductId, SyncType.UPDATE);
        return new CrawledProductSyncOutbox(
                null,
                crawledProductId,
                sellerId,
                itemNo,
                SyncType.UPDATE,
                idempotencyKey,
                externalProductId,
                ProductOutboxStatus.PENDING,
                0,
                null,
                LocalDateTime.now(),
                null);
    }

    /** 기존 데이터로 복원 (영속성 계층 전용) */
    public static CrawledProductSyncOutbox reconstitute(
            Long id,
            CrawledProductId crawledProductId,
            SellerId sellerId,
            long itemNo,
            SyncType syncType,
            String idempotencyKey,
            Long externalProductId,
            ProductOutboxStatus status,
            int retryCount,
            String errorMessage,
            LocalDateTime createdAt,
            LocalDateTime processedAt) {
        return new CrawledProductSyncOutbox(
                id,
                crawledProductId,
                sellerId,
                itemNo,
                syncType,
                idempotencyKey,
                externalProductId,
                status,
                retryCount,
                errorMessage,
                createdAt,
                processedAt);
    }

    private static String generateIdempotencyKey(
            CrawledProductId crawledProductId, SyncType syncType) {
        return String.format(
                "sync-%s-%s-%s",
                crawledProductId.value(),
                syncType.name().toLowerCase(),
                UUID.randomUUID().toString().substring(0, 8));
    }

    /** 처리 시작 (API 호출 시작) */
    public void markAsProcessing() {
        this.status = ProductOutboxStatus.PROCESSING;
        this.processedAt = LocalDateTime.now();
    }

    /** 동기화 완료 (신규 등록 시 외부 ID 저장) */
    public void markAsCompleted(Long externalProductId) {
        if (this.syncType == SyncType.CREATE && externalProductId == null) {
            throw new IllegalArgumentException("신규 등록 완료 시 externalProductId는 필수입니다.");
        }
        if (this.syncType == SyncType.CREATE) {
            this.externalProductId = externalProductId;
        }
        this.status = ProductOutboxStatus.COMPLETED;
        this.processedAt = LocalDateTime.now();
    }

    /** 처리 실패 */
    public void markAsFailed(String errorMessage) {
        this.status = ProductOutboxStatus.FAILED;
        this.retryCount++;
        this.errorMessage = errorMessage;
        this.processedAt = LocalDateTime.now();
    }

    /** 재시도를 위해 PENDING으로 복귀 */
    public void resetToPending() {
        if (canRetry()) {
            this.status = ProductOutboxStatus.PENDING;
            this.errorMessage = null;
        }
    }

    /** 재시도 가능 여부 확인 */
    public boolean canRetry() {
        return this.retryCount < MAX_RETRY_COUNT;
    }

    /** 처리 대기 상태인지 확인 */
    public boolean isPending() {
        return this.status.isPending();
    }

    /** 완료 상태인지 확인 */
    public boolean isCompleted() {
        return this.status.isCompleted();
    }

    /** 신규 등록 요청인지 확인 */
    public boolean isCreateRequest() {
        return this.syncType == SyncType.CREATE;
    }

    /** 갱신 요청인지 확인 */
    public boolean isUpdateRequest() {
        return this.syncType == SyncType.UPDATE;
    }

    // Getters

    public Long getId() {
        return id;
    }

    public CrawledProductId getCrawledProductId() {
        return crawledProductId;
    }

    public Long getCrawledProductIdValue() {
        return crawledProductId.value();
    }

    public SellerId getSellerId() {
        return sellerId;
    }

    public Long getSellerIdValue() {
        return sellerId.value();
    }

    public long getItemNo() {
        return itemNo;
    }

    public SyncType getSyncType() {
        return syncType;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public Long getExternalProductId() {
        return externalProductId;
    }

    public ProductOutboxStatus getStatus() {
        return status;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    /** 동기화 타입 */
    public enum SyncType {
        /** 신규 상품 등록 */
        CREATE,
        /** 기존 상품 갱신 */
        UPDATE
    }
}
