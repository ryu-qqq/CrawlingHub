package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.CrawlSchedulerOutBoxJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.QCrawlSchedulerOutBoxJpaEntity;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerOubBoxStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * CrawlSchedulerOutBoxQueryDslRepository - CrawlSchedulerOutBox QueryDSL Repository
 *
 * <p>QueryDSL 기반 조회 쿼리를 처리하는 전용 Repository입니다.
 *
 * <p><strong>표준 메서드:</strong>
 *
 * <ul>
 *   <li>findById(Long id): 단건 조회
 *   <li>findByHistoryId(Long historyId): 히스토리 ID로 단건 조회
 *   <li>findByStatus(status, limit): 상태별 목록 조회
 *   <li>findByStatusIn(statuses, limit): 여러 상태로 목록 조회
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
public class CrawlSchedulerOutBoxQueryDslRepository {

    private final JPAQueryFactory queryFactory;
    private static final QCrawlSchedulerOutBoxJpaEntity qOutBox =
            QCrawlSchedulerOutBoxJpaEntity.crawlSchedulerOutBoxJpaEntity;

    public CrawlSchedulerOutBoxQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * ID로 CrawlSchedulerOutBox 단건 조회
     *
     * @param id CrawlSchedulerOutBox ID
     * @return CrawlSchedulerOutBoxJpaEntity (Optional)
     */
    public Optional<CrawlSchedulerOutBoxJpaEntity> findById(Long id) {
        return Optional.ofNullable(
                queryFactory.selectFrom(qOutBox).where(qOutBox.id.eq(id)).fetchOne());
    }

    /**
     * 히스토리 ID로 CrawlSchedulerOutBox 단건 조회
     *
     * @param historyId 히스토리 ID
     * @return CrawlSchedulerOutBoxJpaEntity (Optional)
     */
    public Optional<CrawlSchedulerOutBoxJpaEntity> findByHistoryId(Long historyId) {
        return Optional.ofNullable(
                queryFactory.selectFrom(qOutBox).where(qOutBox.historyId.eq(historyId)).fetchOne());
    }

    /**
     * 상태별 아웃박스 목록 조회
     *
     * @param status 조회할 상태
     * @param limit 조회 개수 제한
     * @return 아웃박스 엔티티 목록
     */
    public List<CrawlSchedulerOutBoxJpaEntity> findByStatus(
            CrawlSchedulerOubBoxStatus status, int limit) {
        return queryFactory
                .selectFrom(qOutBox)
                .where(qOutBox.status.eq(status))
                .orderBy(qOutBox.createdAt.asc())
                .limit(limit)
                .fetch();
    }

    /**
     * 여러 상태로 아웃박스 목록 조회 (PENDING 또는 FAILED)
     *
     * @param statuses 조회할 상태 목록
     * @param limit 조회 개수 제한
     * @return 아웃박스 엔티티 목록
     */
    public List<CrawlSchedulerOutBoxJpaEntity> findByStatusIn(
            List<CrawlSchedulerOubBoxStatus> statuses, int limit) {
        return queryFactory
                .selectFrom(qOutBox)
                .where(qOutBox.status.in(statuses))
                .orderBy(qOutBox.createdAt.asc())
                .limit(limit)
                .fetch();
    }
}
