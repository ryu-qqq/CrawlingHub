package com.ryuqq.crawlinghub.application.schedule.service.command;

import com.ryuqq.crawlinghub.application.common.dto.command.UpdateContext;
import com.ryuqq.crawlinghub.application.schedule.assembler.CrawlSchedulerAssembler;
import com.ryuqq.crawlinghub.application.schedule.component.CrawlSchedulerPersistenceValidator;
import com.ryuqq.crawlinghub.application.schedule.dto.command.UpdateCrawlSchedulerCommand;
import com.ryuqq.crawlinghub.application.schedule.dto.response.CrawlSchedulerResponse;
import com.ryuqq.crawlinghub.application.schedule.facade.CrawlerSchedulerFacade;
import com.ryuqq.crawlinghub.application.schedule.factory.command.CrawlSchedulerCommandFactory;
import com.ryuqq.crawlinghub.application.schedule.manager.CrawlSchedulerReadManager;
import com.ryuqq.crawlinghub.application.schedule.port.in.command.UpdateCrawlSchedulerUseCase;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerUpdateData;
import org.springframework.stereotype.Service;

/**
 * 크롤 스케줄러 수정 UseCase 구현체.
 *
 * <p><strong>트랜잭션</strong>: CommandService는 @Transactional 금지 (Facade 책임)
 */
@Service
public class UpdateCrawlSchedulerService implements UpdateCrawlSchedulerUseCase {

    private final CrawlSchedulerPersistenceValidator validator;
    private final CrawlSchedulerReadManager readManager;
    private final CrawlSchedulerCommandFactory commandFactory;
    private final CrawlerSchedulerFacade facade;
    private final CrawlSchedulerAssembler assembler;

    public UpdateCrawlSchedulerService(
            CrawlSchedulerPersistenceValidator validator,
            CrawlSchedulerReadManager readManager,
            CrawlSchedulerCommandFactory commandFactory,
            CrawlerSchedulerFacade facade,
            CrawlSchedulerAssembler assembler) {
        this.validator = validator;
        this.readManager = readManager;
        this.commandFactory = commandFactory;
        this.facade = facade;
        this.assembler = assembler;
    }

    @Override
    public CrawlSchedulerResponse update(UpdateCrawlSchedulerCommand command) {
        // 1. Factory → UpdateContext 생성 (데이터 변환 + 시간 주입)
        UpdateContext<CrawlSchedulerId, CrawlSchedulerUpdateData> context =
                commandFactory.createUpdateContext(command);

        // 2. 스케줄러 조회
        CrawlScheduler crawlScheduler = readManager.getById(context.id());

        // 3. 중복 검증 (이름 변경 시)
        validator.validateDuplicateSchedulerNameForUpdate(
                crawlScheduler, context.updateData().schedulerName().value());

        // 4. 도메인 수정 + Facade를 통해 저장
        crawlScheduler.update(context.updateData(), context.changedAt());
        facade.update(crawlScheduler);

        // 5. Response 변환
        return assembler.toResponse(crawlScheduler);
    }
}
