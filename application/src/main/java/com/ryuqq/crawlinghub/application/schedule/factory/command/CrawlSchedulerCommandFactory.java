package com.ryuqq.crawlinghub.application.schedule.factory.command;

import com.ryuqq.crawlinghub.application.common.dto.command.UpdateContext;
import com.ryuqq.crawlinghub.application.common.time.TimeProvider;
import com.ryuqq.crawlinghub.application.schedule.dto.bundle.CrawlSchedulerBundle;
import com.ryuqq.crawlinghub.application.schedule.dto.command.RegisterCrawlSchedulerCommand;
import com.ryuqq.crawlinghub.application.schedule.dto.command.UpdateCrawlSchedulerCommand;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerUpdateData;
import com.ryuqq.crawlinghub.domain.schedule.vo.CronExpression;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerName;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import org.springframework.stereotype.Component;

/**
 * CrawlScheduler CommandFactory
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
public class CrawlSchedulerCommandFactory {

    private final TimeProvider timeProvider;

    public CrawlSchedulerCommandFactory(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    /**
     * RegisterCrawlSchedulerCommand → CrawlSchedulerBundle 변환
     *
     * @param command 등록 명령
     * @return CrawlSchedulerBundle
     */
    public CrawlSchedulerBundle createBundle(RegisterCrawlSchedulerCommand command) {
        java.time.Instant now = timeProvider.now();
        CrawlScheduler scheduler = createScheduler(command, now);
        return CrawlSchedulerBundle.of(scheduler, now);
    }

    /**
     * RegisterCrawlSchedulerCommand → CrawlScheduler Aggregate 변환
     *
     * @param command 등록 명령
     * @return 신규 CrawlScheduler Aggregate
     */
    public CrawlScheduler createScheduler(RegisterCrawlSchedulerCommand command) {
        return createScheduler(command, timeProvider.now());
    }

    private CrawlScheduler createScheduler(
            RegisterCrawlSchedulerCommand command, java.time.Instant now) {
        return CrawlScheduler.forNew(
                SellerId.of(command.sellerId()),
                SchedulerName.of(command.schedulerName()),
                CronExpression.of(command.cronExpression()),
                now);
    }

    /**
     * UpdateCrawlSchedulerCommand → UpdateContext 변환
     *
     * @param command 수정 명령
     * @return UpdateContext (ID, UpdateData, changedAt)
     */
    public UpdateContext<CrawlSchedulerId, CrawlSchedulerUpdateData> createUpdateContext(
            UpdateCrawlSchedulerCommand command) {
        CrawlSchedulerId id = CrawlSchedulerId.of(command.crawlSchedulerId());
        CrawlSchedulerUpdateData updateData =
                CrawlSchedulerUpdateData.of(
                        SchedulerName.of(command.schedulerName()),
                        CronExpression.of(command.cronExpression()),
                        command.active() ? SchedulerStatus.ACTIVE : SchedulerStatus.INACTIVE);
        return new UpdateContext<>(id, updateData, timeProvider.now());
    }
}
