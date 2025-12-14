package com.ryuqq.crawlinghub.domain.product.aggregate;

import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.ImageType;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

/**
 * 이미지 업로드 Outbox
 *
 * <p>파일서버로 이미지 업로드 요청 상태를 관리합니다.
 *
 * <p><strong>Outbox 패턴 흐름</strong>:
 *
 * <pre>
 * 1. CrawledProduct 저장 시 ImageOutbox 함께 저장 (같은 트랜잭션)
 * 2. 이벤트 리스너 또는 스케줄러가 PENDING 상태 조회
 * 3. FileServer API 호출 성공 시 PROCESSING으로 변경
 * 4. 웹훅 수신 시 COMPLETED로 변경
 * 5. 실패 시 FAILED로 변경 후 재시도
 * </pre>
 *
 * @author development-team
 * @since 1.0.0
 */
public class CrawledProductImageOutbox {

    private static final int MAX_RETRY_COUNT = 3;

    private final Long id;
    private final CrawledProductId crawledProductId;
    private final String originalUrl;
    private final ImageType imageType;
    private final String idempotencyKey;
    private String s3Url;
    private ProductOutboxStatus status;
    private int retryCount;
    private String errorMessage;
    private final Instant createdAt;
    private Instant processedAt;

    private CrawledProductImageOutbox(
            Long id,
            CrawledProductId crawledProductId,
            String originalUrl,
            ImageType imageType,
            String idempotencyKey,
            String s3Url,
            ProductOutboxStatus status,
            int retryCount,
            String errorMessage,
            Instant createdAt,
            Instant processedAt) {
        this.id = id;
        this.crawledProductId = crawledProductId;
        this.originalUrl = originalUrl;
        this.imageType = imageType;
        this.idempotencyKey = idempotencyKey;
        this.s3Url = s3Url;
        this.status = status;
        this.retryCount = retryCount;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
    }

    /** 신규 Outbox 생성 */
    public static CrawledProductImageOutbox forNew(
            CrawledProductId crawledProductId,
            String originalUrl,
            ImageType imageType,
            Clock clock) {
        String idempotencyKey = generateIdempotencyKey(crawledProductId, originalUrl);
        Instant now = clock.instant();
        return new CrawledProductImageOutbox(
                null,
                crawledProductId,
                originalUrl,
                imageType,
                idempotencyKey,
                null,
                ProductOutboxStatus.PENDING,
                0,
                null,
                now,
                null);
    }

    /** 기존 데이터로 복원 (영속성 계층 전용) */
    public static CrawledProductImageOutbox reconstitute(
            Long id,
            CrawledProductId crawledProductId,
            String originalUrl,
            ImageType imageType,
            String idempotencyKey,
            String s3Url,
            ProductOutboxStatus status,
            int retryCount,
            String errorMessage,
            Instant createdAt,
            Instant processedAt) {
        return new CrawledProductImageOutbox(
                id,
                crawledProductId,
                originalUrl,
                imageType,
                idempotencyKey,
                s3Url,
                status,
                retryCount,
                errorMessage,
                createdAt,
                processedAt);
    }

    private static String generateIdempotencyKey(
            CrawledProductId crawledProductId, String originalUrl) {
        return String.format(
                "img-%s-%s-%s",
                crawledProductId.value(),
                originalUrl.hashCode(),
                UUID.randomUUID().toString().substring(0, 8));
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
     * @param s3Url S3 URL
     * @param clock 시간 제어
     */
    public void markAsCompleted(String s3Url, Clock clock) {
        if (s3Url == null || s3Url.isBlank()) {
            throw new IllegalArgumentException("s3Url은 필수입니다.");
        }
        this.s3Url = s3Url;
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

    public String getOriginalUrl() {
        return originalUrl;
    }

    public ImageType getImageType() {
        return imageType;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getS3Url() {
        return s3Url;
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
