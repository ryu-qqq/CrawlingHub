package com.ryuqq.crawlinghub.domain.fixture;

import com.ryuqq.crawlinghub.domain.crawler.aggregate.schedule.CrawlingSchedule;
import com.ryuqq.crawlinghub.domain.crawler.vo.CrawlingInterval;
import com.ryuqq.crawlinghub.domain.crawler.vo.ScheduleId;
import com.ryuqq.crawlinghub.domain.crawler.vo.ScheduleStatus;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * CrawlingSchedule 관련 테스트 데이터 생성 Fixture
 *
 * <p>CrawlingSchedule Aggregate와 관련 Value Object의 테스트 데이터를 제공합니다.</p>
 *
 * <p><strong>표준 메서드:</strong></p>
 * <ul>
 *   <li>{@link #forNew()} - 새 CrawlingSchedule 생성 (표준 패턴)</li>
 *   <li>{@link #of()} - 기본 CrawlingSchedule 생성 (표준 패턴)</li>
 *   <li>{@link #reconstitute(ScheduleId, SellerId, CrawlingInterval, String, String, ScheduleStatus, LocalDateTime, LocalDateTime)} - DB에서 복원 (표준 패턴)</li>
 * </ul>
 *
 * <p><strong>헬퍼 메서드:</strong></p>
 * <ul>
 *   <li>{@link #defaultSchedule()} - 1일 주기 기본 스케줄 (ACTIVE)</li>
 *   <li>{@link #hourlySchedule()} - 6시간 주기 스케줄 (ACTIVE)</li>
 *   <li>{@link #inactiveSchedule()} - 비활성화된 스케줄 (INACTIVE)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
public class CrawlingScheduleFixture {

    private static final SellerId DEFAULT_SELLER_ID = new SellerId(1L);
    private static final SellerId HOURLY_SELLER_ID = new SellerId(2L);

    /**
     * 새로운 CrawlingSchedule 생성 (표준 패턴)
     *
     * <p>Aggregate의 create() Factory Method를 호출하여 새 Aggregate를 생성합니다.</p>
     *
     * @return 새로 생성된 CrawlingSchedule
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public static CrawlingSchedule forNew() {
        return defaultSchedule();
    }

    /**
     * 기본 CrawlingSchedule 생성 (표준 패턴)
     *
     * <p>가장 기본적인 CrawlingSchedule를 반환합니다.</p>
     *
     * @return 기본 CrawlingSchedule
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public static CrawlingSchedule of() {
        return defaultSchedule();
    }

    /**
     * DB에서 복원된 CrawlingSchedule 생성 (표준 패턴)
     *
     * <p>Aggregate의 reconstitute() 메서드를 호출하여 영속화된 상태를 복원합니다.</p>
     *
     * @param scheduleId 스케줄 ID
     * @param sellerId 판매자 ID
     * @param interval 크롤링 주기
     * @param scheduleRule 스케줄 규칙 이름
     * @param scheduleExpression 스케줄 표현식
     * @param status 스케줄 상태
     * @param createdAt 생성 일시
     * @param updatedAt 수정 일시
     * @return 복원된 CrawlingSchedule
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public static CrawlingSchedule reconstitute(
            ScheduleId scheduleId,
            SellerId sellerId,
            CrawlingInterval interval,
            String scheduleRule,
            String scheduleExpression,
            ScheduleStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return CrawlingSchedule.reconstitute(
                scheduleId,
                sellerId,
                interval,
                scheduleRule,
                scheduleExpression,
                status,
                createdAt,
                updatedAt
        );
    }

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

    /**
     * 비활성화된 CrawlingSchedule 생성 (INACTIVE 상태)
     *
     * <p><strong>설정:</strong></p>
     * <ul>
     *   <li>SellerId: seller_12345</li>
     *   <li>CrawlingInterval: 1 day</li>
     *   <li>Status: INACTIVE</li>
     * </ul>
     *
     * <p>reconstitute 메서드를 사용하여 INACTIVE 상태로 직접 생성합니다.</p>
     *
     * @return INACTIVE 상태의 CrawlingSchedule
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public static CrawlingSchedule inactiveSchedule() {
        ScheduleId scheduleId = ScheduleId.forNew();
        CrawlingInterval interval = new CrawlingInterval(1, ChronoUnit.DAYS);
        LocalDateTime now = LocalDateTime.now();

        return CrawlingSchedule.reconstitute(
                scheduleId,
                DEFAULT_SELLER_ID,
                interval,
                "mustit-crawler-" + DEFAULT_SELLER_ID.value(),
                "rate(1 day)",
                ScheduleStatus.INACTIVE,
                now.minusDays(1),
                now
        );
    }
}
