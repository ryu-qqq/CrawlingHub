package com.ryuqq.crawlinghub.domain.common.vo;

import java.time.Instant;

/**
 * DeletionStatus - Soft Delete 상태 Value Object
 *
 * <p>삭제 여부와 삭제 시간을 함께 관리하여 동기화 문제를 방지합니다.
 *
 * <p><strong>사용 예시:</strong>
 *
 * <pre>{@code
 * // Aggregate 생성 시
 * private DeletionStatus deletionStatus = DeletionStatus.active();
 *
 * // 삭제 시
 * public void delete(Instant occurredAt) {
 *     this.deletionStatus = DeletionStatus.deletedAt(occurredAt);
 * }
 *
 * // 조회 시
 * if (aggregate.deletionStatus().isDeleted()) { ... }
 * }</pre>
 *
 * @author ryu-qqq
 * @since 2025-01-02
 */
public record DeletionStatus(boolean deleted, Instant deletedAt) {

    private static final DeletionStatus ACTIVE = new DeletionStatus(false, null);

    public DeletionStatus {
        if (deleted && deletedAt == null) {
            throw new IllegalArgumentException("deletedAt must not be null when deleted is true");
        }
        if (!deleted && deletedAt != null) {
            throw new IllegalArgumentException("deletedAt must be null when deleted is false");
        }
    }

    /** 활성 상태 (삭제되지 않음) */
    public static DeletionStatus active() {
        return ACTIVE;
    }

    /**
     * 삭제 상태
     *
     * @param occurredAt 삭제 발생 시각
     * @return 삭제된 DeletionStatus
     */
    public static DeletionStatus deletedAt(Instant occurredAt) {
        if (occurredAt == null) {
            throw new IllegalArgumentException("occurredAt must not be null");
        }
        return new DeletionStatus(true, occurredAt);
    }

    /** 영속성에서 복원 시 사용 */
    public static DeletionStatus reconstitute(boolean deleted, Instant deletedAt) {
        if (deleted) {
            return deletedAt(deletedAt);
        }
        return active();
    }

    /** 삭제 여부 확인 */
    public boolean isDeleted() {
        return deleted;
    }

    /** 활성 여부 확인 */
    public boolean isActive() {
        return !deleted;
    }
}
