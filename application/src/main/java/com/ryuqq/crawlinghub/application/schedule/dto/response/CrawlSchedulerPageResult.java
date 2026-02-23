package com.ryuqq.crawlinghub.application.schedule.dto.response;

import com.ryuqq.crawlinghub.domain.common.vo.PageMeta;
import java.util.List;

/**
 * 크롤 스케줄러 페이지 조회 결과 (PageMeta 기반)
 *
 * @param results 스케줄러 결과 목록
 * @param pageMeta 페이지 메타 정보
 * @author development-team
 * @since 1.0.0
 */
public record CrawlSchedulerPageResult(List<CrawlSchedulerResult> results, PageMeta pageMeta) {

    public CrawlSchedulerPageResult {
        results = results != null ? List.copyOf(results) : List.of();
    }

    public static CrawlSchedulerPageResult of(
            List<CrawlSchedulerResult> results, PageMeta pageMeta) {
        return new CrawlSchedulerPageResult(results, pageMeta);
    }

    public static CrawlSchedulerPageResult empty() {
        return new CrawlSchedulerPageResult(List.of(), PageMeta.empty());
    }

    public boolean isEmpty() {
        return results.isEmpty();
    }

    public int size() {
        return results.size();
    }
}
