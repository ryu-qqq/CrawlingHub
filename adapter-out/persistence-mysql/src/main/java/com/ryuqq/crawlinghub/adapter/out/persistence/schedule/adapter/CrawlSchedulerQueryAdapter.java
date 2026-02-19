package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.CrawlSchedulerJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.mapper.CrawlSchedulerJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.repository.CrawlSchedulerQueryDslRepository;
import com.ryuqq.crawlinghub.application.schedule.port.out.query.CrawlScheduleQueryPort;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.schedule.query.CrawlSchedulerPageCriteria;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * CrawlSchedulerQueryAdapter - CrawlScheduler Query Adapter
 *
 * <p>CQRS의 Query(읽기) 담당 Adapter입니다.
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>단건 조회 (findById)
 *   <li>존재 여부 확인 (existsBySellerIdAndSchedulerName)
 *   <li>목록 조회 (findByCriteria)
 *   <li>카운트 조회 (count)
 *   <li>QueryDslRepository 호출
 *   <li>Mapper를 통한 Entity → Domain 변환
 * </ul>
 *
 * <p><strong>금지 사항:</strong>
 *
 * <ul>
 *   <li>❌ 비즈니스 로직
 *   <li>❌ 저장/수정/삭제 (CommandAdapter로 분리)
 *   <li>❌ JPAQueryFactory 직접 사용 (QueryDslRepository에서 처리)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlSchedulerQueryAdapter implements CrawlScheduleQueryPort {

    private final CrawlSchedulerQueryDslRepository queryDslRepository;
    private final CrawlSchedulerJpaEntityMapper mapper;

    public CrawlSchedulerQueryAdapter(
            CrawlSchedulerQueryDslRepository queryDslRepository,
            CrawlSchedulerJpaEntityMapper mapper) {
        this.queryDslRepository = queryDslRepository;
        this.mapper = mapper;
    }

    /**
     * ID로 CrawlScheduler 단건 조회
     *
     * @param crawlSchedulerId CrawlScheduler ID
     * @return CrawlScheduler Domain (Optional)
     */
    @Override
    public Optional<CrawlScheduler> findById(CrawlSchedulerId crawlSchedulerId) {
        return queryDslRepository.findById(crawlSchedulerId.value()).map(mapper::toDomain);
    }

    /**
     * 셀러 ID와 스케줄러 이름으로 존재 여부 확인
     *
     * @param sellerId 셀러 ID
     * @param schedulerName 스케줄러 이름
     * @return 존재 여부
     */
    @Override
    public boolean existsBySellerIdAndSchedulerName(SellerId sellerId, String schedulerName) {
        return queryDslRepository.existsBySellerIdAndSchedulerName(sellerId.value(), schedulerName);
    }

    /**
     * 검색 조건으로 CrawlScheduler 목록 조회
     *
     * @param criteria 검색 조건
     * @return CrawlScheduler Domain 목록
     */
    @Override
    public List<CrawlScheduler> findByCriteria(CrawlSchedulerPageCriteria criteria) {
        List<CrawlSchedulerJpaEntity> entities = queryDslRepository.findByCriteria(criteria);
        return entities.stream().map(mapper::toDomain).toList();
    }

    /**
     * 검색 조건으로 CrawlScheduler 개수 조회
     *
     * @param criteria 검색 조건
     * @return CrawlScheduler 개수
     */
    @Override
    public long count(CrawlSchedulerPageCriteria criteria) {
        return queryDslRepository.countByCriteria(criteria);
    }

    /**
     * 셀러별 활성 스케줄러 목록 조회
     *
     * @param sellerId 셀러 ID
     * @return 활성 CrawlScheduler Domain 목록
     */
    @Override
    public List<CrawlScheduler> findActiveSchedulersBySellerId(SellerId sellerId) {
        List<CrawlSchedulerJpaEntity> entities =
                queryDslRepository.findActiveBySellerIdmethod(sellerId.value());
        return entities.stream().map(mapper::toDomain).toList();
    }

    /**
     * 셀러별 전체 스케줄러 개수 조회
     *
     * @param sellerId 셀러 ID
     * @return 전체 스케줄러 개수
     */
    @Override
    public long countBySellerId(SellerId sellerId) {
        return queryDslRepository.countBySellerId(sellerId.value());
    }

    /**
     * 셀러별 활성 스케줄러 개수 조회
     *
     * @param sellerId 셀러 ID
     * @return 활성 스케줄러 개수
     */
    @Override
    public long countActiveSchedulersBySellerId(SellerId sellerId) {
        return queryDslRepository.countActiveSchedulersBySellerId(sellerId.value());
    }

    /**
     * 셀러별 전체 스케줄러 목록 조회
     *
     * @param sellerId 셀러 ID
     * @return 스케줄러 목록 (생성일시 내림차순)
     */
    @Override
    public List<CrawlScheduler> findBySellerId(SellerId sellerId) {
        List<CrawlSchedulerJpaEntity> entities =
                queryDslRepository.findBySellerId(sellerId.value());
        return entities.stream().map(mapper::toDomain).toList();
    }
}
