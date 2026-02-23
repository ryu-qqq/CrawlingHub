package com.ryuqq.crawlinghub.adapter.in.rest.monitoring.mapper;

import com.ryuqq.crawlinghub.adapter.in.rest.monitoring.dto.response.CrawlTaskSummaryApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.monitoring.dto.response.CrawledRawSummaryApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.monitoring.dto.response.DashboardSummaryApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.monitoring.dto.response.ExternalSystemHealthApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.monitoring.dto.response.ExternalSystemHealthApiResponse.SystemHealthApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.monitoring.dto.response.OutboxSummaryApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.monitoring.dto.response.OutboxSummaryApiResponse.OutboxDetailApiResponse;
import com.ryuqq.crawlinghub.application.monitoring.dto.composite.CrawlTaskSummaryResult;
import com.ryuqq.crawlinghub.application.monitoring.dto.composite.CrawledRawSummaryResult;
import com.ryuqq.crawlinghub.application.monitoring.dto.composite.DashboardSummaryResult;
import com.ryuqq.crawlinghub.application.monitoring.dto.composite.ExternalSystemHealthResult;
import com.ryuqq.crawlinghub.application.monitoring.dto.composite.OutboxSummaryResult;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MonitoringQueryApiMapper {

    public DashboardSummaryApiResponse toDashboardApiResponse(DashboardSummaryResult result) {
        return new DashboardSummaryApiResponse(
                result.activeSchedulers(),
                result.runningTasks(),
                result.pendingOutbox(),
                result.recentErrors(),
                result.overallStatus().name());
    }

    public CrawlTaskSummaryApiResponse toCrawlTaskSummaryApiResponse(
            CrawlTaskSummaryResult result) {
        return new CrawlTaskSummaryApiResponse(
                result.countsByStatus(), result.stuckTasks(), result.totalTasks());
    }

    public OutboxSummaryApiResponse toOutboxSummaryApiResponse(OutboxSummaryResult result) {
        return new OutboxSummaryApiResponse(
                toOutboxDetailApiResponse(result.crawlTaskOutbox()),
                toOutboxDetailApiResponse(result.schedulerOutbox()),
                toOutboxDetailApiResponse(result.productSyncOutbox()));
    }

    public CrawledRawSummaryApiResponse toCrawledRawSummaryApiResponse(
            CrawledRawSummaryResult result) {
        return new CrawledRawSummaryApiResponse(result.countsByStatus(), result.totalRaw());
    }

    public ExternalSystemHealthApiResponse toExternalSystemHealthApiResponse(
            ExternalSystemHealthResult result) {
        List<SystemHealthApiResponse> systems =
                result.systems().stream()
                        .map(
                                s ->
                                        new SystemHealthApiResponse(
                                                s.system(), s.recentFailures(), s.status()))
                        .toList();
        return new ExternalSystemHealthApiResponse(systems);
    }

    private OutboxDetailApiResponse toOutboxDetailApiResponse(
            OutboxSummaryResult.OutboxDetail detail) {
        return new OutboxDetailApiResponse(detail.countsByStatus(), detail.total());
    }
}
