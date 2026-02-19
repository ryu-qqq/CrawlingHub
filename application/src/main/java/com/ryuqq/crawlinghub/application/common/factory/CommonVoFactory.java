package com.ryuqq.crawlinghub.application.common.factory;

import com.ryuqq.crawlinghub.domain.common.vo.DateRange;
import com.ryuqq.crawlinghub.domain.common.vo.PageRequest;
import com.ryuqq.crawlinghub.domain.common.vo.QueryContext;
import com.ryuqq.crawlinghub.domain.common.vo.SortDirection;
import com.ryuqq.crawlinghub.domain.common.vo.SortKey;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

/**
 * Domain Layer 공통 VO 생성 팩토리
 *
 * <p>단순 변환만 수행합니다 (비즈니스 로직/검증 로직 금지).
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CommonVoFactory {

    public DateRange createDateRange(LocalDate startDate, LocalDate endDate) {
        return DateRange.of(startDate, endDate);
    }

    public PageRequest createPageRequest(Integer page, Integer size) {
        return PageRequest.of(page, size);
    }

    public SortDirection parseSortDirection(String direction) {
        return SortDirection.fromString(direction);
    }

    public <K extends SortKey> QueryContext<K> createQueryContext(
            K sortKey, SortDirection sortDirection, PageRequest pageRequest) {
        return QueryContext.of(sortKey, sortDirection, pageRequest);
    }

    public <K extends SortKey> QueryContext<K> createQueryContext(
            K sortKey,
            SortDirection sortDirection,
            PageRequest pageRequest,
            boolean includeDeleted) {
        return QueryContext.of(sortKey, sortDirection, pageRequest, includeDeleted);
    }
}
