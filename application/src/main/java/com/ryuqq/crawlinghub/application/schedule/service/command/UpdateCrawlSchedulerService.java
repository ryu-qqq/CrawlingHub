package com.ryuqq.crawlinghub.application.schedule.service.command;

import com.ryuqq.crawlinghub.application.schedule.assembler.CrawlSchedulerAssembler;
import com.ryuqq.crawlinghub.application.schedule.dto.command.UpdateCrawlSchedulerCommand;
import com.ryuqq.crawlinghub.application.schedule.dto.response.CrawlSchedulerResponse;
import com.ryuqq.crawlinghub.application.schedule.facade.CrawlerSchedulerFacade;
import com.ryuqq.crawlinghub.application.schedule.manager.query.CrawlSchedulerReadManager;
import com.ryuqq.crawlinghub.application.schedule.port.in.command.UpdateCrawlSchedulerUseCase;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.exception.CrawlSchedulerNotFoundException;
import com.ryuqq.crawlinghub.domain.schedule.exception.DuplicateSchedulerNameException;
import com.ryuqq.crawlinghub.domain.schedule.identifier.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CronExpression;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerName;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import org.springframework.stereotype.Service;

/**
 * 크롤 스케줄러 수정 UseCase 구현체.
 *
 * <p><strong>ClockHolder</strong>: Facade가 관리 (Service는 시간 의존성 없음)
 */
@Service
public class UpdateCrawlSchedulerService implements UpdateCrawlSchedulerUseCase {

    private final CrawlSchedulerReadManager readManager;
    private final CrawlerSchedulerFacade crawlerSchedulerFacade;
    private final CrawlSchedulerAssembler crawlSchedulerAssembler;

    public UpdateCrawlSchedulerService(
            CrawlSchedulerReadManager readManager,
            CrawlerSchedulerFacade crawlerSchedulerFacade,
            CrawlSchedulerAssembler crawlSchedulerAssembler) {
        this.readManager = readManager;
        this.crawlerSchedulerFacade = crawlerSchedulerFacade;
        this.crawlSchedulerAssembler = crawlSchedulerAssembler;
    }

    /**
     * 크롤 스케줄러 수정.
     *
     * <p><strong>처리 흐름</strong>:
     *
     * <ol>
     *   <li>스케줄러 조회
     *   <li>중복 검증 (이름 변경 시)
     *   <li>Aggregate에서 비즈니스 로직 수행
     *   <li>Facade를 통해 저장 + 이벤트 발행
     * </ol>
     *
     * @param command 수정 명령 (crawlSchedulerId, schedulerName, cronExpression, active)
     * @return 수정된 스케줄러 정보
     */
    @Override
    public CrawlSchedulerResponse update(UpdateCrawlSchedulerCommand command) {
        // 1. 스케줄러 조회
        CrawlScheduler crawlScheduler =
                readManager
                        .findById(CrawlSchedulerId.of(command.crawlSchedulerId()))
                        .orElseThrow(
                                () ->
                                        new CrawlSchedulerNotFoundException(
                                                command.crawlSchedulerId()));

        // 2. 중복 검증 (이름 변경 시)
        validateDuplicateSchedulerName(crawlScheduler, command.schedulerName());

        // 3. Facade에서 상태 변경 + 저장 + 이벤트 발행 (ClockHolder 캡슐화)
        SchedulerStatus newStatus =
                command.active() ? SchedulerStatus.ACTIVE : SchedulerStatus.INACTIVE;

        crawlerSchedulerFacade.updateScheduler(
                crawlScheduler,
                SchedulerName.of(command.schedulerName()),
                CronExpression.of(command.cronExpression()),
                newStatus);

        // 4. Response 변환
        return crawlSchedulerAssembler.toResponse(crawlScheduler);
    }

    private void validateDuplicateSchedulerName(
            CrawlScheduler currentScheduler, String newSchedulerName) {
        if (currentScheduler.hasSameSchedulerName(newSchedulerName)) {
            return;
        }
        SellerId sellerId = currentScheduler.getSellerId();
        boolean exists = readManager.existsBySellerIdAndSchedulerName(sellerId, newSchedulerName);
        if (exists) {
            throw new DuplicateSchedulerNameException(sellerId.value(), newSchedulerName);
        }
    }
}
