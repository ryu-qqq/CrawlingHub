package com.ryuqq.crawlinghub.domain.product.aggregate;

import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

/**
 * 이미지 업로드 Outbox (작업 큐)
 *
 * <p>파일서버로 이미지 업로드 요청 상태를 관리합니다.
 *
 * <p><strong>Outbox 패턴 흐름</strong>:
 *
 * <pre>
 * 1. CrawledProductImage 저장 시 ProductImageOutbox 함께 저장 (같은 트랜잭션)
 * 2. 이벤트 리스너 또는 스케줄러가 PENDING 상태 조회
 * 3. FileServer API 호출 성공 시 PROCESSING으로 변경
 * 4. 웹훅 수신 시 CrawledProductImage.s3Url 업데이트 + Outbox COMPLETED
 * 5. 완료된 Outbox는 정리/삭제 가능
 * </pre>
 *
 * <p><strong>CrawledProductImage와의 관계</strong>:
 *
 * <pre>
 * ProductImageOutbox (N) ──→ (1) CrawledProductImage
 * - crawledProductImageId로 참조
 * - 재시도 시 새 Outbox 생성 가능
 * </pre>
 *
 * @author development-team
 * @since 1.0.0
 */
public class ProductImageOutbox {

    private static final int MAX_RETRY_COUNT = 3;

    private final Long id;
    private final Long crawledProductImageId;
    private final String idempotencyKey;
    private ProductOutboxStatus status;
    private int retryCount;
    private String errorMessage;
    private final Instant createdAt;
    private Instant processedAt;

    private ProductImageOutbox(
            Long id,
            Long crawledProductImageId,
            String idempotencyKey,
            ProductOutboxStatus status,
            int retryCount,
            String errorMessage,
            Instant createdAt,
            Instant processedAt) {
        this.id = id;
        this.crawledProductImageId = crawledProductImageId;
        this.idempotencyKey = idempotencyKey;
        this.status = status;
        this.retryCount = retryCount;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
    }

    /**
     * 신규 Outbox 생성
     *
     * @param crawledProductImage 대상 이미지
     * @param clock 시간 제어
     * @return 새로운 ProductImageOutbox
     */
    public static ProductImageOutbox forNew(CrawledProductImage crawledProductImage, Clock clock) {
        if (crawledProductImage.getId() == null) {
            throw new IllegalArgumentException("CrawledProductImage는 먼저 저장되어야 합니다.");
        }
        String idempotencyKey = generateIdempotencyKey(crawledProductImage);
        Instant now = clock.instant();
        return new ProductImageOutbox(
                null,
                crawledProductImage.getId(),
                idempotencyKey,
                ProductOutboxStatus.PENDING,
                0,
                null,
                now,
                null);
    }

    /**
     * 신규 Outbox 생성 (이미지 ID 직접 지정)
     *
     * @param crawledProductImageId 대상 이미지 ID
     * @param originalUrl 원본 URL (멱등성 키 생성용)
     * @param clock 시간 제어
     * @return 새로운 ProductImageOutbox
     */
    public static ProductImageOutbox forNewWithImageId(
            Long crawledProductImageId, String originalUrl, Clock clock) {
        String idempotencyKey = generateIdempotencyKeyFromUrl(crawledProductImageId, originalUrl);
        Instant now = clock.instant();
        return new ProductImageOutbox(
                null,
                crawledProductImageId,
                idempotencyKey,
                ProductOutboxStatus.PENDING,
                0,
                null,
                now,
                null);
    }

    /**
     * 기존 데이터로 복원 (영속성 계층 전용)
     *
     * @param id Outbox ID
     * @param crawledProductImageId 이미지 ID
     * @param idempotencyKey 멱등성 키
     * @param status 상태
     * @param retryCount 재시도 횟수
     * @param errorMessage 에러 메시지
     * @param createdAt 생성 일시
     * @param processedAt 처리 일시
     * @return 복원된 ProductImageOutbox
     */
    public static ProductImageOutbox reconstitute(
            Long id,
            Long crawledProductImageId,
            String idempotencyKey,
            ProductOutboxStatus status,
            int retryCount,
            String errorMessage,
            Instant createdAt,
            Instant processedAt) {
        return new ProductImageOutbox(
                id,
                crawledProductImageId,
                idempotencyKey,
                status,
                retryCount,
                errorMessage,
                createdAt,
                processedAt);
    }

    private static String generateIdempotencyKey(CrawledProductImage image) {
        return String.format(
                "img-%s-%s-%s",
                image.getId(),
                image.getOriginalUrl().hashCode(),
                UUID.randomUUID().toString().substring(0, 8));
    }

    private static String generateIdempotencyKeyFromUrl(Long imageId, String originalUrl) {
        return String.format(
                "img-%s-%s-%s",
                imageId, originalUrl.hashCode(), UUID.randomUUID().toString().substring(0, 8));
    }

    /**
     * 처리 시작 (API 호출 시작)
     *
     * @param clock 시간 제어
     */
    public void markAsProcessing(Clock clock) {
        this.status = ProductOutboxStatus.PROCESSING;
        this.processedAt = clock.instant();
    }

    /**
     * 업로드 완료 (웹훅 수신)
     *
     * @param clock 시간 제어
     */
    public void markAsCompleted(Clock clock) {
        this.status = ProductOutboxStatus.COMPLETED;
        this.processedAt = clock.instant();
    }

    /**
     * 처리 실패
     *
     * @param errorMessage 에러 메시지
     * @param clock 시간 제어
     */
    public void markAsFailed(String errorMessage, Clock clock) {
        this.status = ProductOutboxStatus.FAILED;
        this.retryCount++;
        this.errorMessage = errorMessage;
        this.processedAt = clock.instant();
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

    /** 실패 상태인지 확인 */
    public boolean isFailed() {
        return this.status.isFailed();
    }

    // Getters

    public Long getId() {
        return id;
    }

    public Long getCrawledProductImageId() {
        return crawledProductImageId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }
}
