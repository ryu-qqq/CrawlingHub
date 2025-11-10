package com.ryuqq.crawlinghub.adapter.persistence.jpa.token;

import com.ryuqq.crawlinghub.application.token.port.UserAgentInfo;
import com.ryuqq.crawlinghub.application.token.port.UserAgentTokenPort;
import org.springframework.stereotype.Component;

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
    public void recordUsage(Long userAgentId) {
        poolRepository.findById(userAgentId)
                .ifPresent(entity -> {
                    entity.recordUsage();
                    poolRepository.save(entity);
                });
    }

    @Override
    public void recordSuccess(Long userAgentId) {
        poolRepository.findById(userAgentId)
                .ifPresent(entity -> {
                    entity.recordSuccess();
                    poolRepository.save(entity);
                });
    }

    @Override
    public void recordFailure(Long userAgentId) {
        poolRepository.findById(userAgentId)
                .ifPresent(entity -> {
                    entity.recordFailure();
                    poolRepository.save(entity);
                });
    }

    @Override
    public void saveOrUpdateToken(Long userAgentId, String tokenValue, String tokenType, long expiresIn) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusSeconds(expiresIn);

        UserAgentTokenEntity existingToken = tokenRepository.findActiveTokenByAgentId(userAgentId)
                .orElse(null);

        if (existingToken != null) {
            // 기존 토큰 업데이트
            existingToken.refresh(tokenValue, expiresAt);
            tokenRepository.save(existingToken);
        } else {
            // 신규 토큰 생성
            UserAgentTokenEntity newToken = new UserAgentTokenEntity(
                    userAgentId,
                    tokenValue,
                    now,
                    expiresAt
            );
            tokenRepository.save(newToken);
        }
    }

    @Override
    public List<Long> findAllActiveUserAgents() {
        return poolRepository.findAllActiveAndUnblocked(LocalDateTime.now())
                .stream()
                .map(UserAgentPoolEntity::getAgentId)
                .collect(Collectors.toList());
    }
}
