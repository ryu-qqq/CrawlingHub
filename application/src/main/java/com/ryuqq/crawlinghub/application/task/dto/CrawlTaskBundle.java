package com.ryuqq.crawlinghub.application.task.dto;

import com.ryuqq.crawlinghub.domain.schedule.identifier.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTaskOutbox;
import com.ryuqq.crawlinghub.domain.task.identifier.CrawlTaskId;

/**
 * CrawlTask 번들 DTO
 *
 * <p><strong>용도</strong>: CrawlTask와 Outbox를 하나로 묶어 관리
 *
 * <p><strong>저장 흐름</strong>:
 *
 * <ol>
 *   <li>CrawlTask 저장 → ID 반환 → withTaskId()
 *   <li>Outbox 저장 (Task ID 참조)
 * </ol>
 *
 * @author development-team
 * @since 1.0.0
 */
public class CrawlTaskBundle {

    private final CrawlTask crawlTask;
    private final String outboxPayload;

    private CrawlTaskId savedTaskId;

    private CrawlTaskBundle(CrawlTask crawlTask, String outboxPayload) {
        this.crawlTask = crawlTask;
        this.outboxPayload = outboxPayload;
    }

    /**
     * 번들 생성
     *
     * @param crawlTask CrawlTask Aggregate
     * @param outboxPayload Outbox 페이로드 (JSON)
     * @return CrawlTaskBundle
     */
    public static CrawlTaskBundle of(CrawlTask crawlTask, String outboxPayload) {
        return new CrawlTaskBundle(crawlTask, outboxPayload);
    }

    /**
     * Task ID 설정 (저장 후 호출)
     *
     * @param taskId 저장된 Task ID
     */
    public void withTaskId(CrawlTaskId taskId) {
        this.savedTaskId = taskId;
    }

    /**
     * CrawlTask 반환
     *
     * @return CrawlTask
     */
    public CrawlTask getCrawlTask() {
        return crawlTask;
    }

    public CrawlSchedulerId getCrawlScheduleId() {
        return crawlTask.getCrawlSchedulerId();
    }

    /**
     * ID가 할당된 CrawlTask 반환 (등록 이벤트 자동 발행)
     *
     * <p><strong>주의</strong>: 이 메서드는 등록 이벤트를 자동 발행합니다.
     *
     * @return CrawlTask with ID (이벤트 발행됨)
     * @throws IllegalStateException ID가 아직 할당되지 않은 경우
     */
    public CrawlTask getSavedCrawlTask() {
        if (savedTaskId == null) {
            throw new IllegalStateException("CrawlTask ID가 아직 할당되지 않았습니다.");
        }
        CrawlTask savedTask =
                CrawlTask.reconstitute(
                        savedTaskId,
                        crawlTask.getCrawlSchedulerId(),
                        crawlTask.getSellerId(),
                        crawlTask.getTaskType(),
                        crawlTask.getEndpoint(),
                        crawlTask.getStatus(),
                        crawlTask.getRetryCount(),
                        crawlTask.getOutbox(),
                        crawlTask.getCreatedAt(),
                        crawlTask.getUpdatedAt());

        // ID 할당 후 자동으로 등록 이벤트 발행
        savedTask.addRegisteredEvent(outboxPayload);
        return savedTask;
    }

    /**
     * Outbox 생성 (Task ID 할당 후)
     *
     * @return CrawlTaskOutbox
     * @throws IllegalStateException Task ID가 아직 할당되지 않은 경우
     */
    public CrawlTaskOutbox createOutbox() {
        if (savedTaskId == null) {
            throw new IllegalStateException("CrawlTask ID가 아직 할당되지 않았습니다.");
        }
        return CrawlTaskOutbox.forNew(savedTaskId, outboxPayload);
    }

    /**
     * 저장된 Task ID 반환
     *
     * @return CrawlTaskId
     */
    public CrawlTaskId getSavedTaskId() {
        return savedTaskId;
    }

    /**
     * Outbox 페이로드 반환
     *
     * @return Outbox 페이로드 (JSON)
     */
    public String getOutboxPayload() {
        return outboxPayload;
    }
}
