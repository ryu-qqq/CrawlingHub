package com.ryuqq.crawlinghub.application.schedule.dto;

import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerHistory;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerOutBox;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerHistoryId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerId;
import java.time.Clock;

/**
 * 크롤 스케줄러 번들 VO
 *
 * <p><strong>용도</strong>: 스케줄러, 히스토리, 아웃박스를 하나로 묶어 관리
 *
 * <p><strong>저장 흐름</strong>:
 *
 * <ol>
 *   <li>스케줄러 저장 → ID 반환 → withSchedulerId()
 *   <li>히스토리 저장 (스케줄러 ID 참조) → ID 반환 → withHistoryId()
 *   <li>아웃박스 저장 (히스토리 ID 참조)
 * </ol>
 *
 * @author development-team
 * @since 1.0.0
 */
public class CrawlSchedulerBundle {

    private final CrawlScheduler scheduler;
    private final String eventPayload;
    private final Clock clock;

    private CrawlSchedulerId savedSchedulerId;
    private CrawlSchedulerHistoryId savedHistoryId;

    private CrawlSchedulerBundle(CrawlScheduler scheduler, String eventPayload, Clock clock) {
        this.scheduler = scheduler;
        this.eventPayload = eventPayload;
        this.clock = clock;
    }

    /**
     * 번들 생성
     *
     * @param scheduler 스케줄러 Aggregate
     * @param eventPayload 이벤트 페이로드 (JSON)
     * @param clock 시간 제어
     * @return CrawlSchedulerBundle
     */
    public static CrawlSchedulerBundle of(
            CrawlScheduler scheduler, String eventPayload, Clock clock) {
        return new CrawlSchedulerBundle(scheduler, eventPayload, clock);
    }

    /**
     * 스케줄러 ID 설정 (저장 후 호출)
     *
     * @param schedulerId 저장된 스케줄러 ID
     */
    public void withSchedulerId(CrawlSchedulerId schedulerId) {
        this.savedSchedulerId = schedulerId;
    }

    /**
     * 히스토리 ID 설정 (저장 후 호출)
     *
     * @param historyId 저장된 히스토리 ID
     */
    public void withHistoryId(CrawlSchedulerHistoryId historyId) {
        this.savedHistoryId = historyId;
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
     * <p><strong>주의</strong>: 이 메서드는 등록 이벤트를 자동 발행합니다. 이미 발행된 스케줄러를 다시 가져오려면 캐시된 인스턴스를 사용하세요.
     *
     * @return CrawlScheduler with ID (이벤트 발행됨)
     * @throws IllegalStateException ID가 아직 할당되지 않은 경우
     */
    public CrawlScheduler getSavedScheduler() {
        if (savedSchedulerId == null) {
            throw new IllegalStateException("스케줄러 ID가 아직 할당되지 않았습니다.");
        }
        CrawlScheduler savedScheduler =
                CrawlScheduler.of(
                        savedSchedulerId,
                        scheduler.getSellerId(),
                        scheduler.getSchedulerName(),
                        scheduler.getCronExpression(),
                        scheduler.getStatus(),
                        scheduler.getCreatedAt(),
                        scheduler.getUpdatedAt(),
                        clock);

        // ID 할당 후 자동으로 등록 이벤트 발행
        savedScheduler.addRegisteredEvent();
        return savedScheduler;
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
        return CrawlSchedulerHistory.fromScheduler(getSavedScheduler(), clock);
    }

    /**
     * 아웃박스 생성 (히스토리 ID 할당 후)
     *
     * @return CrawlSchedulerOutBox
     * @throws IllegalStateException 히스토리 ID가 아직 할당되지 않은 경우
     */
    public CrawlSchedulerOutBox createOutBox() {
        if (savedHistoryId == null) {
            throw new IllegalStateException("히스토리 ID가 아직 할당되지 않았습니다.");
        }
        return CrawlSchedulerOutBox.forNew(savedHistoryId, eventPayload, clock);
    }

    /**
     * 저장된 스케줄러 ID 반환
     *
     * @return CrawlSchedulerId
     */
    public CrawlSchedulerId getSavedSchedulerId() {
        return savedSchedulerId;
    }

    /**
     * 저장된 히스토리 ID 반환
     *
     * @return CrawlSchedulerHistoryId
     */
    public CrawlSchedulerHistoryId getSavedHistoryId() {
        return savedHistoryId;
    }
}
