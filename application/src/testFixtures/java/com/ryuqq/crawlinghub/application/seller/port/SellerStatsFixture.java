package com.ryuqq.crawlinghub.application.seller.port;

/**
 * SellerStats Test Fixture
 *
 * @author Cascade
 * @since 2025-10-31
 */
public class SellerStatsFixture {

    private static final Integer DEFAULT_TOTAL_SCHEDULES = 10;
    private static final Integer DEFAULT_ACTIVE_SCHEDULES = 8;
    private static final Integer DEFAULT_TOTAL_TASKS = 100;
    private static final Integer DEFAULT_SUCCESSFUL_TASKS = 95;
    private static final Integer DEFAULT_FAILED_TASKS = 5;

    /**
     * 기본 SellerStats 생성
     *
     * @return SellerStats
     */
    public static SellerStats create() {
        return new SellerStats(
            DEFAULT_TOTAL_SCHEDULES,
            DEFAULT_ACTIVE_SCHEDULES,
            DEFAULT_TOTAL_TASKS,
            DEFAULT_SUCCESSFUL_TASKS,
            DEFAULT_FAILED_TASKS
        );
    }

    /**
     * 높은 성공률을 가진 SellerStats 생성 (98%)
     *
     * @return SellerStats
     */
    public static SellerStats createHighSuccessRate() {
        return new SellerStats(
            DEFAULT_TOTAL_SCHEDULES,
            DEFAULT_ACTIVE_SCHEDULES,
            100,
            98,
            2
        );
    }

    /**
     * 낮은 성공률을 가진 SellerStats 생성 (50%)
     *
     * @return SellerStats
     */
    public static SellerStats createLowSuccessRate() {
        return new SellerStats(
            DEFAULT_TOTAL_SCHEDULES,
            DEFAULT_ACTIVE_SCHEDULES,
            100,
            50,
            50
        );
    }

    /**
     * 태스크가 없는 SellerStats 생성
     *
     * @return SellerStats
     */
    public static SellerStats createNoTasks() {
        return new SellerStats(
            0,
            0,
            0,
            0,
            0
        );
    }

    /**
     * 활성 스케줄이 없는 SellerStats 생성
     *
     * @return SellerStats
     */
    public static SellerStats createNoActiveSchedules() {
        return new SellerStats(
            5,
            0,
            DEFAULT_TOTAL_TASKS,
            DEFAULT_SUCCESSFUL_TASKS,
            DEFAULT_FAILED_TASKS
        );
    }

    /**
     * 완전한 커스텀 SellerStats 생성
     *
     * @param totalSchedules  총 스케줄 수
     * @param activeSchedules 활성 스케줄 수
     * @param totalCrawlTasks 총 크롤링 태스크 수
     * @param successfulTasks 성공한 태스크 수
     * @param failedTasks     실패한 태스크 수
     * @return SellerStats
     */
    public static SellerStats createCustom(
        Integer totalSchedules,
        Integer activeSchedules,
        Integer totalCrawlTasks,
        Integer successfulTasks,
        Integer failedTasks
    ) {
        return new SellerStats(
            totalSchedules,
            activeSchedules,
            totalCrawlTasks,
            successfulTasks,
            failedTasks
        );
    }
}
