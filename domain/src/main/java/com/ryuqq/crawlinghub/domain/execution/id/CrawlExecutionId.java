package com.ryuqq.crawlinghub.domain.execution.id;

/**
 * CrawlExecution ID Value Object
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
public record CrawlExecutionId(Long value) {

    /**
     * Compact Constructor (검증 로직)
     *
     * <p>null이 아닌 값은 양수여야 합니다.
     */
    public CrawlExecutionId {
        if (value != null && value <= 0) {
            throw new IllegalArgumentException("CrawlExecutionId 값은 양수여야 합니다: " + value);
        }
    }

    /**
     * 미할당 ID 생성 (Auto Increment 대비)
     *
     * @return CrawlExecutionId (value = null)
     */
    public static CrawlExecutionId forNew() {
        return new CrawlExecutionId(null);
    }

    /**
     * 값 기반 생성
     *
     * @param value ID 값 (null 불가)
     * @return CrawlExecutionId
     * @throws IllegalArgumentException value가 null이거나 0 이하인 경우
     */
    public static CrawlExecutionId of(Long value) {
        if (value == null) {
            throw new IllegalArgumentException("CrawlExecutionId 값은 null일 수 없습니다.");
        }
        return new CrawlExecutionId(value);
    }

    /**
     * ID 할당 여부 확인
     *
     * @return ID가 할당되어 있으면 true
     */
    /**
     * 신규 ID 여부 확인 (미할당 상태)
     *
     * @return ID가 미할당이면 true
     */
    public boolean isNew() {
        return value == null;
    }
}
