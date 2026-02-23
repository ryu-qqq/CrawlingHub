package com.ryuqq.crawlinghub.application.schedule.dto.bundle;

import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerHistory;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerOutBox;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerHistoryId;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
import java.time.Instant;

/**
 * 크롤 스케줄러 번들 DTO (Immutable)
 *
 * <p><strong>용도</strong>: 스케줄러, 히스토리, 아웃박스를 하나로 묶어 관리
 *
 * <p><strong>불변 설계</strong>: with* 메서드는 새 인스턴스를 반환합니다.
 *
 * <p><strong>저장 흐름</strong>:
 *
 * <ol>
 *   <li>스케줄러 저장 → ID 반환 → withSchedulerId() → 새 번들 반환
 *   <li>히스토리 저장 (스케줄러 ID 참조) → ID 반환 → withHistoryId() → 새 번들 반환
 *   <li>아웃박스 저장 (히스토리 ID 참조)
 * </ol>
 *
 * @param scheduler 스케줄러 Aggregate
 * @param registeredAt 등록 시각 (Factory에서 주입)
 * @param savedSchedulerId 저장된 스케줄러 ID (nullable)
 * @param savedHistoryId 저장된 히스토리 ID (nullable)
 * @author development-team
 * @since 1.0.0
 */
public record CrawlSchedulerBundle(
        CrawlScheduler scheduler,
        Instant registeredAt,
        CrawlSchedulerId savedSchedulerId,
        CrawlSchedulerHistoryId savedHistoryId) {

    /**
     * 번들 생성 (ID 미할당 상태)
     *
     * @param scheduler 스케줄러 Aggregate
     * @param registeredAt 등록 시각
     * @return CrawlSchedulerBundle
     */
    public static CrawlSchedulerBundle of(CrawlScheduler scheduler, Instant registeredAt) {
        return new CrawlSchedulerBundle(scheduler, registeredAt, null, null);
    }

    /**
     * 스케줄러 ID 설정 (새 인스턴스 반환)
     *
     * @param schedulerId 저장된 스케줄러 ID
     * @return 새 CrawlSchedulerBundle (ID 할당됨)
     */
    public CrawlSchedulerBundle withSchedulerId(CrawlSchedulerId schedulerId) {
        return new CrawlSchedulerBundle(scheduler, registeredAt, schedulerId, savedHistoryId);
    }

    /**
     * 히스토리 ID 설정 (새 인스턴스 반환)
     *
     * @param historyId 저장된 히스토리 ID
     * @return 새 CrawlSchedulerBundle (ID 할당됨)
     */
    public CrawlSchedulerBundle withHistoryId(CrawlSchedulerHistoryId historyId) {
        return new CrawlSchedulerBundle(scheduler, registeredAt, savedSchedulerId, historyId);
    }

    /**
     * 스케줄러 반환
     *
     * @return CrawlScheduler
     */
    public CrawlScheduler getScheduler() {
        return scheduler;
    }

    /**
     * 저장된 스케줄러 ID 반환
     *
     * @return CrawlSchedulerId
     * @throws IllegalStateException ID가 아직 할당되지 않은 경우
     */
    public CrawlSchedulerId getSavedSchedulerId() {
        if (savedSchedulerId == null) {
            throw new IllegalStateException("스케줄러 ID가 아직 할당되지 않았습니다.");
        }
        return savedSchedulerId;
    }

    /**
     * 히스토리 생성 (스케줄러 ID 할당 후)
     *
     * @return CrawlSchedulerHistory
     * @throws IllegalStateException 스케줄러 ID가 아직 할당되지 않은 경우
     */
    public CrawlSchedulerHistory createHistory() {
        if (savedSchedulerId == null) {
            throw new IllegalStateException("스케줄러 ID가 아직 할당되지 않았습니다.");
        }
        CrawlScheduler schedulerForHistory =
                CrawlScheduler.reconstitute(
                        savedSchedulerId,
                        scheduler.getSellerId(),
                        scheduler.getSchedulerName(),
                        scheduler.getCronExpression(),
                        scheduler.getStatus(),
                        scheduler.getCreatedAt(),
                        scheduler.getUpdatedAt());
        return CrawlSchedulerHistory.fromScheduler(schedulerForHistory, registeredAt);
    }

    /**
     * 아웃박스 생성 (히스토리 ID 할당 후)
     *
     * @return CrawlSchedulerOutBox
     * @throws IllegalStateException 히스토리 ID 또는 스케줄러 ID가 아직 할당되지 않은 경우
     */
    public CrawlSchedulerOutBox createOutBox() {
        if (savedHistoryId == null) {
            throw new IllegalStateException("히스토리 ID가 아직 할당되지 않았습니다.");
        }
        if (savedSchedulerId == null) {
            throw new IllegalStateException("스케줄러 ID가 아직 할당되지 않았습니다.");
        }
        CrawlScheduler s =
                CrawlScheduler.reconstitute(
                        savedSchedulerId,
                        scheduler.getSellerId(),
                        scheduler.getSchedulerName(),
                        scheduler.getCronExpression(),
                        scheduler.getStatus(),
                        scheduler.getCreatedAt(),
                        scheduler.getUpdatedAt());
        return CrawlSchedulerOutBox.forNew(
                savedHistoryId,
                s.getCrawlSchedulerIdValue(),
                s.getSellerIdValue(),
                s.getSchedulerNameValue(),
                s.getCronExpressionValue(),
                s.getStatus(),
                registeredAt);
    }

    /**
     * 저장된 히스토리 ID 반환
     *
     * @return CrawlSchedulerHistoryId (nullable)
     */
    public CrawlSchedulerHistoryId getSavedHistoryId() {
        return savedHistoryId;
    }
}
