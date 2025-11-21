package com.ryuqq.crawlinghub.domain.schedule.vo;

/**
 * CrawlSchedulerOutBox ID Value Object
 *
 * <p><strong>생성 패턴</strong>:
 *
 * <ul>
 *   <li>{@code forNew()} - 신규 생성 (ID = null, Auto Increment 대비)
 *   <li>{@code of(Long value)} - 값 기반 생성 (null 체크 필수)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public record CrawlSchedulerOutBoxId(Long value) {

    /** Compact Constructor (검증 로직) */
    public CrawlSchedulerOutBoxId {
        if (value != null && value <= 0) {
            throw new IllegalArgumentException("CrawlSchedulerOutBoxId 값은 양수여야 합니다: " + value);
        }
    }

    /**
     * 신규 생성 (Auto Increment 대비)
     *
     * @return CrawlSchedulerOutBoxId (value = null)
     */
    public static CrawlSchedulerOutBoxId forNew() {
        return new CrawlSchedulerOutBoxId(null);
    }

    /**
     * 값 기반 생성
     *
     * @param value ID 값 (null 불가)
     * @return CrawlSchedulerOutBoxId
     * @throws IllegalArgumentException value가 null이거나 음수인 경우
     */
    public static CrawlSchedulerOutBoxId of(Long value) {
        if (value == null) {
            throw new IllegalArgumentException("CrawlSchedulerOutBoxId 값은 null일 수 없습니다.");
        }
        return new CrawlSchedulerOutBoxId(value);
    }

    /**
     * null 여부 확인
     *
     * @return ID가 null이면 true
     */
    public boolean isNew() {
        return value == null;
    }
}
