package com.ryuqq.crawlinghub.adapter.persistence.jpa.token;

import com.ryuqq.crawlinghub.application.token.port.UserAgentInfo;
import com.ryuqq.crawlinghub.application.token.port.UserAgentTokenPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User-Agent Token Adapter
 * UserAgentTokenPort| JPA\ l
 *
 * @author crawlinghub
 */
@Component
public class UserAgentTokenAdapter implements UserAgentTokenPort {

    private final UserAgentPoolJpaRepository poolRepository;
    private final UserAgentTokenJpaRepository tokenRepository;

    public UserAgentTokenAdapter(
            UserAgentPoolJpaRepository poolRepository,
            UserAgentTokenJpaRepository tokenRepository) {
        this.poolRepository = poolRepository;
        this.tokenRepository = tokenRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserAgentInfo findActiveToken(Long userAgentId) {
        UserAgentPoolEntity poolEntity = poolRepository.findById(userAgentId)
                .orElse(null);

        if (poolEntity == null || !poolEntity.isActive()) {
            return null;
        }

        UserAgentTokenEntity tokenEntity = tokenRepository.findActiveTokenByAgentId(userAgentId)
                .orElse(null);

        if (tokenEntity == null) {
            return null;
        }

        return new UserAgentInfo(
                poolEntity.getAgentId(),
                poolEntity.getUserAgent(),
                tokenEntity.getTokenId(),
                tokenEntity.getTokenValue(),
                tokenEntity.getExpiresAt()
        );
    }

    @Override
    @Transactional
    public void recordUsage(Long userAgentId) {
        poolRepository.findById(userAgentId)
                .ifPresent(entity -> {
                    entity.recordUsage();
                    poolRepository.save(entity);
                });
    }

    @Override
    @Transactional
    public void recordSuccess(Long userAgentId) {
        poolRepository.findById(userAgentId)
                .ifPresent(entity -> {
                    entity.recordSuccess();
                    poolRepository.save(entity);
                });
    }

    @Override
    @Transactional
    public void recordFailure(Long userAgentId) {
        poolRepository.findById(userAgentId)
                .ifPresent(entity -> {
                    entity.recordFailure();
                    poolRepository.save(entity);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> findAllActiveUserAgents() {
        return poolRepository.findAllActiveAndUnblocked(LocalDateTime.now())
                .stream()
                .map(UserAgentPoolEntity::getAgentId)
                .collect(Collectors.toList());
    }
}
