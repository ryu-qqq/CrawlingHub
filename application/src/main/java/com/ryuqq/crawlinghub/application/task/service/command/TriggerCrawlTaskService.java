package com.ryuqq.crawlinghub.application.task.service.command;

import com.ryuqq.crawlinghub.application.seller.port.out.query.SellerQueryPort;
import com.ryuqq.crawlinghub.application.task.assembler.CrawlTaskAssembler;
import com.ryuqq.crawlinghub.application.task.component.CrawlTaskPersistenceValidator;
import com.ryuqq.crawlinghub.application.task.dto.bundle.CrawlTaskBundle;
import com.ryuqq.crawlinghub.application.task.dto.command.TriggerCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskResponse;
import com.ryuqq.crawlinghub.application.task.facade.CrawlTaskFacade;
import com.ryuqq.crawlinghub.application.task.factory.command.CrawlTaskCommandFactory;
import com.ryuqq.crawlinghub.application.task.port.in.command.TriggerCrawlTaskUseCase;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.identifier.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.exception.SellerNotFoundException;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import org.springframework.stereotype.Service;

/**
 * CrawlTask 트리거 Service
 *
 * <p>TriggerCrawlTaskUseCase 구현체
 *
 * <ul>
 *   <li>EventBridge에서 호출되어 CrawlTask 생성
 *   <li>CommandFactory로 번들 생성
 *   <li>Facade에 위임하여 트랜잭션 처리
 *   <li>ClockHolder 의존성 없음 (Facade가 관리)
 * </ul>
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>Validator로 스케줄러 조회 및 검증 (ACTIVE 상태만)
 *   <li>CommandFactory로 CrawlTaskBundle 생성 (Task + Outbox payload)
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
    private final CrawlTaskCommandFactory commandFactory;
    private final CrawlTaskAssembler assembler;
    private final CrawlTaskFacade facade;
    private final SellerQueryPort sellerQueryPort;

    public TriggerCrawlTaskService(
            CrawlTaskPersistenceValidator validator,
            CrawlTaskCommandFactory commandFactory,
            CrawlTaskAssembler assembler,
            CrawlTaskFacade facade,
            SellerQueryPort sellerQueryPort) {
        this.validator = validator;
        this.commandFactory = commandFactory;
        this.assembler = assembler;
        this.facade = facade;
        this.sellerQueryPort = sellerQueryPort;
    }

    @Override
    public CrawlTaskResponse execute(TriggerCrawlTaskCommand command) {
        CrawlSchedulerId crawlSchedulerId = CrawlSchedulerId.of(command.crawlSchedulerId());

        // 1. 스케줄러 조회 및 검증 (ACTIVE 상태만)
        CrawlScheduler scheduler = validator.findAndValidateScheduler(crawlSchedulerId);

        // 2. Seller 조회 (mustItSellerName 조회용)
        Seller seller =
                sellerQueryPort
                        .findById(scheduler.getSellerId())
                        .orElseThrow(
                                () -> new SellerNotFoundException(scheduler.getSellerIdValue()));

        // 3. CrawlTaskBundle 생성 (CommandFactory)
        CrawlTaskBundle bundle = commandFactory.createBundle(command, scheduler, seller);

        // 4. Facade에서 persist (중복 검증 + 저장) → CrawlTask 반환
        CrawlTask savedTask = facade.persist(bundle);

        // 5. 응답 변환 (Assembler)
        return assembler.toResponse(savedTask);
    }
}
