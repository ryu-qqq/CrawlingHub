package com.ryuqq.crawlinghub.application.mustit.seller.dto.response;

/**
 * SellerDetailResponse Test Fixture
 *
 * @author Cascade
 * @since 2025-10-31
 */
public class SellerDetailResponseFixture {

    private static final Integer DEFAULT_TOTAL_SCHEDULES = 10;
    private static final Integer DEFAULT_ACTIVE_SCHEDULES = 8;
    private static final Integer DEFAULT_TOTAL_TASKS = 100;
    private static final Integer DEFAULT_SUCCESSFUL_TASKS = 95;
    private static final Integer DEFAULT_FAILED_TASKS = 5;

    /**
     * 기본 SellerDetailResponse 생성
     *
     * @return SellerDetailResponse
     */
    public static SellerDetailResponse create() {
        return new SellerDetailResponse(
            SellerResponseFixture.create(),
            DEFAULT_TOTAL_SCHEDULES,
            DEFAULT_ACTIVE_SCHEDULES,
            DEFAULT_TOTAL_TASKS,
            DEFAULT_SUCCESSFUL_TASKS,
            DEFAULT_FAILED_TASKS
        );
    }

    /**
     * 특정 SellerResponse로 SellerDetailResponse 생성
     *
     * @param sellerResponse 셀러 기본 정보
     * @return SellerDetailResponse
     */
    public static SellerDetailResponse createWithSeller(SellerResponse sellerResponse) {
        return new SellerDetailResponse(
            sellerResponse,
            DEFAULT_TOTAL_SCHEDULES,
            DEFAULT_ACTIVE_SCHEDULES,
            DEFAULT_TOTAL_TASKS,
            DEFAULT_SUCCESSFUL_TASKS,
            DEFAULT_FAILED_TASKS
        );
    }

    /**
     * 높은 성공률을 가진 SellerDetailResponse 생성 (98%)
     *
     * @return SellerDetailResponse
     */
    public static SellerDetailResponse createHighSuccessRate() {
        return new SellerDetailResponse(
            SellerResponseFixture.create(),
            DEFAULT_TOTAL_SCHEDULES,
            DEFAULT_ACTIVE_SCHEDULES,
            100,
            98,
            2
        );
    }

    /**
     * 낮은 성공률을 가진 SellerDetailResponse 생성 (50%)
     *
     * @return SellerDetailResponse
     */
    public static SellerDetailResponse createLowSuccessRate() {
        return new SellerDetailResponse(
            SellerResponseFixture.create(),
            DEFAULT_TOTAL_SCHEDULES,
            DEFAULT_ACTIVE_SCHEDULES,
            100,
            50,
            50
        );
    }

    /**
     * 태스크가 없는 SellerDetailResponse 생성
     *
     * @return SellerDetailResponse
     */
    public static SellerDetailResponse createNoTasks() {
        return new SellerDetailResponse(
            SellerResponseFixture.create(),
            0,
            0,
            0,
            0,
            0
        );
    }

    /**
     * 활성 스케줄이 없는 SellerDetailResponse 생성
     *
     * @return SellerDetailResponse
     */
    public static SellerDetailResponse createNoActiveSchedules() {
        return new SellerDetailResponse(
            SellerResponseFixture.create(),
            5,
            0,
            DEFAULT_TOTAL_TASKS,
            DEFAULT_SUCCESSFUL_TASKS,
            DEFAULT_FAILED_TASKS
        );
    }

    /**
     * 완전한 커스텀 SellerDetailResponse 생성
     *
     * @param sellerResponse    셀러 기본 정보
     * @param totalSchedules    총 스케줄 수
     * @param activeSchedules   활성 스케줄 수
     * @param totalCrawlTasks   총 크롤링 태스크 수
     * @param successfulTasks   성공한 태스크 수
     * @param failedTasks       실패한 태스크 수
     * @return SellerDetailResponse
     */
    public static SellerDetailResponse createCustom(
        SellerResponse sellerResponse,
        Integer totalSchedules,
        Integer activeSchedules,
        Integer totalCrawlTasks,
        Integer successfulTasks,
        Integer failedTasks
    ) {
        return new SellerDetailResponse(
            sellerResponse,
            totalSchedules,
            activeSchedules,
            totalCrawlTasks,
            successfulTasks,
            failedTasks
        );
    }
}
