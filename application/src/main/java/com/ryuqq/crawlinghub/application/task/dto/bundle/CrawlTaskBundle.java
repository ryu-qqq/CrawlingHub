package com.ryuqq.crawlinghub.application.task.dto.bundle;

import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTaskOutbox;
import com.ryuqq.crawlinghub.domain.task.id.CrawlTaskId;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlEndpoint;
import java.time.Instant;

/**
 * CrawlTask 번들 DTO (Immutable)
 *
 * <p><strong>용도</strong>: CrawlTask와 Outbox 생성 정보를 하나로 묶어 관리
 *
 * <p><strong>불변 설계</strong>: with* 메서드는 새 인스턴스를 반환합니다.
 *
 * @param crawlTask CrawlTask Aggregate
 * @param createdAt 생성 시각
 * @param savedTaskId 저장된 Task ID (nullable)
 * @author development-team
 * @since 1.0.0
 */
public record CrawlTaskBundle(CrawlTask crawlTask, Instant createdAt, CrawlTaskId savedTaskId) {

    /**
     * 번들 생성 (ID 미할당 상태)
     *
     * @param crawlTask CrawlTask Aggregate
     * @param createdAt 생성 시각
     * @return CrawlTaskBundle
     */
    public static CrawlTaskBundle of(CrawlTask crawlTask, Instant createdAt) {
        return new CrawlTaskBundle(crawlTask, createdAt, null);
    }

    /**
     * Task ID 설정 (새 인스턴스 반환)
     *
     * @param taskId 저장된 Task ID
     * @return 새 CrawlTaskBundle (ID 할당됨)
     */
    public CrawlTaskBundle withTaskId(CrawlTaskId taskId) {
        return new CrawlTaskBundle(crawlTask, createdAt, taskId);
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
     * Outbox 생성 (Task ID 할당 후)
     *
     * <p>페이로드를 내부적으로 빌드합니다.
     *
     * @return CrawlTaskOutbox
     * @throws IllegalStateException Task ID가 아직 할당되지 않은 경우
     */
    public CrawlTaskOutbox createOutbox() {
        if (savedTaskId == null) {
            throw new IllegalStateException("CrawlTask ID가 아직 할당되지 않았습니다.");
        }
        String payload = buildPayload();
        return CrawlTaskOutbox.forNew(savedTaskId, payload, createdAt);
    }

    /**
     * Outbox 페이로드 빌드 (Jackson 미사용)
     *
     * @return JSON 페이로드 문자열
     */
    private String buildPayload() {
        CrawlEndpoint endpoint = crawlTask.getEndpoint();
        String queryParamsJson = endpoint.toQueryParamsJson();

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"taskId\":").append(savedTaskId.value());
        sb.append(",\"schedulerId\":").append(crawlTask.getCrawlSchedulerIdValue());
        sb.append(",\"sellerId\":").append(crawlTask.getSellerIdValue());
        sb.append(",\"taskType\":\"").append(crawlTask.getTaskType().name()).append("\"");
        sb.append(",\"endpoint\":\"").append(endpoint.toFullUrl()).append("\"");
        if (queryParamsJson != null) {
            sb.append(",\"queryParams\":").append(queryParamsJson);
        }
        sb.append("}");
        return sb.toString();
    }
}
