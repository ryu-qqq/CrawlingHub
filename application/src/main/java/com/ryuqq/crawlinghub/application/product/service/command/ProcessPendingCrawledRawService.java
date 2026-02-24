package com.ryuqq.crawlinghub.application.product.service.command;

import com.ryuqq.crawlinghub.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.crawlinghub.application.common.metric.annotation.CrawlMetric;
import com.ryuqq.crawlinghub.application.product.dto.command.ProcessPendingCrawledRawCommand;
import com.ryuqq.crawlinghub.application.product.internal.processor.CrawledRawProcessor;
import com.ryuqq.crawlinghub.application.product.internal.processor.CrawledRawProcessorProvider;
import com.ryuqq.crawlinghub.application.product.manager.CrawledRawReadManager;
import com.ryuqq.crawlinghub.application.product.manager.CrawledRawTransactionManager;
import com.ryuqq.crawlinghub.application.product.port.in.command.ProcessPendingCrawledRawUseCase;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledRaw;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlType;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * PENDING 상태의 CrawledRaw 가공 처리 Service
 *
 * <p>스케줄러에서 호출하여 PENDING 상태의 Raw 데이터를 타입별 프로세서로 가공합니다.
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>PENDING 상태의 CrawledRaw 조회 (타입별, 배치 크기)
 *   <li>CrawledRawProcessorProvider로 타입별 프로세서 조회
 *   <li>프로세서가 역직렬화 + 가공을 처리
 *   <li>성공 시 PROCESSED, 실패 시 FAILED로 상태 변경
 * </ol>
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class ProcessPendingCrawledRawService implements ProcessPendingCrawledRawUseCase {

    private static final Logger log =
            LoggerFactory.getLogger(ProcessPendingCrawledRawService.class);

    private final CrawledRawReadManager crawledRawReadManager;
    private final CrawledRawTransactionManager crawledRawTransactionManager;
    private final CrawledRawProcessorProvider crawledRawProcessorProvider;

    public ProcessPendingCrawledRawService(
            CrawledRawReadManager crawledRawReadManager,
            CrawledRawTransactionManager crawledRawTransactionManager,
            CrawledRawProcessorProvider crawledRawProcessorProvider) {
        this.crawledRawReadManager = crawledRawReadManager;
        this.crawledRawTransactionManager = crawledRawTransactionManager;
        this.crawledRawProcessorProvider = crawledRawProcessorProvider;
    }

    @CrawlMetric(value = "crawled_raw", operation = "process")
    @Override
    public SchedulerBatchProcessingResult execute(ProcessPendingCrawledRawCommand command) {
        CrawlType crawlType = command.crawlType();
        int batchSize = command.batchSize();

        List<CrawledRaw> pendingRaws =
                crawledRawReadManager.findPendingByType(crawlType, batchSize);

        if (pendingRaws.isEmpty()) {
            return SchedulerBatchProcessingResult.empty();
        }

        log.info("CrawledRaw 가공 시작: type={}, count={}", crawlType, pendingRaws.size());

        int success = 0;
        int failed = 0;

        for (CrawledRaw raw : pendingRaws) {
            try {
                processRaw(raw);
                crawledRawTransactionManager.markAsProcessed(raw, Instant.now());
                success++;
            } catch (Exception e) {
                log.warn(
                        "CrawledRaw 가공 실패: id={}, type={}, itemNo={}, error={}",
                        raw.getIdValue(),
                        crawlType,
                        raw.getItemNo(),
                        e.getMessage());
                crawledRawTransactionManager.markAsFailed(raw, e.getMessage(), Instant.now());
                failed++;
            }
        }

        log.info(
                "CrawledRaw 가공 완료: type={}, total={}, success={}, failed={}",
                crawlType,
                pendingRaws.size(),
                success,
                failed);

        return SchedulerBatchProcessingResult.of(pendingRaws.size(), success, failed);
    }

    private void processRaw(CrawledRaw raw) {
        CrawledRawProcessor processor =
                crawledRawProcessorProvider.getProcessor(raw.getCrawlType());
        processor.process(raw);
    }
}
