package com.ryuqq.crawlinghub.domain.schedule.query;

import com.ryuqq.crawlinghub.domain.common.vo.SortKey;

/**
 * CrawlScheduler 정렬 키
 *
 * <p>스케줄러 목록 조회 시 사용 가능한 정렬 필드를 정의합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public enum CrawlSchedulerSortKey implements SortKey {
    CREATED_AT("createdAt"),
    UPDATED_AT("updatedAt"),
    SCHEDULER_NAME("schedulerName");

    private final String fieldName;

    CrawlSchedulerSortKey(String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    public String fieldName() {
        return fieldName;
    }

    public static CrawlSchedulerSortKey defaultKey() {
        return CREATED_AT;
    }
}
