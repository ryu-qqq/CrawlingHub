package com.ryuqq.crawlinghub.adapter.out.persistence.useragent.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.crawlinghub.adapter.out.persistence.useragent.entity.QUserAgentJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.useragent.entity.UserAgentJpaEntity;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentStatus;
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
