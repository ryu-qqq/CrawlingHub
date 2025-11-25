package com.ryuqq.crawlinghub.application.crawl.task.service.command;

import com.ryuqq.crawlinghub.application.crawl.task.assembler.CrawlTaskAssembler;
import com.ryuqq.crawlinghub.application.crawl.task.dto.command.TriggerCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.crawl.task.dto.response.CrawlTaskResponse;
import com.ryuqq.crawlinghub.application.crawl.task.manager.CrawlTaskTransactionManager;
import com.ryuqq.crawlinghub.application.crawl.task.port.in.command.TriggerCrawlTaskUseCase;
import com.ryuqq.crawlinghub.domain.crawl.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.schedule.identifier.CrawlSchedulerId;
import org.springframework.stereotype.Service;

/**
 * CrawlTask 트리거 Service
 *
 * <p>TriggerCrawlTaskUseCase 구현체
 *
 * <ul>
 *   <li>EventBridge에서 호출되어 CrawlTask 생성</li>
 *   <li>TransactionManager에 위임하여 트랜잭션 처리</li>
 *   <li>Assembler로 응답 변환</li>
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class TriggerCrawlTaskService implements TriggerCrawlTaskUseCase {

    private final CrawlTaskTransactionManager transactionManager;
    private final CrawlTaskAssembler assembler;

    public TriggerCrawlTaskService(
            CrawlTaskTransactionManager transactionManager,
            CrawlTaskAssembler assembler
    ) {
        this.transactionManager = transactionManager;
        this.assembler = assembler;
    }

    @Override
    public CrawlTaskResponse execute(TriggerCrawlTaskCommand command) {
        // 1. TransactionManager에 위임 (검증 + 생성 + 저장 + SQS 발행 예약)
        CrawlTask crawlTask = transactionManager.trigger(
                CrawlSchedulerId.of(command.crawlSchedulerId())
        );

        // 2. 응답 변환 (Assembler)
        return assembler.toResponse(crawlTask);
    }
}
