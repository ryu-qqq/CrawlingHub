package com.ryuqq.crawlinghub.application.schedule.facade;

import com.ryuqq.crawlinghub.application.common.time.TimeProvider;
import com.ryuqq.crawlinghub.application.schedule.dto.CrawlSchedulerBundle;
import com.ryuqq.crawlinghub.application.schedule.factory.command.CrawlSchedulerCommandFactory;
import com.ryuqq.crawlinghub.application.schedule.manager.CrawlSchedulerHistoryTransactionManager;
import com.ryuqq.crawlinghub.application.schedule.manager.CrawlSchedulerOutBoxTransactionManager;
import com.ryuqq.crawlinghub.application.schedule.manager.CrawlSchedulerTransactionManager;
import com.ryuqq.crawlinghub.domain.common.event.DomainEvent;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerHistory;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerOutBox;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerHistoryId;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CronExpression;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerName;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import java.time.Instant;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * CrawlScheduler CommandFacade
 *
 * <p><strong>책임</strong>: 여러 TransactionManager 조합하여 단일 트랜잭션으로 처리
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlerSchedulerFacade {

    private final CrawlSchedulerTransactionManager crawlerSchedulerManager;
    private final CrawlSchedulerOutBoxTransactionManager crawlerSchedulerOutBoxManager;
    private final CrawlSchedulerHistoryTransactionManager crawlerSchedulerHistoryManager;
    private final CrawlSchedulerCommandFactory commandFactory;
    private final ApplicationEventPublisher eventPublisher;
    private final TimeProvider timeProvider;

    public CrawlerSchedulerFacade(
            CrawlSchedulerTransactionManager crawlerSchedulerManager,
            CrawlSchedulerOutBoxTransactionManager crawlerSchedulerOutBoxManager,
            CrawlSchedulerHistoryTransactionManager crawlerSchedulerHistoryManager,
            CrawlSchedulerCommandFactory commandFactory,
            ApplicationEventPublisher eventPublisher,
            TimeProvider timeProvider) {
        this.crawlerSchedulerManager = crawlerSchedulerManager;
        this.crawlerSchedulerOutBoxManager = crawlerSchedulerOutBoxManager;
        this.crawlerSchedulerHistoryManager = crawlerSchedulerHistoryManager;
        this.commandFactory = commandFactory;
        this.eventPublisher = eventPublisher;
        this.timeProvider = timeProvider;
    }

    /**
     * 스케줄러 번들을 하나의 트랜잭션으로 저장.
     *
     * <p><strong>트랜잭션 범위</strong>:
     *
     * <ol>
     *   <li>스케줄러 저장 → ID 반환 → 번들에 설정
     *   <li>히스토리 저장 (스케줄러 ID 참조) → ID 반환 → 번들에 설정
     *   <li>아웃박스 저장 (히스토리 ID 참조)
     *   <li>도메인 이벤트 발행 (트랜잭션 커밋 후 처리)
     * </ol>
     *
     * @param bundle 저장할 스케줄러 번들
     * @return 저장된 CrawlScheduler (ID 할당됨)
     */
    @Transactional
    public CrawlScheduler persist(CrawlSchedulerBundle bundle) {
        Instant now = timeProvider.now();

        // 1. 스케줄러 저장 → ID 반환 → 새 번들 반환 (Immutable)
        CrawlSchedulerId savedId = crawlerSchedulerManager.persist(bundle.getScheduler());
        bundle = bundle.withSchedulerId(savedId);

        // 2. 히스토리 저장 (스케줄러 ID 참조) → 새 번들 반환 (Immutable)
        CrawlSchedulerHistory history = bundle.createHistory(now);
        CrawlSchedulerHistoryId historyId = crawlerSchedulerHistoryManager.persist(history);
        bundle = bundle.withHistoryId(historyId);

        // 3. 아웃박스 저장 (히스토리 ID 참조)
        CrawlSchedulerOutBox outBox = bundle.createOutBox(now);
        crawlerSchedulerOutBoxManager.persist(outBox);

        // 4. 도메인 이벤트 발행 + 저장된 스케줄러 반환
        CrawlScheduler savedScheduler = bundle.getSavedScheduler(now);
        savedScheduler.pollEvents().forEach(eventPublisher::publishEvent);

        return savedScheduler;
    }

    /**
     * 스케줄러 수정 저장.
     *
     * <p><strong>트랜잭션 범위</strong>:
     *
     * <ol>
     *   <li>스케줄러 저장
     *   <li>이벤트가 있는 경우 히스토리 + 아웃박스 저장
     *   <li>도메인 이벤트 발행
     * </ol>
     *
     * @param crawlScheduler 수정된 스케줄러
     */
    @Transactional
    public void update(CrawlScheduler crawlScheduler) {
        Instant now = timeProvider.now();

        // 1. 스케줄러 저장
        crawlerSchedulerManager.persist(crawlScheduler);

        // 2. 이벤트가 있는 경우에만 히스토리/아웃박스 저장
        List<DomainEvent> events = crawlScheduler.pollEvents();
        if (!events.isEmpty()) {
            CrawlSchedulerHistory history =
                    CrawlSchedulerHistory.fromScheduler(crawlScheduler, now);
            CrawlSchedulerHistoryId historyId = crawlerSchedulerHistoryManager.persist(history);

            String eventPayload = commandFactory.toEventPayload(crawlScheduler);
            CrawlSchedulerOutBox outBox = CrawlSchedulerOutBox.forNew(historyId, eventPayload, now);
            crawlerSchedulerOutBoxManager.persist(outBox);

            // 3. 이벤트 발행
            events.forEach(eventPublisher::publishEvent);
        }
    }

    /**
     * 스케줄러 정보 수정 + 저장.
     *
     * <p>TimeProvider를 캡슐화하여 Service에서 시간 의존성 제거
     *
     * @param crawlScheduler 수정 대상 스케줄러
     * @param newName 새로운 스케줄러 이름
     * @param newCronExpression 새로운 Cron 표현식
     * @param newStatus 새로운 상태
     */
    @Transactional
    public void updateScheduler(
            CrawlScheduler crawlScheduler,
            SchedulerName newName,
            CronExpression newCronExpression,
            SchedulerStatus newStatus) {
        Instant now = timeProvider.now();
        crawlScheduler.update(newName, newCronExpression, newStatus, now);
        update(crawlScheduler);
    }

    /**
     * 여러 스케줄러 일괄 수정 저장.
     *
     * <p><strong>트랜잭션 범위</strong>: 모든 스케줄러 업데이트를 단일 트랜잭션으로 처리
     *
     * @param schedulers 수정할 스케줄러 목록
     */
    @Transactional
    public void updateAll(java.util.List<CrawlScheduler> schedulers) {
        for (CrawlScheduler scheduler : schedulers) {
            update(scheduler);
        }
    }

    /**
     * 여러 스케줄러 일괄 비활성화.
     *
     * <p>TimeProvider를 캡슐화하여 Service에서 시간 의존성 제거
     *
     * <p><strong>트랜잭션 범위</strong>: 모든 스케줄러 비활성화를 단일 트랜잭션으로 처리
     *
     * @param schedulers 비활성화할 스케줄러 목록
     */
    @Transactional
    public void deactivateSchedulers(java.util.List<CrawlScheduler> schedulers) {
        Instant now = timeProvider.now();
        for (CrawlScheduler scheduler : schedulers) {
            scheduler.update(
                    scheduler.getSchedulerName(),
                    scheduler.getCronExpression(),
                    SchedulerStatus.INACTIVE,
                    now);
            update(scheduler);
        }
    }
}
