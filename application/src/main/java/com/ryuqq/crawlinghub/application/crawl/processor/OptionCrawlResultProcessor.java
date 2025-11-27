package com.ryuqq.crawlinghub.application.crawl.processor;

import static com.ryuqq.crawlinghub.application.common.utils.StringTruncator.truncate;

import com.ryuqq.crawlinghub.application.crawl.dto.CrawlResult;
import com.ryuqq.crawlinghub.application.crawl.parser.OptionResponseParser;
import com.ryuqq.crawlinghub.application.product.assembler.CrawledRawAssembler;
import com.ryuqq.crawlinghub.application.product.manager.CrawledRawManager;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledRaw;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledRawId;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOption;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * OPTION 크롤링 결과 처리기
 *
 * <p><strong>처리 내용</strong>:
 * <ul>
 *   <li>상품 옵션 정보 파싱 (사이즈, 색상, 재고 등)</li>
 *   <li>파싱된 ProductOption 목록을 JSON으로 변환하여 crawled_raw 테이블에 저장</li>
 *   <li>후속 Task 없음 (최종 단계)</li>
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
    private final CrawledRawAssembler crawledRawAssembler;
    private final CrawledRawManager crawledRawManager;

    public OptionCrawlResultProcessor(
            OptionResponseParser optionResponseParser,
            CrawledRawAssembler crawledRawAssembler,
            CrawledRawManager crawledRawManager) {
        this.optionResponseParser = optionResponseParser;
        this.crawledRawAssembler = crawledRawAssembler;
        this.crawledRawManager = crawledRawManager;
    }

    @Override
    public CrawlTaskType supportedType() {
        return CrawlTaskType.OPTION;
    }

    @Override
    public ProcessingResult process(CrawlResult crawlResult, CrawlTask crawlTask) {
        log.debug(
                "OPTION 결과 처리 시작: taskId={}, schedulerId={}",
                crawlTask.getId().value(),
                crawlTask.getCrawlSchedulerId().value());

        Long itemNo = extractItemNoFromTask(crawlTask);

        // 1. 응답 파싱 - Domain VO 목록 반환
        List<ProductOption> options =
                optionResponseParser.parse(crawlResult.getResponseBody(), itemNo);

        if (options.isEmpty()) {
            log.warn(
                    "OPTION 응답 파싱 실패 또는 옵션 없음: taskId={}, itemNo={}, responseBody 일부={}",
                    crawlTask.getId().value(),
                    itemNo,
                    truncate(crawlResult.getResponseBody()));
            return ProcessingResult.empty();
        }

        long schedulerId = crawlTask.getCrawlSchedulerId().value();
        long sellerId = crawlTask.getSellerId().value();

        // 2. Assembler로 ProductOption 목록 → CrawledRaw 변환
        CrawledRaw crawledRaw = crawledRawAssembler.toOptionRaw(schedulerId, sellerId, itemNo, options);

        // 3. Manager로 저장
        int savedCount = 0;
        if (crawledRaw != null) {
            CrawledRawId savedId = crawledRawManager.save(crawledRaw);
            savedCount = savedId != null ? 1 : 0;
        }

        int parsedCount = options.size();
        int availableCount = (int) options.stream().filter(ProductOption::isInStock).count();

        log.info(
                "OPTION Raw 저장 완료: schedulerId={}, sellerId={}, itemNo={}, optionCount={}, availableCount={}",
                schedulerId,
                sellerId,
                itemNo,
                parsedCount,
                availableCount);

        // 4. 후속 Task 없음 (최종 단계)
        return ProcessingResult.completed(parsedCount, savedCount);
    }

    private Long extractItemNoFromTask(CrawlTask crawlTask) {
        String endpoint = crawlTask.getEndpoint().toFullUrl();
        return parseItemNoFromEndpoint(endpoint);
    }

    private Long parseItemNoFromEndpoint(String endpoint) {
        try {
            if (endpoint.contains("/products/")) {
                String[] parts = endpoint.split("/products/");
                if (parts.length > 1) {
                    String afterProducts = parts[1];
                    String itemNoStr = afterProducts.split("/")[0].split("\\?")[0];
                    return Long.parseLong(itemNoStr);
                }
            }
            if (endpoint.contains("itemNo=")) {
                String[] parts = endpoint.split("itemNo=");
                if (parts.length > 1) {
                    String itemNoStr = parts[1].split("&")[0];
                    return Long.parseLong(itemNoStr);
                }
            }
        } catch (NumberFormatException e) {
            log.warn("itemNo 파싱 실패: endpoint={}", endpoint);
        }
        return null;
    }
}
