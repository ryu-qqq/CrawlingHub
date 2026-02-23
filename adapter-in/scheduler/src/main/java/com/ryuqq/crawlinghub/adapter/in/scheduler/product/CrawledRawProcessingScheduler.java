package com.ryuqq.crawlinghub.adapter.in.scheduler.product;

import com.ryuqq.crawlinghub.adapter.in.scheduler.annotation.SchedulerJob;
import com.ryuqq.crawlinghub.adapter.in.scheduler.config.SchedulerProperties;
import com.ryuqq.crawlinghub.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.crawlinghub.application.product.dto.command.ProcessPendingCrawledRawCommand;
import com.ryuqq.crawlinghub.application.product.port.in.command.ProcessPendingCrawledRawUseCase;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * CrawledRaw 가공 처리 스케줄러
 *
 * <p><strong>용도</strong>: PENDING 상태의 CrawledRaw를 타입별로 역직렬화하여 CrawledProduct 생성/업데이트
 *
 * <p><strong>스케줄 구성</strong>:
 *
 * <ul>
 *   <li>{@code processMiniShop} — MINI_SHOP 타입 Raw 가공 (SEARCH에서 수집된 상품 목록)
 *   <li>{@code processDetail} — DETAIL 타입 Raw 가공 (상품 상세 정보)
 *   <li>{@code processOption} — OPTION 타입 Raw 가공 (상품 옵션 정보)
 * </ul>
 *
 * <p><strong>순서 보장</strong>: MINI_SHOP → DETAIL → OPTION 순서로 처리하여 CrawledProduct가 먼저 생성됨을 보장합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
@ConditionalOnProperty(
        prefix = "scheduler.jobs.crawled-raw-processing.process-mini-shop",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = false)
public class CrawledRawProcessingScheduler {

    private final ProcessPendingCrawledRawUseCase processPendingCrawledRawUseCase;
    private final SchedulerProperties.CrawledRawProcessing config;

    public CrawledRawProcessingScheduler(
            ProcessPendingCrawledRawUseCase processPendingCrawledRawUseCase,
            SchedulerProperties properties) {
        this.processPendingCrawledRawUseCase = processPendingCrawledRawUseCase;
        this.config = properties.jobs().crawledRawProcessing();
    }

    /**
     * MINI_SHOP 타입 CrawledRaw 가공
     *
     * <p>SEARCH에서 수집된 MiniShopItem Raw 데이터를 역직렬화하여 CrawledProduct 생성/업데이트
     *
     * @return 배치 처리 결과
     */
    @Scheduled(
            cron = "${scheduler.jobs.crawled-raw-processing.process-mini-shop.cron}",
            zone = "${scheduler.jobs.crawled-raw-processing.process-mini-shop.timezone}")
    @SchedulerJob("CrawledRaw-ProcessMiniShop")
    public SchedulerBatchProcessingResult processMiniShop() {
        SchedulerProperties.ProcessCrawledRaw miniShop = config.processMiniShop();

        ProcessPendingCrawledRawCommand command =
                ProcessPendingCrawledRawCommand.of(CrawlType.MINI_SHOP, miniShop.batchSize());

        return processPendingCrawledRawUseCase.execute(command);
    }

    /**
     * DETAIL 타입 CrawledRaw 가공
     *
     * <p>상품 상세 정보 Raw 데이터를 역직렬화하여 CrawledProduct 업데이트
     *
     * @return 배치 처리 결과
     */
    @Scheduled(
            cron = "${scheduler.jobs.crawled-raw-processing.process-detail.cron}",
            zone = "${scheduler.jobs.crawled-raw-processing.process-detail.timezone}")
    @SchedulerJob("CrawledRaw-ProcessDetail")
    public SchedulerBatchProcessingResult processDetail() {
        SchedulerProperties.ProcessCrawledRaw detail = config.processDetail();

        ProcessPendingCrawledRawCommand command =
                ProcessPendingCrawledRawCommand.of(CrawlType.DETAIL, detail.batchSize());

        return processPendingCrawledRawUseCase.execute(command);
    }

    /**
     * OPTION 타입 CrawledRaw 가공
     *
     * <p>상품 옵션 정보 Raw 데이터를 역직렬화하여 CrawledProduct 업데이트
     *
     * @return 배치 처리 결과
     */
    @Scheduled(
            cron = "${scheduler.jobs.crawled-raw-processing.process-option.cron}",
            zone = "${scheduler.jobs.crawled-raw-processing.process-option.timezone}")
    @SchedulerJob("CrawledRaw-ProcessOption")
    public SchedulerBatchProcessingResult processOption() {
        SchedulerProperties.ProcessCrawledRaw option = config.processOption();

        ProcessPendingCrawledRawCommand command =
                ProcessPendingCrawledRawCommand.of(CrawlType.OPTION, option.batchSize());

        return processPendingCrawledRawUseCase.execute(command);
    }
}
