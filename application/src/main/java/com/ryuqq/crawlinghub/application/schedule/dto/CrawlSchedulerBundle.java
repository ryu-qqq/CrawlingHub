package com.ryuqq.crawlinghub.application.schedule.dto;

import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerHistory;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerOutBox;
import com.ryuqq.crawlinghub.domain.schedule.identifier.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerHistoryId;
import java.time.Clock;

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
 * @param eventPayload 이벤트 페이로드 (JSON)
 * @param savedSchedulerId 저장된 스케줄러 ID (nullable)
 * @param savedHistoryId 저장된 히스토리 ID (nullable)
 * @author development-team
 * @since 1.0.0
 */
public record CrawlSchedulerBundle(
        CrawlScheduler scheduler,
        String eventPayload,
        CrawlSchedulerId savedSchedulerId,
        CrawlSchedulerHistoryId savedHistoryId) {

    /**
     * 번들 생성 (ID 미할당 상태)
     *
     * @param scheduler 스케줄러 Aggregate
     * @param eventPayload 이벤트 페이로드 (JSON)
     * @return CrawlSchedulerBundle
     */
    public static CrawlSchedulerBundle of(CrawlScheduler scheduler, String eventPayload) {
        return new CrawlSchedulerBundle(scheduler, eventPayload, null, null);
    }

    /**
     * 스케줄러 ID 설정 (새 인스턴스 반환)
     *
     * @param schedulerId 저장된 스케줄러 ID
     * @return 새 CrawlSchedulerBundle (ID 할당됨)
     */
    public CrawlSchedulerBundle withSchedulerId(CrawlSchedulerId schedulerId) {
        return new CrawlSchedulerBundle(scheduler, eventPayload, schedulerId, savedHistoryId);
    }

    /**
     * 히스토리 ID 설정 (새 인스턴스 반환)
     *
     * @param historyId 저장된 히스토리 ID
     * @return 새 CrawlSchedulerBundle (ID 할당됨)
     */
    public CrawlSchedulerBundle withHistoryId(CrawlSchedulerHistoryId historyId) {
        return new CrawlSchedulerBundle(scheduler, eventPayload, savedSchedulerId, historyId);
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
     * ID가 할당된 스케줄러 반환 (등록 이벤트 자동 발행)
     *
     * <p><strong>주의</strong>: 이 메서드는 등록 이벤트를 자동 발행합니다.
     *
     * @param clock 시간 제어
     * @return CrawlScheduler with ID (이벤트 발행됨)
     * @throws IllegalStateException ID 또는 히스토리 ID가 아직 할당되지 않은 경우
     */
    public CrawlScheduler getSavedScheduler(Clock clock) {
        if (savedSchedulerId == null) {
            throw new IllegalStateException("스케줄러 ID가 아직 할당되지 않았습니다.");
        }
        if (savedHistoryId == null) {
            throw new IllegalStateException("히스토리 ID가 아직 할당되지 않았습니다.");
        }
        CrawlScheduler savedScheduler =
                CrawlScheduler.of(
                        savedSchedulerId,
                        scheduler.getSellerId(),
                        scheduler.getSchedulerName(),
                        scheduler.getCronExpression(),
                        scheduler.getStatus(),
                        scheduler.getCreatedAt(),
                        scheduler.getUpdatedAt());

        savedScheduler.addRegisteredEvent(savedHistoryId, clock);
        return savedScheduler;
    }

    /**
     * 히스토리 생성 (스케줄러 ID 할당 후)
     *
     * @param clock 시간 제어
     * @return CrawlSchedulerHistory
     * @throws IllegalStateException 스케줄러 ID가 아직 할당되지 않은 경우
     */
    public CrawlSchedulerHistory createHistory(Clock clock) {
        if (savedSchedulerId == null) {
            throw new IllegalStateException("스케줄러 ID가 아직 할당되지 않았습니다.");
        }
        CrawlScheduler schedulerForHistory =
                CrawlScheduler.of(
                        savedSchedulerId,
                        scheduler.getSellerId(),
                        scheduler.getSchedulerName(),
                        scheduler.getCronExpression(),
                        scheduler.getStatus(),
                        scheduler.getCreatedAt(),
                        scheduler.getUpdatedAt());
        return CrawlSchedulerHistory.fromScheduler(schedulerForHistory, clock);
    }

    /**
     * 아웃박스 생성 (히스토리 ID 할당 후)
     *
     * @param clock 시간 제어
     * @return CrawlSchedulerOutBox
     * @throws IllegalStateException 히스토리 ID가 아직 할당되지 않은 경우
     */
    public CrawlSchedulerOutBox createOutBox(Clock clock) {
        if (savedHistoryId == null) {
            throw new IllegalStateException("히스토리 ID가 아직 할당되지 않았습니다.");
        }
        return CrawlSchedulerOutBox.forNew(savedHistoryId, eventPayload, clock);
    }

    /**
     * 저장된 스케줄러 ID 반환
     *
     * @return CrawlSchedulerId (nullable)
     */
    public CrawlSchedulerId getSavedSchedulerId() {
        return savedSchedulerId;
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
