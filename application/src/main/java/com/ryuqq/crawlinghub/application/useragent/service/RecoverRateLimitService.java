package com.ryuqq.crawlinghub.application.useragent.service;

import com.ryuqq.crawlinghub.application.useragent.assembler.UserAgentAssembler;
import com.ryuqq.crawlinghub.application.useragent.dto.command.RecoverRateLimitCommand;
import com.ryuqq.crawlinghub.application.useragent.dto.response.UserAgentResponse;
import com.ryuqq.crawlinghub.application.useragent.port.in.RecoverRateLimitUseCase;
import com.ryuqq.crawlinghub.application.useragent.port.out.LoadUserAgentPort;
import com.ryuqq.crawlinghub.application.useragent.port.out.SaveUserAgentPort;
import com.ryuqq.crawlinghub.domain.useragent.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.UserAgentId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Rate Limit 복구 UseCase 구현체
 *
 * <p>UserAgent의 Rate Limit을 복구합니다.
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@Service
public class RecoverRateLimitService implements RecoverRateLimitUseCase {

    private final LoadUserAgentPort loadUserAgentPort;
    private final SaveUserAgentPort saveUserAgentPort;
    private final UserAgentAssembler assembler;

    public RecoverRateLimitService(
        LoadUserAgentPort loadUserAgentPort,
        SaveUserAgentPort saveUserAgentPort,
        UserAgentAssembler assembler
    ) {
        this.loadUserAgentPort = loadUserAgentPort;
        this.saveUserAgentPort = saveUserAgentPort;
        this.assembler = assembler;
    }

    /**
     * Rate Limit 복구
     *
     * <p>트랜잭션 내에서:
     * 1. UserAgent 조회
     * 2. 도메인 객체로 변환
     * 3. Rate Limit 복구 (Domain 메서드 호출)
     * 4. 저장
     *
     * @param command Rate Limit 복구 Command
     * @return 복구된 UserAgent 정보
     */
    @Override
    @Transactional
    public UserAgentResponse execute(RecoverRateLimitCommand command) {
        // 1. UserAgent 조회 (DTO)
        UserAgentId userAgentId = UserAgentId.of(command.userAgentId());
        UserAgent userAgent = loadUserAgentPort.findById(userAgentId)
            .map(assembler::toDomain)
            .orElseThrow(() -> new com.ryuqq.crawlinghub.domain.useragent.exception.NoAvailableUserAgentException());

        // 2. Rate Limit 복구 (Domain 메서드)
        userAgent.recoverFromRateLimit();

        // 3. 저장
        UserAgent savedUserAgent = saveUserAgentPort.save(userAgent);

        // 4. 응답 변환
        return UserAgentAssembler.toResponse(savedUserAgent);
    }
}

