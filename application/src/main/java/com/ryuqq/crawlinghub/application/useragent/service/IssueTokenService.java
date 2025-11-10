package com.ryuqq.crawlinghub.application.useragent.service;

import com.ryuqq.crawlinghub.application.useragent.assembler.UserAgentAssembler;
import com.ryuqq.crawlinghub.application.useragent.dto.command.IssueTokenCommand;
import com.ryuqq.crawlinghub.application.useragent.dto.response.UserAgentResponse;
import com.ryuqq.crawlinghub.application.useragent.port.in.IssueTokenUseCase;
import com.ryuqq.crawlinghub.application.useragent.port.out.LoadUserAgentPort;
import com.ryuqq.crawlinghub.application.useragent.port.out.SaveUserAgentPort;
import com.ryuqq.crawlinghub.domain.token.Token;
import com.ryuqq.crawlinghub.domain.useragent.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.UserAgentId;
import com.ryuqq.crawlinghub.domain.useragent.exception.NoAvailableUserAgentException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 토큰 발급 UseCase 구현체
 *
 * <p>UserAgent에 새 토큰을 발급합니다.
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@Service
public class IssueTokenService implements IssueTokenUseCase {

    private final LoadUserAgentPort loadUserAgentPort;
    private final SaveUserAgentPort saveUserAgentPort;
    private final UserAgentAssembler assembler;

    public IssueTokenService(
        LoadUserAgentPort loadUserAgentPort,
        SaveUserAgentPort saveUserAgentPort,
        UserAgentAssembler assembler
    ) {
        this.loadUserAgentPort = loadUserAgentPort;
        this.saveUserAgentPort = saveUserAgentPort;
        this.assembler = assembler;
    }

    /**
     * 토큰 발급
     *
     * <p>트랜잭션 내에서:
     * 1. UserAgent 조회
     * 2. 도메인 객체로 변환
     * 3. 토큰 발급 (Domain 메서드 호출)
     * 4. 저장
     *
     * @param command 토큰 발급 Command
     * @return 토큰이 발급된 UserAgent 정보
     */
    @Override
    @Transactional
    public UserAgentResponse execute(IssueTokenCommand command) {
        // 1. UserAgent 조회 (DTO)
        UserAgentId userAgentId = UserAgentId.of(command.userAgentId());
        UserAgent userAgent = loadUserAgentPort.findById(userAgentId)
            .map(assembler::toDomain)
            .orElseThrow(NoAvailableUserAgentException::new);

        // 2. 토큰 발급 (Domain 메서드)
        Token token = Token.of(
            command.token(),
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(24)
        );
        userAgent.issueNewToken(token);

        // 3. 저장
        UserAgent savedUserAgent = saveUserAgentPort.save(userAgent);

        // 4. 응답 변환
        return UserAgentAssembler.toResponse(savedUserAgent);
    }
}

