package com.ryuqq.crawlinghub.domain.product;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 상품 동기화 Outbox Aggregate Root
 *
 * <p>역할: Outbox Pattern으로 외부 API 호출 트랜잭션 보장
 *
 * <p>처리 흐름:
 * <ol>
 *   <li>PENDING: 동기화 대기 중</li>
 *   <li>PROCESSING: 동기화 진행 중</li>
 *   <li>COMPLETED: 동기화 완료</li>
 *   <li>FAILED: 동기화 실패 (최대 재시도 초과)</li>
 * </ol>
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
public class ProductSyncOutbox {

    private static final int MAX_RETRY_COUNT = 3;

    private final ProductSyncOutboxId id;
    private final Long productId;
    private final String productJson;
    private SyncStatus status;
    private Integer retryCount;
    private String errorMessage;
    private final Clock clock;
    private final LocalDateTime createdAt;
    private LocalDateTime processedAt;

    /**
     * Private 전체 생성자 (reconstitute 전용)
     */
    private ProductSyncOutbox(
        ProductSyncOutboxId id,
        Long productId,
        String productJson,
        SyncStatus status,
        Integer retryCount,
        String errorMessage,
        Clock clock,
        LocalDateTime createdAt,
        LocalDateTime processedAt
    ) {
        this.id = id;
        this.productId = productId;
        this.productJson = productJson;
        this.status = status;
        this.retryCount = retryCount;
        this.errorMessage = errorMessage;
        this.clock = clock;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
    }

    /**
     * Package-private 주요 생성자 (검증 포함)
     */
    ProductSyncOutbox(
        ProductSyncOutboxId id,
        Long productId,
        String productJson,
        Clock clock
    ) {
        validateRequiredFields(productId, productJson);

        this.id = id;
        this.productId = productId;
        this.productJson = productJson;
        this.status = SyncStatus.PENDING;
        this.retryCount = 0;
        this.errorMessage = null;
        this.clock = clock;
        this.createdAt = LocalDateTime.now(clock);
        this.processedAt = null;
    }

    /**
     * 신규 Outbox 생성 (ID 없음)
     */
    public static ProductSyncOutbox create(Long productId, String productJson) {
        return new ProductSyncOutbox(null, productId, productJson, Clock.systemDefaultZone());
    }

    /**
     * DB reconstitute (모든 필드 포함)
     */
    public static ProductSyncOutbox reconstitute(
        ProductSyncOutboxId id,
        Long productId,
        String productJson,
        SyncStatus status,
        Integer retryCount,
        String errorMessage,
        LocalDateTime createdAt,
        LocalDateTime processedAt
    ) {
        if (id == null) {
            throw new IllegalArgumentException("DB reconstitute는 ID가 필수입니다");
        }
        return new ProductSyncOutbox(
            id, productId, productJson,
            status, retryCount, errorMessage,
            Clock.systemDefaultZone(),
            createdAt, processedAt
        );
    }

    private static void validateRequiredFields(Long productId, String productJson) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID는 필수입니다");
        }
        if (productJson == null || productJson.isBlank()) {
            throw new IllegalArgumentException("Product JSON은 필수입니다");
        }
    }

    /**
     * 처리 시작
     */
    public void markAsProcessing() {
        if (status != SyncStatus.PENDING) {
            throw new IllegalStateException("PENDING 상태에서만 PROCESSING으로 변경 가능합니다");
        }
        this.status = SyncStatus.PROCESSING;
    }

    /**
     * 처리 완료
     */
    public void markAsCompleted() {
        if (status != SyncStatus.PROCESSING) {
            throw new IllegalStateException("PROCESSING 상태에서만 COMPLETED로 변경 가능합니다");
        }
        this.status = SyncStatus.COMPLETED;
        this.processedAt = LocalDateTime.now(clock);
    }

    /**
     * 재시도 카운트 증가
     */
    public void incrementRetryCount() {
        this.retryCount++;
        this.status = SyncStatus.PENDING; // 재시도를 위해 PENDING으로 복원
    }

    /**
     * 에러 메시지 기록
     */
    public void recordError(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * 처리 실패
     */
    public void markAsFailed() {
        this.status = SyncStatus.FAILED;
        this.processedAt = LocalDateTime.now(clock);
    }

    /**
     * 최대 재시도 횟수 초과 여부
     */
    public boolean isMaxRetriesExceeded() {
        return retryCount >= MAX_RETRY_COUNT;
    }

    /**
     * 처리 가능 여부 확인
     */
    public boolean canProcess() {
        return status == SyncStatus.PENDING && !isMaxRetriesExceeded();
    }

    // Law of Demeter 준수 메서드
    public Long getIdValue() {
        return id != null ? id.value() : null;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductJson() {
        return productJson;
    }

    public SyncStatus getStatus() {
        return status;
    }

    public Integer getRetryCount() {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProductSyncOutbox that = (ProductSyncOutbox) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ProductSyncOutbox{" +
            "id=" + id +
            ", productId=" + productId +
            ", status=" + status +
            ", retryCount=" + retryCount +
            '}';
    }
}

