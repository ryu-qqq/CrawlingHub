package com.ryuqq.crawlinghub.domain.schedule.aggregate;

import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerHistoryId;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CronExpression;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerName;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import java.time.Instant;
import java.util.Objects;

/**
 * 크롤 스케줄러 히스토리
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

    private final CrawlSchedulerHistoryId historyId;
    private final CrawlSchedulerId crawlSchedulerId;
    private final SellerId sellerId;
    private final SchedulerName schedulerName;
    private final CronExpression cronExpression;
    private final SchedulerStatus status;
    private final Instant createdAt;

    /**
     * 신규 생성 (Auto Increment ID)
     *
     * @param crawlSchedulerId 스케줄러 ID
     * @param sellerId 셀러 ID
     * @param schedulerName 스케줄러 이름
     * @param cronExpression 크론 표현식
     * @param status 스케줄러 상태
     * @param now 현재 시각
     * @return 신규 CrawlSchedulerHistory
     */
    public static CrawlSchedulerHistory forNew(
            CrawlSchedulerId crawlSchedulerId,
            SellerId sellerId,
            SchedulerName schedulerName,
            CronExpression cronExpression,
            SchedulerStatus status,
            Instant now) {
        return new CrawlSchedulerHistory(
                null, crawlSchedulerId, sellerId, schedulerName, cronExpression, status, now);
    }

    /**
     * 영속성 복원 (Mapper 전용)
     *
     * @param historyId 히스토리 ID
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
     * CrawlScheduler로부터 히스토리 생성
     *
     * @param scheduler 스케줄러
     * @param now 현재 시각
     * @return CrawlSchedulerHistory
     */
    public static CrawlSchedulerHistory fromScheduler(CrawlScheduler scheduler, Instant now) {
        return forNew(
                scheduler.getCrawlSchedulerId(),
                scheduler.getSellerId(),
                scheduler.getSchedulerName(),
                scheduler.getCronExpression(),
                scheduler.getStatus(),
                now);
    }

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

    public Long getHistoryIdValue() {
        return historyId != null ? historyId.value() : null;
    }

    public CrawlSchedulerId getCrawlSchedulerId() {
        return crawlSchedulerId;
    }

    public Long getCrawlSchedulerIdValue() {
        return crawlSchedulerId.value();
    }

    public SellerId getSellerId() {
        return sellerId;
    }

    public Long getSellerIdValue() {
        return sellerId.value();
    }

    public SchedulerName getSchedulerName() {
        return schedulerName;
    }

    public String getSchedulerNameValue() {
        return schedulerName.value();
    }

    public CronExpression getCronExpression() {
        return cronExpression;
    }

    public String getCronExpressionValue() {
        return cronExpression.value();
    }

    public SchedulerStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    // ==================== equals/hashCode (ID 기반) ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CrawlSchedulerHistory that = (CrawlSchedulerHistory) o;
        return Objects.equals(historyId, that.historyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(historyId);
    }
}
