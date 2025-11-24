package com.ryuqq.crawlinghub.application.schedule.service.command;

import com.ryuqq.crawlinghub.application.schedule.assembler.CrawlSchedulerAssembler;
import com.ryuqq.crawlinghub.application.schedule.dto.CrawlSchedulerBundle;
import com.ryuqq.crawlinghub.application.schedule.dto.command.RegisterCrawlSchedulerCommand;
import com.ryuqq.crawlinghub.application.schedule.dto.response.CrawlSchedulerResponse;
import com.ryuqq.crawlinghub.application.schedule.facade.CrawlerSchedulerFacade;
import com.ryuqq.crawlinghub.application.schedule.port.in.command.RegisterCrawlSchedulerUseCase;
import com.ryuqq.crawlinghub.application.schedule.port.out.query.CrawlScheduleQueryPort;
import com.ryuqq.crawlinghub.domain.schedule.exception.DuplicateSchedulerNameException;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;
import org.springframework.stereotype.Service;

/** 크롤 스케줄러 등록 UseCase 구현체. */
@Service
public class RegisterCrawlSchedulerService implements RegisterCrawlSchedulerUseCase {

    private final CrawlSchedulerAssembler crawlSchedulerAssembler;
    private final CrawlerSchedulerFacade crawlerSchedulerFacade;
    private final CrawlScheduleQueryPort crawlScheduleQueryPort;

    public RegisterCrawlSchedulerService(
            CrawlSchedulerAssembler crawlSchedulerAssembler,
            CrawlerSchedulerFacade crawlerSchedulerFacade,
            CrawlScheduleQueryPort crawlScheduleQueryPort) {
        this.crawlSchedulerAssembler = crawlSchedulerAssembler;
        this.crawlerSchedulerFacade = crawlerSchedulerFacade;
        this.crawlScheduleQueryPort = crawlScheduleQueryPort;
    }

    /**
     * 크롤 스케줄러 등록.
     *
     * <p><strong>처리 흐름</strong>:
     *
     * <ol>
     *   <li>Assembler를 통해 CrawlSchedulerBundle 생성
     *   <li>Facade를 통해 저장 (스케줄러 + 히스토리 + 아웃박스, 단일 트랜잭션)
     *   <li>Facade에서 이벤트 발행 (AfterCommit 리스너에서 EventBridge 호출)
     * </ol>
     *
     * @param command 등록 명령 (sellerId, schedulerName, cronExpression)
     * @return 등록된 스케줄러 정보
     */
    @Override
    public CrawlSchedulerResponse register(RegisterCrawlSchedulerCommand command) {
        // 1. 중복 검증 (sellerId + schedulerName)
        validateDuplicateScheduler(command);

        // 2. Assembler를 통해 CrawlSchedulerBundle 생성
        CrawlSchedulerBundle bundle = crawlSchedulerAssembler.toBundle(command);

        // 3. Facade를 통해 저장 (스케줄러 + 히스토리 + 아웃박스, 단일 트랜잭션)
        CrawlSchedulerBundle savedBundle = crawlerSchedulerFacade.persist(bundle);

        // 4. Response 변환
        return crawlSchedulerAssembler.toResponse(savedBundle.getSavedScheduler());
    }

    private void validateDuplicateScheduler(RegisterCrawlSchedulerCommand command) {
        boolean exists =
                crawlScheduleQueryPort.existsBySellerIdAndSchedulerName(
                        SellerId.of(command.sellerId()), command.schedulerName());
        if (exists) {
            throw new DuplicateSchedulerNameException(command.sellerId(), command.schedulerName());
        }
    }
}
