package com.ryuqq.crawlinghub.application.mustit.seller.assembler;

import com.ryuqq.crawlinghub.application.common.dto.PageResponse;
import com.ryuqq.crawlinghub.application.mustit.seller.dto.response.ProductCountHistoryResponse;
import com.ryuqq.crawlinghub.application.mustit.seller.dto.response.ScheduleHistoryResponse;
import com.ryuqq.crawlinghub.application.mustit.seller.dto.response.ScheduleInfoResponse;
import com.ryuqq.crawlinghub.application.mustit.seller.dto.response.SellerDetailResponse;
import com.ryuqq.crawlinghub.application.mustit.seller.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.application.mustit.seller.port.out.LoadSellerStatsPort;
import com.ryuqq.crawlinghub.domain.crawl.schedule.CrawlSchedule;
import com.ryuqq.crawlinghub.domain.crawl.schedule.history.CrawlScheduleHistory;
import com.ryuqq.crawlinghub.domain.crawl.schedule.history.ScheduleExecutionStatus;
import com.ryuqq.crawlinghub.domain.mustit.seller.MustitSeller;
import com.ryuqq.crawlinghub.domain.mustit.seller.history.ProductCountHistory;
import org.springframework.stereotype.Component;

/**
 * 셀러 Assembler
 *
 * <p>Domain 객체와 DTO 간 변환을 담당합니다.
 * Law of Demeter를 준수하여 직접적인 getter 체이닝을 피합니다.
 *
 * <p>주요 책임:
 * <ul>
 *   <li>Domain → Response DTO 변환</li>
 *   <li>스케줄 정보 변환</li>
 *   <li>스케줄 히스토리 변환</li>
 *   <li>상품 수 이력 변환</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
@Component
public class SellerAssembler {

    /**
     * Domain → Response DTO 변환 (Static 메서드 - 기존 호환성 유지)
     *
     * @param seller 도메인 셀러 객체 (null 불가)
     * @return SellerResponse
     * @throws IllegalArgumentException seller가 null인 경우
     */
    public static SellerResponse toResponse(MustitSeller seller) {
        if (seller == null) {
            throw new IllegalArgumentException("seller must not be null");
        }

        return new SellerResponse(
            seller.getIdValue(),
            seller.getSellerCode(),
            seller.getSellerName(),
            seller.getStatus(),
            seller.getTotalProductCount(),
            seller.getLastCrawledAt(),
            seller.getCreatedAt(),
            seller.getUpdatedAt()
        );
    }

    /**
     * Domain + Stats → DetailResponse DTO 변환 (기존 호환성 유지)
     *
     * @param seller 도메인 셀러 객체 (null 불가)
     * @param stats  셀러 통계 정보 (null 불가)
     * @return SellerDetailResponse
     * @throws IllegalArgumentException seller 또는 stats가 null인 경우
     */
    public static SellerDetailResponse toDetailResponse(
        MustitSeller seller,
        LoadSellerStatsPort.SellerStats stats
    ) {
        if (seller == null) {
            throw new IllegalArgumentException("seller must not be null");
        }
        if (stats == null) {
            throw new IllegalArgumentException("stats must not be null");
        }

        return new SellerDetailResponse(
            toResponse(seller),
            stats.totalSchedules(),
            stats.activeSchedules(),
            stats.totalCrawlTasks(),
            stats.successfulTasks(),
            stats.failedTasks()
        );
    }

    /**
     * SellerDetailResponse 생성 (확장된 버전) ⭐
     *
     * <p>확장된 필드들을 포함한 상세 응답을 생성합니다.
     *
     * @param seller 셀러 Domain 객체 (null 불가)
     * @param totalProductCount 총 상품 수
     * @param productCountHistories 상품 수 변경 이력
     * @param scheduleInfo 스케줄 정보 (null 가능)
     * @param scheduleHistories 스케줄 실행 이력
     * @param stats 셀러 통계 정보 (null 불가)
     * @return SellerDetailResponse
     * @throws IllegalArgumentException seller 또는 stats가 null인 경우
     */
    public SellerDetailResponse toSellerDetailResponse(
        MustitSeller seller,
        Integer totalProductCount,
        PageResponse<ProductCountHistoryResponse> productCountHistories,
        ScheduleInfoResponse scheduleInfo,
        PageResponse<ScheduleHistoryResponse> scheduleHistories,
        LoadSellerStatsPort.SellerStats stats
    ) {
        if (seller == null) {
            throw new IllegalArgumentException("seller must not be null");
        }
        if (stats == null) {
            throw new IllegalArgumentException("stats must not be null");
        }

        return new SellerDetailResponse(
            toResponse(seller),
            stats.totalSchedules(),
            stats.activeSchedules(),
            stats.totalCrawlTasks(),
            stats.successfulTasks(),
            stats.failedTasks(),
            productCountHistories,
            scheduleInfo,
            scheduleHistories
        );
    }

    /**
     * ProductCountHistory → ProductCountHistoryResponse 변환
     *
     * @param history ProductCountHistory Domain 객체 (null 불가)
     * @return ProductCountHistoryResponse
     * @throws IllegalArgumentException history가 null인 경우
     */
    public ProductCountHistoryResponse toProductCountHistoryResponse(ProductCountHistory history) {
        if (history == null) {
            throw new IllegalArgumentException("history must not be null");
        }

        return new ProductCountHistoryResponse(
            history.getId() != null ? history.getId().value() : null,
            history.getExecutedDate(),
            history.getProductCount()
        );
    }

    /**
     * CrawlSchedule → ScheduleInfoResponse 변환 ⭐
     *
     * <p>Law of Demeter 준수: Domain의 getter 메서드만 사용합니다.
     *
     * @param schedule CrawlSchedule Domain 객체 (null 불가)
     * @return ScheduleInfoResponse
     * @throws IllegalArgumentException schedule이 null인 경우
     */
    public ScheduleInfoResponse toScheduleInfoResponse(CrawlSchedule schedule) {
        if (schedule == null) {
            throw new IllegalArgumentException("schedule must not be null");
        }

        return new ScheduleInfoResponse(
            schedule.getIdValue(),
            schedule.getCronExpressionValue(),
            schedule.getStatus().name(),
            schedule.getNextExecutionTime(),
            schedule.getCreatedAt()
        );
    }

    /**
     * CrawlScheduleHistory → ScheduleHistoryResponse 변환 ⭐
     *
     * <p>Domain의 toResponse() 메서드를 활용하여 데이터를 받고,
     * 이를 Seller 패키지의 ScheduleHistoryResponse 형식으로 변환합니다.
     *
     * <p>주의: Seller 패키지의 ScheduleHistoryResponse는 다른 형식입니다.
     * <ul>
     *   <li>startedAt: executedAt과 동일</li>
     *   <li>completedAt: executedAt + executionDurationMs</li>
     *   <li>status: COMPLETED → "SUCCESS", FAILED → "FAILURE"</li>
     *   <li>message: errorMessage</li>
     * </ul>
     *
     * @param history CrawlScheduleHistory Domain 객체 (null 불가)
     * @return ScheduleHistoryResponse
     * @throws IllegalArgumentException history가 null인 경우
     */
    public ScheduleHistoryResponse toScheduleHistoryResponse(CrawlScheduleHistory history) {
        if (history == null) {
            throw new IllegalArgumentException("history must not be null");
        }

        // Domain의 toResponse() 활용 (Tell, Don't Ask)
        var data = history.toResponse();

        // 상태 변환 (COMPLETED → SUCCESS, FAILED → FAILURE)
        String statusStr = convertStatusToString(data.status());

        // 완료 시간 계산 (executedAt + executionDurationMs)
        java.time.LocalDateTime completedAt = calculateCompletedAt(
            data.executedAt(),
            data.executionDurationMs()
        );

        return new ScheduleHistoryResponse(
            data.historyId(),
            data.executedAt(),
            completedAt,
            statusStr,
            data.errorMessage()
        );
    }

    /**
     * ScheduleExecutionStatus → 문자열 변환
     *
     * <p>COMPLETED → "SUCCESS", FAILED → "FAILURE", 기타 → 상태명 그대로
     *
     * @param status 실행 상태
     * @return 상태 문자열
     */
    private String convertStatusToString(ScheduleExecutionStatus status) {
        if (status == ScheduleExecutionStatus.COMPLETED) {
            return "SUCCESS";
        } else if (status == ScheduleExecutionStatus.FAILED) {
            return "FAILURE";
        } else {
            return status.name();
        }
    }

    /**
     * 완료 시간 계산
     *
     * <p>executedAt + executionDurationMs로 완료 시간을 계산합니다.
     *
     * @param executedAt 실행 시작 시간
     * @param executionDurationMs 실행 소요 시간 (밀리초, null 가능)
     * @return 완료 시간 (executionDurationMs가 null이면 null)
     */
    private java.time.LocalDateTime calculateCompletedAt(
        java.time.LocalDateTime executedAt,
        Long executionDurationMs
    ) {
        if (executedAt == null || executionDurationMs == null) {
            return null;
        }
        return executedAt.plusNanos(executionDurationMs * 1_000_000L);
    }
}
