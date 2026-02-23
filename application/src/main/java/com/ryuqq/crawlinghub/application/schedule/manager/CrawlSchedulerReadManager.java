package com.ryuqq.crawlinghub.application.schedule.manager;

import com.ryuqq.crawlinghub.application.schedule.port.out.query.CrawlScheduleQueryPort;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.exception.CrawlSchedulerNotFoundException;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.schedule.query.CrawlSchedulerSearchCriteria;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional(readOnly = true)
    public Optional<CrawlScheduler> findById(CrawlSchedulerId crawlSchedulerId) {
        return crawlScheduleQueryPort.findById(crawlSchedulerId);
    }

    /**
     * 크롤 스케줄러 ID로 단건 조회 (필수)
     *
     * <p>존재하지 않으면 CrawlSchedulerNotFoundException 발생
     *
     * @param crawlSchedulerId 크롤 스케줄러 ID
     * @return 크롤 스케줄러
     * @throws CrawlSchedulerNotFoundException 스케줄러가 존재하지 않는 경우
     */
    @Transactional(readOnly = true)
    public CrawlScheduler getById(CrawlSchedulerId crawlSchedulerId) {
        return crawlScheduleQueryPort
                .findById(crawlSchedulerId)
                .orElseThrow(() -> new CrawlSchedulerNotFoundException(crawlSchedulerId.value()));
    }

    /**
     * 셀러 ID와 스케줄러 이름으로 존재 여부 확인
     *
     * @param sellerId 셀러 ID
     * @param schedulerName 스케줄러 이름
     * @return 존재 여부
     */
    @Transactional(readOnly = true)
    public boolean existsBySellerIdAndSchedulerName(SellerId sellerId, String schedulerName) {
        return crawlScheduleQueryPort.existsBySellerIdAndSchedulerName(sellerId, schedulerName);
    }

    /**
     * 크롤 스케줄러 다건 조회
     *
     * @param criteria 조회 조건
     * @return 크롤 스케줄러 리스트
     */
    @Transactional(readOnly = true)
    public List<CrawlScheduler> findByCriteria(CrawlSchedulerSearchCriteria criteria) {
        return crawlScheduleQueryPort.findByCriteria(criteria);
    }

    /**
     * 크롤 스케줄러 총 개수 조회
     *
     * @param criteria 조회 조건
     * @return 총 개수
     */
    @Transactional(readOnly = true)
    public long countByCriteria(CrawlSchedulerSearchCriteria criteria) {
        return crawlScheduleQueryPort.countByCriteria(criteria);
    }

    /**
     * 셀러별 활성 스케줄러 목록 조회
     *
     * @param sellerId 셀러 ID
     * @return 활성 스케줄러 리스트
     */
    @Transactional(readOnly = true)
    public List<CrawlScheduler> findActiveSchedulersBySellerId(SellerId sellerId) {
        return crawlScheduleQueryPort.findActiveSchedulersBySellerId(sellerId);
    }

    /**
     * 셀러별 전체 스케줄러 개수 조회
     *
     * @param sellerId 셀러 ID
     * @return 전체 스케줄러 개수
     */
    @Transactional(readOnly = true)
    public long countBySellerId(SellerId sellerId) {
        return crawlScheduleQueryPort.countBySellerId(sellerId);
    }

    /**
     * 셀러별 활성 스케줄러 개수 조회
     *
     * @param sellerId 셀러 ID
     * @return 활성 스케줄러 개수
     */
    @Transactional(readOnly = true)
    public long countActiveSchedulersBySellerId(SellerId sellerId) {
        return crawlScheduleQueryPort.countActiveSchedulersBySellerId(sellerId);
    }

    /**
     * 셀러별 전체 스케줄러 목록 조회
     *
     * @param sellerId 셀러 ID
     * @return 스케줄러 목록 (생성일시 내림차순)
     */
    @Transactional(readOnly = true)
    public List<CrawlScheduler> findBySellerId(SellerId sellerId) {
        return crawlScheduleQueryPort.findBySellerId(sellerId);
    }
}
