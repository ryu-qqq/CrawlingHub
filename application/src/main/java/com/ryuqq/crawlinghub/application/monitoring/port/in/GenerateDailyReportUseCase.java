package com.ryuqq.crawlinghub.application.monitoring.port.in;

import com.ryuqq.crawlinghub.application.monitoring.dto.query.DailyReportQuery;
import com.ryuqq.crawlinghub.application.monitoring.dto.response.DailyReportResponse;

/**
 * 일일 리포트 생성 UseCase
 *
 * <p>전체 시스템의 일일 크롤링 리포트를 생성합니다.
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public interface GenerateDailyReportUseCase {

    /**
     * 일일 리포트 생성
     *
     * @param query 리포트 조회 Query
     * @return 일일 리포트
     */
    DailyReportResponse execute(DailyReportQuery query);
}
