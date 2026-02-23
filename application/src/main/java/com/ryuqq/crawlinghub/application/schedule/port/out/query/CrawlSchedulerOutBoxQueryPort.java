package com.ryuqq.crawlinghub.application.schedule.port.out.query;

import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerOutBox;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerHistoryId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerOubBoxStatus;
import java.util.List;
import java.util.Optional;

/**
 * 크롤 스케줄러 아웃박스 조회 Port (Port Out).
 *
 * @author development-team
 * @since 1.0.0
 */
public interface CrawlSchedulerOutBoxQueryPort {

    /**
     * 히스토리 ID로 아웃박스 조회
     *
     * @param historyId 히스토리 ID
     * @return 아웃박스 (Optional)
     */
    Optional<CrawlSchedulerOutBox> findByHistoryId(CrawlSchedulerHistoryId historyId);

    /**
     * 상태별 아웃박스 목록 조회.
     *
     * @param status 아웃박스 상태
     * @param limit 조회 개수 제한
     * @return 아웃박스 목록
     */
    List<CrawlSchedulerOutBox> findByStatus(CrawlSchedulerOubBoxStatus status, int limit);

    /**
     * 지정 시간(초) 이상 경과한 PENDING 상태의 아웃박스 조회.
     *
     * @param limit 조회 개수 제한
     * @param delaySeconds 최소 경과 시간 (초)
     * @return PENDING 아웃박스 목록
     */
    List<CrawlSchedulerOutBox> findPendingOlderThan(int limit, int delaySeconds);

    /**
     * 지정 시간(초) 이상 PROCESSING 상태인 좀비 아웃박스 조회.
     *
     * @param limit 조회 개수 제한
     * @param timeoutSeconds 타임아웃 기준 (초)
     * @return PROCESSING 좀비 아웃박스 목록
     */
    List<CrawlSchedulerOutBox> findStaleProcessing(int limit, long timeoutSeconds);
}
