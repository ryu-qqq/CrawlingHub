package com.ryuqq.crawlinghub.domain.schedule.query;

import com.ryuqq.crawlinghub.domain.common.vo.SearchField;

/**
 * CrawlScheduler 검색 필드
 *
 * <p>스케줄러 목록 조회 시 검색 가능한 필드를 정의합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public enum CrawlSchedulerSearchField implements SearchField {
    SCHEDULER_NAME("schedulerName");

    private final String fieldName;

    CrawlSchedulerSearchField(String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    public String fieldName() {
        return fieldName;
    }

    /**
     * 문자열로부터 CrawlSchedulerSearchField 파싱
     *
     * @param value 필드명 문자열
     * @return CrawlSchedulerSearchField (null이거나 매칭 없으면 null 반환)
     */
    public static CrawlSchedulerSearchField fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        for (CrawlSchedulerSearchField field : values()) {
            if (field.fieldName().equalsIgnoreCase(value) || field.name().equalsIgnoreCase(value)) {
                return field;
            }
        }
        return null;
    }
}
