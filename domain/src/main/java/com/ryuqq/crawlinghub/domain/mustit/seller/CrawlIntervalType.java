package com.ryuqq.crawlinghub.domain.mustit.seller;

/**
 * 크롤링 주기 타입을 정의하는 Enum
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
public enum CrawlIntervalType {
    /**
     * 시간 단위 크롤링
     */
    HOURLY,

    /**
     * 일 단위 크롤링
     */
    DAILY,

    /**
     * 주 단위 크롤링
     */
    WEEKLY
}
