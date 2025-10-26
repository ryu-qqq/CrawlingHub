package com.ryuqq.crawlinghub.domain.mustit.seller;

import java.util.Objects;

/**
 * 크롤링 주기를 표현하는 Value Object
 * <p>
 * 크롤링 주기 타입, 주기 값, EventBridge용 cron 표현식을 포함합니다.
 * Java Record로 구현하여 불변성과 코드 간결성을 보장합니다.
 * </p>
 *
 * @param intervalType   크롤링 주기 타입
 * @param intervalValue  주기 값 (양수)
 * @param cronExpression EventBridge용 cron 표현식
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
public record CrawlInterval(
        CrawlIntervalType intervalType,
        int intervalValue,
        String cronExpression
) {

    /**
     * 크롤링 주기를 생성합니다.
     *
     * @param intervalType  크롤링 주기 타입
     * @param intervalValue 주기 값 (양수)
     * @throws IllegalArgumentException 주기 값이 0 이하인 경우
     */
    public CrawlInterval(CrawlIntervalType intervalType, int intervalValue) {
        this(intervalType, intervalValue, generateCronExpression(intervalType, intervalValue));
    }

    /**
     * Compact Constructor - 유효성 검증
     */
    public CrawlInterval {
        Objects.requireNonNull(intervalType, "intervalType must not be null");
        Objects.requireNonNull(cronExpression, "cronExpression must not be null");

        if (intervalValue <= 0) {
            throw new IllegalArgumentException("intervalValue must be greater than 0");
        }
    }

    /**
     * 주기 타입과 값을 기반으로 AWS EventBridge용 cron 표현식을 생성합니다.
     *
     * @param type  크롤링 주기 타입
     * @param value 주기 값
     * @return cron 표현식
     */
    private static String generateCronExpression(CrawlIntervalType type, int value) {
        return switch (type) {
            case HOURLY -> String.format("0 0/%d * * ? *", value);
            case DAILY -> String.format("0 0 0/%d * ? *", value);
            case WEEKLY -> String.format("0 0 0 ? * 1/%d *", value);
        };
    }

    /**
     * 크롤링 주기 타입을 반환합니다.
     *
     * @return 주기 타입
     */
    public CrawlIntervalType getIntervalType() {
        return intervalType;
    }

    /**
     * 주기 값을 반환합니다.
     *
     * @return 주기 값
     */
    public int getIntervalValue() {
        return intervalValue;
    }

    /**
     * EventBridge용 cron 표현식을 반환합니다.
     *
     * @return cron 표현식
     */
    public String getCronExpression() {
        return cronExpression;
    }
}
