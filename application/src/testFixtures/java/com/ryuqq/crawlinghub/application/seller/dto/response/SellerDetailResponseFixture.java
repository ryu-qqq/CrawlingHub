package com.ryuqq.crawlinghub.application.seller.dto.response;

import com.ryuqq.crawlinghub.application.common.dto.PageResponse;

import java.time.LocalDateTime;
import java.util.Collections;

/**
 * SellerDetailResponse Test Fixture
 *
 * @author Cascade
 * @since 2025-10-31
 */
public class SellerDetailResponseFixture {

    private static final Long DEFAULT_SELLER_ID = 1L;
    private static final String DEFAULT_SELLER_CODE = "SELLER001";
    private static final String DEFAULT_SELLER_NAME = "테스트 셀러";
    private static final String DEFAULT_STATUS = "ACTIVE";
    private static final Integer DEFAULT_TOTAL_PRODUCT_COUNT = 100;

    /**
     * 기본 SellerDetailResponse 생성
     *
     * @return SellerDetailResponse
     */
    public static SellerDetailResponse create() {
        return new SellerDetailResponse(
            DEFAULT_SELLER_ID,
            DEFAULT_SELLER_CODE,
            DEFAULT_SELLER_NAME,
            DEFAULT_STATUS,
            DEFAULT_TOTAL_PRODUCT_COUNT,
            createEmptyProductCountHistories(),
            createDefaultScheduleInfo(),
            createEmptyScheduleHistories()
        );
    }

    /**
     * 특정 셀러 ID로 SellerDetailResponse 생성
     *
     * @param sellerId 셀러 ID
     * @return SellerDetailResponse
     */
    public static SellerDetailResponse createWithSellerId(Long sellerId) {
        return new SellerDetailResponse(
            sellerId,
            DEFAULT_SELLER_CODE,
            DEFAULT_SELLER_NAME,
            DEFAULT_STATUS,
            DEFAULT_TOTAL_PRODUCT_COUNT,
            createEmptyProductCountHistories(),
            createDefaultScheduleInfo(),
            createEmptyScheduleHistories()
        );
    }

    /**
     * 상품 수 이력이 포함된 SellerDetailResponse 생성
     *
     * @param productCountHistories 상품 수 변경 이력
     * @return SellerDetailResponse
     */
    public static SellerDetailResponse createWithProductCountHistories(
        PageResponse<ProductCountHistoryResponse> productCountHistories
    ) {
        return new SellerDetailResponse(
            DEFAULT_SELLER_ID,
            DEFAULT_SELLER_CODE,
            DEFAULT_SELLER_NAME,
            DEFAULT_STATUS,
            DEFAULT_TOTAL_PRODUCT_COUNT,
            productCountHistories,
            createDefaultScheduleInfo(),
            createEmptyScheduleHistories()
        );
    }

    /**
     * 스케줄 정보가 포함된 SellerDetailResponse 생성
     *
     * @param scheduleInfo 크롤링 스케줄 정보
     * @return SellerDetailResponse
     */
    public static SellerDetailResponse createWithScheduleInfo(
        ScheduleInfoResponse scheduleInfo
    ) {
        return new SellerDetailResponse(
            DEFAULT_SELLER_ID,
            DEFAULT_SELLER_CODE,
            DEFAULT_SELLER_NAME,
            DEFAULT_STATUS,
            DEFAULT_TOTAL_PRODUCT_COUNT,
            createEmptyProductCountHistories(),
            scheduleInfo,
            createEmptyScheduleHistories()
        );
    }

    /**
     * 스케줄 이력이 포함된 SellerDetailResponse 생성
     *
     * @param scheduleHistories 크롤링 실행 이력
     * @return SellerDetailResponse
     */
    public static SellerDetailResponse createWithScheduleHistories(
        PageResponse<ScheduleHistoryResponse> scheduleHistories
    ) {
        return new SellerDetailResponse(
            DEFAULT_SELLER_ID,
            DEFAULT_SELLER_CODE,
            DEFAULT_SELLER_NAME,
            DEFAULT_STATUS,
            DEFAULT_TOTAL_PRODUCT_COUNT,
            createEmptyProductCountHistories(),
            createDefaultScheduleInfo(),
            scheduleHistories
        );
    }

    /**
     * 완전한 커스텀 SellerDetailResponse 생성
     *
     * @param sellerId 셀러 ID
     * @param sellerCode 셀러 코드
     * @param sellerName 셀러명
     * @param status 상태
     * @param totalProductCount 총 상품 수
     * @param productCountHistories 상품 수 변경 이력
     * @param scheduleInfo 크롤링 스케줄 정보
     * @param scheduleHistories 크롤링 실행 이력
     * @return SellerDetailResponse
     */
    public static SellerDetailResponse createCustom(
        Long sellerId,
        String sellerCode,
        String sellerName,
        String status,
        Integer totalProductCount,
        PageResponse<ProductCountHistoryResponse> productCountHistories,
        ScheduleInfoResponse scheduleInfo,
        PageResponse<ScheduleHistoryResponse> scheduleHistories
    ) {
        return new SellerDetailResponse(
            sellerId,
            sellerCode,
            sellerName,
            status,
            totalProductCount,
            productCountHistories,
            scheduleInfo,
            scheduleHistories
        );
    }

    // ========================================
    // Private Helper Methods
    // ========================================

    private static PageResponse<ProductCountHistoryResponse> createEmptyProductCountHistories() {
        return new PageResponse<>(
            Collections.emptyList(),
            0,          // page
            10,         // size
            0L,         // totalElements
            0,          // totalPages
            true,       // first
            true        // last
        );
    }

    private static ScheduleInfoResponse createDefaultScheduleInfo() {
        return new ScheduleInfoResponse(
            1L,                           // scheduleId
            "0 0 * * *",                  // cronExpression
            "ACTIVE",                     // status
            LocalDateTime.now().plusHours(1),  // nextExecutionTime
            LocalDateTime.now()           // createdAt
        );
    }

    private static PageResponse<ScheduleHistoryResponse> createEmptyScheduleHistories() {
        return new PageResponse<>(
            Collections.emptyList(),
            0,          // page
            10,         // size
            0L,         // totalElements
            0,          // totalPages
            true,       // first
            true        // last
        );
    }
}
