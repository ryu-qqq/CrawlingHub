package com.ryuqq.crawlinghub.adapter.out.persistence.useragent.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.crawlinghub.adapter.out.persistence.useragent.entity.QUserAgentJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.useragent.entity.UserAgentJpaEntity;
import com.ryuqq.crawlinghub.application.useragent.dto.query.UserAgentSearchCriteria;
import com.ryuqq.crawlinghub.domain.common.vo.PageRequest;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentStatus;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * UserAgentQueryDslRepository - UserAgent QueryDSL Repository
 *
 * <p>QueryDSL 기반 조회 쿼리를 처리하는 전용 Repository입니다.
 *
 * <p><strong>표준 메서드 (4개) + 상태별 조회 메서드:</strong>
 *
 * <ul>
 *   <li>findById(Long id): 단건 조회
 *   <li>existsById(Long id): 존재 여부 확인
 *   <li>findByStatus(UserAgentStatus): 상태별 목록 조회
 *   <li>countByStatus(UserAgentStatus): 상태별 개수 조회
 *   <li>countAll(): 전체 개수 조회
 * </ul>
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>동적 쿼리 구성 (BooleanExpression)
 *   <li>단순 조회 (Join 없음)
 * </ul>
 *
 * <p><strong>금지 사항:</strong>
 *
 * <ul>
 *   <li>❌ Join 절대 금지 (fetch join, left join, inner join)
 *   <li>❌ 비즈니스 로직 금지
 *   <li>❌ Mapper 호출 금지
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Repository
public class UserAgentQueryDslRepository {

    private final JPAQueryFactory queryFactory;
    private static final QUserAgentJpaEntity qUserAgent = QUserAgentJpaEntity.userAgentJpaEntity;

    public UserAgentQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * ID로 UserAgent 단건 조회
     *
     * @param id UserAgent ID
     * @return UserAgentJpaEntity (Optional)
     */
    public Optional<UserAgentJpaEntity> findById(Long id) {
        return Optional.ofNullable(
                queryFactory.selectFrom(qUserAgent).where(qUserAgent.id.eq(id)).fetchOne());
    }

    /**
     * ID로 UserAgent 존재 여부 확인
     *
     * @param id UserAgent ID
     * @return 존재 여부
     */
    public boolean existsById(Long id) {
        Integer count =
                queryFactory.selectOne().from(qUserAgent).where(qUserAgent.id.eq(id)).fetchFirst();

        return count != null;
    }

    /**
     * 상태별 UserAgent 목록 조회
     *
     * @param status UserAgent 상태
     * @return UserAgentJpaEntity 목록
     */
    public List<UserAgentJpaEntity> findByStatus(UserAgentStatus status) {
        return queryFactory
                .selectFrom(qUserAgent)
                .where(qUserAgent.status.eq(status))
                .orderBy(qUserAgent.id.asc())
                .fetch();
    }

    /**
     * 상태별 UserAgent 개수 조회
     *
     * @param status UserAgent 상태
     * @return UserAgent 개수
     */
    public long countByStatus(UserAgentStatus status) {
        Long count =
                queryFactory
                        .select(qUserAgent.count())
                        .from(qUserAgent)
                        .where(qUserAgent.status.eq(status))
                        .fetchOne();

        return count != null ? count : 0L;
    }

    /**
     * 전체 UserAgent 개수 조회
     *
     * @return 전체 UserAgent 개수
     */
    public long countAll() {
        Long count = queryFactory.select(qUserAgent.count()).from(qUserAgent).fetchOne();

        return count != null ? count : 0L;
    }

    /**
     * 상태 필터 + 페이징 조회 (하위 호환성)
     *
     * @param status UserAgent 상태 (null이면 전체 조회)
     * @param pageRequest 페이징 정보
     * @return UserAgentJpaEntity 목록
     */
    public List<UserAgentJpaEntity> findByStatusWithPaging(
            UserAgentStatus status, PageRequest pageRequest) {
        return queryFactory
                .selectFrom(qUserAgent)
                .where(statusEq(status))
                .orderBy(qUserAgent.id.desc())
                .offset(pageRequest.offset())
                .limit(pageRequest.size())
                .fetch();
    }

