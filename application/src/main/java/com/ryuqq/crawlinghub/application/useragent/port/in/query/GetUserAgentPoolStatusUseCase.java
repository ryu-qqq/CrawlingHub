package com.ryuqq.crawlinghub.application.useragent.port.in.query;

import com.ryuqq.crawlinghub.application.useragent.dto.response.UserAgentPoolStatusResponse;

/**
 * UserAgent Pool 상태 조회 UseCase
 *
 * <p>Pool의 전체적인 상태를 조회합니다. 모니터링 및 Circuit Breaker 상태 확인에 사용됩니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface GetUserAgentPoolStatusUseCase {

    /**
     * Pool 상태 조회 실행
     *
     * @return Pool 상태 응답 (총 수, 가용 수, 정지 수, 가용률, Health Score 통계)
     */
    UserAgentPoolStatusResponse execute();
}
