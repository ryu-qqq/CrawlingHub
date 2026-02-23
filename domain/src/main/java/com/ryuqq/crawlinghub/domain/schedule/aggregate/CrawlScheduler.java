package com.ryuqq.crawlinghub.domain.schedule.aggregate;

import com.ryuqq.crawlinghub.domain.schedule.exception.InvalidSchedulerStateException;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerUpdateData;
import com.ryuqq.crawlinghub.domain.schedule.vo.CronExpression;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerName;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import java.time.Instant;
import java.util.Objects;

/**
 * 크롤 스케줄러 Aggregate Root
 *
 * <p><strong>도메인 규칙</strong>:
 *
 * <ul>
 *   <li>셀러별 스케줄러 이름 중복 불가 (외부 검증 필요)
 *   <li>등록/수정 시 아웃박스를 통해 AWS EventBridge와 동기화
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public class CrawlScheduler {

    private final CrawlSchedulerId crawlSchedulerId;
    private final SellerId sellerId;
    private SchedulerName schedulerName;
    private CronExpression cronExpression;
    private SchedulerStatus status;

    private final Instant createdAt;
    private Instant updatedAt;

    /**
     * 신규 생성 (Auto Increment ID)
     *
     * @param sellerId 셀러 ID
     * @param schedulerName 스케줄러 이름 (셀러별 중복 불가)
     * @param cronExpression 크론 표현식
     * @param now 현재 시각
     * @return 신규 CrawlScheduler
     */
    public static CrawlScheduler forNew(
            SellerId sellerId,
            SchedulerName schedulerName,
            CronExpression cronExpression,
            Instant now) {
        return new CrawlScheduler(
                null, sellerId, schedulerName, cronExpression, SchedulerStatus.ACTIVE, now, now);
    }

    /**
     * 영속성 복원 (Mapper 전용)
     *
     * @param crawlSchedulerId 스케줄러 ID
     * @param sellerId 셀러 ID
     * @param schedulerName 스케줄러 이름
     * @param cronExpression 크론 표현식
     * @param status 스케줄러 상태
     * @param createdAt 생성 시각
     * @param updatedAt 수정 시각
     * @return CrawlScheduler
     */
    public static CrawlScheduler reconstitute(
            CrawlSchedulerId crawlSchedulerId,
            SellerId sellerId,
            SchedulerName schedulerName,
            CronExpression cronExpression,
            SchedulerStatus status,
            Instant createdAt,
            Instant updatedAt) {
        return new CrawlScheduler(
                crawlSchedulerId,
                sellerId,
                schedulerName,
                cronExpression,
                status,
                createdAt,
                updatedAt);
    }

    private CrawlScheduler(
            CrawlSchedulerId crawlSchedulerId,
            SellerId sellerId,
            SchedulerName schedulerName,
            CronExpression cronExpression,
            SchedulerStatus status,
            Instant createdAt,
            Instant updatedAt) {
        this.crawlSchedulerId = crawlSchedulerId;
        this.sellerId = sellerId;
        this.schedulerName = schedulerName;
        this.cronExpression = cronExpression;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ==================== 비즈니스 메서드 ====================

    /**
     * 스케줄러 정보 통합 수정
     *
     * <p><strong>비즈니스 룰</strong>:
     *
     * <ul>
     *   <li>이름 변경: 상태 무관하게 허용
     *   <li>cron 변경: 상태 무관하게 저장
     *   <li>상태 변경: ACTIVE/INACTIVE 전환
     * </ul>
     *
     * @param updateData 수정 데이터 (schedulerName, cronExpression, status)
     * @param now 현재 시각
     */
    public void update(CrawlSchedulerUpdateData updateData, Instant now) {
        this.schedulerName = updateData.schedulerName();
        this.cronExpression = updateData.cronExpression();
        this.status = updateData.status();
        this.updatedAt = now;
    }

    // ==================== Getter ====================

    public CrawlSchedulerId getCrawlSchedulerId() {
        return crawlSchedulerId;
    }

    public Long getCrawlSchedulerIdValue() {
        return crawlSchedulerId != null ? crawlSchedulerId.value() : null;
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

    public boolean hasSameSchedulerName(String name) {
        return schedulerName.isSameAs(name);
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

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public boolean isActive() {
        return this.status == SchedulerStatus.ACTIVE;
    }

    public boolean isInactive() {
        return this.status == SchedulerStatus.INACTIVE;
    }

    /**
     * ACTIVE 상태 검증
     *
     * @throws InvalidSchedulerStateException ACTIVE 상태가 아닌 경우
     */
    public void validateActive() {
        if (!isActive()) {
            throw new InvalidSchedulerStateException(this.status, SchedulerStatus.ACTIVE);
        }
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
        CrawlScheduler that = (CrawlScheduler) o;
        return Objects.equals(crawlSchedulerId, that.crawlSchedulerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(crawlSchedulerId);
    }
}
