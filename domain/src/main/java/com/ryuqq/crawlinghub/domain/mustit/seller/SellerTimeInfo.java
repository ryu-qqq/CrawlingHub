package com.ryuqq.crawlinghub.domain.mustit.seller;

import java.time.LocalDateTime;

/**
 * 머스트잇 셀러 시간 정보 Value Object
 * <p>
 * 생성 시각과 수정 시각을 그룹화하여 응집도를 높입니다.
 * </p>
 *
 * @param createdAt 생성 시각
 * @param updatedAt 수정 시각
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
public record SellerTimeInfo(
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * 정적 팩토리 메서드 - 시간 정보 생성
     *
     * @param createdAt 생성 시각
     * @param updatedAt 수정 시각
     * @return SellerTimeInfo 인스턴스
     */
    public static SellerTimeInfo of(LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new SellerTimeInfo(createdAt, updatedAt);
    }

    /**
     * Compact constructor - 유효성 검증
     */
    public SellerTimeInfo {
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt must not be null");
        }
        if (updatedAt == null) {
            throw new IllegalArgumentException("updatedAt must not be null");
        }
    }
}
