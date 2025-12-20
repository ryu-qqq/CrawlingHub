package com.ryuqq.crawlinghub.application.crawl.processor;

import static com.ryuqq.crawlinghub.application.common.utils.StringTruncator.truncate;

import com.ryuqq.crawlinghub.application.crawl.dto.CrawlResult;
import com.ryuqq.crawlinghub.application.crawl.parser.MetaResponseParser;
import com.ryuqq.crawlinghub.application.seller.port.in.command.UpdateSellerProductCountUseCase;
import com.ryuqq.crawlinghub.application.task.dto.command.CreateCrawlTaskCommand;
import com.ryuqq.crawlinghub.domain.product.vo.ProductCount;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * META 크롤링 결과 처리기
 *
 * <p><strong>처리 내용</strong>:
 *
 * <ul>
 *   <li>미니샵 메타 정보 파싱 (총 상품 수)
 *   <li>셀러 상품 수 업데이트 (UseCase 호출)
 *   <li>MINI_SHOP 후속 Task 생성 (페이지별, 0번부터 시작)
 * </ul>
 *
 * <p><strong>페이지 계산</strong>: 총 상품 수 / 500 = 총 페이지 수 (0번 페이지부터 시작)
 *
 * <p><strong>후속 Task</strong>: 총 페이지 수만큼 MINI_SHOP Task 생성
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class MetaCrawlResultProcessor implements CrawlResultProcessor {

    private static final Logger log = LoggerFactory.getLogger(MetaCrawlResultProcessor.class);

    private final MetaResponseParser metaResponseParser;
    private final UpdateSellerProductCountUseCase updateSellerProductCountUseCase;

    public MetaCrawlResultProcessor(
            MetaResponseParser metaResponseParser,
            UpdateSellerProductCountUseCase updateSellerProductCountUseCase) {
        this.metaResponseParser = metaResponseParser;
        this.updateSellerProductCountUseCase = updateSellerProductCountUseCase;
    }

    @Override
    public CrawlTaskType supportedType() {
        return CrawlTaskType.META;
    }

    @Override
    public ProcessingResult process(CrawlResult crawlResult, CrawlTask crawlTask) {
        log.debug(
                "META 결과 처리 시작: taskId={}, schedulerId={}",
                crawlTask.getIdValue(),
                crawlTask.getCrawlSchedulerIdValue());

        // 1. 응답 파싱
        Optional<ProductCount> parsedOpt =
                metaResponseParser.parseResponse(crawlResult.getResponseBody());

        if (parsedOpt.isEmpty()) {
            log.warn(
                    "META 응답 파싱 실패: taskId={}, responseBody 일부={}",
                    crawlTask.getIdValue(),
                    truncate(crawlResult.getResponseBody()));
            return ProcessingResult.empty();
        }

        ProductCount productCount = parsedOpt.get();

        // 2. 셀러 상품 수 업데이트
        updateSellerProductCountUseCase.execute(
                crawlTask.getSellerIdValue(), productCount.totalCount());

        log.info(
                "META 정보 처리 완료: sellerId={}, totalProducts={}, totalPages={}",
                crawlTask.getSellerIdValue(),
                productCount.totalCount(),
                productCount.calculateTotalPages());

        // 3. 후속 MINI_SHOP Task 생성 (페이지별, 0번부터)
        List<CreateCrawlTaskCommand> followUpCommands =
                createMiniShopTaskCommands(crawlTask, productCount);

        return ProcessingResult.withFollowUp(followUpCommands, 1, 1);
    }

    /**
     * MINI_SHOP 후속 Task 커맨드 생성
     *
     * <p>총 페이지 수만큼 MINI_SHOP Task를 생성합니다. (0번 페이지부터 시작)
     */
    private List<CreateCrawlTaskCommand> createMiniShopTaskCommands(
            CrawlTask crawlTask, ProductCount productCount) {
        int totalPages = productCount.calculateTotalPages();

        if (totalPages <= 0) {
            log.warn("페이지 수가 0 이하: sellerId={}", crawlTask.getSellerId().value());
            return Collections.emptyList();
        }

        Long schedulerId = crawlTask.getCrawlSchedulerId().value();
        Long sellerId = crawlTask.getSellerId().value();
        String mustItSellerName = crawlTask.getMustItSellerName();

        log.info(
                "MINI_SHOP Task 생성 예정: sellerId={}, totalPages={} (0~{})",
                sellerId,
                totalPages,
                totalPages - 1);

        // 0번 페이지부터 시작 (0, 1, 2, ..., totalPages-1)
        return IntStream.range(0, totalPages)
                .mapToObj(
                        page ->
                                CreateCrawlTaskCommand.forMiniShop(
                                        schedulerId, sellerId, mustItSellerName, (long) page))
                .toList();
    }
}
