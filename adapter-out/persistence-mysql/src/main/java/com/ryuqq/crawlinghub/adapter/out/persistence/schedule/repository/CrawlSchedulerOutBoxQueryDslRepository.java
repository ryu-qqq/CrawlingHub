package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.CrawlSchedulerOutBoxJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.QCrawlSchedulerOutBoxJpaEntity;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerOubBoxStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * CrawlSchedulerOutBoxQueryDslRepository - CrawlSchedulerOutBox QueryDSL Repository
 *
 * <p>QueryDSL 기반 조회 쿼리를 처리하는 전용 Repository입니다.
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
     * 지정 시간(초) 이상 경과한 PENDING 상태의 아웃박스 조회.
     *
     * @param limit 조회 개수 제한
     * @param delaySeconds 최소 경과 시간 (초)
     * @return PENDING 아웃박스 엔티티 목록
     */
    public List<CrawlSchedulerOutBoxJpaEntity> findPendingOlderThan(int limit, int delaySeconds) {
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(delaySeconds);

        return queryFactory
                .selectFrom(qOutBox)
                .where(
                        qOutBox.status.eq(CrawlSchedulerOubBoxStatus.PENDING),
                        qOutBox.createdAt.loe(threshold))
                .orderBy(qOutBox.createdAt.asc())
                .limit(limit)
                .fetch();
    }

    /**
     * 지정 시간(초) 이상 PROCESSING 상태인 좀비 아웃박스 조회.
     *
     * @param limit 조회 개수 제한
     * @param timeoutSeconds 타임아웃 기준 (초)
     * @return PROCESSING 좀비 아웃박스 엔티티 목록
     */
    public List<CrawlSchedulerOutBoxJpaEntity> findStaleProcessing(int limit, long timeoutSeconds) {
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(timeoutSeconds);

        return queryFactory
                .selectFrom(qOutBox)
                .where(
                        qOutBox.status.eq(CrawlSchedulerOubBoxStatus.PROCESSING),
                        qOutBox.processedAt.loe(threshold))
                .orderBy(qOutBox.processedAt.asc())
                .limit(limit)
                .fetch();
    }
}
