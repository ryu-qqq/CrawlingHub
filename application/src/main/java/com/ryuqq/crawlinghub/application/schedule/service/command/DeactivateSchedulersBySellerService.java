package com.ryuqq.crawlinghub.application.schedule.service.command;

import com.ryuqq.crawlinghub.application.schedule.facade.CrawlerSchedulerFacade;
import com.ryuqq.crawlinghub.application.schedule.manager.query.CrawlSchedulerReadManager;
import com.ryuqq.crawlinghub.application.schedule.port.in.command.DeactivateSchedulersBySellerUseCase;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 셀러별 스케줄러 비활성화 Service
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>셀러의 활성 스케줄러 조회
 *   <li>각 스케줄러 비활성화 (기존 값 유지, 상태만 INACTIVE)
 *   <li>Facade를 통해 저장 + 이벤트 발행
 * </ul>
 *
 * <p><strong>트랜잭션</strong>: CommandService는 @Transactional 금지 (Facade 책임)
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class DeactivateSchedulersBySellerService implements DeactivateSchedulersBySellerUseCase {

    private static final Logger log =
            LoggerFactory.getLogger(DeactivateSchedulersBySellerService.class);

    private final CrawlSchedulerReadManager readManager;
    private final CrawlerSchedulerFacade crawlerSchedulerFacade;

    public DeactivateSchedulersBySellerService(
            CrawlSchedulerReadManager readManager, CrawlerSchedulerFacade crawlerSchedulerFacade) {
        this.readManager = readManager;
        this.crawlerSchedulerFacade = crawlerSchedulerFacade;
    }

    /**
     * 셀러의 모든 활성 스케줄러 비활성화
     *
     * <p><strong>실행 흐름</strong>:
     *
     * <ol>
     *   <li>셀러의 활성 스케줄러 조회
     *   <li>각 스케줄러 비활성화 (update 메서드 호출)
     *   <li>Facade를 통해 일괄 저장 + History + OutBox 생성
     * </ol>
     *
     * @param sellerId 셀러 ID
     * @return 비활성화된 스케줄러 수
     */
    @Override
    public int execute(Long sellerId) {
        SellerId sellerIdVo = SellerId.of(sellerId);

        // 1. 활성 스케줄러 조회
        List<CrawlScheduler> activeSchedulers =
                readManager.findActiveSchedulersBySellerId(sellerIdVo);

        if (activeSchedulers.isEmpty()) {
            log.info("No active schedulers found for sellerId={}", sellerId);
            return 0;
        }

        log.info("Deactivating {} schedulers for sellerId={}", activeSchedulers.size(), sellerId);

        // 2. Facade에서 비활성화 + 저장 + 이벤트 발행 (TimeProvider 캡슐화)
        crawlerSchedulerFacade.deactivateSchedulers(activeSchedulers);

        log.info(
                "Successfully deactivated {} schedulers for sellerId={}",
                activeSchedulers.size(),
                sellerId);

        return activeSchedulers.size();
    }
}
