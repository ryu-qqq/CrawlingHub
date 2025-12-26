package com.ryuqq.crawlinghub.application.task.manager;

import com.ryuqq.crawlinghub.application.task.component.CrawlTaskPersistenceValidator;
import com.ryuqq.crawlinghub.application.task.port.out.command.CrawlTaskPersistencePort;
import com.ryuqq.crawlinghub.application.task.port.out.query.CrawlTaskQueryPort;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.identifier.CrawlTaskId;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import java.time.Clock;
import org.springframework.stereotype.Component;

/**
 * CrawlTask Transaction Manager
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>CrawlTask 저장
 *   <li>CrawlTask 상태 변경
 * </ul>
 *
 * <p><strong>주의</strong>: 검증 로직은 {@link CrawlTaskPersistenceValidator}에서 수행
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlTaskTransactionManager {

    private final CrawlTaskPersistencePort crawlTaskPersistencePort;
    private final CrawlTaskQueryPort crawlTaskQueryPort;

    public CrawlTaskTransactionManager(
            CrawlTaskPersistencePort crawlTaskPersistencePort,
            CrawlTaskQueryPort crawlTaskQueryPort) {
        this.crawlTaskPersistencePort = crawlTaskPersistencePort;
        this.crawlTaskQueryPort = crawlTaskQueryPort;
    }

    /**
     * CrawlTask 저장
     *
     * @param crawlTask 저장할 CrawlTask
     * @return 저장된 CrawlTask ID
     */
    public CrawlTaskId persist(CrawlTask crawlTask) {
        return crawlTaskPersistencePort.persist(crawlTask);
    }

    /**
     * CrawlTask 상태를 PUBLISHED로 변경
     *
     * <p>SQS 메시지 발행 성공 후 호출
     *
     * <p><strong>상태 전환 규칙</strong>:
     *
     * <ul>
     *   <li>WAITING → PUBLISHED: 최초 발행
     *   <li>RETRY → PUBLISHED: 재시도 발행
     * </ul>
     *
     * @param crawlTaskId Task ID
     * @param clock 시간 제어
     */
    public void markAsPublished(CrawlTaskId crawlTaskId, Clock clock) {
        CrawlTask crawlTask =
                crawlTaskQueryPort
                        .findById(crawlTaskId)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "CrawlTask를 찾을 수 없습니다: " + crawlTaskId.value()));

        // RETRY 상태면 markAsPublishedAfterRetry() 호출
        if (crawlTask.getStatus() == CrawlTaskStatus.RETRY) {
            crawlTask.markAsPublishedAfterRetry(clock);
        } else {
            crawlTask.markAsPublished(clock);
        }

        crawlTaskPersistencePort.persist(crawlTask);
    }
}
