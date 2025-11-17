package com.ryuqq.crawlinghub.domain.fixture;

import com.ryuqq.crawlinghub.domain.crawler.aggregate.schedule.CrawlingSchedule;
import com.ryuqq.crawlinghub.domain.crawler.vo.CrawlingInterval;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;

import java.time.temporal.ChronoUnit;

/**
 * CrawlingSchedule 관련 테스트 데이터 생성 Fixture
 *
 * <p>CrawlingSchedule Aggregate와 관련 Value Object의 테스트 데이터를 제공합니다.</p>
 *
 * <p><strong>Factory Method 패턴:</strong></p>
 * <ul>
 *   <li>{@link #defaultSchedule()} - 1일 주기 기본 스케줄</li>
 *   <li>{@link #hourlySchedule()} - 6시간 주기 스케줄</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
public class CrawlingScheduleFixture {

    private static final SellerId DEFAULT_SELLER_ID = new SellerId("seller_12345");
    private static final SellerId HOURLY_SELLER_ID = new SellerId("seller_67890");

    /**
     * 기본 CrawlingSchedule 생성 (1일 주기)
     *
     * <p><strong>설정:</strong></p>
     * <ul>
     *   <li>SellerId: seller_12345</li>
     *   <li>CrawlingInterval: 1 day</li>
     *   <li>scheduleRule: mustit-crawler-seller_12345</li>
     *   <li>scheduleExpression: rate(1 day)</li>
     *   <li>Status: ACTIVE</li>
     * </ul>
     *
     * @return 1일 주기 CrawlingSchedule
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public static CrawlingSchedule defaultSchedule() {
        CrawlingInterval interval = new CrawlingInterval(1, ChronoUnit.DAYS);
        return CrawlingSchedule.create(DEFAULT_SELLER_ID, interval);
    }

    /**
     * 시간 단위 CrawlingSchedule 생성 (6시간 주기)
     *
     * <p><strong>설정:</strong></p>
     * <ul>
     *   <li>SellerId: seller_67890</li>
     *   <li>CrawlingInterval: 6 hours</li>
     *   <li>scheduleRule: mustit-crawler-seller_67890</li>
     *   <li>scheduleExpression: rate(6 hours)</li>
     *   <li>Status: ACTIVE</li>
     * </ul>
     *
     * @return 6시간 주기 CrawlingSchedule
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public static CrawlingSchedule hourlySchedule() {
        CrawlingInterval interval = new CrawlingInterval(6, ChronoUnit.HOURS);
        return CrawlingSchedule.create(HOURLY_SELLER_ID, interval);
    }
}
