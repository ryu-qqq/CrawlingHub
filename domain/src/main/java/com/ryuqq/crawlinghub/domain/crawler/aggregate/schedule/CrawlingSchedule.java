package com.ryuqq.crawlinghub.domain.crawler.aggregate.schedule;

import com.ryuqq.crawlinghub.domain.crawler.vo.CrawlingInterval;
import com.ryuqq.crawlinghub.domain.crawler.vo.ScheduleId;
import com.ryuqq.crawlinghub.domain.crawler.vo.ScheduleStatus;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * CrawlingSchedule - 크롤링 스케줄 Aggregate Root
 *
 * <p>주기적 크롤링을 위한 스케줄 정보를 관리합니다.</p>
 *
 * <p><strong>핵심 책임:</strong></p>
 * <ul>
 *   <li>✅ Seller별 크롤링 주기 관리</li>
 *   <li>✅ AWS EventBridge Rate Expression 자동 변환</li>
 *   <li>✅ scheduleRule 자동 생성 (mustit-crawler-seller_{sellerId})</li>
 *   <li>✅ 스케줄 활성화/비활성화 관리</li>
 * </ul>
 *
 * <p><strong>AWS EventBridge Rate Expression 규칙:</strong></p>
 * <ul>
 *   <li>✅ 1일: "rate(1 day)" (단수형)</li>
 *   <li>✅ N일: "rate(N days)" (복수형)</li>
 *   <li>✅ N시간: "rate(N hours)" (복수형)</li>
 * </ul>
 *
 * <p><strong>Zero-Tolerance 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Lombok 금지</li>
 *   <li>✅ Law of Demeter 준수</li>
 *   <li>✅ Tell Don't Ask 패턴</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
public class CrawlingSchedule {

