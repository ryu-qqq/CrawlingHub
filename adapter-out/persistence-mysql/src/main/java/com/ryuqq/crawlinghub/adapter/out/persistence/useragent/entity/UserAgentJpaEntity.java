package com.ryuqq.crawlinghub.adapter.out.persistence.useragent.entity;

import com.ryuqq.crawlinghub.adapter.out.persistence.entity.BaseAuditEntity;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * UserAgentJpaEntity - UserAgent JPA Entity
 *
 * <p>Persistence Layer의 JPA Entity로서 user_agent 테이블과 매핑됩니다.
 *
 * <p><strong>BaseAuditEntity 상속:</strong>
 *
 * <ul>
 *   <li>공통 감사 필드 상속: createdAt, updatedAt
 *   <li>시간 정보는 Domain에서 관리하여 전달
 * </ul>
 *
 * <p><strong>Long FK 전략:</strong>
 *
 * <ul>
 *   <li>JPA 관계 어노테이션 사용 금지 (@ManyToOne, @OneToMany 등)
 *   <li>모든 외래키는 Long 타입으로 직접 관리
 * </ul>
 *
 * <p><strong>Lombok 금지:</strong>
 *
 * <ul>
 *   <li>Plain Java getter 사용
 *   <li>Setter 제공 금지
 *   <li>명시적 생성자 제공
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Entity
@Table(name = "user_agent")
public class UserAgentJpaEntity extends BaseAuditEntity {

    /** 기본 키 - AUTO_INCREMENT */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 암호화된 토큰 (AES-256 Base64)
     *
     * <p>복호화 불가, 암호화 상태로만 저장/비교
     *
     * <p><strong>Lazy Token Issuance:</strong> nullable 허용 (토큰 미발급 상태 지원)
     */
    @Column(name = "token", nullable = true, length = 500)
    private String token;

    /** User-Agent 헤더 문자열 (실제 User-Agent 값) */
    @Column(name = "user_agent_string", nullable = false, length = 500)
    private String userAgentString;

    /** 디바이스 타입 (MOBILE/TABLET/DESKTOP) */
    @Column(name = "device_type", nullable = false, length = 20)
    private String deviceType;

    /** 디바이스 브랜드 (IPHONE/SAMSUNG/PIXEL/XIAOMI/HUAWEI/OPPO/ONEPLUS/IPAD/GALAXY_TAB/GENERIC) */
    @Column(name = "device_brand", nullable = false, length = 30)
    private String deviceBrand;

    /** OS 타입 (WINDOWS/MACOS/LINUX/IOS/ANDROID/CHROME_OS) */
    @Column(name = "os_type", nullable = false, length = 20)
    private String osType;

    /** OS 버전 (nullable, 예: 17.0, 10.15.7, 14) */
    @Column(name = "os_version", length = 50)
    private String osVersion;

    /** 브라우저 타입 (CHROME/SAFARI/FIREFOX/EDGE/OPERA/SAMSUNG_BROWSER) */
    @Column(name = "browser_type", nullable = false, length = 30)
    private String browserType;

    /** 브라우저 버전 (nullable, 예: 120.0.0.0, 17.0) */
    @Column(name = "browser_version", length = 50)
    private String browserVersion;

    /** UserAgent 상태 (IDLE/BORROWED/COOLDOWN/SESSION_REQUIRED/SUSPENDED/BLOCKED) */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserAgentStatus status;

    /** Health Score (0-100) */
    @Column(name = "health_score", nullable = false)
    private int healthScore;

    /** 마지막 사용 시각 (nullable) */
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    /** 일일 요청 수 */
    @Column(name = "requests_per_day", nullable = false)
    private int requestsPerDay;

    /** 쿨다운 만료 시각 (COOLDOWN 상태에서 사용, nullable) */
    @Column(name = "cooldown_until")
    private LocalDateTime cooldownUntil;

    /** 연속 429 횟수 (Graduated Backoff용, default 0) */
    @Column(name = "consecutive_rate_limits", nullable = false)
    private int consecutiveRateLimits;

    /**
     * JPA 기본 생성자 (protected)
     *
     * <p>JPA 스펙 요구사항으로 반드시 필요합니다.
     */
    protected UserAgentJpaEntity() {}

