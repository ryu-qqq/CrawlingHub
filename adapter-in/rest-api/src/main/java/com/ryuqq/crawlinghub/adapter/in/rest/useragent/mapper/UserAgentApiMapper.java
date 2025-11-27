package com.ryuqq.crawlinghub.adapter.in.rest.useragent.mapper;

import com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.response.RecoverUserAgentApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.response.UserAgentPoolStatusApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.response.UserAgentPoolStatusApiResponse.HealthScoreStatsApiResponse;
import com.ryuqq.crawlinghub.application.useragent.dto.response.UserAgentPoolStatusResponse;
import org.springframework.stereotype.Component;

/**
 * UserAgentApiMapper - UserAgent REST API ↔ Application Layer 변환
 *
 * <p>UserAgent 요청/응답에 대한 DTO 변환을 담당합니다.
 *
 * <p><strong>변환 방향:</strong>
 *
 * <ul>
 *   <li>Application Response → API Response (Application → Controller)
 * </ul>
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>필드 매핑만 수행 (비즈니스 로직 포함 금지)
 *   <li>Application DTO → API DTO 단순 변환
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class UserAgentApiMapper {

    /**
     * UserAgentPoolStatusResponse → UserAgentPoolStatusApiResponse 변환
     *
     * @param appResponse Application Layer Pool 상태 응답
     * @return REST API Pool 상태 응답
     */
    public UserAgentPoolStatusApiResponse toApiResponse(UserAgentPoolStatusResponse appResponse) {
        HealthScoreStatsApiResponse healthScoreStatsApi =
                new HealthScoreStatsApiResponse(
                        appResponse.healthScoreStats().avg(),
                        appResponse.healthScoreStats().min(),
                        appResponse.healthScoreStats().max());

        return new UserAgentPoolStatusApiResponse(
                appResponse.totalAgents(),
                appResponse.availableAgents(),
                appResponse.suspendedAgents(),
                appResponse.availableRate(),
                healthScoreStatsApi,
                appResponse.isCircuitBreakerOpen(),
                appResponse.isHealthy());
    }

    /**
     * 복구된 UserAgent 수 → RecoverUserAgentApiResponse 변환
     *
     * @param recoveredCount 복구된 UserAgent 수
     * @return REST API 복구 응답
     */
    public RecoverUserAgentApiResponse toRecoverApiResponse(int recoveredCount) {
        return RecoverUserAgentApiResponse.of(recoveredCount);
    }
}
