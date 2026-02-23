package com.ryuqq.crawlinghub.application.execution.validator;

import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskReadManager;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.exception.CrawlTaskNotFoundException;
import com.ryuqq.crawlinghub.domain.task.id.CrawlTaskId;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * CrawlTask 실행 검증기
 *
 * <p><strong>책임</strong>: CrawlTask 조회 + 멱등성 체크 + 상태 검증
 *
 * <ul>
 *   <li>Task 조회 (없으면 CrawlTaskNotFoundException)
 *   <li>터미널 상태 → Optional.empty() (멱등성 보장)
 *   <li>PUBLISHED가 아님 → Optional.empty() (비정상 상태 스킵)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlTaskExecutionValidator {

    private static final Logger log = LoggerFactory.getLogger(CrawlTaskExecutionValidator.class);

    private final CrawlTaskReadManager crawlTaskReadManager;

    public CrawlTaskExecutionValidator(CrawlTaskReadManager crawlTaskReadManager) {
        this.crawlTaskReadManager = crawlTaskReadManager;
    }

    /**
     * 실행 가능 여부 검증
     *
     * <p>SQS 중복 메시지로 인해 이미 완료된 Task에 대해 재처리 요청이 올 경우, 예외를 던지지 않고 Optional.empty()를 반환하여 호출자가 안전하게
     * 처리를 스킵할 수 있도록 합니다.
     *
     * @param taskId CrawlTask ID
     * @return 실행 가능한 CrawlTask, 스킵 시 Optional.empty()
     * @throws CrawlTaskNotFoundException CrawlTask가 존재하지 않는 경우
     */
    public Optional<CrawlTask> validateAndGet(Long taskId) {
        CrawlTaskId crawlTaskId = CrawlTaskId.of(taskId);

        CrawlTask crawlTask =
                crawlTaskReadManager
                        .findById(crawlTaskId)
                        .orElseThrow(
                                () -> {
                                    log.error("CrawlTask를 찾을 수 없습니다: taskId={}", taskId);
                                    return new CrawlTaskNotFoundException(taskId);
                                });

        CrawlTaskStatus currentStatus = crawlTask.getStatus();

        if (currentStatus.isTerminal()) {
            log.info(
                    "CrawlTask 이미 처리 완료 (멱등성 스킵): taskId={}, currentStatus={}",
                    taskId,
                    currentStatus);
            return Optional.empty();
        }

        if (currentStatus != CrawlTaskStatus.PUBLISHED) {
            log.warn(
                    "CrawlTask 처리 불가 상태 (스킵): taskId={}, currentStatus={},"
                            + " expectedStatus=PUBLISHED",
                    taskId,
                    currentStatus);
            return Optional.empty();
        }

        return Optional.of(crawlTask);
    }
}