    /**
     * 전체 필드 생성자 (private)
     *
     * <p>직접 호출 금지, of() 스태틱 메서드로만 생성하세요.
     *
     * @param id 기본 키
     * @param token 암호화된 토큰
     * @param userAgentString User-Agent 헤더 문자열
     * @param deviceType 디바이스 타입
     * @param deviceBrand 디바이스 브랜드
     * @param osType OS 타입
     * @param osVersion OS 버전 (nullable)
     * @param browserType 브라우저 타입
     * @param browserVersion 브라우저 버전 (nullable)
     * @param status UserAgent 상태
     * @param healthScore Health Score
     * @param lastUsedAt 마지막 사용 시각
     * @param requestsPerDay 일일 요청 수
     * @param cooldownUntil 쿨다운 만료 시각 (nullable)
     * @param consecutiveRateLimits 연속 429 횟수
     * @param createdAt 생성 일시
     * @param updatedAt 수정 일시
     */
    private UserAgentJpaEntity(
            Long id,
            String token,
            String userAgentString,
            String deviceType,
            String deviceBrand,
            String osType,
            String osVersion,
            String browserType,
            String browserVersion,
            UserAgentStatus status,
            int healthScore,
            LocalDateTime lastUsedAt,
            int requestsPerDay,
            LocalDateTime cooldownUntil,
            int consecutiveRateLimits,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        super(createdAt, updatedAt);
        this.id = id;
        this.token = token;
        this.userAgentString = userAgentString;
        this.deviceType = deviceType;
        this.deviceBrand = deviceBrand;
        this.osType = osType;
        this.osVersion = osVersion;
        this.browserType = browserType;
        this.browserVersion = browserVersion;
        this.status = status;
        this.healthScore = healthScore;
        this.lastUsedAt = lastUsedAt;
        this.requestsPerDay = requestsPerDay;
        this.cooldownUntil = cooldownUntil;
        this.consecutiveRateLimits = consecutiveRateLimits;
    }

    /**
     * of() 스태틱 팩토리 메서드 (Mapper 전용)
     *
     * <p>Entity 생성은 반드시 이 메서드를 통해서만 가능합니다.
     *
     * <p>Mapper에서 Domain → Entity 변환 시 사용합니다.
     *
     * @param id 기본 키
     * @param token 암호화된 토큰
     * @param userAgentString User-Agent 헤더 문자열
     * @param deviceType 디바이스 타입
     * @param deviceBrand 디바이스 브랜드
     * @param osType OS 타입
     * @param osVersion OS 버전 (nullable)
     * @param browserType 브라우저 타입
     * @param browserVersion 브라우저 버전 (nullable)
     * @param status UserAgent 상태
     * @param healthScore Health Score
     * @param lastUsedAt 마지막 사용 시각
     * @param requestsPerDay 일일 요청 수
     * @param cooldownUntil 쿨다운 만료 시각 (nullable)
     * @param consecutiveRateLimits 연속 429 횟수
     * @param createdAt 생성 일시
     * @param updatedAt 수정 일시
     * @return UserAgentJpaEntity 인스턴스
     */
    public static UserAgentJpaEntity of(
            Long id,
            String token,
            String userAgentString,
            String deviceType,
            String deviceBrand,
            String osType,
            String osVersion,
            String browserType,
            String browserVersion,
            UserAgentStatus status,
            int healthScore,
            LocalDateTime lastUsedAt,
            int requestsPerDay,
            LocalDateTime cooldownUntil,
            int consecutiveRateLimits,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        return new UserAgentJpaEntity(
                id,
                token,
                userAgentString,
                deviceType,
                deviceBrand,
                osType,
                osVersion,
                browserType,
                browserVersion,
                status,
                healthScore,
                lastUsedAt,
                requestsPerDay,
                cooldownUntil,
                consecutiveRateLimits,
                createdAt,
                updatedAt);
    }

    // ===== Getters (Setter 제공 금지) =====

    public Long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public String getUserAgentString() {
        return userAgentString;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getDeviceBrand() {
        return deviceBrand;
    }

    public String getOsType() {
        return osType;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public String getBrowserType() {
        return browserType;
    }

    public String getBrowserVersion() {
        return browserVersion;
    }

    public UserAgentStatus getStatus() {
        return status;
    }

    public int getHealthScore() {
        return healthScore;
    }

    public LocalDateTime getLastUsedAt() {
        return lastUsedAt;
    }

    public int getRequestsPerDay() {
        return requestsPerDay;
    }

    public LocalDateTime getCooldownUntil() {
        return cooldownUntil;
    }

    public int getConsecutiveRateLimits() {
        return consecutiveRateLimits;
    }
}
