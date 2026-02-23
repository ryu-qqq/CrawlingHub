package com.ryuqq.crawlinghub.application.task.dto.response;

import com.ryuqq.crawlinghub.domain.common.vo.PageMeta;
import java.util.List;

/**
 * CrawlTask 페이지 조회 결과 (PageMeta 기반)
 *
 * @param results 태스크 결과 목록
 * @param pageMeta 페이지 메타 정보
 * @author development-team
 * @since 1.0.0
 */
public record CrawlTaskPageResult(List<CrawlTaskResult> results, PageMeta pageMeta) {

    public CrawlTaskPageResult {
        results = results != null ? List.copyOf(results) : List.of();
    }

    public static CrawlTaskPageResult of(List<CrawlTaskResult> results, PageMeta pageMeta) {
        return new CrawlTaskPageResult(results, pageMeta);
    }

    public static CrawlTaskPageResult empty() {
        return new CrawlTaskPageResult(List.of(), PageMeta.empty());
    }

    public boolean isEmpty() {
        return results.isEmpty();
    }

    public int size() {
        return results.size();
    }
}
