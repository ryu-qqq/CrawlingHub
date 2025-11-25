package com.ryuqq.crawlinghub.domain.crawl.task.identifier;

/**
 * CrawlTask ID Value Object
 *
 * <p><strong>생성 패턴</strong>:
 *
 * <ul>
 *   <li>{@code unassigned()} - 미할당 상태 (ID = null, Auto Increment 대비)
 *   <li>{@code of(Long value)} - 값 기반 생성 (null 체크 필수)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public record CrawlTaskId(Long value) {

    /**
     * Compact Constructor (검증 로직)
     *
     * <p>null이 아닌 값은 양수여야 합니다.
     */
    public CrawlTaskId {
        if (value != null && value <= 0) {
            throw new IllegalArgumentException("CrawlTaskId 값은 양수여야 합니다: " + value);
        }
    }

    /**
     * 미할당 ID 생성 (Auto Increment 대비)
     *
     * @return CrawlTaskId (value = null)
     */
    public static CrawlTaskId unassigned() {
        return new CrawlTaskId(null);
    }

    /**
     * 값 기반 생성
     *
     * @param value ID 값 (null 불가)
     * @return CrawlTaskId
     * @throws IllegalArgumentException value가 null이거나 0 이하인 경우
     */
    public static CrawlTaskId of(Long value) {
        if (value == null) {
            throw new IllegalArgumentException("CrawlTaskId 값은 null일 수 없습니다.");
        }
        return new CrawlTaskId(value);
    }

    /**
     * ID 할당 여부 확인
     *
     * @return ID가 할당되어 있으면 true
     */
    public boolean isAssigned() {
        return value != null;
    }
}
