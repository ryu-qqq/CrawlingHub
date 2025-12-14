package com.ryuqq.crawlinghub.application.task.factory.query;

import com.ryuqq.crawlinghub.application.task.dto.query.ListCrawlTasksQuery;
import com.ryuqq.crawlinghub.domain.schedule.identifier.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskCriteria;
import org.springframework.stereotype.Component;

/**
 * CrawlTask QueryFactory
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>Query → Criteria 변환
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
public class CrawlTaskQueryFactory {

    /**
     * ListCrawlTasksQuery → CrawlTaskCriteria 변환
     *
     * @param query 목록 조회 쿼리
     * @return Domain 조회 조건 객체
     */
    public CrawlTaskCriteria createCriteria(ListCrawlTasksQuery query) {
        return new CrawlTaskCriteria(
                CrawlSchedulerId.of(query.crawlSchedulerId()),
                query.status(),
                query.page(),
                query.size());
    }
}
