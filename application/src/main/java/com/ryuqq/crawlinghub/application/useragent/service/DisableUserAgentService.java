package com.ryuqq.crawlinghub.application.useragent.service;

import com.ryuqq.crawlinghub.application.useragent.assembler.UserAgentAssembler;
import com.ryuqq.crawlinghub.application.useragent.dto.command.DisableUserAgentCommand;
import com.ryuqq.crawlinghub.application.useragent.dto.response.UserAgentResponse;
import com.ryuqq.crawlinghub.application.useragent.port.in.DisableUserAgentUseCase;
import com.ryuqq.crawlinghub.application.useragent.port.out.LoadUserAgentPort;
import com.ryuqq.crawlinghub.application.useragent.port.out.SaveUserAgentPort;
import com.ryuqq.crawlinghub.domain.useragent.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.UserAgentId;
import com.ryuqq.crawlinghub.domain.useragent.exception.NoAvailableUserAgentException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UserAgent 비활성화 UseCase 구현체
 *
 * <p>UserAgent를 비활성화합니다.
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@Service
public class DisableUserAgentService implements DisableUserAgentUseCase {

    private final LoadUserAgentPort loadUserAgentPort;
    private final SaveUserAgentPort saveUserAgentPort;
    private final UserAgentAssembler assembler;

    public DisableUserAgentService(
        LoadUserAgentPort loadUserAgentPort,
        SaveUserAgentPort saveUserAgentPort,
        UserAgentAssembler assembler
    ) {
        this.loadUserAgentPort = loadUserAgentPort;
        this.saveUserAgentPort = saveUserAgentPort;
        this.assembler = assembler;
    }

    /**
     * UserAgent 비활성화
     *
     * <p>트랜잭션 내에서:
     * 1. UserAgent 조회
     * 2. 도메인 객체로 변환
     * 3. 비활성화 (Domain 메서드 호출)
     * 4. 저장
     *
     * @param command UserAgent 비활성화 Command
     * @return 비활성화된 UserAgent 정보
     */
    @Override
    @Transactional
    public UserAgentResponse execute(DisableUserAgentCommand command) {
        // 1. UserAgent 조회 (DTO)
        UserAgentId userAgentId = UserAgentId.of(command.userAgentId());
        UserAgent userAgent = loadUserAgentPort.findById(userAgentId)
            .map(assembler::toDomain)
            .orElseThrow(NoAvailableUserAgentException::new);

        // 2. 비활성화 (Domain 메서드)
        userAgent.disable();

        // 3. 저장
        UserAgent savedUserAgent = saveUserAgentPort.save(userAgent);

        // 4. 응답 변환
        return UserAgentAssembler.toResponse(savedUserAgent);
    }
}

