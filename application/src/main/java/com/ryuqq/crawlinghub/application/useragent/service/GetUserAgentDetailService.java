package com.ryuqq.crawlinghub.application.useragent.service;

import com.ryuqq.crawlinghub.application.useragent.assembler.UserAgentAssembler;
import com.ryuqq.crawlinghub.application.useragent.dto.query.GetUserAgentDetailQuery;
import com.ryuqq.crawlinghub.application.useragent.dto.response.UserAgentResponse;
import com.ryuqq.crawlinghub.application.useragent.port.in.GetUserAgentDetailUseCase;
import com.ryuqq.crawlinghub.application.useragent.port.out.LoadUserAgentPort;
import com.ryuqq.crawlinghub.domain.useragent.UserAgentId;
import com.ryuqq.crawlinghub.domain.useragent.exception.NoAvailableUserAgentException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UserAgent 상세 조회 UseCase 구현체
 *
 * <p>UserAgent의 상세 정보를 조회합니다.
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@Service
public class GetUserAgentDetailService implements GetUserAgentDetailUseCase {

    private final LoadUserAgentPort loadUserAgentPort;

    public GetUserAgentDetailService(LoadUserAgentPort loadUserAgentPort) {
        this.loadUserAgentPort = loadUserAgentPort;
    }

    /**
     * UserAgent 상세 조회
     *
     * <p>읽기 전용 트랜잭션에서:
     * 1. UserAgent 조회 (DTO)
     * 2. Response DTO로 변환
     *
     * @param query UserAgent 상세 조회 Query
     * @return UserAgent 상세 정보
     */
    @Override
    @Transactional(readOnly = true)
    public UserAgentResponse execute(GetUserAgentDetailQuery query) {
        // 1. UserAgent 조회 (DTO)
        UserAgentId userAgentId = UserAgentId.of(query.userAgentId());
        return loadUserAgentPort.findById(userAgentId)
            .map(UserAgentAssembler::toResponse)
            .orElseThrow(NoAvailableUserAgentException::new);
    }
}

