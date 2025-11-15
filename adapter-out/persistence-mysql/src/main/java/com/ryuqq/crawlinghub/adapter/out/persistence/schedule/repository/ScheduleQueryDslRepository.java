package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.dto.ScheduleQueryDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.QScheduleEntity;
import com.ryuqq.crawlinghub.domain.schedule.ScheduleStatus;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * ScheduleQueryDslRepository - Query Repository (QueryDSL)
 *
 * <p><strong>QueryDSL 기반 읽기 전용 Repository ⭐</strong></p>
 * <ul>
 *   <li>✅ JPAQueryFactory 캡슐화</li>
 *   <li>✅ DTO Projection 최적화</li>
 *   <li>✅ N+1 문제 방지</li>
 *   <li>✅ 타입 안전한 쿼리</li>
 * </ul>
 *
 * <p><strong>사용처:</strong></p>
 * <ul>
 *   <li>ScheduleQueryAdapter에서 주입받아 사용</li>
 *   <li>Query Adapter는 이 Repository를 통해 조회</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-11
 */
@Repository
public class ScheduleQueryDslRepository {

    private final JPAQueryFactory queryFactory;
    private static final QScheduleEntity scheduleEntity = QScheduleEntity.scheduleEntity;

    public ScheduleQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = Objects.requireNonNull(queryFactory, "queryFactory must not be null");
    }

    /**
     * ID로 스케줄 조회
     *
     * <p>QueryDSL DTO Projection으로 직접 조회하여 Domain Model을 거치지 않습니다.</p>
     *
     * @param scheduleId 스케줄 ID (null 불가)
     * @return 스케줄 Query DTO (없으면 Optional.empty())
     * @throws IllegalArgumentException scheduleId가 null인 경우
     */
    public Optional<ScheduleQueryDto> findById(Long scheduleId) {
        Objects.requireNonNull(scheduleId, "scheduleId must not be null");

        ScheduleQueryDto result = queryFactory
            .select(Projections.constructor(
                ScheduleQueryDto.class,
                scheduleEntity.id,
                scheduleEntity.sellerId,
                scheduleEntity.cronExpression,
                scheduleEntity.status,
                scheduleEntity.nextExecutionTime,
                scheduleEntity.lastExecutedAt,
                scheduleEntity.createdAt,
                scheduleEntity.updatedAt
            ))
            .from(scheduleEntity)
            .where(scheduleEntity.id.eq(scheduleId))
            .fetchOne();

        return Optional.ofNullable(result);
    }

    /**
     * Seller ID로 활성 스케줄 조회
     *
     * <p><strong>비즈니스 규칙:</strong> 한 셀러는 하나의 활성 스케줄만 가능</p>
     *
     * @param sellerId 셀러 ID (null 불가)
     * @return 활성 스케줄 Query DTO (없으면 Optional.empty())
     * @throws IllegalArgumentException sellerId가 null인 경우
     */
    public Optional<ScheduleQueryDto> findActiveBySellerId(Long sellerId) {
        Objects.requireNonNull(sellerId, "sellerId must not be null");

        ScheduleQueryDto result = queryFactory
            .select(Projections.constructor(
                ScheduleQueryDto.class,
                scheduleEntity.id,
                scheduleEntity.sellerId,
                scheduleEntity.cronExpression,
                scheduleEntity.status,
                scheduleEntity.nextExecutionTime,
                scheduleEntity.lastExecutedAt,
                scheduleEntity.createdAt,
                scheduleEntity.updatedAt
            ))
            .from(scheduleEntity)
            .where(
                scheduleEntity.sellerId.eq(sellerId),
                scheduleEntity.status.eq(ScheduleStatus.ACTIVE)
            )
            .fetchOne();

        return Optional.ofNullable(result);
    }

    /**
     * Seller ID로 모든 스케줄 조회
     *
     * @param sellerId 셀러 ID (null 불가)
     * @return 스케줄 Query DTO 목록
     * @throws IllegalArgumentException sellerId가 null인 경우
     */
    public List<ScheduleQueryDto> findAllBySellerId(Long sellerId) {
        Objects.requireNonNull(sellerId, "sellerId must not be null");

        return queryFactory
            .select(Projections.constructor(
                ScheduleQueryDto.class,
                scheduleEntity.id,
                scheduleEntity.sellerId,
                scheduleEntity.cronExpression,
                scheduleEntity.status,
                scheduleEntity.nextExecutionTime,
                scheduleEntity.lastExecutedAt,
                scheduleEntity.createdAt,
                scheduleEntity.updatedAt
            ))
            .from(scheduleEntity)
            .where(scheduleEntity.sellerId.eq(sellerId))
            .orderBy(scheduleEntity.createdAt.desc())
            .fetch();
    }
}
