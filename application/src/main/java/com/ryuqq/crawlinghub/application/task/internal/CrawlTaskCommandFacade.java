package com.ryuqq.crawlinghub.application.task.internal;

import com.ryuqq.crawlinghub.application.task.dto.bundle.CrawlTaskBundle;
import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskCommandManager;
import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskOutboxCommandManager;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.id.CrawlTaskId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * CrawlTask Command Coordinator
 *
 * <p><strong>책임</strong>: CrawlTask + Outbox 저장 트랜잭션 조율
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>CrawlTask 저장 → ID 반환
 *   <li>Bundle에 ID 설정 → Outbox 생성
 *   <li>Outbox 저장
 * </ol>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlTaskCommandFacade {

    private final CrawlTaskCommandManager commandManager;
    private final CrawlTaskOutboxCommandManager outboxCommandManager;

    public CrawlTaskCommandFacade(
            CrawlTaskCommandManager commandManager,
            CrawlTaskOutboxCommandManager outboxCommandManager) {
        this.commandManager = commandManager;
        this.outboxCommandManager = outboxCommandManager;
    }

    /**
     * CrawlTask + Outbox 저장 (단일 트랜잭션)
     *
     * @param bundle CrawlTask 번들
     * @return 저장된 CrawlTask ID
     */
    @Transactional
    public CrawlTaskId persist(CrawlTaskBundle bundle) {
        CrawlTask crawlTask = bundle.crawlTask();

        // 1. CrawlTask 저장 → ID 반환
        CrawlTaskId savedTaskId = commandManager.persist(crawlTask);

        // 2. Bundle에 ID 설정 → Outbox 생성 및 저장
        CrawlTaskBundle enrichedBundle = bundle.withTaskId(savedTaskId);
        outboxCommandManager.persist(enrichedBundle.createOutbox());

        return savedTaskId;
    }

    /**
     * CrawlTask 재시도 (상태 업데이트 + 새 Outbox 생성)
     *
     * @param crawlTask 재시도할 CrawlTask (attemptRetry 호출 완료 상태)
     * @param bundle 재시도용 번들
     */
    @Transactional
    public void retry(CrawlTask crawlTask, CrawlTaskBundle bundle) {
        // 1. CrawlTask 업데이트 저장
        commandManager.persist(crawlTask);

        // 2. Outbox 저장 (SQS 재발행용)
        CrawlTaskBundle enrichedBundle = bundle.withTaskId(crawlTask.getId());
        outboxCommandManager.persist(enrichedBundle.createOutbox());
    }
}
