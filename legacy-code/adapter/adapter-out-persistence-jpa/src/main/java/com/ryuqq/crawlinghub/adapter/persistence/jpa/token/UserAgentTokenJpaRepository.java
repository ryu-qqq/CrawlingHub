package com.ryuqq.crawlinghub.adapter.persistence.jpa.token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * User-Agent Token JPA Repository
 *
 * @author crawlinghub
 */
public interface UserAgentTokenJpaRepository extends JpaRepository<UserAgentTokenEntity, Long> {

    /**
     * Agent ID로 활성화된 토큰 조회
     */
    @Query("SELECT t FROM UserAgentTokenEntity t " +
           "WHERE t.agentId = :agentId " +
           "AND t.isActive = true " +
           "ORDER BY t.expiresAt DESC")
    Optional<UserAgentTokenEntity> findActiveTokenByAgentId(@Param("agentId") Long agentId);

    /**
     * Agent ID로 모든 토큰 조회
     */
    List<UserAgentTokenEntity> findAllByAgentId(Long agentId);

    /**
     * 만료된 토큰 조회
     */
    @Query("SELECT t FROM UserAgentTokenEntity t " +
           "WHERE t.isActive = true " +
           "AND t.expiresAt < :now")
    List<UserAgentTokenEntity> findExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Token Value로 조회
     */
    Optional<UserAgentTokenEntity> findByTokenValue(String tokenValue);

    /**
     * Agent ID 목록으로 활성화된 토큰 조회
     */
    @Query("SELECT t FROM UserAgentTokenEntity t " +
           "WHERE t.agentId IN :agentIds " +
           "AND t.isActive = true")
    List<UserAgentTokenEntity> findActiveTokensByAgentIds(@Param("agentIds") List<Long> agentIds);
}
