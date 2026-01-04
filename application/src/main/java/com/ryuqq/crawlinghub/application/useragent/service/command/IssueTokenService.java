package com.ryuqq.crawlinghub.application.useragent.service.command;

import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentTransactionManager;
import com.ryuqq.crawlinghub.application.useragent.manager.query.UserAgentReadManager;
import com.ryuqq.crawlinghub.application.useragent.port.in.command.IssueTokenUseCase;
import com.ryuqq.crawlinghub.application.useragent.port.out.command.TokenGeneratorPort;
import com.ryuqq.crawlinghub.domain.common.util.ClockHolder;
import com.ryuqq.crawlinghub.domain.useragent.aggregate.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.exception.UserAgentNotFoundException;
import com.ryuqq.crawlinghub.domain.useragent.identifier.UserAgentId;
import com.ryuqq.crawlinghub.domain.useragent.vo.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 토큰 발급 Service (Lazy Token Issuance)
 *
 * <p>{@link IssueTokenUseCase} 구현체
 *
 * <p><strong>처리 순서</strong>:
 *
 * <ol>
 *   <li>UserAgent 조회 (DB)
 *   <li>토큰 미발급 상태 확인
 *   <li>암호화된 토큰 생성 (TokenGeneratorPort)
 *   <li>UserAgent에 토큰 발급 (Domain 로직)
 *   <li>DB 저장
 * </ol>
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class IssueTokenService implements IssueTokenUseCase {

    private static final Logger log = LoggerFactory.getLogger(IssueTokenService.class);

    private final UserAgentReadManager readManager;
    private final UserAgentTransactionManager transactionManager;
    private final TokenGeneratorPort tokenGeneratorPort;
    private final ClockHolder clockHolder;

    public IssueTokenService(
            UserAgentReadManager readManager,
            UserAgentTransactionManager transactionManager,
            TokenGeneratorPort tokenGeneratorPort,
            ClockHolder clockHolder) {
        this.readManager = readManager;
        this.transactionManager = transactionManager;
        this.tokenGeneratorPort = tokenGeneratorPort;
        this.clockHolder = clockHolder;
    }

    /**
     * UserAgent에 토큰 발급
     *
     * @param userAgentId 토큰을 발급할 UserAgent ID
     * @return 발급된 토큰의 암호화 값
     * @throws UserAgentNotFoundException UserAgent를 찾을 수 없는 경우
     * @throws IllegalStateException 이미 토큰이 발급된 경우
     */
    @Override
    @Transactional
    public String issueToken(Long userAgentId) {
        log.info("토큰 발급 시작: userAgentId={}", userAgentId);

        UserAgentId id = UserAgentId.of(userAgentId);

        // 1. DB에서 UserAgent 조회
        UserAgent userAgent =
                readManager.findById(id).orElseThrow(() -> new UserAgentNotFoundException(id));

        // 2. 이미 토큰이 있는지 확인
        if (userAgent.hasToken()) {
            throw new IllegalStateException("이미 토큰이 발급된 UserAgent입니다: userAgentId=" + userAgentId);
        }

        // 3. 새 토큰 생성
        Token token = tokenGeneratorPort.generate();

        // 4. Domain 로직 실행 (토큰 발급)
        userAgent.issueToken(token, clockHolder.getClock());

        // 5. DB 저장
        transactionManager.persist(userAgent);

        log.info("토큰 발급 완료: userAgentId={}", userAgentId);

        return token.encryptedValue();
    }
}
