package com.ryuqq.crawlinghub.adapter.in.rest.task.mapper;

import static com.ryuqq.crawlinghub.adapter.in.rest.common.util.DateTimeFormatUtils.format;

import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.PageApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.task.dto.query.SearchCrawlTasksOutboxApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.task.dto.response.CrawlTaskOutboxApiResponse;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.task.dto.query.GetOutboxListQuery;
import com.ryuqq.crawlinghub.application.task.dto.response.OutboxResponse;
import com.ryuqq.crawlinghub.domain.task.vo.OutboxStatus;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * CrawlTaskOutboxApiMapper - Outbox REST API ↔ Application Layer 변환
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>SearchCrawlTasksOutboxApiRequest → GetOutboxListQuery 변환
 *   <li>OutboxResponse → CrawlTaskOutboxApiResponse 변환
 *   <li>PageResponse → PageApiResponse 변환
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlTaskOutboxApiMapper {

    public GetOutboxListQuery toQuery(SearchCrawlTasksOutboxApiRequest request) {
        List<OutboxStatus> parsedStatuses = parseStatuses(request.statuses());
        return GetOutboxListQuery.of(
                parsedStatuses,
                request.createdFrom(),
                request.createdTo(),
                request.page(),
                request.size());
    }

    private List<OutboxStatus> parseStatuses(List<String> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return null;
        }
        return statuses.stream().map(this::parseStatus).toList();
    }

    private OutboxStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status cannot be null or blank");
        }
        try {
            return OutboxStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            String validValues =
                    Arrays.stream(OutboxStatus.values())
                            .map(Enum::name)
                            .collect(Collectors.joining(", "));
            throw new IllegalArgumentException(
                    "Invalid outbox status: '" + status + "'. Valid values: " + validValues);
        }
    }

    public CrawlTaskOutboxApiResponse toApiResponse(OutboxResponse appResponse) {
        return new CrawlTaskOutboxApiResponse(
                appResponse.crawlTaskId(),
                appResponse.idempotencyKey(),
                appResponse.status() != null ? appResponse.status().name() : null,
                appResponse.retryCount(),
                format(appResponse.createdAt()),
                format(appResponse.updatedAt()),
                format(appResponse.processedAt()));
    }

    public PageApiResponse<CrawlTaskOutboxApiResponse> toPageApiResponse(
            PageResponse<OutboxResponse> appPageResponse) {
        List<CrawlTaskOutboxApiResponse> content =
                appPageResponse.content().stream().map(this::toApiResponse).toList();
        return PageApiResponse.of(
                content,
                appPageResponse.page(),
                appPageResponse.size(),
                appPageResponse.totalElements());
    }
}