    /**
     * 검색 조건 + 페이징 조회 (다중 상태, 기간 필터 지원)
     *
     * @param criteria 검색 조건
     * @return UserAgentJpaEntity 목록
     */
    public List<UserAgentJpaEntity> findByCriteria(UserAgentSearchCriteria criteria) {
        PageRequest pageRequest = criteria.pageRequest();
        return queryFactory
                .selectFrom(qUserAgent)
                .where(
                        statusesIn(criteria.statuses()),
                        createdAtGoe(criteria.createdFrom()),
                        createdAtLoe(criteria.createdTo()))
                .orderBy(qUserAgent.id.desc())
                .offset(pageRequest.offset())
                .limit(pageRequest.size())
                .fetch();
    }

    /**
     * 검색 조건 + 전체 개수 조회 (다중 상태, 기간 필터 지원)
     *
     * @param criteria 검색 조건
     * @return UserAgent 개수
     */
    public long countByCriteria(UserAgentSearchCriteria criteria) {
        Long count =
                queryFactory
                        .select(qUserAgent.count())
                        .from(qUserAgent)
                        .where(
                                statusesIn(criteria.statuses()),
                                createdAtGoe(criteria.createdFrom()),
                                createdAtLoe(criteria.createdTo()))
                        .fetchOne();

        return count != null ? count : 0L;
    }

    /**
     * 상태 필터 + 전체 개수 조회 (하위 호환성)
     *
     * @param status UserAgent 상태 (null이면 전체 조회)
     * @return UserAgent 개수
     */
    public long countByStatusOrAll(UserAgentStatus status) {
        Long count =
                queryFactory
                        .select(qUserAgent.count())
                        .from(qUserAgent)
                        .where(statusEq(status))
                        .fetchOne();

        return count != null ? count : 0L;
    }

    /**
     * 단일 상태 필터 BooleanExpression (하위 호환성)
     *
     * @param status UserAgent 상태 (null이면 필터 없음)
     * @return BooleanExpression (null이면 조건 없음)
     */
    private BooleanExpression statusEq(UserAgentStatus status) {
        return status != null ? qUserAgent.status.eq(status) : null;
    }

    /**
     * 다중 상태 필터 BooleanExpression
     *
     * @param statuses UserAgent 상태 목록 (null이거나 빈 리스트면 필터 없음)
     * @return BooleanExpression (null이면 조건 없음)
     */
    private BooleanExpression statusesIn(List<UserAgentStatus> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return null;
        }
        return qUserAgent.status.in(statuses);
    }

    /**
     * 생성일 시작 조건
     *
     * @param createdFrom 생성일 시작 (null이면 조건 없음)
     * @return BooleanExpression (null이면 조건 없음)
     */
    private BooleanExpression createdAtGoe(java.time.Instant createdFrom) {
        if (createdFrom == null) {
            return null;
        }
        LocalDateTime localDateTime = LocalDateTime.ofInstant(createdFrom, ZoneId.systemDefault());
        return qUserAgent.createdAt.goe(localDateTime);
    }

    /**
     * 생성일 종료 조건
     *
     * @param createdTo 생성일 종료 (null이면 조건 없음)
     * @return BooleanExpression (null이면 조건 없음)
     */
    private BooleanExpression createdAtLoe(java.time.Instant createdTo) {
        if (createdTo == null) {
            return null;
        }
        LocalDateTime localDateTime = LocalDateTime.ofInstant(createdTo, ZoneId.systemDefault());
        return qUserAgent.createdAt.loe(localDateTime);
    }

    /**
     * 여러 ID로 UserAgent 목록 조회 (배치 처리용)
     *
     * @param ids UserAgent ID 목록
     * @return UserAgentJpaEntity 목록
     */
    public List<UserAgentJpaEntity> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return queryFactory
                .selectFrom(qUserAgent)
                .where(qUserAgent.id.in(ids))
                .orderBy(qUserAgent.id.asc())
                .fetch();
    }
}
