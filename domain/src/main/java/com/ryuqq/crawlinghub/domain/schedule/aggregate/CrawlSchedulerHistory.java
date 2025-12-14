package com.ryuqq.crawlinghub.domain.schedule.aggregate;

import com.ryuqq.crawlinghub.domain.schedule.identifier.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerHistoryId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CronExpression;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerName;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.time.Clock;
import java.time.Instant;

/**
 * 크롤 스케줄러 히스토리 Aggregate Root
 *
 * <p><strong>도메인 규칙</strong>:
 *
 * <ul>
 *   <li>스케줄러 변경 이력 저장 (불변 객체)
 *   <li>실행 기록이 어떤 스케줄러 버전을 참조했는지 추적
 *   <li>등록/수정 시마다 히스토리 생성
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public class CrawlSchedulerHistory {

    // ==================== 필드 ====================

    private final CrawlSchedulerHistoryId historyId;
    private final CrawlSchedulerId crawlSchedulerId;
    private final SellerId sellerId;
    private final SchedulerName schedulerName;
    private final CronExpression cronExpression;
    private final SchedulerStatus status;
    private final Instant createdAt;

    // ==================== 생성 메서드 (3종) ====================

    /**
     * 신규 생성 (Auto Increment ID)
     *
     * @param crawlSchedulerId 스케줄러 ID
     * @param sellerId 셀러 ID
     * @param schedulerName 스케줄러 이름
     * @param cronExpression 크론 표현식
     * @param status 스케줄러 상태
     * @param clock 시간 제어
     * @return 신규 CrawlSchedulerHistory
     */
    public static CrawlSchedulerHistory forNew(
            CrawlSchedulerId crawlSchedulerId,
            SellerId sellerId,
            SchedulerName schedulerName,
            CronExpression cronExpression,
            SchedulerStatus status,
            Clock clock) {
        Instant now = clock.instant();
        return new CrawlSchedulerHistory(
                null, // Auto Increment: ID null
                crawlSchedulerId,
                sellerId,
                schedulerName,
                cronExpression,
                status,
                now);
    }

    /**
     * ID 기반 생성 (비즈니스 로직용)
     *
     * @param historyId 히스토리 ID (null 불가)
     * @param crawlSchedulerId 스케줄러 ID
     * @param sellerId 셀러 ID
     * @param schedulerName 스케줄러 이름
     * @param cronExpression 크론 표현식
     * @param status 스케줄러 상태
     * @param createdAt 생성 시각
     * @return CrawlSchedulerHistory
     */
    public static CrawlSchedulerHistory of(
            CrawlSchedulerHistoryId historyId,
            CrawlSchedulerId crawlSchedulerId,
            SellerId sellerId,
            SchedulerName schedulerName,
            CronExpression cronExpression,
            SchedulerStatus status,
            Instant createdAt) {
        if (historyId == null) {
            throw new IllegalArgumentException("historyId는 null일 수 없습니다.");
        }
        return new CrawlSchedulerHistory(
                historyId,
                crawlSchedulerId,
                sellerId,
                schedulerName,
                cronExpression,
                status,
                createdAt);
    }

    /**
     * 영속성 복원 (Mapper 전용)
     *
     * @param historyId 히스토리 ID (null 불가)
     * @param crawlSchedulerId 스케줄러 ID
     * @param sellerId 셀러 ID
     * @param schedulerName 스케줄러 이름
     * @param cronExpression 크론 표현식
     * @param status 스케줄러 상태
     * @param createdAt 생성 시각
     * @return CrawlSchedulerHistory
     */
    public static CrawlSchedulerHistory reconstitute(
            CrawlSchedulerHistoryId historyId,
            CrawlSchedulerId crawlSchedulerId,
            SellerId sellerId,
            SchedulerName schedulerName,
            CronExpression cronExpression,
            SchedulerStatus status,
            Instant createdAt) {
        if (historyId == null) {
            throw new IllegalArgumentException("historyId는 null일 수 없습니다.");
        }
        return new CrawlSchedulerHistory(
                historyId,
                crawlSchedulerId,
                sellerId,
                schedulerName,
                cronExpression,
                status,
                createdAt);
    }

    /**
     * CrawlScheduler로부터 히스토리 생성 (팩토리 메서드)
     *
     * @param scheduler 스케줄러
     * @param clock 시간 제어
     * @return CrawlSchedulerHistory
     */
    public static CrawlSchedulerHistory fromScheduler(CrawlScheduler scheduler, Clock clock) {
        return forNew(
                scheduler.getCrawlSchedulerId(),
                scheduler.getSellerId(),
                scheduler.getSchedulerName(),
                scheduler.getCronExpression(),
                scheduler.getStatus(),
                clock);
    }

    /** 생성자 (private) */
    private CrawlSchedulerHistory(
            CrawlSchedulerHistoryId historyId,
            CrawlSchedulerId crawlSchedulerId,
            SellerId sellerId,
            SchedulerName schedulerName,
            CronExpression cronExpression,
            SchedulerStatus status,
            Instant createdAt) {
        this.historyId = historyId;
        this.crawlSchedulerId = crawlSchedulerId;
        this.sellerId = sellerId;
        this.schedulerName = schedulerName;
        this.cronExpression = cronExpression;
        this.status = status;
        this.createdAt = createdAt;
    }

    // ==================== Getter ====================

    public CrawlSchedulerHistoryId getHistoryId() {
        return historyId;
    }

    /** Law of Demeter: 원시 타입이 필요한 경우 별도 메서드 제공 */
    public Long getHistoryIdValue() {
        return historyId != null ? historyId.value() : null;
    }

    public CrawlSchedulerId getCrawlSchedulerId() {
        return crawlSchedulerId;
    }

    /** Law of Demeter: 스케줄러 ID의 원시값 */
    public Long getCrawlSchedulerIdValue() {
        return crawlSchedulerId.value();
    }

    public SellerId getSellerId() {
        return sellerId;
    }

    /** Law of Demeter: Seller ID의 원시값 */
    public Long getSellerIdValue() {
        return sellerId.value();
    }

    public SchedulerName getSchedulerName() {
        return schedulerName;
    }

    /** Law of Demeter: 스케줄러 이름의 원시값 */
    public String getSchedulerNameValue() {
        return schedulerName.value();
    }

    public CronExpression getCronExpression() {
        return cronExpression;
    }

    /** Law of Demeter: 크론 표현식의 원시값 */
    public String getCronExpressionValue() {
        return cronExpression.value();
    }

    public SchedulerStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
