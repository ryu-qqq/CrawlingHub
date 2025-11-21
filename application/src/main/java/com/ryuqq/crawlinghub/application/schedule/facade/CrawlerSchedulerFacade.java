package com.ryuqq.crawlinghub.application.schedule.facade;

import com.ryuqq.crawlinghub.application.schedule.assembler.CrawlSchedulerAssembler;
import com.ryuqq.crawlinghub.application.schedule.dto.CrawlSchedulerBundle;
import com.ryuqq.crawlinghub.application.schedule.manager.CrawlerSchedulerHistoryManager;
import com.ryuqq.crawlinghub.application.schedule.manager.CrawlerSchedulerManager;
import com.ryuqq.crawlinghub.application.schedule.manager.CrawlerSchedulerOutBoxManager;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerHistory;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerOutBox;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerHistoryId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerId;
import java.time.Clock;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CrawlerSchedulerFacade {

    private final CrawlerSchedulerManager crawlerSchedulerManager;
    private final CrawlerSchedulerOutBoxManager crawlerSchedulerOutBoxManager;
    private final CrawlerSchedulerHistoryManager crawlerSchedulerHistoryManager;
    private final CrawlSchedulerAssembler crawlSchedulerAssembler;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    public CrawlerSchedulerFacade(
            CrawlerSchedulerManager crawlerSchedulerManager,
            CrawlerSchedulerOutBoxManager crawlerSchedulerOutBoxManager,
            CrawlerSchedulerHistoryManager crawlerSchedulerHistoryManager,
            CrawlSchedulerAssembler crawlSchedulerAssembler,
            ApplicationEventPublisher eventPublisher,
            Clock clock) {
        this.crawlerSchedulerManager = crawlerSchedulerManager;
        this.crawlerSchedulerOutBoxManager = crawlerSchedulerOutBoxManager;
        this.crawlerSchedulerHistoryManager = crawlerSchedulerHistoryManager;
        this.crawlSchedulerAssembler = crawlSchedulerAssembler;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
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
     * @return 저장된 스케줄러 번들 (ID 할당됨)
     */
    @Transactional
    public CrawlSchedulerBundle persist(CrawlSchedulerBundle bundle) {
        // 1. 스케줄러 저장 → ID 반환 → 번들에 설정
        CrawlSchedulerId savedId = crawlerSchedulerManager.persist(bundle.getScheduler());
        bundle.withSchedulerId(savedId);

        // 2. 히스토리 저장 (스케줄러 ID 참조)
        CrawlSchedulerHistory history = bundle.createHistory();
        CrawlSchedulerHistoryId historyId = crawlerSchedulerHistoryManager.persist(history);
        bundle.withHistoryId(historyId);

        // 3. 아웃박스 저장 (히스토리 ID 참조)
        CrawlSchedulerOutBox outBox = bundle.createOutBox();
        crawlerSchedulerOutBoxManager.persist(outBox);

        // 4. 도메인 이벤트 발행 (AfterCommit 리스너에서 처리)
        // getSavedScheduler()가 자동으로 등록 이벤트를 발행
        CrawlScheduler savedScheduler = bundle.getSavedScheduler();
        savedScheduler.getDomainEvents().forEach(eventPublisher::publishEvent);
        savedScheduler.clearDomainEvents();

        return bundle;
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
        // 1. 스케줄러 저장
        crawlerSchedulerManager.persist(crawlScheduler);

        // 2. 이벤트가 있는 경우에만 히스토리/아웃박스 저장
        if (!crawlScheduler.getDomainEvents().isEmpty()) {
            CrawlSchedulerHistory history =
                    CrawlSchedulerHistory.fromScheduler(crawlScheduler, clock);
            CrawlSchedulerHistoryId historyId = crawlerSchedulerHistoryManager.persist(history);

            String eventPayload = crawlSchedulerAssembler.toEventPayload(crawlScheduler);
            CrawlSchedulerOutBox outBox =
                    CrawlSchedulerOutBox.forNew(historyId, eventPayload, clock);
            crawlerSchedulerOutBoxManager.persist(outBox);

            // 3. 이벤트 발행
            crawlScheduler.getDomainEvents().forEach(eventPublisher::publishEvent);
            crawlScheduler.clearDomainEvents();
        }
    }
}
