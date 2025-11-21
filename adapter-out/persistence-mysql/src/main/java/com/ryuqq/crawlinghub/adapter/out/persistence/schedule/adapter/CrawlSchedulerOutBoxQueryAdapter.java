package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.CrawlSchedulerOutBoxJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.mapper.CrawlSchedulerOutBoxJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.repository.CrawlSchedulerOutBoxJpaRepository;
import com.ryuqq.crawlinghub.application.schedule.port.out.query.CrawlSchedulerOutBoxQueryPort;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerOutBox;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerOubBoxStatus;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

/**
 * CrawlSchedulerOutBoxQueryAdapter - CrawlSchedulerOutBox Query Adapter
 *
 * <p>CQRS의 Query(읽기) 담당 Adapter입니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlSchedulerOutBoxQueryAdapter implements CrawlSchedulerOutBoxQueryPort {

    private final CrawlSchedulerOutBoxJpaRepository jpaRepository;
    private final CrawlSchedulerOutBoxJpaEntityMapper mapper;

    public CrawlSchedulerOutBoxQueryAdapter(
            CrawlSchedulerOutBoxJpaRepository jpaRepository,
            CrawlSchedulerOutBoxJpaEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
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
                jpaRepository.findByStatus(status, PageRequest.of(0, limit));

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
                jpaRepository.findByStatusIn(statuses, PageRequest.of(0, limit));

        return entities.stream().map(mapper::toDomain).toList();
    }
}
