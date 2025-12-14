package com.ryuqq.crawlinghub.application.execution.factory.query;

import com.ryuqq.crawlinghub.application.execution.dto.query.ListCrawlExecutionsQuery;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlExecutionCriteria;
import com.ryuqq.crawlinghub.domain.schedule.identifier.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.task.identifier.CrawlTaskId;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.stereotype.Component;

/**
 * CrawlExecution QueryFactory
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
public class CrawlExecutionQueryFactory {

    /**
     * ListCrawlExecutionsQuery → CrawlExecutionCriteria 변환
     *
     * @param query 목록 조회 쿼리
     * @return Domain 조회 조건 객체
     */
    public CrawlExecutionCriteria createCriteria(ListCrawlExecutionsQuery query) {
        return new CrawlExecutionCriteria(
                query.crawlTaskId() != null ? CrawlTaskId.of(query.crawlTaskId()) : null,
                query.crawlSchedulerId() != null
                        ? CrawlSchedulerId.of(query.crawlSchedulerId())
                        : null,
                query.status(),
                toInstant(query.from()),
                toInstant(query.to()),
                query.page(),
                query.size());
    }

    private Instant toInstant(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant();
    }
}
