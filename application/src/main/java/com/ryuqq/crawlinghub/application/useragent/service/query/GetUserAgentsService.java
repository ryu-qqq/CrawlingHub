package com.ryuqq.crawlinghub.application.useragent.service.query;

import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.useragent.dto.query.UserAgentSearchCriteria;
import com.ryuqq.crawlinghub.application.useragent.dto.response.UserAgentSummaryResponse;
import com.ryuqq.crawlinghub.application.useragent.port.in.query.GetUserAgentsUseCase;
import com.ryuqq.crawlinghub.application.useragent.port.out.query.UserAgentQueryPort;
import org.springframework.stereotype.Service;

/**
 * UserAgent 목록 조회 Service
 *
 * <p>{@link GetUserAgentsUseCase} 구현체
 *
 * <p>UserAgent 목록을 페이징하여 조회합니다. 상태별 필터링을 지원합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class GetUserAgentsService implements GetUserAgentsUseCase {

    private final UserAgentQueryPort userAgentQueryPort;

    public GetUserAgentsService(UserAgentQueryPort userAgentQueryPort) {
        this.userAgentQueryPort = userAgentQueryPort;
    }

    @Override
    public PageResponse<UserAgentSummaryResponse> execute(UserAgentSearchCriteria criteria) {
        return userAgentQueryPort.findByCriteria(criteria);
    }
}
