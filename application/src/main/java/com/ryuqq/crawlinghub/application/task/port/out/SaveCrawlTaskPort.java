package com.ryuqq.crawlinghub.application.crawl.orchestration.port.out;

import com.ryuqq.crawlinghub.domain.task.CrawlTask;

import java.util.List;

/**
 * 크롤링 태스크 저장 Port
 *
 * <p>Persistence Adapter에 의해 구현됩니다.
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public interface SaveCrawlTaskPort {

    /**
     * 태스크 저장 (신규 생성 또는 수정)
     *
     * @param task 저장할 태스크
     * @return 저장된 태스크 (ID 포함)
     */
    CrawlTask save(CrawlTask task);

    /**
     * 태스크 일괄 저장
     *
     * @param tasks 저장할 태스크 목록
     * @return 저장된 태스크 목록
     */
    List<CrawlTask> saveAll(List<CrawlTask> tasks);
}
