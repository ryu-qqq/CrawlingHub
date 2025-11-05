package com.ryuqq.crawlinghub.adapter.out.persistence.useragent.adapter;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.crawlinghub.adapter.out.persistence.useragent.entity.QUserAgentEntity;
import com.ryuqq.crawlinghub.application.useragent.dto.query.UserAgentQueryDto;
import com.ryuqq.crawlinghub.application.useragent.port.out.LoadUserAgentPort;
import com.ryuqq.crawlinghub.domain.useragent.TokenStatus;
import com.ryuqq.crawlinghub.domain.useragent.UserAgentId;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * UserAgent Query Adapter - CQRS Query Adapter (읽기 전용)
 *
 * <p><strong>CQRS 패턴 적용 - Query 작업만 수행 ⭐</strong></p>
 * <ul>
 *   <li>✅ Read 작업 전용 (findById, findAvailableForRotation, findByStatus)</li>
 *   <li>✅ QueryDSL DTO Projection으로 직접 조회 (Domain Model 거치지 않음)</li>
 *   <li>✅ N+1 문제 방지</li>
 *   <li>✅ 인덱스 활용 최적화</li>
 * </ul>
 *
 * <p><strong>주의사항:</strong></p>
 * <ul>
 *   <li>❌ Write 작업은 UserAgentCommandAdapter에서 처리</li>
 *   <li>❌ Command 작업은 이 Adapter에서 금지</li>
 *   <li>❌ Domain Model 변환 없이 DTO 직접 반환</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@Component
public class UserAgentQueryAdapter implements LoadUserAgentPort {

    private final JPAQueryFactory queryFactory;
    private static final QUserAgentEntity userAgent = QUserAgentEntity.userAgentEntity;

    /**
     * Adapter 생성자
     *
     * @param queryFactory QueryDSL QueryFactory
     */
    public UserAgentQueryAdapter(JPAQueryFactory queryFactory) {
        this.queryFactory = Objects.requireNonNull(queryFactory, "queryFactory must not be null");
    }

    /**
     * ID로 UserAgent 조회
     *
     * <p>QueryDSL DTO Projection으로 직접 조회하여 Domain Model을 거치지 않습니다.</p>
     *
     * @param id UserAgent ID (null 불가)
     * @return UserAgent Query DTO (없으면 Optional.empty())
     * @throws IllegalArgumentException id가 null인 경우
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<UserAgentQueryDto> findById(UserAgentId id) {
        Objects.requireNonNull(id, "id must not be null");

        UserAgentQueryDto result = queryFactory
            .select(Projections.constructor(
                UserAgentQueryDto.class,
                userAgent.id,
                userAgent.userAgentString,
                userAgent.currentToken,
                userAgent.tokenStatus,
                userAgent.remainingRequests,
                userAgent.tokenIssuedAt,
                userAgent.rateLimitResetAt,
                userAgent.createdAt,
                userAgent.updatedAt
            ))
            .from(userAgent)
            .where(userAgent.id.eq(id.value()))
            .fetchOne();

        return Optional.ofNullable(result);
    }

    /**
     * 로테이션용 사용 가능한 UserAgent 조회
     *
     * <p>요청 가능한 상태의 UserAgent 중 하나를 반환합니다.</p>
     * <p>QueryDSL DTO Projection으로 직접 조회합니다.</p>
     *
     * @return 사용 가능한 UserAgent Query DTO (없으면 Optional.empty())
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<UserAgentQueryDto> findAvailableForRotation() {
        UserAgentQueryDto result = queryFactory
            .select(Projections.constructor(
                UserAgentQueryDto.class,
                userAgent.id,
                userAgent.userAgentString,
                userAgent.currentToken,
                userAgent.tokenStatus,
                userAgent.remainingRequests,
                userAgent.tokenIssuedAt,
                userAgent.rateLimitResetAt,
                userAgent.createdAt,
                userAgent.updatedAt
            ))
            .from(userAgent)
            .where(
                userAgent.tokenStatus.in(TokenStatus.IDLE, TokenStatus.ACTIVE, TokenStatus.RECOVERED)
                    .and(userAgent.remainingRequests.gt(0))
            )
            .orderBy(userAgent.remainingRequests.desc())
            .limit(1)
            .fetchOne();

        return Optional.ofNullable(result);
    }

    /**
     * 상태로 UserAgent 목록 조회
     *
     * <p>특정 상태의 UserAgent 목록을 조회합니다.</p>
     * <p>QueryDSL DTO Projection으로 직접 조회합니다.</p>
     *
     * @param status TokenStatus (null 불가)
     * @return UserAgent Query DTO 목록
     * @throws IllegalArgumentException status가 null인 경우
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserAgentQueryDto> findByStatus(TokenStatus status) {
        Objects.requireNonNull(status, "status must not be null");

        return queryFactory
            .select(Projections.constructor(
                UserAgentQueryDto.class,
                userAgent.id,
                userAgent.userAgentString,
                userAgent.currentToken,
                userAgent.tokenStatus,
                userAgent.remainingRequests,
                userAgent.tokenIssuedAt,
                userAgent.rateLimitResetAt,
                userAgent.createdAt,
                userAgent.updatedAt
            ))
            .from(userAgent)
            .where(userAgent.tokenStatus.eq(status))
            .orderBy(userAgent.createdAt.desc())
            .fetch();
    }
}

