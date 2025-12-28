package com.ryuqq.crawlinghub.application.schedule.port.out.query;

import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.identifier.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerQueryCriteria;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.util.List;
import java.util.Optional;

/** 크롤 스케줄러 조회 Port (Port Out). */
public interface CrawlScheduleQueryPort {

    /**
     * 크롤 스케줄러 ID로 단건 조회.
     *
     * @param crawlSchedulerId 크롤 스케줄러 ID
     * @return 크롤 스케줄러 (Optional)
     */
    Optional<CrawlScheduler> findById(CrawlSchedulerId crawlSchedulerId);

    /**
     * 셀러 ID와 스케줄러 이름으로 존재 여부 확인.
     *
     * @param sellerId 셀러 ID
     * @param schedulerName 스케줄러 이름
     * @return 존재 여부
     */
    boolean existsBySellerIdAndSchedulerName(SellerId sellerId, String schedulerName);

    /**
     * 크롤 스케줄러 다건 조회.
     *
     * <p>등록 최신순(createdAt DESC) 정렬
     *
     * @param criteria 조회 조건 (sellerId, status, page, size)
     * @return 크롤 스케줄러 리스트
     */
    List<CrawlScheduler> findByCriteria(CrawlSchedulerQueryCriteria criteria);

    /**
     * 크롤 스케줄러 총 개수 조회.
     *
     * @param criteria 조회 조건 (sellerId, status)
     * @return 총 개수
     */
    long count(CrawlSchedulerQueryCriteria criteria);

    /**
     * 셀러별 활성 스케줄러 목록 조회.
     *
     * <p>Seller 비활성화 시 해당 셀러의 모든 활성 스케줄러를 비활성화하기 위해 사용
     *
     * @param sellerId 셀러 ID
     * @return 활성 스케줄러 리스트
     */
    List<CrawlScheduler> findActiveSchedulersBySellerId(SellerId sellerId);

    /**
     * 셀러별 전체 스케줄러 개수 조회.
     *
     * @param sellerId 셀러 ID
     * @return 전체 스케줄러 개수
     */
    long countBySellerId(SellerId sellerId);

    /**
     * 셀러별 활성 스케줄러 개수 조회.
     *
     * @param sellerId 셀러 ID
     * @return 활성 스케줄러 개수
     */
    long countActiveSchedulersBySellerId(SellerId sellerId);

    /**
     * 셀러별 전체 스케줄러 목록 조회.
     *
     * <p>셀러 상세 조회 시 연관 스케줄러 목록을 표시하기 위해 사용
     *
     * @param sellerId 셀러 ID
     * @return 스케줄러 리스트 (생성일시 내림차순)
     */
    List<CrawlScheduler> findBySellerId(SellerId sellerId);
}
