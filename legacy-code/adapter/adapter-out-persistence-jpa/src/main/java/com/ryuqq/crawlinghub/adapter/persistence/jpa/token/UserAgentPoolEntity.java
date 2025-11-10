package com.ryuqq.crawlinghub.adapter.persistence.jpa.token;

import com.ryuqq.crawlinghub.adapter.persistence.jpa.common.BaseTimeEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * User-Agent Pool Entity
 * 100개 User-Agent 동시 운영으로 처리량 극대화
 *
 * @author crawlinghub
 */
@Entity
@Table(name = "user_agent_pool")
public class UserAgentPoolEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "agent_id")
    private Long agentId;

    @Column(name = "user_agent", nullable = false, length = 500, unique = true)
    private String userAgent;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "is_blocked", nullable = false)
    private boolean isBlocked = false;

    @Column(name = "blocked_until")
    private LocalDateTime blockedUntil;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "usage_count", nullable = false)
    private int usageCount = 0;

    @Column(name = "success_count", nullable = false)
    private int successCount = 0;

    @Column(name = "failure_count", nullable = false)
    private int failureCount = 0;

    protected UserAgentPoolEntity() {
    }

    public UserAgentPoolEntity(String userAgent) {
        this.userAgent = userAgent;
    }

    /**
     * User-Agent 사용 기록
     */
    public void recordUsage() {
        this.lastUsedAt = LocalDateTime.now();
        this.usageCount++;
    }

    /**
     * 성공 기록
     */
    public void recordSuccess() {
        this.successCount++;
    }

    /**
     * 실패 기록
     */
    public void recordFailure() {
        this.failureCount++;
    }

    /**
     * 차단 설정
     */
    public void block(LocalDateTime until) {
        this.isBlocked = true;
        this.blockedUntil = until;
    }

    /**
     * 차단 해제
     */
    public void unblock() {
        this.isBlocked = false;
        this.blockedUntil = null;
    }

    /**
     * 활성화 여부 변경
     */
    public void setActive(boolean active) {
        this.isActive = active;
    }

    /**
     * 차단 만료 여부 확인
     */
    public boolean isBlockExpired() {
        if (!isBlocked || blockedUntil == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(blockedUntil);
    }

    public Long getAgentId() {
        return agentId;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public LocalDateTime getBlockedUntil() {
        return blockedUntil;
    }

    public LocalDateTime getLastUsedAt() {
        return lastUsedAt;
    }

    public int getUsageCount() {
        return usageCount;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getFailureCount() {
        return failureCount;
    }
}
