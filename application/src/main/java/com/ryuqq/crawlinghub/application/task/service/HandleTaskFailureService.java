package com.ryuqq.crawlinghub.application.task.service;


import com.ryuqq.crawlinghub.application.task.port.out.LoadCrawlTaskPort;
import com.ryuqq.crawlinghub.application.task.port.out.OutboxPort;
import com.ryuqq.crawlinghub.application.task.port.out.SaveCrawlTaskPort;
import com.ryuqq.crawlinghub.application.task.command.TaskFailureCommand;
import com.ryuqq.crawlinghub.application.task.port.in.HandleTaskFailureUseCase;
import com.ryuqq.crawlinghub.application.task.port.out.NotificationPort;
import com.ryuqq.crawlinghub.domain.task.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.CrawlTaskId;
import com.ryuqq.crawlinghub.domain.task.TaskStatus;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 태스크 실패 처리 UseCase 구현체
 *
 * <p>재시도 가능 여부에 따라:
 * <ul>
 *   <li>재시도 가능: RETRY 상태로 변경 → Outbox 재발행</li>
 *   <li>재시도 불가: FAILED 상태로 변경 → DLQ 이동 → 알림 발송</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
@Service
public class HandleTaskFailureService implements HandleTaskFailureUseCase {

    private final LoadCrawlTaskPort loadCrawlTaskPort;
    private final SaveCrawlTaskPort saveCrawlTaskPort;
    private final OutboxPort outboxPort;
    private final NotificationPort notificationPort;

    public HandleTaskFailureService(
        LoadCrawlTaskPort loadCrawlTaskPort,
        SaveCrawlTaskPort saveCrawlTaskPort,
        OutboxPort outboxPort,
        NotificationPort notificationPort
    ) {
        this.loadCrawlTaskPort = loadCrawlTaskPort;
        this.saveCrawlTaskPort = saveCrawlTaskPort;
        this.outboxPort = outboxPort;
        this.notificationPort = notificationPort;
    }

    /**
     * 태스크 실패 처리
     *
     * <p>실행 순서:
     * 1. 태스크 조회
     * 2. 재시도 가능 여부 확인
     * 3-A. 재시도 가능: RETRY 상태 → Outbox 재발행
     * 3-B. 재시도 불가: FAILED 상태 → 알림 발송
     * 4. 에러 로깅
     *
     * @param command 실패 정보
     * @throws IllegalArgumentException 태스크를 찾을 수 없는 경우
     */
    @Override
    @Transactional
    public void execute(TaskFailureCommand command) {
        CrawlTaskId taskId = CrawlTaskId.of(command.taskId());

        // 1. 태스크 조회
        CrawlTask task = loadCrawlTaskPort.findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException(
                "태스크를 찾을 수 없습니다: " + command.taskId()
            ));

        // 2. 실패 처리 (도메인 메서드)
        task.failWithError(command.error());

        // 3. 상태에 따라 분기 처리
        if (task.hasStatus(TaskStatus.RETRY)) {
            // 3-A. 재시도 가능: Outbox 재발행
            handleRetryableFailure(task);
        } else {
            // 3-B. 재시도 불가: 최종 실패 처리
            handlePermanentFailure(task, command.error());
        }

        // 4. 저장
        saveCrawlTaskPort.save(task);
    }

    /**
     * 재시도 가능한 실패 처리
     */
    private void handleRetryableFailure(CrawlTask task) {
        // Outbox 재발행 (재시도)
        String payload = String.format(
            "{\"taskId\":%d,\"sellerId\":%d,\"taskType\":\"%s\",\"url\":\"%s\",\"retryCount\":%d}",
            task.getIdValue(),
            task.getSellerIdValue(),
            task.getTaskType().name(),
            task.getRequestUrlValue(),
            task.getRetryCount()
        );

        outboxPort.saveOutboxMessage(
            "CrawlTask",
            task.getIdValue(),
            "CrawlTaskRetry",
            payload
        );

        // 알림 발송 (재시도 안내)
        notificationPort.notifyTaskFailure(
            task.getIdValue(),
            task.getSellerIdValue(),
            "재시도 예정 (" + task.getRetryCount() + "/3)",
            task.getRetryCount()
        );
    }

    /**
     * 최종 실패 처리 (DLQ 이동)
     */
    private void handlePermanentFailure(CrawlTask task, String error) {
        // DLQ 이동을 위한 Outbox 저장
        String payload = String.format(
            "{\"taskId\":%d,\"sellerId\":%d,\"taskType\":\"%s\",\"error\":\"%s\"}",
            task.getIdValue(),
            task.getSellerIdValue(),
            task.getTaskType().name(),
            error.replace("\"", "\\\"")
        );

        outboxPort.saveOutboxMessage(
            "CrawlTask",
            task.getIdValue(),
            "CrawlTaskDead",
            payload
        );

        // 최종 실패 알림 발송
        notificationPort.notifyTaskDead(
            task.getIdValue(),
            task.getSellerIdValue(),
            error
        );
    }
}
