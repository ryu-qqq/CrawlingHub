package com.ryuqq.crawlinghub.application.useragent.manager;

import com.ryuqq.crawlinghub.application.useragent.port.out.command.UserAgentPersistencePort;
import com.ryuqq.crawlinghub.domain.useragent.aggregate.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.identifier.UserAgentId;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * UserAgent Transaction Manager
 *
 * <p><strong>책임</strong>: UserAgent 영속성 작업 위임
 *
 * <p><strong>규칙</strong>: 단일 PersistencePort만 의존
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class UserAgentTransactionManager {

    private final UserAgentPersistencePort persistencePort;

    public UserAgentTransactionManager(UserAgentPersistencePort persistencePort) {
        this.persistencePort = persistencePort;
    }

    /**
     * UserAgent 저장
     *
     * @param userAgent 저장할 UserAgent
     * @return 저장된 UserAgent ID
     */
    public UserAgentId persist(UserAgent userAgent) {
        return persistencePort.persist(userAgent);
    }

    /**
     * 여러 UserAgent 저장 (배치 처리용)
     *
     * @param userAgents 저장할 UserAgent 목록
     */
    public void persistAll(List<UserAgent> userAgents) {
        persistencePort.persistAll(userAgents);
    }
}
