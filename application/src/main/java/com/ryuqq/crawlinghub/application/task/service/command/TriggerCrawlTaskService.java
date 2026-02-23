package com.ryuqq.crawlinghub.application.task.service.command;

import com.ryuqq.crawlinghub.application.common.metric.annotation.CrawlMetric;
import com.ryuqq.crawlinghub.application.seller.manager.SellerReadManager;
import com.ryuqq.crawlinghub.application.task.dto.bundle.CrawlTaskBundle;
import com.ryuqq.crawlinghub.application.task.dto.command.TriggerCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.task.factory.command.CrawlTaskCommandFactory;
import com.ryuqq.crawlinghub.application.task.internal.CrawlTaskCommandFacade;
import com.ryuqq.crawlinghub.application.task.port.in.command.TriggerCrawlTaskUseCase;
import com.ryuqq.crawlinghub.application.task.validator.CrawlTaskPersistenceValidator;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.exception.SellerNotFoundException;
import org.springframework.stereotype.Service;

/**
 * CrawlTask 트리거 Service
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class TriggerCrawlTaskService implements TriggerCrawlTaskUseCase {

    private final CrawlTaskPersistenceValidator validator;
    private final CrawlTaskCommandFactory commandFactory;
    private final CrawlTaskCommandFacade coordinator;
    private final SellerReadManager sellerReadManager;

    public TriggerCrawlTaskService(
            CrawlTaskPersistenceValidator validator,
            CrawlTaskCommandFactory commandFactory,
            CrawlTaskCommandFacade coordinator,
            SellerReadManager sellerReadManager) {
        this.validator = validator;
        this.commandFactory = commandFactory;
        this.coordinator = coordinator;
        this.sellerReadManager = sellerReadManager;
    }

    @CrawlMetric(value = "crawl_task", operation = "trigger")
    @Override
    public void execute(TriggerCrawlTaskCommand command) {
        CrawlSchedulerId crawlSchedulerId = CrawlSchedulerId.of(command.crawlSchedulerId());

        // 1. 스케줄러 조회 및 검증 (ACTIVE 상태만)
        CrawlScheduler scheduler = validator.findAndValidateScheduler(crawlSchedulerId);

        // 2. Seller 조회
        Seller seller =
                sellerReadManager
                        .findById(scheduler.getSellerId())
                        .orElseThrow(
                                () -> new SellerNotFoundException(scheduler.getSellerIdValue()));

        // 3. CrawlTaskBundle 생성
        CrawlTaskBundle bundle = commandFactory.createBundle(scheduler, seller);

        // 4. 중복 Task 검증
        validator.validateNoDuplicateTask(bundle.crawlTask());

        // 5. Coordinator에서 persist
        coordinator.persist(bundle);
    }
}
