package com.ryuqq.crawlinghub.adapter.in.rest.task.mapper;

import com.ryuqq.crawlinghub.adapter.in.rest.task.dto.response.CrawlTaskApiResponse;
import com.ryuqq.crawlinghub.application.task.dto.command.RetryCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskResponse;
import java.time.Instant;
import org.springframework.stereotype.Component;

/**
 * CrawlTaskCommandApiMapper - CrawlTask Command REST API ↔ Application Layer 변환
 *
 * <p>CrawlTask Command 요청/응답에 대한 DTO 변환을 담당합니다.
 *
 * <p><strong>변환 방향:</strong>
 *
 * <ul>
 *   <li>Path Variable → Application Command (Controller → Application)
 *   <li>Application Response → API Response (Application → Controller)
 * </ul>
 *
 * <p><strong>CQRS 패턴 적용:</strong>
 *
 * <ul>
 *   <li>Command: RetryCrawlTask 요청 변환
 * </ul>
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>필드 매핑만 수행 (비즈니스 로직 포함 금지)
 *   <li>API DTO ↔ Application DTO 단순 변환
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlTaskCommandApiMapper {

    /**
     * Path Variable → RetryCrawlTaskCommand 변환
     *
     * @param crawlTaskId 크롤 태스크 ID (PathVariable)
     * @return Application Layer 재시도 명령
     */
    public RetryCrawlTaskCommand toRetryCommand(Long crawlTaskId) {
        return new RetryCrawlTaskCommand(crawlTaskId);
    }

    /**
     * CrawlTaskResponse → CrawlTaskApiResponse 변환
     *
     * @param appResponse Application Layer 크롤 태스크 응답
     * @return REST API 크롤 태스크 응답
     */
    public CrawlTaskApiResponse toApiResponse(CrawlTaskResponse appResponse) {
        return new CrawlTaskApiResponse(
                appResponse.crawlTaskId(),
                appResponse.crawlSchedulerId(),
                appResponse.sellerId(),
                appResponse.requestUrl(),
                appResponse.status().name(),
                appResponse.taskType().name(),
                appResponse.retryCount(),
                toIsoString(appResponse.createdAt()));
    }

    private String toIsoString(Instant instant) {
        return instant != null ? instant.toString() : null;
    }
}
