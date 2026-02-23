package com.ryuqq.crawlinghub.application.execution.internal.crawler.processor;

import com.ryuqq.crawlinghub.application.execution.internal.crawler.parser.SearchResponseParser;
import com.ryuqq.crawlinghub.application.product.assembler.CrawledRawMapper;
import com.ryuqq.crawlinghub.application.product.manager.CrawledRawTransactionManager;
import com.ryuqq.crawlinghub.application.task.dto.command.CreateCrawlTaskCommand;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlResult;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledRaw;
import com.ryuqq.crawlinghub.domain.product.id.CrawledRawId;
import com.ryuqq.crawlinghub.domain.product.vo.MiniShopItem;
import com.ryuqq.crawlinghub.domain.product.vo.SearchParseResult;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * SEARCH 크롤링 결과 처리기
 *
 * <p><strong>무한스크롤 방식</strong>: META/MINI_SHOP 대체
 *
 * <p><strong>처리 내용</strong>:
 *
 * <ul>
 *   <li>Search API 응답 파싱 (moduleList → MiniShopItem 변환)
 *   <li>파싱된 MiniShopItem을 JSON으로 변환하여 crawled_raw 테이블에 벌크 저장 (PENDING 상태)
 *   <li>nextApiUrl이 있으면 다음 SEARCH 태스크 생성 (무한스크롤 연속)
 *   <li>DETAIL + OPTION 후속 Task 생성 (상품별)
 * </ul>
 *
 * <p><strong>종료 조건</strong>: moduleList 비어있음 AND nextApiUrl 없음
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class SearchCrawlResultProcessor implements CrawlResultProcessor {

    private static final Logger log = LoggerFactory.getLogger(SearchCrawlResultProcessor.class);

    private final SearchResponseParser searchResponseParser;
    private final CrawledRawMapper crawledRawMapper;
    private final CrawledRawTransactionManager crawledRawCommandManager;

    public SearchCrawlResultProcessor(
            SearchResponseParser searchResponseParser,
            CrawledRawMapper crawledRawMapper,
            CrawledRawTransactionManager crawledRawCommandManager) {
        this.searchResponseParser = searchResponseParser;
        this.crawledRawMapper = crawledRawMapper;
        this.crawledRawCommandManager = crawledRawCommandManager;
    }

    @Override
    public CrawlTaskType supportedType() {
        return CrawlTaskType.SEARCH;
    }

    @Override
    public ProcessingResult process(CrawlResult crawlResult, CrawlTask crawlTask) {
        log.debug(
                "SEARCH 결과 처리 시작: taskId={}, schedulerId={}",
                crawlTask.getIdValue(),
                crawlTask.getCrawlSchedulerIdValue());

        // 1. 응답 파싱 - SearchItem → MiniShopItem 변환된 목록 + nextApiUrl
        SearchParseResult parseResult = searchResponseParser.parse(crawlResult.responseBody());

        if (parseResult.isEmpty() && !parseResult.hasNextPage()) {
            log.info("SEARCH 종료 조건 충족: taskId={}, 상품 없음 + nextApiUrl 없음", crawlTask.getIdValue());
            return ProcessingResult.empty();
        }

        List<MiniShopItem> items = parseResult.items();
        int parsedCount = items.size();
        long schedulerId = crawlTask.getCrawlSchedulerIdValue();
        long sellerId = crawlTask.getSellerIdValue();

        int savedCount = 0;

        if (!items.isEmpty()) {
            // 2. Assembler로 MiniShopItem → CrawledRaw 변환
            Instant now = Instant.now();
            List<CrawledRaw> crawledRaws =
                    crawledRawMapper.toMiniShopRaws(schedulerId, sellerId, items, now);

            // 3. Manager로 벌크 저장 (PENDING 상태, 별도 스케줄러에서 가공)
            List<CrawledRawId> savedIds = crawledRawCommandManager.saveAll(crawledRaws);
            savedCount = savedIds.size();

            log.info(
                    "SEARCH Raw 벌크 저장 완료: schedulerId={}, sellerId={}, parsedCount={},"
                            + " savedCount={}",
                    schedulerId,
                    sellerId,
                    parsedCount,
                    savedCount);
        }

        // 5. 후속 태스크 생성
        List<CreateCrawlTaskCommand> followUpCommands =
                createFollowUpTaskCommands(crawlTask, items, parseResult.nextApiUrl());

        return ProcessingResult.withFollowUp(followUpCommands, parsedCount, savedCount);
    }

    /**
     * 후속 태스크 커맨드 생성
     *
     * <p><strong>생성되는 태스크</strong>:
     *
     * <ul>
     *   <li>nextApiUrl이 있으면 → 다음 SEARCH 태스크 (무한스크롤 연속)
     *   <li>각 상품별 → DETAIL + OPTION 태스크
     * </ul>
     */
    private List<CreateCrawlTaskCommand> createFollowUpTaskCommands(
            CrawlTask crawlTask, List<MiniShopItem> items, String nextApiUrl) {

        Long schedulerId = crawlTask.getCrawlSchedulerIdValue();
        Long sellerId = crawlTask.getSellerIdValue();
        String mustItSellerName = crawlTask.getEndpoint().getMustItSellerName();

        List<CreateCrawlTaskCommand> commands = new ArrayList<>();

        // 다음 SEARCH 페이지 태스크 생성 (무한스크롤)
        if (nextApiUrl != null && !nextApiUrl.isBlank()) {
            commands.add(
                    CreateCrawlTaskCommand.forSearchNextPage(
                            schedulerId, sellerId, mustItSellerName, nextApiUrl));
            log.debug("다음 SEARCH 페이지 태스크 생성: nextApiUrl={}", nextApiUrl);
        }

        // 상품별 DETAIL + OPTION 태스크 생성
        if (!items.isEmpty()) {
            for (MiniShopItem item : items) {
                Long itemNo = item.itemNo();
                commands.add(
                        CreateCrawlTaskCommand.forDetail(
                                schedulerId, sellerId, mustItSellerName, itemNo));
                commands.add(
                        CreateCrawlTaskCommand.forOption(
                                schedulerId, sellerId, mustItSellerName, itemNo));
            }
            log.debug("후속 Task 생성: DETAIL={}, OPTION={}", items.size(), items.size());
        }

        return commands.isEmpty() ? Collections.emptyList() : commands;
    }
}
