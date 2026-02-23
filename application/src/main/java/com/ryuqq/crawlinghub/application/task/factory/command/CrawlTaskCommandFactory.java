package com.ryuqq.crawlinghub.application.task.factory.command;

import com.ryuqq.crawlinghub.application.common.dto.command.StatusChangeContext;
import com.ryuqq.crawlinghub.application.common.time.TimeProvider;
import com.ryuqq.crawlinghub.application.task.dto.bundle.CrawlTaskBundle;
import com.ryuqq.crawlinghub.application.task.dto.command.CreateCrawlTaskCommand;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.id.CrawlTaskId;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlEndpoint;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import org.springframework.stereotype.Component;

/**
 * CrawlTask CommandFactory
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>Command → Domain 변환
 *   <li>Bundle 생성
 * </ul>
 *
 * <p><strong>금지</strong>:
 *
 * <ul>
 *   <li>@Transactional 금지 (변환만, 트랜잭션 불필요)
 *   <li>Port 의존 금지
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlTaskCommandFactory {

    private final TimeProvider timeProvider;

    public CrawlTaskCommandFactory(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    /**
     * CrawlScheduler + Seller → CrawlTaskBundle 변환
     *
     * @param scheduler 검증된 스케줄러
     * @param seller 셀러 (mustItSellerName 조회용)
     * @return CrawlTask 번들
     */
    public CrawlTaskBundle createBundle(CrawlScheduler scheduler, Seller seller) {
        CrawlEndpoint endpoint = CrawlEndpoint.forSearchItems(seller.getMustItSellerNameValue(), 1);
        CrawlTask crawlTask =
                CrawlTask.forNew(
                        scheduler.getCrawlSchedulerId(),
                        scheduler.getSellerId(),
                        CrawlTaskType.SEARCH,
                        endpoint,
                        timeProvider.now());

        return CrawlTaskBundle.of(crawlTask, timeProvider.now());
    }

    /**
     * CreateCrawlTaskCommand → CrawlTaskBundle 변환
     *
     * @param command 동적 생성 명령
     * @return CrawlTask 번들
     */
    public CrawlTaskBundle createBundle(CreateCrawlTaskCommand command) {
        CrawlEndpoint endpoint = createEndpoint(command);
        CrawlTask crawlTask =
                CrawlTask.forNew(
                        CrawlSchedulerId.of(command.crawlSchedulerId()),
                        SellerId.of(command.sellerId()),
                        command.taskType(),
                        endpoint,
                        timeProvider.now());

        return CrawlTaskBundle.of(crawlTask, timeProvider.now());
    }

    /**
     * CrawlTask → 재시도용 CrawlTaskBundle 생성
     *
     * @param crawlTask 재시도할 CrawlTask
     * @return CrawlTask 번들
     */
    public CrawlTaskBundle createRetryBundle(CrawlTask crawlTask) {
        return CrawlTaskBundle.of(crawlTask, timeProvider.now());
    }

    /**
     * CrawlTask 상태 변경 컨텍스트 생성
     *
     * @param taskId CrawlTask ID
     * @return StatusChangeContext (ID + changedAt)
     */
    public StatusChangeContext<CrawlTaskId> createStatusChangeContext(Long taskId) {
        return new StatusChangeContext<>(CrawlTaskId.of(taskId), timeProvider.now());
    }

    private CrawlEndpoint createEndpoint(CreateCrawlTaskCommand command) {
        return switch (command.taskType()) {
            case MINI_SHOP -> CrawlEndpoint.forMiniShopList(command.mustItSellerName(), 1, 100);
            case DETAIL -> CrawlEndpoint.forProductDetail(command.targetId());
            case OPTION -> CrawlEndpoint.forProductOption(command.targetId());
            case SEARCH -> CrawlEndpoint.forSearchApi(command.endpoint());
        };
    }
}
