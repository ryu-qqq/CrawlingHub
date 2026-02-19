package com.ryuqq.crawlinghub.application.common.dto.query;

import com.ryuqq.crawlinghub.domain.common.vo.PageRequest;
import com.ryuqq.crawlinghub.domain.common.vo.QueryContext;
import com.ryuqq.crawlinghub.domain.common.vo.SortDirection;
import com.ryuqq.crawlinghub.domain.common.vo.SortKey;
import java.time.LocalDate;

/**
 * Offset 기반 페이징 + 정렬 + 날짜 범위 + 삭제 포함 여부 공통 검색 파라미터
 *
 * @param includeDeleted 삭제된 항목 포함 여부
 * @param startDate 시작 날짜
 * @param endDate 종료 날짜
 * @param sortKey 정렬 키 (문자열)
 * @param sortDirection 정렬 방향 (문자열)
 * @param page 페이지 번호 (0-based)
 * @param size 페이지 크기
 */
public record CommonSearchParams(
        Boolean includeDeleted,
        LocalDate startDate,
        LocalDate endDate,
        String sortKey,
        String sortDirection,
        Integer page,
        Integer size) {

    /** Compact Constructor - null 방어 */
    public CommonSearchParams {
        if (includeDeleted == null) {
            includeDeleted = false;
        }
        if (sortKey == null || sortKey.isBlank()) {
            sortKey = "createdAt";
        }
        if (sortDirection == null || sortDirection.isBlank()) {
            sortDirection = "DESC";
        }
        if (page == null || page < 0) {
            page = 0;
        }
        if (size == null || size <= 0) {
            size = 20;
        }
    }

    public static CommonSearchParams of(
            Boolean includeDeleted,
            LocalDate startDate,
            LocalDate endDate,
            String sortKey,
            String sortDirection,
            Integer page,
            Integer size) {
        return new CommonSearchParams(
                includeDeleted, startDate, endDate, sortKey, sortDirection, page, size);
    }

    /**
     * Domain QueryContext로 변환
     *
     * @param sortKeyClass SortKey enum 클래스
     * @param <K> SortKey 타입
     * @return QueryContext
     */
    public <K extends SortKey> QueryContext<K> toQueryContext(Class<K> sortKeyClass) {
        K resolvedSortKey = resolveSortKey(sortKeyClass);
        SortDirection direction = SortDirection.fromString(this.sortDirection);
        PageRequest pageRequest = PageRequest.of(this.page, this.size);
        return QueryContext.of(resolvedSortKey, direction, pageRequest, this.includeDeleted);
    }

    @SuppressWarnings("unchecked")
    private <K extends SortKey> K resolveSortKey(Class<K> sortKeyClass) {
        if (!sortKeyClass.isEnum()) {
            throw new IllegalArgumentException("SortKey class must be an enum: " + sortKeyClass);
        }

        K[] constants = sortKeyClass.getEnumConstants();
        for (K constant : constants) {
            if (constant.fieldName().equalsIgnoreCase(this.sortKey)
                    || ((Enum<?>) constant).name().equalsIgnoreCase(this.sortKey)) {
                return constant;
            }
        }

        return constants[0];
    }
}
