package com.ryuqq.crawlinghub.application.seller.port.out.query;

import java.util.List;

/**
 * Scheduler 조회 Port.
 */
public interface SchedulerQueryPort {

    int countActiveSchedulersBySellerId(Long sellerId);

    int countTotalSchedulersBySellerId(Long sellerId);

    /**
     * 활성 스케줄러 ID 목록을 조회합니다.
     *
     * @param sellerId 셀러 ID
     * @return 활성 스케줄러 ID 목록
     */
    List<Long> findActiveSchedulerIdsBySellerId(Long sellerId);
}

