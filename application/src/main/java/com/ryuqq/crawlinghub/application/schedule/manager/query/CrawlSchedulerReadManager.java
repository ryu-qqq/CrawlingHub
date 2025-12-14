package com.ryuqq.crawlinghub.application.schedule.manager.query;

import com.ryuqq.crawlinghub.application.schedule.port.out.query.CrawlScheduleQueryPort;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.identifier.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerQueryCriteria;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * CrawlScheduler 조회 전용 Manager
 *
 * <p><strong>책임</strong>: CrawlScheduler 조회 작업 위임
 *
 * <p><strong>규칙</strong>: 단일 QueryPort만 의존, 트랜잭션 없음
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlSchedulerReadManager {

    private final CrawlScheduleQueryPort crawlScheduleQueryPort;

    public CrawlSchedulerReadManager(CrawlScheduleQueryPort crawlScheduleQueryPort) {
        this.crawlScheduleQueryPort = crawlScheduleQueryPort;
    }

    /**
     * 크롤 스케줄러 ID로 단건 조회
     *
     * @param crawlSchedulerId 크롤 스케줄러 ID
     * @return 크롤 스케줄러 (Optional)
     */
    public Optional<CrawlScheduler> findById(CrawlSchedulerId crawlSchedulerId) {
        return crawlScheduleQueryPort.findById(crawlSchedulerId);
    }

    /**
     * 셀러 ID와 스케줄러 이름으로 존재 여부 확인
     *
     * @param sellerId 셀러 ID
     * @param schedulerName 스케줄러 이름
     * @return 존재 여부
     */
    public boolean existsBySellerIdAndSchedulerName(SellerId sellerId, String schedulerName) {
        return crawlScheduleQueryPort.existsBySellerIdAndSchedulerName(sellerId, schedulerName);
    }

    /**
     * 크롤 스케줄러 다건 조회
     *
     * @param criteria 조회 조건
     * @return 크롤 스케줄러 리스트
     */
    public List<CrawlScheduler> findByCriteria(CrawlSchedulerQueryCriteria criteria) {
        return crawlScheduleQueryPort.findByCriteria(criteria);
    }

    /**
     * 크롤 스케줄러 총 개수 조회
     *
     * @param criteria 조회 조건
     * @return 총 개수
     */
    public long count(CrawlSchedulerQueryCriteria criteria) {
        return crawlScheduleQueryPort.count(criteria);
    }

    /**
     * 셀러별 활성 스케줄러 목록 조회
     *
     * @param sellerId 셀러 ID
     * @return 활성 스케줄러 리스트
     */
    public List<CrawlScheduler> findActiveSchedulersBySellerId(SellerId sellerId) {
        return crawlScheduleQueryPort.findActiveSchedulersBySellerId(sellerId);
    }
}
