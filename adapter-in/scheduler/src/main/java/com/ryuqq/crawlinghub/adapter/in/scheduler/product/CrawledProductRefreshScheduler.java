package com.ryuqq.crawlinghub.adapter.in.scheduler.product;

import com.ryuqq.crawlinghub.adapter.in.scheduler.annotation.SchedulerJob;
import com.ryuqq.crawlinghub.adapter.in.scheduler.config.SchedulerProperties;
import com.ryuqq.crawlinghub.application.product.port.in.command.RefreshStaleCrawledProductsUseCase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 기존 상품 재고/가격 갱신 스케줄러
 *
 * <p>updatedAt이 가장 오래된 상품부터 주기적으로 DETAIL + OPTION 태스크를 생성하여 재고/가격을 갱신합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
@ConditionalOnProperty(
        prefix = "scheduler.jobs.product-refresh.refresh-stale",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = false)
public class CrawledProductRefreshScheduler {

    private final RefreshStaleCrawledProductsUseCase useCase;
    private final SchedulerProperties.RefreshStale config;

    public CrawledProductRefreshScheduler(
            RefreshStaleCrawledProductsUseCase useCase, SchedulerProperties properties) {
        this.useCase = useCase;
        this.config = properties.jobs().productRefresh().refreshStale();
    }

    /**
     * stale 상품에 대한 DETAIL + OPTION 태스크 생성
     *
     * <p>batchSize만큼의 오래된 상품을 조회하여 재크롤링 태스크를 생성합니다.
     */
    @Scheduled(
            cron = "${scheduler.jobs.product-refresh.refresh-stale.cron}",
            zone = "${scheduler.jobs.product-refresh.refresh-stale.timezone}")
    @SchedulerJob("ProductRefresh-RefreshStale")
    public void refreshStale() {
        useCase.execute(config.batchSize());
    }
}
