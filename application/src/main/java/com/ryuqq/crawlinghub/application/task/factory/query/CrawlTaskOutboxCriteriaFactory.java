package com.ryuqq.crawlinghub.application.task.factory.query;

import com.ryuqq.crawlinghub.application.task.dto.query.GetOutboxListQuery;
import com.ryuqq.crawlinghub.domain.task.query.CrawlTaskOutboxCriteria;
import org.springframework.stereotype.Component;

/**
 * CrawlTaskOutbox Criteria Factory
 *
 * <p>GetOutboxListQuery → CrawlTaskOutboxCriteria 변환 담당
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>Query DTO → Domain Criteria 변환
 *   <li>페이징 offset 계산 (page * size)
 *   <li>기본값 적용 및 유효성 보장
 * </ul>
 *
 * <p><strong>변환 규칙</strong>:
 *
 * <ul>
 *   <li>statuses: Query의 상태 목록 → Criteria의 statuses (null이면 PENDING, FAILED 기본값)
 *   <li>createdFrom/createdTo: 기간 필터 그대로 전달
 *   <li>offset: page * size로 계산
 *   <li>limit: size 값 사용
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlTaskOutboxCriteriaFactory {

    /**
     * GetOutboxListQuery → CrawlTaskOutboxCriteria 변환
     *
     * @param query Outbox 목록 조회 Query
     * @return CrawlTaskOutboxCriteria (Domain Criteria)
     */
    public CrawlTaskOutboxCriteria create(GetOutboxListQuery query) {
        return CrawlTaskOutboxCriteria.withDateRange(
                query.statuses(),
                query.createdFrom(),
                query.createdTo(),
                query.offset(),
                query.size());
    }
}
