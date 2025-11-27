package com.ryuqq.crawlinghub.application.task.service.command;

import com.ryuqq.crawlinghub.application.task.assembler.CrawlTaskAssembler;
import com.ryuqq.crawlinghub.application.task.component.CrawlTaskPersistenceValidator;
import com.ryuqq.crawlinghub.application.task.dto.CrawlTaskBundle;
import com.ryuqq.crawlinghub.application.task.dto.command.TriggerCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskResponse;
import com.ryuqq.crawlinghub.application.task.facade.CrawlTaskFacade;
import com.ryuqq.crawlinghub.application.task.port.in.command.TriggerCrawlTaskUseCase;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.identifier.CrawlSchedulerId;
import org.springframework.stereotype.Service;

/**
 * CrawlTask 트리거 Service
 *
 * <p>TriggerCrawlTaskUseCase 구현체
 *
 * <ul>
 *   <li>EventBridge에서 호출되어 CrawlTask 생성
 *   <li>Assembler로 번들 생성
 *   <li>Facade에 위임하여 트랜잭션 처리
 * </ul>
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>Validator로 스케줄러 조회 및 검증 (ACTIVE 상태만)
 *   <li>Assembler로 CrawlTaskBundle 생성 (Task + Outbox payload)
 *   <li>Facade에서 persist (검증 + 저장)
 *   <li>Assembler로 응답 변환
 * </ol>
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class TriggerCrawlTaskService implements TriggerCrawlTaskUseCase {

    private final CrawlTaskPersistenceValidator validator;
    private final CrawlTaskAssembler assembler;
    private final CrawlTaskFacade facade;

    public TriggerCrawlTaskService(
            CrawlTaskPersistenceValidator validator,
            CrawlTaskAssembler assembler,
            CrawlTaskFacade facade) {
        this.validator = validator;
        this.assembler = assembler;
        this.facade = facade;
    }

    @Override
    public CrawlTaskResponse execute(TriggerCrawlTaskCommand command) {
        CrawlSchedulerId crawlSchedulerId = CrawlSchedulerId.of(command.crawlSchedulerId());

        // 1. 스케줄러 조회 및 검증 (ACTIVE 상태만)
        CrawlScheduler scheduler = validator.findAndValidateScheduler(crawlSchedulerId);

        // 2. CrawlTaskBundle 생성 (Assembler)
        CrawlTaskBundle bundle = assembler.toBundle(command, scheduler);

        // 3. Facade에서 persist (중복 검증 + 저장)
        CrawlTaskBundle savedBundle = facade.persist(bundle);

        // 4. 응답 변환 (Assembler)
        return assembler.toResponse(savedBundle.getSavedCrawlTask());
    }
}
