package com.ryuqq.crawlinghub.application.schedule.facade;

import com.ryuqq.crawlinghub.application.schedule.dto.bundle.CrawlSchedulerBundle;
import com.ryuqq.crawlinghub.application.schedule.manager.CrawlSchedulerCommandManager;
import com.ryuqq.crawlinghub.application.schedule.manager.CrawlSchedulerHistoryCommandManager;
import com.ryuqq.crawlinghub.application.schedule.manager.CrawlSchedulerOutBoxCommandManager;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerHistory;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerOutBox;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerHistoryId;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
import java.time.Instant;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * CrawlScheduler CommandFacade
 *
 * <p><strong>책임</strong>: 여러 TransactionManager 조합하여 단일 트랜잭션으로 처리
 *
 * <p><strong>순수 트랜잭션 경계</strong>: EventPublisher/TimeProvider 없이 순수 저장만 담당
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlerSchedulerFacade {

    private final CrawlSchedulerCommandManager crawlerSchedulerManager;
    private final CrawlSchedulerOutBoxCommandManager crawlerSchedulerOutBoxManager;
    private final CrawlSchedulerHistoryCommandManager crawlerSchedulerHistoryManager;

    public CrawlerSchedulerFacade(
            CrawlSchedulerCommandManager crawlerSchedulerManager,
            CrawlSchedulerOutBoxCommandManager crawlerSchedulerOutBoxManager,
            CrawlSchedulerHistoryCommandManager crawlerSchedulerHistoryManager) {
        this.crawlerSchedulerManager = crawlerSchedulerManager;
        this.crawlerSchedulerOutBoxManager = crawlerSchedulerOutBoxManager;
        this.crawlerSchedulerHistoryManager = crawlerSchedulerHistoryManager;
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
     * </ol>
     *
     * @param bundle 저장할 스케줄러 번들
     * @return 저장된 스케줄러 ID
     */
    @Transactional
    public CrawlSchedulerId persist(CrawlSchedulerBundle bundle) {
        // 1. 스케줄러 저장 → ID 반환 → 새 번들 반환 (Immutable)
        CrawlSchedulerId savedId = crawlerSchedulerManager.persist(bundle.getScheduler());
        bundle = bundle.withSchedulerId(savedId);

        // 2. 히스토리 저장 (스케줄러 ID 참조) → 새 번들 반환 (Immutable)
        CrawlSchedulerHistory history = bundle.createHistory();
        CrawlSchedulerHistoryId historyId = crawlerSchedulerHistoryManager.persist(history);
        bundle = bundle.withHistoryId(historyId);

        // 3. 아웃박스 저장 (히스토리 ID 참조)
        CrawlSchedulerOutBox outBox = bundle.createOutBox();
        crawlerSchedulerOutBoxManager.persist(outBox);

        // 4. 저장된 스케줄러 ID 반환
        return bundle.getSavedSchedulerId();
    }

    /**
     * 스케줄러 수정 저장.
     *
     * <p><strong>트랜잭션 범위</strong>:
     *
     * <ol>
     *   <li>스케줄러 저장
     *   <li>히스토리 + 아웃박스 저장
     * </ol>
     *
     * @param crawlScheduler 수정된 스케줄러
     */
    @Transactional
    public void update(CrawlScheduler crawlScheduler) {
        Instant now = crawlScheduler.getUpdatedAt();

        // 1. 스케줄러 저장
        crawlerSchedulerManager.persist(crawlScheduler);

        // 2. 히스토리/아웃박스 저장
        CrawlSchedulerHistory history = CrawlSchedulerHistory.fromScheduler(crawlScheduler, now);
        CrawlSchedulerHistoryId historyId = crawlerSchedulerHistoryManager.persist(history);

        CrawlSchedulerOutBox outBox =
                CrawlSchedulerOutBox.forNew(
                        historyId,
                        crawlScheduler.getCrawlSchedulerIdValue(),
                        crawlScheduler.getSellerIdValue(),
                        crawlScheduler.getSchedulerNameValue(),
                        crawlScheduler.getCronExpressionValue(),
                        crawlScheduler.getStatus(),
                        now);
        crawlerSchedulerOutBoxManager.persist(outBox);
    }
}