    private final ScheduleId scheduleId;
    private final SellerId sellerId;
    private CrawlingInterval crawlingInterval;
    private String scheduleRule;
    private String scheduleExpression;
    private ScheduleStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Private Constructor - Factory Method 패턴 (create용)
     *
     * @param sellerId Seller ID
     * @param crawlingInterval 크롤링 주기
     * @author ryu-qqq
     * @since 2025-11-17
     */
    private CrawlingSchedule(SellerId sellerId, CrawlingInterval crawlingInterval) {
        this.scheduleId = ScheduleId.generate();
        this.sellerId = sellerId;
        this.crawlingInterval = crawlingInterval;
        this.scheduleRule = generateScheduleRule(sellerId);
        this.scheduleExpression = convertToRateExpression(crawlingInterval);
        this.status = ScheduleStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Private Constructor - Reconstitute 패턴 (DB 조회용)
     *
     * @param scheduleId Schedule ID
     * @param sellerId Seller ID
     * @param crawlingInterval 크롤링 주기
     * @param scheduleRule Schedule Rule
     * @param scheduleExpression Schedule Expression
     * @param status 스케줄 상태
     * @param createdAt 생성 시각
     * @param updatedAt 수정 시각
     * @author ryu-qqq
     * @since 2025-11-17
     */
    private CrawlingSchedule(
            ScheduleId scheduleId,
            SellerId sellerId,
            CrawlingInterval crawlingInterval,
            String scheduleRule,
            String scheduleExpression,
            ScheduleStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.scheduleId = scheduleId;
        this.sellerId = sellerId;
        this.crawlingInterval = crawlingInterval;
        this.scheduleRule = scheduleRule;
        this.scheduleExpression = scheduleExpression;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * CrawlingSchedule 생성 Factory Method
     *
     * <p>초기 상태: ACTIVE</p>
     * <p>scheduleRule: "mustit-crawler-seller_{sellerId}"</p>
     * <p>scheduleExpression: AWS EventBridge Rate Expression</p>
     *
     * @param sellerId Seller ID
     * @param crawlingInterval 크롤링 주기
     * @return 생성된 CrawlingSchedule
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public static CrawlingSchedule create(SellerId sellerId, CrawlingInterval crawlingInterval) {
        return new CrawlingSchedule(sellerId, crawlingInterval);
    }

    /**
     * CrawlingSchedule 재구성 Factory Method (DB 조회용)
     *
     * <p>데이터베이스에서 조회한 데이터로 CrawlingSchedule을 재구성합니다.</p>
     * <p>테스트에서 특정 상태의 스케줄을 생성할 때도 사용됩니다.</p>
     *
     * @param scheduleId Schedule ID
     * @param sellerId Seller ID
     * @param crawlingInterval 크롤링 주기
     * @param scheduleRule Schedule Rule
     * @param scheduleExpression Schedule Expression
     * @param status 스케줄 상태
     * @param createdAt 생성 시각
     * @param updatedAt 수정 시각
     * @return 재구성된 CrawlingSchedule
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public static CrawlingSchedule reconstitute(
            ScheduleId scheduleId,
            SellerId sellerId,
            CrawlingInterval crawlingInterval,
            String scheduleRule,
            String scheduleExpression,
            ScheduleStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new CrawlingSchedule(
                scheduleId, sellerId, crawlingInterval,
                scheduleRule, scheduleExpression, status,
                createdAt, updatedAt
        );
    }

    /**
     * Schedule Rule 생성
     *
     * <p>형식: "mustit-crawler-seller_{sellerId}"</p>
     *
     * @param sellerId Seller ID
     * @return Schedule Rule
     * @author ryu-qqq
     * @since 2025-11-17
     */
    private String generateScheduleRule(SellerId sellerId) {
        return "mustit-crawler-" + sellerId.value();
    }

    /**
     * CrawlingInterval을 AWS EventBridge Rate Expression으로 변환
     *
     * <p><strong>변환 규칙:</strong></p>
     * <ul>
     *   <li>1일: "rate(1 day)" (단수형)</li>
     *   <li>N일: "rate(N days)" (복수형)</li>
     *   <li>N시간: "rate(N hours)" (복수형)</li>
     * </ul>
     *
     * @param interval 크롤링 주기
     * @return AWS EventBridge Rate Expression
     * @author ryu-qqq
     * @since 2025-11-17
     */
    private String convertToRateExpression(CrawlingInterval interval) {
        long amount = interval.amount();
        ChronoUnit unit = interval.unit();

        if (unit == ChronoUnit.DAYS && amount == 1) {
            return "rate(1 day)";
        } else if (unit == ChronoUnit.DAYS) {
            return "rate(" + amount + " days)";
        } else if (unit == ChronoUnit.HOURS) {
            return "rate(" + amount + " hours)";
        }

        throw new IllegalArgumentException("지원하지 않는 크롤링 주기입니다: " + unit);
    }

    // ===== Business Methods =====

    /**
     * 크롤링 주기 변경
     *
     * <p><strong>변경 규칙:</strong></p>
     * <ul>
     *   <li>✅ ACTIVE 상태에서만 변경 가능</li>
     *   <li>✅ scheduleExpression 자동 재계산</li>
     *   <li>✅ updatedAt 갱신</li>
     * </ul>
     *
     * @param newInterval 새로운 크롤링 주기
     * @throws IllegalStateException ACTIVE 상태가 아닐 때
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public void updateInterval(CrawlingInterval newInterval) {
        if (status != ScheduleStatus.ACTIVE) {
            throw new IllegalStateException("ACTIVE 상태에서만 주기를 변경할 수 있습니다");
        }
        this.crawlingInterval = newInterval;
        this.scheduleExpression = convertToRateExpression(newInterval);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 스케줄 활성화
     *
     * <p><strong>활성화 규칙:</strong></p>
     * <ul>
     *   <li>✅ INACTIVE 상태에서만 활성화 가능</li>
     *   <li>✅ 이미 ACTIVE 상태면 예외 발생</li>
     *   <li>✅ updatedAt 갱신</li>
     * </ul>
     *
     * @throws IllegalStateException 이미 ACTIVE 상태일 때
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public void activate() {
        if (status == ScheduleStatus.ACTIVE) {
            throw new IllegalStateException("이미 ACTIVE 상태입니다");
        }
        this.status = ScheduleStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 스케줄 비활성화
     *
     * <p><strong>비활성화 규칙:</strong></p>
     * <ul>
     *   <li>✅ ACTIVE 상태에서만 비활성화 가능</li>
     *   <li>✅ 이미 INACTIVE 상태면 예외 발생</li>
     *   <li>✅ updatedAt 갱신</li>
     * </ul>
     *
     * @throws IllegalStateException 이미 INACTIVE 상태일 때
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public void deactivate() {
        if (status == ScheduleStatus.INACTIVE) {
            throw new IllegalStateException("이미 INACTIVE 상태입니다");
        }
        this.status = ScheduleStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    // ===== Getters =====

    public ScheduleId getScheduleId() {
        return scheduleId;
    }

    public SellerId getSellerId() {
        return sellerId;
    }

    public CrawlingInterval getCrawlingInterval() {
        return crawlingInterval;
    }

    public String getScheduleRule() {
        return scheduleRule;
    }

    public String getScheduleExpression() {
        return scheduleExpression;
    }

    public ScheduleStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
