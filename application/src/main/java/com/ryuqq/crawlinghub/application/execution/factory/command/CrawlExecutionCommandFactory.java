package com.ryuqq.crawlinghub.application.execution.factory.command;

import com.ryuqq.crawlinghub.application.common.time.TimeProvider;
import com.ryuqq.crawlinghub.application.execution.dto.bundle.CrawlTaskExecutionBundle;
import com.ryuqq.crawlinghub.application.execution.dto.command.ExecuteCrawlTaskCommand;
import com.ryuqq.crawlinghub.domain.execution.aggregate.CrawlExecution;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import java.time.Instant;
import org.springframework.stereotype.Component;

/**
 * CrawlExecution CommandFactory
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>Command + CrawlTask → CrawlTaskExecutionBundle 변환
 *   <li>CrawlExecution 생성 + 시간 주입
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
public class CrawlExecutionCommandFactory {

    private final TimeProvider timeProvider;

    public CrawlExecutionCommandFactory(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    /**
     * CrawlTask + Command → CrawlTaskExecutionBundle 생성
     *
     * <p>CrawlExecution과 changedAt을 포함한 완전한 Bundle을 생성합니다.
     *
     * @param crawlTask 검증 완료된 CrawlTask
     * @param command 실행 커맨드
     * @return CrawlTaskExecutionBundle (execution + changedAt 포함)
     */
    public CrawlTaskExecutionBundle createExecutionBundle(
            CrawlTask crawlTask, ExecuteCrawlTaskCommand command) {
        Instant now = timeProvider.now();

        CrawlExecution execution =
                CrawlExecution.forNew(
                        crawlTask.getId(),
                        CrawlSchedulerId.of(command.schedulerId()),
                        SellerId.of(command.sellerId()),
                        now);

        return CrawlTaskExecutionBundle.of(crawlTask, execution, command, now);
    }
}
