package com.ryuqq.crawlinghub.adapter.out.persistence.task.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.task.entity.CrawlTaskJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.task.mapper.CrawlTaskJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.task.repository.CrawlTaskQueryDslRepository;
import com.ryuqq.crawlinghub.application.task.port.out.query.CrawlTaskQueryPort;
import com.ryuqq.crawlinghub.domain.schedule.identifier.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.identifier.CrawlTaskId;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskCriteria;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatisticsCriteria;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * CrawlTaskQueryAdapter - CrawlTask Query Adapter
 *
 * <p>CQRS의 Query(읽기) 담당 Adapter입니다.
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>단건 조회 (findById)
 *   <li>존재 여부 확인 (existsByScheduleIdAndStatusIn)
 *   <li>목록 조회 (findByCriteria)
 *   <li>카운트 조회 (countByCriteria)
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
public class CrawlTaskQueryAdapter implements CrawlTaskQueryPort {

    private final CrawlTaskQueryDslRepository queryDslRepository;
    private final CrawlTaskJpaEntityMapper mapper;

    public CrawlTaskQueryAdapter(
            CrawlTaskQueryDslRepository queryDslRepository, CrawlTaskJpaEntityMapper mapper) {
        this.queryDslRepository = queryDslRepository;
        this.mapper = mapper;
    }

    /**
     * ID로 CrawlTask 단건 조회
     *
     * @param crawlTaskId CrawlTask ID
     * @return CrawlTask Domain (Optional)
     */
    @Override
    public Optional<CrawlTask> findById(CrawlTaskId crawlTaskId) {
        return queryDslRepository.findById(crawlTaskId.value()).map(mapper::toDomain);
    }

    /**
     * 스케줄러 ID와 상태 목록으로 존재 여부 확인
     *
     * <p>중복 Task 생성 방지를 위해 사용
     *
     * @param crawlSchedulerId 스케줄러 ID
     * @param statuses 확인할 상태 목록
     * @return 존재 여부
     * @deprecated 스케줄러 ID만으로는 중복 판단이 불완전합니다. {@link
     *     #existsBySchedulerIdAndTaskTypeAndEndpointAndStatusIn} 사용을 권장합니다.
     */
    @Deprecated
    @Override
    public boolean existsByScheduleIdAndStatusIn(
            CrawlSchedulerId crawlSchedulerId, List<CrawlTaskStatus> statuses) {
        return queryDslRepository.existsBySchedulerIdAndStatusIn(
                crawlSchedulerId.value(), statuses);
    }

    /**
     * 스케줄러 ID, 태스크 타입, 엔드포인트 조합 + 상태로 존재 여부 확인
     *
     * @param crawlSchedulerId 스케줄러 ID
     * @param taskType 태스크 타입
     * @param endpointPath 엔드포인트 경로
     * @param endpointQueryParams 엔드포인트 쿼리 파라미터
     * @param statuses 확인할 상태 목록
     * @return 존재 여부
     */
    @Override
    public boolean existsBySchedulerIdAndTaskTypeAndEndpointAndStatusIn(
            CrawlSchedulerId crawlSchedulerId,
            CrawlTaskType taskType,
            String endpointPath,
            String endpointQueryParams,
            List<CrawlTaskStatus> statuses) {
        return queryDslRepository.existsBySchedulerIdAndTaskTypeAndEndpointAndStatusIn(
                crawlSchedulerId.value(), taskType, endpointPath, endpointQueryParams, statuses);
    }

    /**
     * 검색 조건으로 CrawlTask 목록 조회
     *
     * @param criteria 검색 조건
     * @return CrawlTask Domain 목록
     */
    @Override
    public List<CrawlTask> findByCriteria(CrawlTaskCriteria criteria) {
        List<CrawlTaskJpaEntity> entities = queryDslRepository.findByCriteria(criteria);
        return entities.stream().map(mapper::toDomain).toList();
    }

    /**
     * 검색 조건으로 CrawlTask 총 개수 조회
     *
     * @param criteria 검색 조건
     * @return 총 개수
     */
    @Override
    public long countByCriteria(CrawlTaskCriteria criteria) {
        return queryDslRepository.countByCriteria(criteria);
    }

    /**
     * 상태별 CrawlTask 개수 조회
     *
     * @param criteria 통계 조회 조건
     * @return 상태별 개수
     */
    @Override
    public Map<CrawlTaskStatus, Long> countByStatus(CrawlTaskStatisticsCriteria criteria) {
        return queryDslRepository.countByStatus(criteria);
    }

    /**
     * 타입별 CrawlTask 통계 조회
     *
     * @param criteria 통계 조회 조건
     * @return 타입별 통계
     */
    @Override
    public Map<CrawlTaskType, TaskTypeCount> countByTaskType(CrawlTaskStatisticsCriteria criteria) {
        return queryDslRepository.countByTaskType(criteria);
    }

    /**
     * 셀러별 최근 태스크 조회
     *
     * @param sellerId 셀러 ID
     * @return 최근 태스크 (Optional)
     */
    @Override
    public Optional<CrawlTask> findLatestBySellerId(SellerId sellerId) {
        return queryDslRepository.findLatestBySellerId(sellerId.value()).map(mapper::toDomain);
    }

    /**
     * 셀러별 최근 태스크 N개 조회
     *
     * @param sellerId 셀러 ID
     * @param limit 조회할 개수
     * @return 최근 태스크 리스트 (생성일시 내림차순)
     */
    @Override
    public List<CrawlTask> findRecentBySellerId(SellerId sellerId, int limit) {
        List<CrawlTaskJpaEntity> entities =
                queryDslRepository.findRecentBySellerId(sellerId.value(), limit);
        return entities.stream().map(mapper::toDomain).toList();
    }
}
