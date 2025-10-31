package com.ryuqq.crawlinghub.application.monitoring.port.in;

import com.ryuqq.crawlinghub.application.monitoring.dto.query.CrawlingStatsQuery;
import com.ryuqq.crawlinghub.application.monitoring.dto.response.CrawlingStatsResponse;

/**
 * 크롤링 통계 계산 UseCase
 *
 * <p>태스크 및 상품 크롤링 통계를 계산합니다.
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public interface CalculateCrawlingStatsUseCase {

    /**
     * 크롤링 통계 계산
     *
     * @param query 통계 조회 Query
     * @return 크롤링 통계
     */
    CrawlingStatsResponse execute(CrawlingStatsQuery query);
}
