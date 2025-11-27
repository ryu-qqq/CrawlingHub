package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.CrawlSchedulerOutBoxJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.mapper.CrawlSchedulerOutBoxJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.repository.CrawlSchedulerOutBoxQueryDslRepository;
import com.ryuqq.crawlinghub.application.schedule.port.out.query.CrawlSchedulerOutBoxQueryPort;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerOutBox;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerHistoryId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerOubBoxStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * CrawlSchedulerOutBoxQueryAdapter - CrawlSchedulerOutBox Query Adapter
 *
 * <p>CQRS의 Query(읽기) 담당 Adapter입니다.
 *
 * <p>QueryDSL Repository를 사용하여 조회 쿼리를 처리합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlSchedulerOutBoxQueryAdapter implements CrawlSchedulerOutBoxQueryPort {

    private final CrawlSchedulerOutBoxQueryDslRepository queryDslRepository;
    private final CrawlSchedulerOutBoxJpaEntityMapper mapper;

    public CrawlSchedulerOutBoxQueryAdapter(
            CrawlSchedulerOutBoxQueryDslRepository queryDslRepository,
            CrawlSchedulerOutBoxJpaEntityMapper mapper) {
        this.queryDslRepository = queryDslRepository;
        this.mapper = mapper;
    }

    /**
     * 히스토리 ID로 아웃박스 조회
     *
     * @param historyId 히스토리 ID
     * @return 아웃박스 (Optional)
     */
    @Override
    public Optional<CrawlSchedulerOutBox> findByHistoryId(CrawlSchedulerHistoryId historyId) {
        return queryDslRepository.findByHistoryId(historyId.value()).map(mapper::toDomain);
    }

    /**
     * 상태별 아웃박스 목록 조회
     *
     * @param status 아웃박스 상태
     * @param limit 조회 개수 제한
     * @return 아웃박스 목록
     */
    @Override
    public List<CrawlSchedulerOutBox> findByStatus(CrawlSchedulerOubBoxStatus status, int limit) {
        List<CrawlSchedulerOutBoxJpaEntity> entities =
                queryDslRepository.findByStatus(status, limit);

        return entities.stream().map(mapper::toDomain).toList();
    }

    /**
     * PENDING 또는 FAILED 상태의 아웃박스 목록 조회
     *
     * @param limit 조회 개수 제한
     * @return 재처리 대상 아웃박스 목록
     */
    @Override
    public List<CrawlSchedulerOutBox> findPendingOrFailed(int limit) {
        List<CrawlSchedulerOubBoxStatus> statuses =
                List.of(CrawlSchedulerOubBoxStatus.PENDING, CrawlSchedulerOubBoxStatus.FAILED);

        List<CrawlSchedulerOutBoxJpaEntity> entities =
                queryDslRepository.findByStatusIn(statuses, limit);

        return entities.stream().map(mapper::toDomain).toList();
    }
}
