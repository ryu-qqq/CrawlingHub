package com.ryuqq.crawlinghub.application.useragent.service.command;

import com.ryuqq.crawlinghub.application.useragent.dto.command.RegisterUserAgentCommand;
import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentTransactionManager;
import com.ryuqq.crawlinghub.application.useragent.port.in.command.RegisterUserAgentUseCase;
import com.ryuqq.crawlinghub.application.useragent.port.out.command.TokenGeneratorPort;
import com.ryuqq.crawlinghub.domain.common.util.ClockHolder;
import com.ryuqq.crawlinghub.domain.useragent.aggregate.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.identifier.UserAgentId;
import com.ryuqq.crawlinghub.domain.useragent.vo.Token;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UserAgent 등록 Service
 *
 * <p>{@link RegisterUserAgentUseCase} 구현체
 *
 * <p><strong>처리 순서</strong>:
 *
 * <ol>
 *   <li>암호화된 토큰 생성 (TokenGeneratorPort)
 *   <li>User-Agent 문자열로 Value Object 생성
 *   <li>UserAgent Domain 객체 생성 (forNew 팩토리 메서드 사용)
 *   <li>DB 저장 (PersistencePort)
 * </ol>
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class RegisterUserAgentService implements RegisterUserAgentUseCase {

    private static final Logger log = LoggerFactory.getLogger(RegisterUserAgentService.class);

    private final TokenGeneratorPort tokenGeneratorPort;
    private final UserAgentTransactionManager transactionManager;
    private final ClockHolder clockHolder;

    public RegisterUserAgentService(
            TokenGeneratorPort tokenGeneratorPort,
            UserAgentTransactionManager transactionManager,
            ClockHolder clockHolder) {
        this.tokenGeneratorPort = tokenGeneratorPort;
        this.transactionManager = transactionManager;
        this.clockHolder = clockHolder;
    }

    /**
     * UserAgent 등록
     *
     * <p>새로운 UserAgent를 생성하고 DB에 저장합니다.
     *
     * @param command 등록 Command (User-Agent 문자열, 디바이스 타입, 메타데이터 포함)
     * @return 등록된 UserAgent ID
     */
    @Override
    @Transactional
    public Long register(RegisterUserAgentCommand command) {
        log.info(
                "UserAgent 등록 시작: userAgentString={}",
                truncateUserAgentString(command.userAgentString()));

        Token token = tokenGeneratorPort.generate();
        UserAgentString userAgentString = UserAgentString.of(command.userAgentString());

        UserAgent userAgent = UserAgent.forNew(token, userAgentString, clockHolder.getClock());

        UserAgentId persistedId = transactionManager.persist(userAgent);

        log.info("UserAgent 등록 완료: id={}", persistedId.value());

        return persistedId.value();
    }

    /**
     * 로깅용 User-Agent 문자열 축약
     *
     * @param userAgentString User-Agent 문자열
     * @return 50자로 축약된 문자열
     */
    private String truncateUserAgentString(String userAgentString) {
        if (userAgentString == null) {
            return "null";
        }
        if (userAgentString.length() <= 50) {
            return userAgentString;
        }
        return userAgentString.substring(0, 50) + "...";
    }
}
