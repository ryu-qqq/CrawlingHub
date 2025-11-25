package com.ryuqq.crawlinghub.application.schedule.service.command;

import com.ryuqq.crawlinghub.application.schedule.facade.CrawlerSchedulerFacade;
import com.ryuqq.crawlinghub.application.schedule.port.in.command.DeactivateSchedulersBySellerUseCase;
import com.ryuqq.crawlinghub.application.schedule.port.out.query.CrawlScheduleQueryPort;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
 * <p><strong>트랜잭션 관리</strong>:
 *
 * <ul>
 *   <li>EventHandler는 AFTER_COMMIT이므로 별도 트랜잭션에서 실행
 *   <li>각 스케줄러 업데이트는 동일 트랜잭션에서 처리
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class DeactivateSchedulersBySellerService implements DeactivateSchedulersBySellerUseCase {

    private static final Logger log =
            LoggerFactory.getLogger(DeactivateSchedulersBySellerService.class);

    private final CrawlScheduleQueryPort crawlScheduleQueryPort;
    private final CrawlerSchedulerFacade crawlerSchedulerFacade;

    public DeactivateSchedulersBySellerService(
            CrawlScheduleQueryPort crawlScheduleQueryPort,
            CrawlerSchedulerFacade crawlerSchedulerFacade) {
        this.crawlScheduleQueryPort = crawlScheduleQueryPort;
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
     *   <li>Facade를 통해 저장 + History + OutBox 생성
     * </ol>
     *
     * @param sellerId 셀러 ID
     * @return 비활성화된 스케줄러 수
     */
    @Override
    @Transactional
    public int execute(Long sellerId) {
        SellerId sellerIdVo = SellerId.of(sellerId);

        // 1. 활성 스케줄러 조회
        List<CrawlScheduler> activeSchedulers =
                crawlScheduleQueryPort.findActiveSchedulersBySellerId(sellerIdVo);

        if (activeSchedulers.isEmpty()) {
            log.info("No active schedulers found for sellerId={}", sellerId);
            return 0;
        }

        log.info("Deactivating {} schedulers for sellerId={}", activeSchedulers.size(), sellerId);

        // 2. 각 스케줄러 비활성화
        for (CrawlScheduler scheduler : activeSchedulers) {
            // 기존 값 유지, 상태만 INACTIVE로 변경
            scheduler.update(
                    scheduler.getSchedulerName(),
                    scheduler.getCronExpression(),
                    SchedulerStatus.INACTIVE);

            // 3. Facade를 통해 저장 + 이벤트 발행
            crawlerSchedulerFacade.update(scheduler);

            log.debug(
                    "Deactivated scheduler: id={}, name={}",
                    scheduler.getCrawlSchedulerIdValue(),
                    scheduler.getSchedulerNameValue());
        }

        log.info(
                "Successfully deactivated {} schedulers for sellerId={}",
                activeSchedulers.size(),
                sellerId);

        return activeSchedulers.size();
    }
}
