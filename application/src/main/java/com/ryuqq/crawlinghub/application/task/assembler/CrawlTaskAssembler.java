package com.ryuqq.crawlinghub.application.task.assembler;

import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskPageResult;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskResult;
import com.ryuqq.crawlinghub.domain.common.vo.PageMeta;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * CrawlTask Assembler
 *
 * <p><strong>책임</strong>: Domain → Result 변환만 담당
 *
 * <p><strong>Command → Domain, Query → Criteria 변환은 Factory에서 담당</strong>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlTaskAssembler {

    /**
     * CrawlTask Aggregate → CrawlTaskResult 변환
     *
     * @param crawlTask CrawlTask Aggregate
     * @return CrawlTaskResult
     */
    public CrawlTaskResult toResult(CrawlTask crawlTask) {
        return CrawlTaskResult.from(crawlTask);
    }

    /**
     * CrawlTask 목록 → CrawlTaskResult 목록
     *
     * @param crawlTasks CrawlTask Aggregate 목록
     * @return CrawlTaskResult 목록
     */
    public List<CrawlTaskResult> toResults(List<CrawlTask> crawlTasks) {
        return crawlTasks.stream().map(this::toResult).toList();
    }

    /**
     * CrawlTask 목록 + 페이징 정보 → CrawlTaskPageResult 변환
     *
     * @param crawlTasks CrawlTask 목록
     * @param page 현재 페이지 번호
     * @param size 페이지 크기
     * @param totalElements 전체 데이터 개수
     * @return CrawlTaskPageResult
     */
    public CrawlTaskPageResult toPageResult(
            List<CrawlTask> crawlTasks, int page, int size, long totalElements) {
        List<CrawlTaskResult> results = toResults(crawlTasks);
        PageMeta pageMeta = PageMeta.of(page, size, totalElements);
        return CrawlTaskPageResult.of(results, pageMeta);
    }
}
