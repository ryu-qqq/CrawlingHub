package com.ryuqq.crawlinghub.application.schedule.factory.query;

import com.ryuqq.crawlinghub.application.common.factory.CommonVoFactory;
import com.ryuqq.crawlinghub.application.schedule.dto.query.CrawlSchedulerSearchParams;
import com.ryuqq.crawlinghub.domain.common.vo.PageRequest;
import com.ryuqq.crawlinghub.domain.common.vo.QueryContext;
import com.ryuqq.crawlinghub.domain.common.vo.SortDirection;
import com.ryuqq.crawlinghub.domain.schedule.query.CrawlSchedulerSearchCriteria;
import com.ryuqq.crawlinghub.domain.schedule.query.CrawlSchedulerSearchField;
import com.ryuqq.crawlinghub.domain.schedule.query.CrawlSchedulerSortKey;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * CrawlScheduler QueryFactory
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>SearchParams → SearchCriteria 변환
 *   <li>CommonVoFactory를 통한 공통 VO 생성 위임
 * </ul>
 *
 * <p><strong>금지</strong>:
 *
 * <ul>
 *   <li>@Transactional 금지 (변환만, 트랜잭션 불필요)
 *   <li>Port 의존 금지
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlSchedulerQueryFactory {

    private final CommonVoFactory commonVoFactory;

    public CrawlSchedulerQueryFactory(CommonVoFactory commonVoFactory) {
        this.commonVoFactory = commonVoFactory;
    }

    /**
     * CrawlSchedulerSearchParams → CrawlSchedulerSearchCriteria 변환
     *
     * @param params 스케줄러 검색 파라미터
     * @return Domain 조회 조건 객체
     */
    public CrawlSchedulerSearchCriteria createCriteria(CrawlSchedulerSearchParams params) {
        SellerId sellerId = params.sellerId() != null ? SellerId.of(params.sellerId()) : null;
        List<SchedulerStatus> statuses = parseStatuses(params.statuses());
        CrawlSchedulerSearchField searchField =
                CrawlSchedulerSearchField.fromString(params.searchField());

        CrawlSchedulerSortKey sortKey = resolveSortKey(params.sortKey());
        SortDirection sortDirection = commonVoFactory.parseSortDirection(params.sortDirection());
        PageRequest pageRequest = commonVoFactory.createPageRequest(params.page(), params.size());
        QueryContext<CrawlSchedulerSortKey> queryContext =
                commonVoFactory.createQueryContext(sortKey, sortDirection, pageRequest);

        return CrawlSchedulerSearchCriteria.of(
                sellerId, statuses, searchField, params.searchWord(), queryContext);
    }

    private CrawlSchedulerSortKey resolveSortKey(String sortKeyStr) {
        if (sortKeyStr == null || sortKeyStr.isBlank()) {
            return CrawlSchedulerSortKey.defaultKey();
        }
        for (CrawlSchedulerSortKey key : CrawlSchedulerSortKey.values()) {
            if (key.fieldName().equalsIgnoreCase(sortKeyStr)
                    || key.name().equalsIgnoreCase(sortKeyStr)) {
                return key;
            }
        }
        return CrawlSchedulerSortKey.defaultKey();
    }

    private List<SchedulerStatus> parseStatuses(List<String> statusStrings) {
        if (statusStrings == null || statusStrings.isEmpty()) {
            return null;
        }
        return statusStrings.stream()
                .filter(s -> s != null && !s.isBlank())
                .map(SchedulerStatus::valueOf)
                .toList();
    }
}
