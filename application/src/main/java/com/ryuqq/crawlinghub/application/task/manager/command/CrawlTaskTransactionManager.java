package com.ryuqq.crawlinghub.application.task.manager.command;

import com.ryuqq.crawlinghub.application.task.component.CrawlTaskPersistenceValidator;
import com.ryuqq.crawlinghub.application.task.port.out.command.CrawlTaskPersistencePort;
import com.ryuqq.crawlinghub.application.task.port.out.query.CrawlTaskQueryPort;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.identifier.CrawlTaskId;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import java.time.Clock;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
     *   <li>FAILED/TIMEOUT → RETRY → PUBLISHED: 재시도 처리
     *   <li>RETRY → PUBLISHED: 재시도 발행
     * </ul>
     *
     * @param crawlTaskId Task ID
     * @param clock 시간 제어
     * @throws IllegalStateException 상태 전환이 불가능한 경우
     */
    @Transactional
    public void markAsPublished(CrawlTaskId crawlTaskId, Clock clock) {
        CrawlTask crawlTask =
                crawlTaskQueryPort
                        .findById(crawlTaskId)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "CrawlTask를 찾을 수 없습니다: " + crawlTaskId.value()));

        CrawlTaskStatus currentStatus = crawlTask.getStatus();

        // 이미 PUBLISHED 상태면 early return (멱등성 보장)
        // SQS 발행 실패 후 재시도 시, 상태 변경 없이 SQS 재발행만 수행
        if (currentStatus == CrawlTaskStatus.PUBLISHED) {
            return;
        }

        if (currentStatus == CrawlTaskStatus.FAILED || currentStatus == CrawlTaskStatus.TIMEOUT) {
            // FAILED/TIMEOUT → RETRY → PUBLISHED 플로우
            boolean retrySuccessful = crawlTask.attemptRetry(clock);
            if (!retrySuccessful) {
                throw new IllegalStateException("재시도 횟수를 초과했습니다: taskId=" + crawlTaskId.value());
            }
            crawlTask.markAsPublishedAfterRetry(clock);
        } else if (currentStatus == CrawlTaskStatus.RETRY) {
            // RETRY → PUBLISHED
            crawlTask.markAsPublishedAfterRetry(clock);
        } else {
            // WAITING → PUBLISHED
            crawlTask.markAsPublished(clock);
        }

        crawlTaskPersistencePort.persist(crawlTask);
    }
}
