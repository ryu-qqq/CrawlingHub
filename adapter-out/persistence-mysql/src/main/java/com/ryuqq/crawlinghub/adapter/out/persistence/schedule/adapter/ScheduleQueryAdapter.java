package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.dto.ScheduleQueryDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.mapper.ScheduleMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.repository.ScheduleQueryDslRepository;
import com.ryuqq.crawlinghub.application.schedule.port.out.LoadSchedulePort;
import com.ryuqq.crawlinghub.domain.schedule.CrawlSchedule;
import com.ryuqq.crawlinghub.domain.schedule.CrawlScheduleId;
import com.ryuqq.crawlinghub.domain.seller.MustItSellerId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Schedule Query Adapter - CQRS Query Adapter (읽기 전용)
 *
 * <p><strong>CQRS 패턴 적용 - Query 작업만 수행 ⭐</strong></p>
 * <ul>
 *   <li>✅ Read 작업 전용 (findById, findActiveBySellerId, findAllBySellerId)</li>
 *   <li>✅ QueryDSL DTO Projection으로 직접 조회 → Domain 변환</li>
 *   <li>✅ N+1 문제 방지</li>
 *   <li>✅ ScheduleQueryDslRepository 사용</li>
 * </ul>
 *
 * <p><strong>주의사항:</strong></p>
 * <ul>
 *   <li>❌ Write 작업은 ScheduleCommandAdapter에서 처리</li>
 *   <li>❌ Command 작업은 이 Adapter에서 금지</li>
 *   <li>✅ DTO → Domain 변환은 Adapter에서 처리</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
@Component
public class ScheduleQueryAdapter implements LoadSchedulePort {

    private final ScheduleQueryDslRepository queryDslRepository;
    private final ScheduleMapper scheduleMapper;

    /**
     * Adapter 생성자
     *
     * @param queryDslRepository QueryDSL Repository
     */
    public ScheduleQueryAdapter(ScheduleQueryDslRepository queryDslRepository, ScheduleMapper scheduleMapper) {
        this.queryDslRepository = Objects.requireNonNull(queryDslRepository, "queryDslRepository must not be null");
        this.scheduleMapper = scheduleMapper;
    }

    /**
     * ID로 스케줄 조회
     *
     * <p><strong>처리 흐름:</strong></p>
     * <ol>
     *   <li>ScheduleQueryDslRepository로 DTO 조회</li>
     *   <li>DTO → Domain 변환</li>
     * </ol>
     *
     * @param scheduleId 스케줄 ID
     * @return 스케줄 (Optional)
     */
    @Override
    public Optional<CrawlSchedule> findById(CrawlScheduleId scheduleId) {
        Objects.requireNonNull(scheduleId, "scheduleId must not be null");

        return queryDslRepository.findById(scheduleId.value())
            .map(scheduleMapper::toDomain);
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
    public Optional<CrawlSchedule> findActiveBySellerId(MustItSellerId sellerId) {
        Objects.requireNonNull(sellerId, "sellerId must not be null");

        return queryDslRepository.findActiveBySellerId(sellerId.value())
            .map(scheduleMapper::toDomain);
    }

    /**
     * Seller ID로 모든 스케줄 조회
     *
     * @param sellerId 셀러 ID
     * @return 스케줄 목록
     */
    @Override
    public List<CrawlSchedule> findAllBySellerId(MustItSellerId sellerId) {
        Objects.requireNonNull(sellerId, "sellerId must not be null");

        return queryDslRepository.findAllBySellerId(sellerId.value())
            .stream()
            .map(scheduleMapper::toDomain)
            .toList();
    }

}
