package com.ryuqq.crawlinghub.application.task.dto.bundle;

import com.ryuqq.crawlinghub.domain.schedule.identifier.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTaskOutbox;
import com.ryuqq.crawlinghub.domain.task.identifier.CrawlTaskId;
import java.time.Clock;

/**
 * CrawlTask 번들 DTO (Immutable)
 *
 * <p><strong>용도</strong>: CrawlTask와 Outbox를 하나로 묶어 관리
 *
 * <p><strong>불변 설계</strong>: with* 메서드는 새 인스턴스를 반환합니다.
 *
 * <p><strong>저장 흐름</strong>:
 *
 * <ol>
 *   <li>CrawlTask 저장 → ID 반환 → withTaskId() → 새 번들 반환
 *   <li>Outbox 저장 (Task ID 참조)
 * </ol>
 *
 * @param crawlTask CrawlTask Aggregate
 * @param outboxPayload Outbox 페이로드 (JSON)
 * @param savedTaskId 저장된 Task ID (nullable)
 * @author development-team
 * @since 1.0.0
 */
public record CrawlTaskBundle(CrawlTask crawlTask, String outboxPayload, CrawlTaskId savedTaskId) {

    /**
     * 번들 생성 (ID 미할당 상태)
     *
     * @param crawlTask CrawlTask Aggregate
     * @param outboxPayload Outbox 페이로드 (JSON)
     * @return CrawlTaskBundle
     */
    public static CrawlTaskBundle of(CrawlTask crawlTask, String outboxPayload) {
        return new CrawlTaskBundle(crawlTask, outboxPayload, null);
    }

    /**
     * Task ID 설정 (새 인스턴스 반환)
     *
     * @param taskId 저장된 Task ID
     * @return 새 CrawlTaskBundle (ID 할당됨)
     */
    public CrawlTaskBundle withTaskId(CrawlTaskId taskId) {
        return new CrawlTaskBundle(crawlTask, outboxPayload, taskId);
    }

    /**
     * CrawlTask 반환
     *
     * @return CrawlTask
     */
    public CrawlTask getCrawlTask() {
        return crawlTask;
    }

    /**
     * CrawlScheduler ID 반환
     *
     * @return CrawlSchedulerId
     */
    public CrawlSchedulerId getCrawlScheduleId() {
        return crawlTask.getCrawlSchedulerId();
    }

    /**
     * ID가 할당된 CrawlTask 반환 (등록 이벤트 자동 발행)
     *
     * <p><strong>주의</strong>: 이 메서드는 등록 이벤트를 자동 발행합니다.
     *
     * @param clock 시간 제어
     * @return CrawlTask with ID (이벤트 발행됨)
     * @throws IllegalStateException ID가 아직 할당되지 않은 경우
     */
    public CrawlTask getSavedCrawlTask(Clock clock) {
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

        // 이벤트에도 taskId가 포함된 payload 전달 (SQS 발행 시 사용됨)
        String enrichedPayload = enrichPayloadWithTaskId(outboxPayload, savedTaskId.value());
        savedTask.addRegisteredEvent(enrichedPayload, clock);
        return savedTask;
    }

    /**
     * Outbox 생성 (Task ID 할당 후)
     *
     * <p>페이로드에 taskId를 자동으로 추가합니다.
     *
     * @param clock 시간 제어
     * @return CrawlTaskOutbox
     * @throws IllegalStateException Task ID가 아직 할당되지 않은 경우
     */
    public CrawlTaskOutbox createOutbox(Clock clock) {
        if (savedTaskId == null) {
            throw new IllegalStateException("CrawlTask ID가 아직 할당되지 않았습니다.");
        }
        String enrichedPayload = enrichPayloadWithTaskId(outboxPayload, savedTaskId.value());
        return CrawlTaskOutbox.forNew(savedTaskId, enrichedPayload, clock);
    }

    /**
     * 페이로드에 taskId 추가
     *
     * @param payload 원본 페이로드 (JSON)
     * @param taskId Task ID
     * @return taskId가 포함된 페이로드
     */
    private String enrichPayloadWithTaskId(String payload, Long taskId) {
        return payload.replaceFirst("\\{", "{\"taskId\":" + taskId + ",");
    }

    /**
     * 저장된 Task ID 반환
     *
     * @return CrawlTaskId (nullable)
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
