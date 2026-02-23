package com.ryuqq.crawlinghub.adapter.out.persistence.composite.monitoring.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.composite.monitoring.dto.DashboardCountsDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.monitoring.dto.OutboxStatusCountDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.monitoring.dto.StatusCountDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.monitoring.dto.SystemFailureCountDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.monitoring.mapper.MonitoringCompositeMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.monitoring.repository.MonitoringCompositeQueryDslRepository;
import com.ryuqq.crawlinghub.application.monitoring.dto.composite.CrawlTaskSummaryResult;
import com.ryuqq.crawlinghub.application.monitoring.dto.composite.CrawledRawSummaryResult;
import com.ryuqq.crawlinghub.application.monitoring.dto.composite.DashboardSummaryResult;
import com.ryuqq.crawlinghub.application.monitoring.dto.composite.ExternalSystemHealthResult;
import com.ryuqq.crawlinghub.application.monitoring.dto.composite.OutboxSummaryResult;
import com.ryuqq.crawlinghub.application.monitoring.port.out.query.MonitoringCompositeQueryPort;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
public class MonitoringCompositeQueryAdapter implements MonitoringCompositeQueryPort {

    private final MonitoringCompositeQueryDslRepository repository;
    private final MonitoringCompositeMapper mapper;

    public MonitoringCompositeQueryAdapter(
            MonitoringCompositeQueryDslRepository repository, MonitoringCompositeMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public DashboardSummaryResult getDashboardSummary(Duration lookback) {
        Instant threshold = Instant.now().minus(lookback);
        DashboardCountsDto counts = repository.fetchDashboardCounts(threshold);
        return mapper.toDashboardSummaryResult(counts);
    }

    @Override
    public CrawlTaskSummaryResult getCrawlTaskSummary(Duration lookback) {
        Instant threshold = Instant.now().minus(lookback);
        List<StatusCountDto> statusCounts = repository.fetchCrawlTaskCountsByStatus();
        long stuckTasks = repository.fetchStuckCrawlTasks(threshold);
        return mapper.toCrawlTaskSummaryResult(statusCounts, stuckTasks);
    }

    @Override
    public OutboxSummaryResult getOutboxSummary() {
        List<OutboxStatusCountDto> outboxCounts = repository.fetchOutboxCountsByType();
        return mapper.toOutboxSummaryResult(outboxCounts);
    }

    @Override
    public CrawledRawSummaryResult getCrawledRawSummary() {
        List<StatusCountDto> statusCounts = repository.fetchCrawledRawCountsByStatus();
        return mapper.toCrawledRawSummaryResult(statusCounts);
    }

    @Override
    public ExternalSystemHealthResult getExternalSystemHealth(Duration lookback) {
        Instant threshold = Instant.now().minus(lookback);
        List<SystemFailureCountDto> failureCounts = repository.fetchRecentFailureCounts(threshold);
        return mapper.toExternalSystemHealthResult(failureCounts);
    }
}
