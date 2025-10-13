package com.ryuqq.crawlinghub.adapter.persistence.jpa.token;

import com.ryuqq.crawlinghub.adapter.persistence.jpa.common.BaseTimeEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * User-Agent Token Entity
 * User-Agent와 토큰 매핑 및 라이프사이클 관리
 *
 * @author crawlinghub
 */
@Entity
@Table(name = "user_agent_token")
public class UserAgentTokenEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Long tokenId;

    @Column(name = "agent_id", nullable = false)
    private Long agentId;

    @Column(name = "token_value", nullable = false, length = 1000, unique = true)
    private String tokenValue;

    @Column(name = "token_type", nullable = false, length = 50)
    private String tokenType = "BEARER";

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "refresh_count", nullable = false)
    private int refreshCount = 0;

    @Column(name = "last_refreshed_at")
    private LocalDateTime lastRefreshedAt;

    protected UserAgentTokenEntity() {
    }

    public UserAgentTokenEntity(
            Long agentId,
            String tokenValue,
            LocalDateTime issuedAt,
            LocalDateTime expiresAt) {
        this.agentId = agentId;
        this.tokenValue = tokenValue;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
    }

    /**
     * 토큰 갱신
     */
    public void refresh(String newTokenValue, LocalDateTime newExpiresAt) {
        this.tokenValue = newTokenValue;
        this.expiresAt = newExpiresAt;
        this.lastRefreshedAt = LocalDateTime.now();
        this.refreshCount++;
    }

    /**
     * 토큰 만료 여부 확인
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * 토큰 비활성화
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * 토큰 활성화
     */
    public void activate() {
        this.isActive = true;
    }

    public Long getTokenId() {
        return tokenId;
    }

    public Long getAgentId() {
        return agentId;
    }

    public String getTokenValue() {
        return tokenValue;
    }

    public String getTokenType() {
        return tokenType;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public boolean isActive() {
        return isActive;
    }

    public int getRefreshCount() {
        return refreshCount;
    }

    public LocalDateTime getLastRefreshedAt() {
        return lastRefreshedAt;
    }
}
