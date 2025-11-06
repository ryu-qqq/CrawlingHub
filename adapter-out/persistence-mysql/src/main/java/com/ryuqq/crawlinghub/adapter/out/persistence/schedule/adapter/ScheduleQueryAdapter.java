package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.adapter;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.dto.ScheduleQueryDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.ScheduleEntity;
import com.ryuqq.crawlinghub.application.schedule.port.out.LoadSchedulePort;
import com.ryuqq.crawlinghub.domain.schedule.CrawlSchedule;
import com.ryuqq.crawlinghub.domain.schedule.CrawlScheduleId;
import com.ryuqq.crawlinghub.domain.seller.MustitSellerId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.QScheduleEntity.scheduleEntity;

/**
 * Schedule Query Adapter (CQRS - Query, QueryDSL)
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>✅ R (Read) 작업 전담</li>
 *   <li>✅ LoadSchedulePort 구현</li>
 *   <li>✅ QueryDSL Projections.constructor() 사용</li>
 *   <li>✅ DTO 직접 반환 후 Domain 변환</li>
 * </ul>
 *
 * <p><strong>CQRS 패턴:</strong></p>
 * <ul>
 *   <li>✅ Query (읽기) 전용 Adapter</li>
 *   <li>✅ Command (쓰기)는 ScheduleCommandAdapter에 위임</li>
 *   <li>✅ JPAQueryFactory 사용</li>
 * </ul>
 *
 * <p><strong>컨벤션 준수:</strong></p>
 * <ul>
 *   <li>✅ Lombok 금지 - Pure Java Constructor</li>
 *   <li>✅ @Component (Spring Bean 등록)</li>
 *   <li>✅ Objects.requireNonNull() 검증</li>
 *   <li>✅ QueryDSL Projections.constructor() 사용</li>
 *   <li>✅ DTO Record 패턴</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
@Component
public class ScheduleQueryAdapter implements LoadSchedulePort {

    private final JPAQueryFactory queryFactory;

    /**
     * 생성자
     *
     * @param queryFactory JPAQueryFactory
     */
    public ScheduleQueryAdapter(JPAQueryFactory queryFactory) {
        this.queryFactory = Objects.requireNonNull(queryFactory, "queryFactory must not be null");
    }

    /**
     * ID로 스케줄 조회
     *
     * <p><strong>처리 흐름:</strong></p>
     * <ol>
     *   <li>QueryDSL로 DTO 조회</li>
     *   <li>DTO → Domain 변환</li>
     * </ol>
     *
     * @param scheduleId 스케줄 ID
     * @return 스케줄 (Optional)
     */
    @Override
    public Optional<CrawlSchedule> findById(CrawlScheduleId scheduleId) {
        Objects.requireNonNull(scheduleId, "scheduleId must not be null");

        ScheduleQueryDto dto = queryFactory
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
            .where(scheduleEntity.id.eq(scheduleId.value()))
            .fetchOne();

        return Optional.ofNullable(dto).map(this::toDomain);
    }

    /**
     * Seller ID로 활성 스케줄 조회
     *
     * <p><strong>비즈니스 규칙:</strong> 한 셀러는 하나의 활성 스케줄만 가능</p>
     *
     * @param sellerId 셀러 ID
     * @return 활성 스케줄 (Optional)
     */
    @Override
    public Optional<CrawlSchedule> findActiveBySellerId(MustitSellerId sellerId) {
        Objects.requireNonNull(sellerId, "sellerId must not be null");

        ScheduleQueryDto dto = queryFactory
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
                scheduleEntity.sellerId.eq(sellerId.value()),
                scheduleEntity.status.eq(ScheduleEntity.ScheduleStatus.ACTIVE)
            )
            .fetchOne();

        return Optional.ofNullable(dto).map(this::toDomain);
    }

    /**
     * Seller ID로 모든 스케줄 조회
     *
     * @param sellerId 셀러 ID
     * @return 스케줄 목록
     */
    @Override
    public List<CrawlSchedule> findAllBySellerId(MustitSellerId sellerId) {
        Objects.requireNonNull(sellerId, "sellerId must not be null");

        List<ScheduleQueryDto> dtos = queryFactory
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
            .where(scheduleEntity.sellerId.eq(sellerId.value()))
            .orderBy(scheduleEntity.createdAt.desc())
            .fetch();

        return dtos.stream()
            .map(this::toDomain)
            .toList();
    }

    /**
     * DTO → Domain 변환
     *
     * @param dto ScheduleQueryDto
     * @return CrawlSchedule
     */
    private CrawlSchedule toDomain(ScheduleQueryDto dto) {
        Objects.requireNonNull(dto, "dto must not be null");

        return CrawlSchedule.reconstitute(
            CrawlScheduleId.of(dto.id()),
            MustitSellerId.of(dto.sellerId()),
            com.ryuqq.crawlinghub.domain.schedule.CronExpression.of(dto.cronExpression()),
            toDomainStatus(dto.status()),
            dto.nextExecutionTime(),
            dto.lastExecutedAt(),
            dto.createdAt(),
            dto.updatedAt()
        );
    }

    /**
     * Entity Status → Domain Status 변환
     *
     * @param entityStatus Entity ScheduleStatus
     * @return Domain ScheduleStatus
     */
    private com.ryuqq.crawlinghub.domain.schedule.ScheduleStatus toDomainStatus(ScheduleEntity.ScheduleStatus entityStatus) {
        Objects.requireNonNull(entityStatus, "entityStatus must not be null");

        return switch (entityStatus) {
            case ACTIVE -> com.ryuqq.crawlinghub.domain.schedule.ScheduleStatus.ACTIVE;
            case SUSPENDED -> com.ryuqq.crawlinghub.domain.schedule.ScheduleStatus.SUSPENDED;
            case DELETED -> com.ryuqq.crawlinghub.domain.schedule.ScheduleStatus.DELETED;
        };
    }
}
