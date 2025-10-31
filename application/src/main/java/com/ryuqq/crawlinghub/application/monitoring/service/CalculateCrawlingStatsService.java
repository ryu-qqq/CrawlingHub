package com.ryuqq.crawlinghub.application.monitoring.service;


import com.ryuqq.crawlinghub.application.monitoring.assembler.MonitoringAssembler;
import com.ryuqq.crawlinghub.application.monitoring.dto.query.CrawlingStatsQuery;
import com.ryuqq.crawlinghub.application.monitoring.dto.response.CrawlingStatsResponse;
import com.ryuqq.crawlinghub.application.monitoring.port.in.CalculateCrawlingStatsUseCase;
import com.ryuqq.crawlinghub.application.monitoring.port.out.CrawlingStatsPort;
import com.ryuqq.crawlinghub.domain.mustit.seller.MustitSellerId;
import org.springframework.transaction.annotation.Transactional;

/**
 * 크롤링 통계 계산 UseCase 구현체
 *
 * <p>태스크 및 상품 크롤링 통계를 집계하여 반환합니다.
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
@Service
public class CalculateCrawlingStatsService implements CalculateCrawlingStatsUseCase {

    private final CrawlingStatsPort crawlingStatsPort;

    public CalculateCrawlingStatsService(CrawlingStatsPort crawlingStatsPort) {
        this.crawlingStatsPort = crawlingStatsPort;
    }

    /**
     * 크롤링 통계 계산
     *
     * <p>실행 순서:
     * 1. 셀러 필터 여부 확인
     * 2. 태스크 통계 조회
     * 3. 상품 통계 조회
     * 4. 응답 생성
     *
     * @param query 통계 조회 Query
     * @return 크롤링 통계
     */
    @Override
    @Transactional(readOnly = true)
    public CrawlingStatsResponse execute(CrawlingStatsQuery query) {
        // 1. 태스크 통계 조회
        CrawlingStatsPort.TaskStats taskStats;
        CrawlingStatsPort.ProductStats productStats;

        if (query.hasSellerFilter()) {
            // 특정 셀러 통계
            MustitSellerId sellerId = MustitSellerId.of(query.sellerId());
            taskStats = crawlingStatsPort.getTaskStats(
                sellerId,
                query.startDate(),
                query.endDate()
            );
            productStats = crawlingStatsPort.getProductStats(
                sellerId,
                query.startDate(),
                query.endDate()
            );
        } else {
            // 전체 통계
            taskStats = crawlingStatsPort.getAllTaskStats(
                query.startDate(),
                query.endDate()
            );
            productStats = crawlingStatsPort.getAllProductStats(
                query.startDate(),
                query.endDate()
            );
        }

        // 2. 응답 생성
        return MonitoringAssembler.toCrawlingStatsResponse(
            query.sellerId(),
            query.startDate(),
            query.endDate(),
            taskStats,
            productStats
        );
    }
}
