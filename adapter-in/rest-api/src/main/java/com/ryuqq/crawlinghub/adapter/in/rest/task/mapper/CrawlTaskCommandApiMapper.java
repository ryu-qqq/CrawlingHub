package com.ryuqq.crawlinghub.adapter.in.rest.task.mapper;

import static com.ryuqq.crawlinghub.adapter.in.rest.common.util.DateTimeFormatUtils.format;

import com.ryuqq.crawlinghub.adapter.in.rest.task.dto.response.CrawlTaskApiResponse;
import com.ryuqq.crawlinghub.application.task.dto.command.RetryCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskResult;
import org.springframework.stereotype.Component;

/**
 * CrawlTaskCommandApiMapper - CrawlTask Command REST API ↔ Application Layer 변환
 *
 * <p>CrawlTask Command 요청/응답에 대한 DTO 변환을 담당합니다.
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
     * CrawlTaskResult → CrawlTaskApiResponse 변환
     *
     * @param result Application Layer 크롤 태스크 결과
     * @return REST API 크롤 태스크 응답
     */
    public CrawlTaskApiResponse toApiResponse(CrawlTaskResult result) {
        return new CrawlTaskApiResponse(
                result.crawlTaskId(),
                result.crawlSchedulerId(),
                result.sellerId(),
                result.requestUrl(),
                result.baseUrl(),
                result.path(),
                result.queryParams(),
                result.status(),
                result.taskType(),
                result.retryCount(),
                format(result.createdAt()),
                format(result.updatedAt()));
    }
}
