package com.ryuqq.crawlinghub.application.execution.internal.crawler.processor;

import static com.ryuqq.crawlinghub.application.common.utils.StringTruncator.truncate;

import com.ryuqq.crawlinghub.application.execution.internal.crawler.parser.OptionResponseParser;
import com.ryuqq.crawlinghub.application.product.assembler.CrawledRawMapper;
import com.ryuqq.crawlinghub.application.product.manager.CrawledRawTransactionManager;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlResult;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledRaw;
import com.ryuqq.crawlinghub.domain.product.id.CrawledRawId;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOption;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * OPTION 크롤링 결과 처리기
 *
 * <p><strong>처리 내용</strong>:
 *
 * <ul>
 *   <li>상품 옵션 정보 파싱 (사이즈, 색상, 재고 등)
 *   <li>파싱된 ProductOption 목록을 JSON으로 변환하여 crawled_raw 테이블에 저장
 *   <li>후속 Task 없음 (최종 단계)
 * </ul>
 *
 * <p><strong>성능 최적화</strong>: 중복 체크 없이 JSON 형태로 저장
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class OptionCrawlResultProcessor implements CrawlResultProcessor {

    private static final Logger log = LoggerFactory.getLogger(OptionCrawlResultProcessor.class);

    private final OptionResponseParser optionResponseParser;
    private final CrawledRawMapper crawledRawMapper;
    private final CrawledRawTransactionManager crawledRawTransactionManager;

    public OptionCrawlResultProcessor(
            OptionResponseParser optionResponseParser,
            CrawledRawMapper crawledRawMapper,
            CrawledRawTransactionManager crawledRawTransactionManager) {
        this.optionResponseParser = optionResponseParser;
        this.crawledRawMapper = crawledRawMapper;
        this.crawledRawTransactionManager = crawledRawTransactionManager;
    }

    @Override
    public CrawlTaskType supportedType() {
        return CrawlTaskType.OPTION;
    }

    @Override
    public ProcessingResult process(CrawlResult crawlResult, CrawlTask crawlTask) {
        log.debug(
                "OPTION 결과 처리 시작: taskId={}, schedulerId={}",
                crawlTask.getIdValue(),
                crawlTask.getCrawlSchedulerIdValue());

        Long itemNo = EndpointItemNoResolver.resolve(crawlTask);

        // 1. 응답 파싱 - Domain VO 목록 반환
        List<ProductOption> options =
                optionResponseParser.parse(crawlResult.responseBody(), itemNo);

        if (options.isEmpty()) {
            log.warn(
                    "OPTION 응답 파싱 실패 또는 옵션 없음: taskId={}, itemNo={}, responseBody 일부={}",
                    crawlTask.getIdValue(),
                    itemNo,
                    truncate(crawlResult.responseBody()));
            return ProcessingResult.empty();
        }

        long schedulerId = crawlTask.getCrawlSchedulerIdValue();
        long sellerId = crawlTask.getSellerIdValue();

        // 2. Assembler로 ProductOption 목록 → CrawledRaw 변환
        Instant now = Instant.now();
        CrawledRaw crawledRaw =
                crawledRawMapper.toOptionRaw(schedulerId, sellerId, itemNo, options, now);

        // 3. Manager로 저장
        int savedCount = 0;
        if (crawledRaw != null) {
            CrawledRawId savedId = crawledRawTransactionManager.save(crawledRaw);
            savedCount = savedId != null ? 1 : 0;
        }

        int parsedCount = options.size();
        int availableCount = (int) options.stream().filter(ProductOption::isInStock).count();

        log.info(
                "OPTION Raw 저장 완료: schedulerId={}, sellerId={}, itemNo={}, optionCount={},"
                        + " availableCount={}",
                schedulerId,
                sellerId,
                itemNo,
                parsedCount,
                availableCount);

        // 4. 후속 Task 없음 (PENDING 상태, 별도 스케줄러에서 가공)
        return ProcessingResult.completed(parsedCount, savedCount);
    }
}
