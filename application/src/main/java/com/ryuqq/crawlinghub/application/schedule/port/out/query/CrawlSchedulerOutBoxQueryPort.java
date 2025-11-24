package com.ryuqq.crawlinghub.application.schedule.port.out.query;

import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerOutBox;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerOubBoxStatus;
import java.util.List;

/**
 * 크롤 스케줄러 아웃박스 조회 Port (Port Out).
 *
 * <p><strong>용도</strong>: 재시도 스케줄러에서 PENDING/FAILED 상태 아웃박스 조회
 *
 * @author development-team
 * @since 1.0.0
 */
public interface CrawlSchedulerOutBoxQueryPort {

    /**
     * 상태별 아웃박스 목록 조회.
     *
     * @param status 아웃박스 상태
     * @param limit 조회 개수 제한
     * @return 아웃박스 목록
     */
    List<CrawlSchedulerOutBox> findByStatus(CrawlSchedulerOubBoxStatus status, int limit);

    /**
     * PENDING 또는 FAILED 상태의 아웃박스 목록 조회.
     *
     * @param limit 조회 개수 제한
     * @return 재처리 대상 아웃박스 목록
     */
    List<CrawlSchedulerOutBox> findPendingOrFailed(int limit);
}
