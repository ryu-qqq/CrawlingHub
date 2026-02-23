package com.ryuqq.crawlinghub.application.schedule.service.command;

import com.ryuqq.crawlinghub.application.schedule.component.CrawlSchedulerPersistenceValidator;
import com.ryuqq.crawlinghub.application.schedule.dto.bundle.CrawlSchedulerBundle;
import com.ryuqq.crawlinghub.application.schedule.dto.command.RegisterCrawlSchedulerCommand;
import com.ryuqq.crawlinghub.application.schedule.facade.CrawlerSchedulerFacade;
import com.ryuqq.crawlinghub.application.schedule.factory.command.CrawlSchedulerCommandFactory;
import com.ryuqq.crawlinghub.application.schedule.port.in.command.RegisterCrawlSchedulerUseCase;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
import org.springframework.stereotype.Service;

/**
 * 크롤 스케줄러 등록 UseCase 구현체.
 *
 * <p><strong>트랜잭션</strong>: CommandService는 @Transactional 금지 (Facade 책임)
 */
@Service
public class RegisterCrawlSchedulerService implements RegisterCrawlSchedulerUseCase {

    private final CrawlSchedulerPersistenceValidator validator;
    private final CrawlSchedulerCommandFactory commandFactory;
    private final CrawlerSchedulerFacade facade;

    public RegisterCrawlSchedulerService(
            CrawlSchedulerPersistenceValidator validator,
            CrawlSchedulerCommandFactory commandFactory,
            CrawlerSchedulerFacade facade) {
        this.validator = validator;
        this.commandFactory = commandFactory;
        this.facade = facade;
    }

    @Override
    public long register(RegisterCrawlSchedulerCommand command) {
        validator.validateForRegistration(command.sellerId(), command.schedulerName());
        CrawlSchedulerBundle bundle = commandFactory.createBundle(command);
        CrawlSchedulerId savedId = facade.persist(bundle);
        return savedId.value();
    }
}
