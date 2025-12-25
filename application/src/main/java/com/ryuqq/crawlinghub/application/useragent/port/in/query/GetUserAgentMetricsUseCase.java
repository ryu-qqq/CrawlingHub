package com.ryuqq.crawlinghub.application.useragent.port.in.query;

import com.ryuqq.crawlinghub.application.useragent.dto.response.UserAgentMetricsResponse;

/**
 * UserAgent Metrics 조회 UseCase
 *
 * <p>UserAgent Pool의 메트릭 정보를 조회합니다.
 *
 * <p><strong>제공 정보</strong>:
 *
 * <ul>
 *   <li>Pool 통계 (total, available, suspended)
 *   <li>Health Score 분포
 *   <li>Circuit Breaker 상태
 *   <li>상태별 UserAgent 수
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public interface GetUserAgentMetricsUseCase {

    /**
     * UserAgent Metrics 조회
     *
     * @return UserAgent Metrics 정보
     */
    UserAgentMetricsResponse execute();
}
