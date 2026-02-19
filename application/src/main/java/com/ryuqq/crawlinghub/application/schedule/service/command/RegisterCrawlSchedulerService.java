package com.ryuqq.crawlinghub.application.schedule.service.command;

import com.ryuqq.crawlinghub.application.schedule.assembler.CrawlSchedulerAssembler;
import com.ryuqq.crawlinghub.application.schedule.dto.CrawlSchedulerBundle;
import com.ryuqq.crawlinghub.application.schedule.dto.command.RegisterCrawlSchedulerCommand;
import com.ryuqq.crawlinghub.application.schedule.dto.response.CrawlSchedulerResponse;
import com.ryuqq.crawlinghub.application.schedule.facade.CrawlerSchedulerFacade;
import com.ryuqq.crawlinghub.application.schedule.factory.command.CrawlSchedulerCommandFactory;
import com.ryuqq.crawlinghub.application.schedule.manager.query.CrawlSchedulerReadManager;
import com.ryuqq.crawlinghub.application.schedule.port.in.command.RegisterCrawlSchedulerUseCase;
import com.ryuqq.crawlinghub.application.seller.manager.query.SellerReadManager;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.exception.DuplicateSchedulerNameException;
import com.ryuqq.crawlinghub.domain.seller.exception.SellerNotFoundException;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import org.springframework.stereotype.Service;

/**
 * 크롤 스케줄러 등록 UseCase 구현체.
 *
 * <p><strong>트랜잭션</strong>: CommandService는 @Transactional 금지 (Facade 책임)
 *
 * <p><strong>TimeProvider</strong>: Facade가 관리 (Service는 시간 의존성 없음)
 */
@Service
public class RegisterCrawlSchedulerService implements RegisterCrawlSchedulerUseCase {

    private final CrawlSchedulerCommandFactory commandFactory;
    private final CrawlSchedulerAssembler assembler;
    private final CrawlerSchedulerFacade facade;
    private final CrawlSchedulerReadManager readManager;
    private final SellerReadManager sellerReadManager;

    public RegisterCrawlSchedulerService(
            CrawlSchedulerCommandFactory commandFactory,
            CrawlSchedulerAssembler assembler,
            CrawlerSchedulerFacade facade,
            CrawlSchedulerReadManager readManager,
            SellerReadManager sellerReadManager) {
        this.commandFactory = commandFactory;
        this.assembler = assembler;
        this.facade = facade;
        this.readManager = readManager;
        this.sellerReadManager = sellerReadManager;
    }

    /**
     * 크롤 스케줄러 등록.
     *
     * <p><strong>처리 흐름</strong>:
     *
     * <ol>
     *   <li>셀러 존재 검증
     *   <li>중복 검증 (sellerId + schedulerName)
     *   <li>CommandFactory를 통해 CrawlSchedulerBundle 생성
     *   <li>Facade를 통해 저장 (스케줄러 + 히스토리 + 아웃박스, 단일 트랜잭션)
     *   <li>Assembler로 Response 변환
     * </ol>
     *
     * @param command 등록 명령 (sellerId, schedulerName, cronExpression)
     * @return 등록된 스케줄러 정보
     */
    @Override
    public CrawlSchedulerResponse register(RegisterCrawlSchedulerCommand command) {
        // 1. 셀러 존재 검증
        validateSellerExists(command.sellerId());

        // 2. 중복 검증 (sellerId + schedulerName)
        validateDuplicateScheduler(command);

        // 3. CommandFactory를 통해 CrawlSchedulerBundle 생성
        CrawlSchedulerBundle bundle = commandFactory.createBundle(command);

        // 4. Facade를 통해 저장 (스케줄러 + 히스토리 + 아웃박스, 단일 트랜잭션)
        CrawlScheduler savedScheduler = facade.persist(bundle);

        // 5. Assembler로 Response 변환
        return assembler.toResponse(savedScheduler);
    }

    private void validateSellerExists(Long sellerId) {
        if (!sellerReadManager.existsById(SellerId.of(sellerId))) {
            throw new SellerNotFoundException(sellerId);
        }
    }

    private void validateDuplicateScheduler(RegisterCrawlSchedulerCommand command) {
        boolean exists =
                readManager.existsBySellerIdAndSchedulerName(
                        SellerId.of(command.sellerId()), command.schedulerName());
        if (exists) {
            throw new DuplicateSchedulerNameException(command.sellerId(), command.schedulerName());
        }
    }
}
