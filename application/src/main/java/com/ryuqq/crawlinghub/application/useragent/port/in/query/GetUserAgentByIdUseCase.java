package com.ryuqq.crawlinghub.application.useragent.port.in.query;

import com.ryuqq.crawlinghub.application.useragent.dto.response.UserAgentDetailResponse;

/**
 * 개별 UserAgent 상세 조회 UseCase
 *
 * <p>특정 UserAgent의 상세 정보를 조회합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface GetUserAgentByIdUseCase {

    /**
     * UserAgent ID로 상세 정보 조회
     *
     * @param userAgentId UserAgent ID
     * @return UserAgent 상세 정보
     * @throws com.ryuqq.crawlinghub.domain.useragent.exception.UserAgentNotFoundException
     *     UserAgent가 없는 경우
     */
    UserAgentDetailResponse execute(long userAgentId);
}
