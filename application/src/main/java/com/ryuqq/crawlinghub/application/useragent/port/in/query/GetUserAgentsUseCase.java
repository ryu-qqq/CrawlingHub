package com.ryuqq.crawlinghub.application.useragent.port.in.query;

import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.useragent.dto.query.UserAgentSearchCriteria;
import com.ryuqq.crawlinghub.application.useragent.dto.response.UserAgentSummaryResponse;

/**
 * UserAgent 목록 조회 UseCase
 *
 * <p>UserAgent 목록을 페이징하여 조회합니다. 상태별 필터링을 지원합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface GetUserAgentsUseCase {

    /**
     * UserAgent 목록 조회
     *
     * @param criteria 검색 조건 (상태 필터, 페이징)
     * @return 페이징된 UserAgent 목록
     */
    PageResponse<UserAgentSummaryResponse> execute(UserAgentSearchCriteria criteria);
}
