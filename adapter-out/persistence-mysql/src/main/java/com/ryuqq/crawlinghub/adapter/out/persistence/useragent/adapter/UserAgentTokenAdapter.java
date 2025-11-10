package com.ryuqq.crawlinghub.adapter.out.persistence.useragent.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.useragent.entity.UserAgentEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.useragent.mapper.UserAgentMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.useragent.repository.UserAgentJpaRepository;
import com.ryuqq.crawlinghub.application.token.port.UserAgentTokenPort;
import com.ryuqq.crawlinghub.domain.useragent.UserAgent;

import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * UserAgent Token Management Adapter
 * <p>
 * ⭐ Domain 중심 설계:
 * - UserAgent Domain 객체 사용
 * - Token VO 지원
 * - TokenTransactionService에서 호출되는 Port 구현
 * </p>
 * <p>
 * ⚠️ Zero-Tolerance 규칙:
 * - @Transactional은 Service Layer에서 관리 (여기서는 Repository 호출만)
 * - 각 메서드는 독립적인 트랜잭션으로 실행됨
 * </p>
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
@Component
public class UserAgentTokenAdapter implements UserAgentTokenPort {

    private final UserAgentJpaRepository jpaRepository;
    private final UserAgentMapper mapper;

    /**
     * Adapter 생성자
     *
     * @param jpaRepository JPA Repository
     * @param mapper Domain ↔ Entity 변환 Mapper
     */
    public UserAgentTokenAdapter(
        UserAgentJpaRepository jpaRepository,
        UserAgentMapper mapper
    ) {
        this.jpaRepository = Objects.requireNonNull(jpaRepository, "jpaRepository must not be null");
        this.mapper = Objects.requireNonNull(mapper, "mapper must not be null");
    }

    /**
     * [TX1] User-Agent 조회 (readOnly)
     * <p>
     * DB에서 최신 User-Agent 정보 로드
     * TokenTransactionService의 loadUserAgent()에서 호출
     * </p>
     *
     * @param userAgentId User-Agent ID
     * @return UserAgent Domain 객체
     * @throws IllegalArgumentException userAgentId가 null인 경우
     * @throws RuntimeException User-Agent를 찾을 수 없는 경우
     */
    @Override
    public UserAgent findById(Long userAgentId) {
        Objects.requireNonNull(userAgentId, "userAgentId must not be null");

        UserAgentEntity entity = jpaRepository.findById(userAgentId)
            .orElseThrow(() -> new RuntimeException("UserAgent not found: " + userAgentId));

        return mapper.toDomain(entity);
    }

    /**
     * [TX2] User-Agent 저장 (토큰 포함)
     * <p>
     * 신규 토큰 발급 후 저장
     * TokenTransactionService의 saveUserAgent()에서 호출
     * </p>
     *
     * @param userAgent UserAgent Domain 객체 (null 불가)
     * @throws IllegalArgumentException userAgent가 null인 경우
     */
    @Override
    public void save(UserAgent userAgent) {
        Objects.requireNonNull(userAgent, "userAgent must not be null");

        UserAgentEntity entity = mapper.toEntity(userAgent);
        jpaRepository.save(entity);
    }

    /**
     * [TX3] 사용 기록
     * <p>
     * 토큰 사용 통계 기록
     * TokenTransactionService의 recordUsage()에서 호출
     * </p>
     *
     * @param userAgent UserAgent Domain 객체 (null 불가)
     * @throws IllegalArgumentException userAgent가 null인 경우
     */
    @Override
    public void recordUsage(UserAgent userAgent) {
        Objects.requireNonNull(userAgent, "userAgent must not be null");

        // 사용 기록 저장 (Token 소비 후 상태)
        UserAgentEntity entity = mapper.toEntity(userAgent);
        jpaRepository.save(entity);
    }
}
