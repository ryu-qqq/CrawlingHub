package com.ryuqq.crawlinghub.domain.schedule.aggregate;

import com.ryuqq.crawlinghub.domain.common.event.DomainEvent;
import com.ryuqq.crawlinghub.domain.schedule.event.SchedulerRegisteredEvent;
import com.ryuqq.crawlinghub.domain.schedule.event.SchedulerUpdatedEvent;
import com.ryuqq.crawlinghub.domain.schedule.identifier.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerHistoryId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CronExpression;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerName;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 크롤 스케줄러 Aggregate Root
 *
 * <p><strong>도메인 규칙</strong>:
 *
 * <ul>
 *   <li>셀러별 스케줄러 이름 중복 불가 (외부 검증 필요)
 *   <li>등록/수정 시 SchedulerRegisteredEvent/SchedulerUpdatedEvent 발행
 *   <li>AWS EventBridge와 동기화
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public class CrawlScheduler {

    // ==================== 필드 ====================

    private final CrawlSchedulerId crawlSchedulerId;
    private final SellerId sellerId;
    private SchedulerName schedulerName;
    private CronExpression cronExpression;
    private SchedulerStatus status;

    private final Instant createdAt;
    private Instant updatedAt;

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    // ==================== 생성 메서드 (3종) ====================

    /**
     * 신규 생성 (Auto Increment ID)
     *
     * @param sellerId 셀러 ID
     * @param schedulerName 스케줄러 이름 (셀러별 중복 불가)
     * @param cronExpression 크론 표현식
     * @param clock 시간 제어
     * @return 신규 CrawlScheduler
     */
    public static CrawlScheduler forNew(
            SellerId sellerId,
            SchedulerName schedulerName,
            CronExpression cronExpression,
            Clock clock) {
        Instant now = clock.instant();
        // Auto Increment: ID null

        // 등록 이벤트는 ID 할당 후 발행 (영속화 후 처리)
        return new CrawlScheduler(
                null, // Auto Increment: ID null
                sellerId,
                schedulerName,
                cronExpression,
                SchedulerStatus.ACTIVE,
                now,
                now);
    }

    /**
     * ID 기반 생성 (비즈니스 로직용)
     *
     * @param crawlSchedulerId 스케줄러 ID (null 불가)
     * @param sellerId 셀러 ID
     * @param schedulerName 스케줄러 이름
     * @param cronExpression 크론 표현식
     * @param status 스케줄러 상태
     * @param createdAt 생성 시각
     * @param updatedAt 수정 시각
     * @return CrawlScheduler
     */
    public static CrawlScheduler of(
            CrawlSchedulerId crawlSchedulerId,
            SellerId sellerId,
            SchedulerName schedulerName,
            CronExpression cronExpression,
            SchedulerStatus status,
            Instant createdAt,
            Instant updatedAt) {
        if (crawlSchedulerId == null) {
            throw new IllegalArgumentException("crawlSchedulerId는 null일 수 없습니다.");
        }
        return new CrawlScheduler(
                crawlSchedulerId,
                sellerId,
                schedulerName,
                cronExpression,
                status,
                createdAt,
                updatedAt);
    }

    /**
     * 영속성 복원 (Mapper 전용)
     *
     * @param crawlSchedulerId 스케줄러 ID (null 불가)
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
        return of(
                crawlSchedulerId,
                sellerId,
                schedulerName,
                cronExpression,
                status,
                createdAt,
                updatedAt);
    }

    /** 생성자 (private) */
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
     * 등록 이벤트 발행 (영속화 후 호출)
     *
     * <p>ID 할당 후 호출해야 합니다.
     *
     * @param historyId 히스토리 ID (Outbox 조회용)
     * @param clock 시간 제어
     */
    public void addRegisteredEvent(CrawlSchedulerHistoryId historyId, Clock clock) {
        if (this.crawlSchedulerId == null || this.crawlSchedulerId.isNew()) {
            throw new IllegalStateException("등록 이벤트는 ID 할당 후 발행해야 합니다.");
        }
        if (historyId == null) {
            throw new IllegalArgumentException("historyId는 null일 수 없습니다.");
        }
        this.domainEvents.add(
                SchedulerRegisteredEvent.of(
                        this.crawlSchedulerId,
                        historyId,
                        this.sellerId,
                        this.schedulerName,
                        this.cronExpression,
                        clock));
    }

    /**
     * 스케줄러 정보 통합 수정
     *
     * <p><strong>비즈니스 룰</strong>:
     *
     * <ul>
     *   <li>이름 변경: 상태 무관하게 허용 (AWS 동기화 불필요)
     *   <li>cron 변경: 상태 무관하게 저장
     *   <li>상태 변경: ACTIVE/INACTIVE 전환
     *   <li>이벤트 발행: 최종 상태가 ACTIVE이거나, ACTIVE→INACTIVE 전환 시에만 발행
     * </ul>
     *
     * @param newSchedulerName 새로운 스케줄러 이름 (셀러별 중복 검증은 외부에서)
     * @param newCronExpression 새로운 크론 표현식
     * @param newStatus 새로운 상태
     * @param clock 시간 제어
     */
    public void update(
            SchedulerName newSchedulerName,
            CronExpression newCronExpression,
            SchedulerStatus newStatus,
            Clock clock) {
        if (newSchedulerName == null) {
            throw new IllegalArgumentException("스케줄러 이름은 null일 수 없습니다.");
        }
        if (newCronExpression == null) {
            throw new IllegalArgumentException("크론 표현식은 null일 수 없습니다.");
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("상태는 null일 수 없습니다.");
        }

        SchedulerStatus oldStatus = this.status;

        // 1. 필드 업데이트
        this.schedulerName = newSchedulerName;
        this.cronExpression = newCronExpression;
        this.status = newStatus;
        this.updatedAt = clock.instant();

        // 2. 이벤트 발행 판단
        // - 최종 상태가 ACTIVE → EventBridge 동기화 필요 (enable + cron 업데이트)
        // - ACTIVE → INACTIVE 전환 → EventBridge 동기화 필요 (disable)
        // - INACTIVE 상태 유지 → EventBridge 동기화 불필요
        boolean shouldPublishEvent =
                (newStatus == SchedulerStatus.ACTIVE)
                        || (oldStatus == SchedulerStatus.ACTIVE
                                && newStatus == SchedulerStatus.INACTIVE);

        if (shouldPublishEvent) {
            this.domainEvents.add(
                    SchedulerUpdatedEvent.of(
                            this.crawlSchedulerId,
                            this.sellerId,
                            this.schedulerName,
                            this.cronExpression,
                            this.status,
                            clock));
        }
    }

    // ==================== Getter ====================

    public CrawlSchedulerId getCrawlSchedulerId() {
        return crawlSchedulerId;
    }

    /** Law of Demeter: 원시 타입이 필요한 경우 별도 메서드 제공 */
    public Long getCrawlSchedulerIdValue() {
        return crawlSchedulerId != null ? crawlSchedulerId.value() : null;
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

    /**
     * 현재 스케줄러 이름과 동일한지 확인
     *
     * @param name 비교 대상 이름
     * @return 동일 여부
     */
    public boolean hasSameSchedulerName(String name) {
        return schedulerName.isSameAs(name);
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

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /** 활성 상태 여부 */
    public boolean isActive() {
        return this.status == SchedulerStatus.ACTIVE;
    }

    /** 비활성 상태 여부 */
    public boolean isInactive() {
        return this.status == SchedulerStatus.INACTIVE;
    }

    /** 도메인 이벤트 목록 (읽기 전용) */
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    /** 도메인 이벤트 초기화 */
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
}
