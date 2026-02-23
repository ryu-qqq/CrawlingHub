package com.ryuqq.crawlinghub.application.execution.internal;

import com.ryuqq.crawlinghub.application.execution.dto.bundle.CrawlTaskExecutionBundle;
import com.ryuqq.crawlinghub.application.execution.manager.CrawlExecutionCommandManager;
import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskCommandManager;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Execution Command Facade
 *
 * <p><strong>책임</strong>: CrawlTask + CrawlExecution 두 Aggregate의 persist를 @Transactional 경계 내에서 관리
 *
 * <p><strong>트랜잭션 원칙</strong>: Coordinator는 도메인 상태만 변경하고, 변경된 도메인 객체를 이 Facade에 넘겨 트랜잭션으로 묶어 저장합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class ExecutionCommandFacade {

    private final CrawlTaskCommandManager taskCommandManager;
    private final CrawlExecutionCommandManager executionCommandManager;

    public ExecutionCommandFacade(
            CrawlTaskCommandManager taskCommandManager,
            CrawlExecutionCommandManager executionCommandManager) {
        this.taskCommandManager = taskCommandManager;
        this.executionCommandManager = executionCommandManager;
    }

    /**
     * persist (Execution 완료 + Task 성공/실패)
     *
     * <p>트랜잭션 내에서 수행:
     *
     * <ol>
     *   <li>CrawlExecution 저장 (성공/실패 상태)
     *   <li>CrawlTask 저장 (SUCCESS/FAILED 상태)
     * </ol>
     *
     * @param bundle 도메인 상태가 변경된 Bundle
     */
    @Transactional
    public void persist(CrawlTaskExecutionBundle bundle) {
        executionCommandManager.persist(bundle.execution());
        taskCommandManager.persist(bundle.crawlTask());
    }

    /**
     * CrawlTask 단건 persist (즉시 실패 처리용)
     *
     * @param task 저장할 CrawlTask
     */
    @Transactional
    public void persistTask(CrawlTask task) {
        taskCommandManager.persist(task);
    }
}
