package com.ryuqq.crawlinghub.domain.change;

import com.ryuqq.crawlinghub.domain.product.ProductId;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 변경 감지 Aggregate Root
 * 
 * <p>비즈니스 규칙:
 * <ul>
 *   <li>중요 필드만 해시 계산 (가격, 옵션, 이미지)</li>
 *   <li>동일 변경 24시간 내 중복 알림 방지</li>
 *   <li>FAILED 상태 3회 재시도</li>
 * </ul>
 */
public class ChangeDetection {

    private static final int MAX_RETRY_COUNT = 3;
    private static final int DUPLICATE_NOTIFICATION_HOURS = 24;

    private final ChangeDetectionId id;
    private final ProductId productId;
    private final ChangeType changeType;
    private final String previousHash;
    private final String currentHash;
    private final ChangeData changeDetails;
    private NotificationStatus status;
    private Integer retryCount;
    private String failureReason;
    private final LocalDateTime detectedAt;
    private LocalDateTime notifiedAt;
    private final Clock clock;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Private 전체 생성자 (reconstitute 전용)
     */
    private ChangeDetection(
        ChangeDetectionId id,
        ProductId productId,
        ChangeType changeType,
        String previousHash,
        String currentHash,
        ChangeData changeDetails,
        NotificationStatus status,
        Integer retryCount,
        String failureReason,
        LocalDateTime detectedAt,
        LocalDateTime notifiedAt,
        Clock clock,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.productId = productId;
        this.changeType = changeType;
        this.previousHash = previousHash;
        this.currentHash = currentHash;
        this.changeDetails = changeDetails;
        this.status = status;
        this.retryCount = retryCount;
        this.failureReason = failureReason;
        this.detectedAt = detectedAt;
        this.notifiedAt = notifiedAt;
        this.clock = clock;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Package-private 주요 생성자 (검증 포함)
     */
    ChangeDetection(
        ChangeDetectionId id,
        ProductId productId,
        ChangeType changeType,
        String previousHash,
        String currentHash,
        ChangeData changeDetails,
        Clock clock
    ) {
        validateRequiredFields(productId, changeType, currentHash, changeDetails);

        LocalDateTime now = LocalDateTime.now(clock);
        this.id = id;
        this.productId = productId;
        this.changeType = changeType;
        this.previousHash = previousHash;
        this.currentHash = currentHash;
        this.changeDetails = changeDetails;
        this.status = NotificationStatus.PENDING;
        this.retryCount = 0;
        this.failureReason = null;
        this.detectedAt = now;
        this.notifiedAt = null;
        this.clock = clock;
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * 신규 변경 감지 생성 (ID 없음)
     */
    public static ChangeDetection forNew(
        ProductId productId,
        ChangeType changeType,
        String previousHash,
        String currentHash,
        ChangeData changeDetails
    ) {
        return new ChangeDetection(
            null,
            productId,
            changeType,
            previousHash,
            currentHash,
            changeDetails,
            Clock.systemDefaultZone()
        );
    }

    /**
     * 기존 변경 감지 생성 (ID 있음)
     */
    public static ChangeDetection of(
        ChangeDetectionId id,
        ProductId productId,
        ChangeType changeType,
        String previousHash,
        String currentHash,
        ChangeData changeDetails
    ) {
        if (id == null) {
            throw new IllegalArgumentException("ChangeDetection ID는 필수입니다");
        }
        return new ChangeDetection(
            id,
            productId,
            changeType,
            previousHash,
            currentHash,
            changeDetails,
            Clock.systemDefaultZone()
        );
    }

    /**
     * DB reconstitute (모든 필드 포함)
     */
    public static ChangeDetection reconstitute(
        ChangeDetectionId id,
        ProductId productId,
        ChangeType changeType,
        String previousHash,
        String currentHash,
        ChangeData changeDetails,
        NotificationStatus status,
        Integer retryCount,
        String failureReason,
        LocalDateTime detectedAt,
        LocalDateTime notifiedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        if (id == null) {
            throw new IllegalArgumentException("DB reconstitute는 ID가 필수입니다");
        }
        return new ChangeDetection(
            id,
            productId,
            changeType,
            previousHash,
            currentHash,
            changeDetails,
            status,
            retryCount,
            failureReason,
            detectedAt,
            notifiedAt,
            Clock.systemDefaultZone(),
            createdAt,
            updatedAt
        );
    }

    private static void validateRequiredFields(
        ProductId productId,
        ChangeType changeType,
        String currentHash,
        ChangeData changeDetails
    ) {
        if (productId == null) {
            throw new IllegalArgumentException("상품 ID는 필수입니다");
        }
        if (changeType == null) {
            throw new IllegalArgumentException("변경 유형은 필수입니다");
        }
        if (currentHash == null || currentHash.isBlank()) {
            throw new IllegalArgumentException("현재 해시는 필수입니다");
        }
        if (changeDetails == null) {
            throw new IllegalArgumentException("변경 상세 정보는 필수입니다");
        }
    }

    /**
     * 전송 완료 표시
     */
    public void markAsSent() {
        if (this.status == NotificationStatus.SENT) {
            throw new IllegalStateException("이미 전송된 알림입니다");
        }
        this.status = NotificationStatus.SENT;
        this.notifiedAt = LocalDateTime.now(clock);
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 실패 표시
     */
    public void markAsFailed(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("실패 사유는 필수입니다");
        }
        this.status = NotificationStatus.FAILED;
        this.failureReason = reason;
        this.retryCount++;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 알림 전송 여부 확인
     */
    public boolean shouldNotify() {
        if (status == NotificationStatus.SENT) {
            return false;
        }
        if (status == NotificationStatus.FAILED && retryCount >= MAX_RETRY_COUNT) {
            return false;
        }
        return true;
    }

    /**
     * 재시도 가능 여부
     */
    public boolean canRetry() {
        return status.isFailed() && retryCount < MAX_RETRY_COUNT;
    }

    /**
     * 변경 메시지 생성
     */
    public String generateChangeMessage() {
        return String.format(
            "[%s 변경 감지] 상품 ID: %s, 변경 내용: %s",
            changeType.getDescription(),
            productId.value(),
            changeDetails.getValue()
        );
    }

    /**
     * 중복 알림 확인 (24시간 이내)
     */
    public boolean isDuplicateNotification(LocalDateTime lastNotificationTime) {
        if (lastNotificationTime == null) {
            return false;
        }
        LocalDateTime threshold = lastNotificationTime.plusHours(DUPLICATE_NOTIFICATION_HOURS);
        return detectedAt.isBefore(threshold);
    }

    /**
     * 특정 상태인지 확인
     */
    public boolean hasStatus(NotificationStatus targetStatus) {
        return this.status == targetStatus;
    }

    // Law of Demeter 준수 메서드
    public Long getIdValue() {
        return id != null ? id.value() : null;
    }

    public Long getProductIdValue() {
        return productId != null ? productId.value() : null;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public String getCurrentHash() {
        return currentHash;
    }

    public String getChangeDetailsValue() {
        return changeDetails.getValue();
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public LocalDateTime getDetectedAt() {
        return detectedAt;
    }

    public LocalDateTime getNotifiedAt() {
        return notifiedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChangeDetection that = (ChangeDetection) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ChangeDetection{" +
            "id=" + id +
            ", productId=" + productId +
            ", changeType=" + changeType +
            ", status=" + status +
            ", retryCount=" + retryCount +
            '}';
    }
}
