package com.ryuqq.crawlinghub.adapter.persistence.jpa.token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * User-Agent Pool JPA Repository
 *
 * @author crawlinghub
 */
public interface UserAgentPoolJpaRepository extends JpaRepository<UserAgentPoolEntity, Long> {

    /**
     * User-Agent 문자열로 조회
     */
    Optional<UserAgentPoolEntity> findByUserAgent(String userAgent);

    /**
     * 활성화되고 차단되지 않은 모든 User-Agent 조회
     */
    @Query("SELECT u FROM UserAgentPoolEntity u " +
           "WHERE u.isActive = true " +
           "AND (u.isBlocked = false OR (u.isBlocked = true AND u.blockedUntil < :now))")
    List<UserAgentPoolEntity> findAllActiveAndUnblocked(@Param("now") LocalDateTime now);

    /**
     * 활성화된 모든 User-Agent 조회
     */
    List<UserAgentPoolEntity> findAllByIsActiveTrue();

    /**
     * 차단된 User-Agent 중 차단 만료된 것들 조회
     */
    @Query("SELECT u FROM UserAgentPoolEntity u " +
           "WHERE u.isBlocked = true " +
           "AND u.blockedUntil < :now")
    List<UserAgentPoolEntity> findExpiredBlocked(@Param("now") LocalDateTime now);

    /**
     * User-Agent ID 목록으로 조회
     */
    List<UserAgentPoolEntity> findAllByAgentIdIn(List<Long> agentIds);
}
