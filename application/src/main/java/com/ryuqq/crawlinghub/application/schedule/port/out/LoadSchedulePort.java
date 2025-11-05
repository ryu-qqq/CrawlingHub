package com.ryuqq.crawlinghub.application.schedule.port.out;

import com.ryuqq.crawlinghub.domain.schedule.CrawlSchedule;
import com.ryuqq.crawlinghub.domain.schedule.CrawlScheduleId;
import com.ryuqq.crawlinghub.domain.seller.MustitSellerId;

import java.util.List;
import java.util.Optional;

/**
 * 스케줄 조회 Port
 *
 * <p>Persistence Adapter에 의해 구현됩니다.
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public interface LoadSchedulePort {

    /**
     * ID로 스케줄 조회
     *
     * @param scheduleId 스케줄 ID
     * @return 스케줄 (없으면 Optional.empty())
     */
    Optional<CrawlSchedule> findById(CrawlScheduleId scheduleId);

    /**
     * 셀러의 활성 스케줄 조회
     *
     * @param sellerId 셀러 ID
     * @return 활성 스케줄 (없으면 Optional.empty())
     */
    Optional<CrawlSchedule> findActiveBySellerId(MustitSellerId sellerId);

    /**
     * 셀러의 모든 스케줄 조회
     *
     * @param sellerId 셀러 ID
     * @return 스케줄 목록
     */
    List<CrawlSchedule> findAllBySellerId(MustitSellerId sellerId);
}
