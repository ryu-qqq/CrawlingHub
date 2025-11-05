package com.ryuqq.crawlinghub.application.mustit.seller.assembler;

import org.springframework.stereotype.Component;

import com.ryuqq.crawlinghub.application.common.dto.PageResponse;
import com.ryuqq.crawlinghub.application.mustit.seller.dto.response.ProductCountHistoryResponse;
import com.ryuqq.crawlinghub.application.mustit.seller.dto.response.ScheduleHistoryResponse;
import com.ryuqq.crawlinghub.application.mustit.seller.dto.response.ScheduleInfoResponse;
import com.ryuqq.crawlinghub.application.mustit.seller.dto.response.SellerDetailResponse;
import com.ryuqq.crawlinghub.application.mustit.seller.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.application.mustit.seller.port.out.LoadSellerStatsPort;
import com.ryuqq.crawlinghub.domain.mustit.seller.MustitSeller;
import com.ryuqq.crawlinghub.domain.mustit.seller.history.ProductCountHistory;

/**
 * 셀러 Assembler
 *
 * <p>Domain 객체와 DTO 간 변환을 담당합니다.
 * Law of Demeter를 준수하여 직접적인 getter 체이닝을 피합니다.
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
     * Domain + Stats → DetailResponse DTO 변환 (Static 메서드 - 기존 호환성 유지)
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

        // 기존 SellerDetailResponse 구조는 변경되었으므로, 새로운 구조로 변환
        return new SellerDetailResponse(
            seller.getIdValue(),
            seller.getSellerCode(),
            seller.getSellerName(),
            seller.getStatus().name(),
            seller.getTotalProductCount(),
            PageResponse.empty(0, 10), // 기본값
            null, // scheduleInfo
            PageResponse.empty(0, 10) // 기본값
        );
    }

    /**
     * SellerDetailResponse 생성 (확장된 버전) ⭐
     *
     * @param seller 셀러 Domain 객체
     * @param totalProductCount 총 상품 수
     * @param productCountHistories 상품 수 변경 이력
     * @param scheduleInfo 스케줄 정보
     * @param scheduleHistories 스케줄 실행 이력
     * @return SellerDetailResponse
     */
    public SellerDetailResponse toSellerDetailResponse(
        MustitSeller seller,
        Integer totalProductCount,
        PageResponse<ProductCountHistoryResponse> productCountHistories,
        ScheduleInfoResponse scheduleInfo,
        PageResponse<ScheduleHistoryResponse> scheduleHistories
    ) {
        return new SellerDetailResponse(
            seller.getIdValue(),
            seller.getSellerCode(),
            seller.getSellerName(),
            seller.getStatus().name(),
            totalProductCount,
            productCountHistories,
            scheduleInfo,
            scheduleHistories
        );
    }

    /**
     * ProductCountHistory → ProductCountHistoryResponse 변환
     *
     * @param history ProductCountHistory Domain 객체
     * @return ProductCountHistoryResponse
     */
    public ProductCountHistoryResponse toProductCountHistoryResponse(ProductCountHistory history) {
        return new ProductCountHistoryResponse(
            history.getId() != null ? history.getId().value() : null,
            history.getExecutedDate(),
            history.getProductCount()
        );
    }

    /**
     * CrawlSchedule → ScheduleInfoResponse 변환
     * TODO: 스케줄 관련 기능은 worktree에 없으므로 임시로 비활성화
     */
    /*
    public ScheduleInfoResponse toScheduleInfoResponse(CrawlSchedule schedule) {
        return new ScheduleInfoResponse(
            schedule.getIdValue(),
            schedule.getCronExpressionValue(),
            schedule.getStatus().name(),
            schedule.getNextExecutionTime(),
            schedule.getCreatedAt()
        );
    }
    */

    /**
     * CrawlScheduleHistory → ScheduleHistoryResponse 변환
     * TODO: 스케줄 관련 기능은 worktree에 없으므로 임시로 비활성화
     */
    /*
    public ScheduleHistoryResponse toScheduleHistoryResponse(CrawlScheduleHistory history) {
        // ScheduleExecutionStatus를 문자열로 변환 (COMPLETED -> SUCCESS, FAILED -> FAILURE)
        String statusStr = convertStatusToString(history.getStatus());
        
        return new ScheduleHistoryResponse(
            history.getIdValue(),
            history.getExecutedAt(),
            calculateCompletedAt(history),
            statusStr,
            history.getErrorMessage()
        );
    }

    private String convertStatusToString(ScheduleExecutionStatus status) {
        if (status == ScheduleExecutionStatus.COMPLETED) {
            return "SUCCESS";
        } else if (status == ScheduleExecutionStatus.FAILED) {
            return "FAILURE";
        } else {
            return status.name();
        }
    }

    private java.time.LocalDateTime calculateCompletedAt(CrawlScheduleHistory history) {
        if (history.getExecutionDurationMs() == null) {
            return null;
        }
        return history.getExecutedAt().plusNanos(history.getExecutionDurationMs() * 1_000_000L);
    }
    */
}
