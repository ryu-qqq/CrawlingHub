package com.ryuqq.crawlinghub.domain.task.identifier;

/**
 * 크롤링 대상 ID Value Object
 *
 * <p>DETAIL, OPTION 태스크에서 크롤링 대상 상품의 ID를 나타냅니다.
 *
 * <p><strong>생성 패턴</strong>:
 *
 * <ul>
 *   <li>{@code empty()} - 대상 없음 (SEARCH, META 등)
 *   <li>{@code of(Long value)} - 값 기반 생성
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public record TargetId(Long value) {

    /**
     * Compact Constructor (검증 로직)
     *
     * <p>null이 아닌 값은 양수여야 합니다.
     */
    public TargetId {
        if (value != null && value <= 0) {
            throw new IllegalArgumentException("TargetId 값은 양수여야 합니다: " + value);
        }
    }

    /**
     * 대상 없음 생성 (SEARCH, META 등)
     *
     * @return TargetId (value = null)
     */
    public static TargetId empty() {
        return new TargetId(null);
    }

    /**
     * 값 기반 생성
     *
     * @param value 대상 ID 값 (null 허용)
     * @return TargetId
     */
    public static TargetId of(Long value) {
        return new TargetId(value);
    }

    /**
     * 대상 존재 여부 확인
     *
     * @return 대상이 있으면 true
     */
    public boolean hasTarget() {
        return value != null;
    }
}
