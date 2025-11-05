package com.ryuqq.crawlinghub.application.crawl.orchestration.port.out;

import com.ryuqq.crawlinghub.domain.task.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.CrawlTaskId;
import com.ryuqq.crawlinghub.domain.seller.MustitSellerId;

import java.util.List;
import java.util.Optional;

/**
 * 크롤링 태스크 조회 Port
 *
 * <p>Persistence Adapter에 의해 구현됩니다.
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public interface LoadCrawlTaskPort {

    /**
     * ID로 태스크 조회
     *
     * @param taskId 태스크 ID
     * @return 태스크 (없으면 Optional.empty())
     */
    Optional<CrawlTask> findById(CrawlTaskId taskId);

    /**
     * 셀러의 대기 중인 태스크 조회
     *
     * @param sellerId 셀러 ID
     * @return 대기 중인 태스크 목록
     */
    List<CrawlTask> findWaitingTasksBySellerId(MustitSellerId sellerId);
}
