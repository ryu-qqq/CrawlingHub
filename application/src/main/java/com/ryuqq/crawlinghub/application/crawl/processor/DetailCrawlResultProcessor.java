package com.ryuqq.crawlinghub.application.crawl.processor;

import static com.ryuqq.crawlinghub.application.common.utils.StringTruncator.truncate;

import com.ryuqq.crawlinghub.application.crawl.dto.CrawlResult;
import com.ryuqq.crawlinghub.application.crawl.parser.DetailResponseParser;
import com.ryuqq.crawlinghub.application.product.assembler.CrawledRawAssembler;
import com.ryuqq.crawlinghub.application.product.manager.CrawledRawManager;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledRaw;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledRawId;
import com.ryuqq.crawlinghub.domain.product.vo.ProductDetailInfo;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * DETAIL 크롤링 결과 처리기
 *
 * <p><strong>처리 내용</strong>:
 *
 * <ul>
 *   <li>상품 상세 정보 파싱 (설명, 이미지, 카테고리 등)
 *   <li>파싱된 ProductDetailInfo를 JSON으로 변환하여 crawled_raw 테이블에 저장
 *   <li>후속 Task 없음 (OPTION은 MiniShop에서 이미 생성)
 * </ul>
 *
 * <p><strong>성능 최적화</strong>: 중복 체크 없이 JSON 형태로 저장
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class DetailCrawlResultProcessor implements CrawlResultProcessor {

    private static final Logger log = LoggerFactory.getLogger(DetailCrawlResultProcessor.class);

    private final DetailResponseParser detailResponseParser;
    private final CrawledRawAssembler crawledRawAssembler;
    private final CrawledRawManager crawledRawManager;

    public DetailCrawlResultProcessor(
            DetailResponseParser detailResponseParser,
            CrawledRawAssembler crawledRawAssembler,
            CrawledRawManager crawledRawManager) {
        this.detailResponseParser = detailResponseParser;
        this.crawledRawAssembler = crawledRawAssembler;
        this.crawledRawManager = crawledRawManager;
    }

    @Override
    public CrawlTaskType supportedType() {
        return CrawlTaskType.DETAIL;
    }

    @Override
    public ProcessingResult process(CrawlResult crawlResult, CrawlTask crawlTask) {
        log.debug(
                "DETAIL 결과 처리 시작: taskId={}, schedulerId={}",
                crawlTask.getId().value(),
                crawlTask.getCrawlSchedulerId().value());

        Long itemNo = extractItemNoFromTask(crawlTask);

        // 1. 응답 파싱 - Domain VO 반환
        Optional<ProductDetailInfo> parsedOpt =
                detailResponseParser.parse(crawlResult.getResponseBody(), itemNo);

        if (parsedOpt.isEmpty()) {
            log.warn(
                    "DETAIL 응답 파싱 실패: taskId={}, itemNo={}, responseBody 일부={}",
                    crawlTask.getId().value(),
                    itemNo,
                    truncate(crawlResult.getResponseBody()));
            return ProcessingResult.empty();
        }

        ProductDetailInfo detailInfo = parsedOpt.get();
        long schedulerId = crawlTask.getCrawlSchedulerId().value();
        long sellerId = crawlTask.getSellerId().value();

        // 2. Assembler로 ProductDetailInfo → CrawledRaw 변환
        CrawledRaw crawledRaw = crawledRawAssembler.toDetailRaw(schedulerId, sellerId, detailInfo);

        // 3. Manager로 저장
        int savedCount = 0;
        if (crawledRaw != null) {
            CrawledRawId savedId = crawledRawManager.save(crawledRaw);
            savedCount = savedId != null ? 1 : 0;
        }

        log.info(
                "DETAIL Raw 저장 완료: schedulerId={}, sellerId={}, itemNo={}, itemName={}",
                schedulerId,
                sellerId,
                detailInfo.itemNo(),
                detailInfo.itemName());

        // 4. 후속 Task 없음
        return ProcessingResult.completed(1, savedCount);
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
