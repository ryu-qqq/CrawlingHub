package com.ryuqq.crawlinghub.application.crawl.schedule.dto.command;

/**
 * CreateScheduleCommand Test Fixture
 *
 * @author Cascade
 * @since 2025-10-31
 */
public class CreateScheduleCommandFixture {

    private static final Long DEFAULT_SELLER_ID = 1L;
    private static final String DEFAULT_CRON_EXPRESSION = "0 0 * * *";

    /**
     * 기본 CreateScheduleCommand 생성 (매일 자정 실행)
     *
     * @return CreateScheduleCommand
     */
    public static CreateScheduleCommand create() {
        return new CreateScheduleCommand(
            DEFAULT_SELLER_ID,
            DEFAULT_CRON_EXPRESSION
        );
    }

    /**
     * 특정 셀러 ID로 CreateScheduleCommand 생성
     *
     * @param sellerId 셀러 ID
     * @return CreateScheduleCommand
     */
    public static CreateScheduleCommand createWithSellerId(Long sellerId) {
        return new CreateScheduleCommand(
            sellerId,
            DEFAULT_CRON_EXPRESSION
        );
    }

    /**
     * 특정 Cron 표현식으로 CreateScheduleCommand 생성
     *
     * @param cronExpression Cron 표현식
     * @return CreateScheduleCommand
     */
    public static CreateScheduleCommand createWithCronExpression(String cronExpression) {
        return new CreateScheduleCommand(
            DEFAULT_SELLER_ID,
            cronExpression
        );
    }

    /**
     * 완전한 커스텀 CreateScheduleCommand 생성
     *
     * @param sellerId       셀러 ID
     * @param cronExpression Cron 표현식
     * @return CreateScheduleCommand
     */
    public static CreateScheduleCommand createCustom(
        Long sellerId,
        String cronExpression
    ) {
        return new CreateScheduleCommand(sellerId, cronExpression);
    }
}
