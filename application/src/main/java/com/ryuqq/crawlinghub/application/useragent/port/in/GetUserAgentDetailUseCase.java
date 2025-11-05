package com.ryuqq.crawlinghub.application.useragent.port.in;

import com.ryuqq.crawlinghub.application.useragent.dto.query.GetUserAgentDetailQuery;
import com.ryuqq.crawlinghub.application.useragent.dto.response.UserAgentResponse;

/**
 * UserAgent 상세 조회 UseCase
 *
 * <p>UserAgent의 상세 정보를 조회합니다.
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
public interface GetUserAgentDetailUseCase {

    /**
     * UserAgent 상세 조회
     *
     * @param query UserAgent 상세 조회 Query
     * @return UserAgent 상세 정보
     */
    UserAgentResponse execute(GetUserAgentDetailQuery query);
}

