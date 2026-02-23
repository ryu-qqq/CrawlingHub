package com.ryuqq.crawlinghub.adapter.in.sqs.task;

import com.ryuqq.crawlinghub.application.execution.dto.command.ExecuteCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.execution.port.in.command.CrawlTaskExecutionUseCase;
import com.ryuqq.crawlinghub.application.execution.port.in.command.FailCrawlTaskDirectlyUseCase;
import com.ryuqq.crawlinghub.application.task.dto.messaging.CrawlTaskPayload;
import com.ryuqq.crawlinghub.domain.execution.exception.RetryableExecutionException;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * CrawlTask SQS 리스너
 *
 * <p><strong>용도</strong>: CrawlTask SQS 큐에서 메시지를 수신하여 크롤링 작업 실행
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>SQS 큐에서 메시지 수신
 *   <li>Payload → Command 변환 (ListenerMapper)
 *   <li>크롤링 작업 실행 (Application Layer 호출)
 * </ol>
 *
 * <p><strong>에러 분류</strong>:
 *
 * <ul>
 *   <li>일시적 오류 (DB 커넥션, 트랜잭션): 예외 재전파 → SQS NACK → visibility timeout 후 재시도
 *   <li>영구적 오류 (비즈니스 실패, 잘못된 페이로드): 예외 삼킴 → ACK → 재시도 무의미
 * </ul>
 *
 * <p><strong>멱등성</strong>: Application Layer(CrawlTaskExecutionValidator)에서 상태 체크로 보장
 *
 * <p><strong>실패 처리</strong>:
 *
 * <ul>
 *   <li>RUNNING 전 영구 오류: failSafely → 즉시 FAILED 처리 (PUBLISHED 고아 방지)
 *   <li>RUNNING 후 오류: Application Layer의 safeCompleteWithFailure 처리
 *   <li>RUNNING 고아: RecoverStuckCrawlTask 스케줄러에서 복구
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
@ConditionalOnProperty(
        name = "aws.sqs.listener.crawl-task-listener-enabled",
        havingValue = "true",
        matchIfMissing = true)
public class CrawlTaskSqsListener {

    private static final Logger log = LoggerFactory.getLogger(CrawlTaskSqsListener.class);

    private final CrawlTaskListenerMapper mapper;
    private final CrawlTaskExecutionUseCase crawlTaskExecutionUseCase;
    private final FailCrawlTaskDirectlyUseCase failCrawlTaskDirectlyUseCase;

    public CrawlTaskSqsListener(
            CrawlTaskListenerMapper mapper,
            CrawlTaskExecutionUseCase crawlTaskExecutionUseCase,
            FailCrawlTaskDirectlyUseCase failCrawlTaskDirectlyUseCase) {
        this.mapper = mapper;
        this.crawlTaskExecutionUseCase = crawlTaskExecutionUseCase;
        this.failCrawlTaskDirectlyUseCase = failCrawlTaskDirectlyUseCase;
    }

    /**
     * CrawlTask 메시지 수신 및 처리
     *
     * <p>에러 유형에 따라 선택적으로 예외를 재전파합니다.
     *
     * <ul>
     *   <li>일시적 오류 (RetryableExecutionException): throw → SQS 재시도
     *   <li>영구적 오류 (페이로드 변환 실패, 비즈니스 실패): failDirectly로 즉시 FAILED 처리 → ACK
     * </ul>
     *
     * @param payload CrawlTask 페이로드
     */
    @SqsListener("${aws.sqs.listener.crawl-task-queue-url}")
    public void handleMessage(@Payload CrawlTaskPayload payload) {
        Long taskId = payload.taskId();

        log.debug(
                "CrawlTask 메시지 수신: taskId={}, schedulerId={}, sellerId={}, taskType={}",
                taskId,
                payload.schedulerId(),
                payload.sellerId(),
                payload.taskType());

        try {
            ExecuteCrawlTaskCommand command = mapper.toCommand(payload);
            crawlTaskExecutionUseCase.execute(command);
            log.info("CrawlTask 처리 완료: taskId={}, taskType={}", taskId, payload.taskType());
        } catch (Exception e) {
            if (isRetryable(e)) {
                log.warn(
                        "CrawlTask 일시적 오류, SQS 재시도 위임: taskId={}, error={}",
                        taskId,
                        e.getMessage());
                throw e;
            }
            log.error("CrawlTask 영구적 오류, 즉시 실패 처리: taskId={}, error={}", taskId, e.getMessage(), e);
            failSafely(taskId, e.getMessage());
        }
    }

    /**
     * CrawlTask 안전한 실패 처리
     *
     * <p>PUBLISHED 상태의 고아 Task를 방지하기 위해 즉시 FAILED로 전환합니다. failDirectly 호출 자체가 실패해도 예외를 전파하지 않습니다
     * (로그만 기록).
     *
     * @param taskId CrawlTask ID
     * @param reason 실패 사유
     */
    private void failSafely(Long taskId, String reason) {
        try {
            failCrawlTaskDirectlyUseCase.execute(taskId, reason);
        } catch (Exception e) {
            log.error(
                    "CrawlTask 실패 처리 중 오류 - PUBLISHED 고아 가능: taskId={}, reason={}, error={}",
                    taskId,
                    reason,
                    e.getMessage(),
                    e);
        }
    }

    /**
     * 일시적 오류 여부 판별
     *
     * @param e 발생한 예외
     * @return 일시적 오류이면 true
     */
    private boolean isRetryable(Exception e) {
        return e instanceof RetryableExecutionException;
    }
}
