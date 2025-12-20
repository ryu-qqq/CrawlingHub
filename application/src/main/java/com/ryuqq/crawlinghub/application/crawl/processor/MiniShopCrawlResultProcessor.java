package com.ryuqq.crawlinghub.application.crawl.processor;

import static com.ryuqq.crawlinghub.application.common.utils.StringTruncator.truncate;

import com.ryuqq.crawlinghub.application.crawl.dto.CrawlResult;
import com.ryuqq.crawlinghub.application.crawl.parser.MiniShopResponseParser;
import com.ryuqq.crawlinghub.application.product.assembler.CrawledRawAssembler;
import com.ryuqq.crawlinghub.application.product.manager.CrawledRawManager;
import com.ryuqq.crawlinghub.application.product.port.in.command.ProcessMiniShopItemUseCase;
import com.ryuqq.crawlinghub.application.task.dto.command.CreateCrawlTaskCommand;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledRaw;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledRawId;
import com.ryuqq.crawlinghub.domain.product.vo.MiniShopItem;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * MINI_SHOP 크롤링 결과 처리기
 *
 * <p><strong>처리 내용</strong>:
 *
 * <ul>
 *   <li>상품 목록 파싱 (상품번호, 상품명, 브랜드, 가격, 할인율 등)
 *   <li>파싱된 MiniShopItem을 JSON으로 변환하여 crawled_raw 테이블에 벌크 저장
 *   <li>DETAIL + OPTION 후속 Task 생성 (상품별)
 * </ul>
 *
 * <p><strong>성능 최적화</strong>: 중복 체크 없이 JSON 형태로 벌크 저장
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class MiniShopCrawlResultProcessor implements CrawlResultProcessor {

    private static final Logger log = LoggerFactory.getLogger(MiniShopCrawlResultProcessor.class);

    private final MiniShopResponseParser miniShopResponseParser;
    private final CrawledRawAssembler crawledRawAssembler;
    private final CrawledRawManager crawledRawManager;
    private final ProcessMiniShopItemUseCase processMiniShopItemUseCase;

    public MiniShopCrawlResultProcessor(
            MiniShopResponseParser miniShopResponseParser,
            CrawledRawAssembler crawledRawAssembler,
            CrawledRawManager crawledRawManager,
            ProcessMiniShopItemUseCase processMiniShopItemUseCase) {
        this.miniShopResponseParser = miniShopResponseParser;
        this.crawledRawAssembler = crawledRawAssembler;
        this.crawledRawManager = crawledRawManager;
        this.processMiniShopItemUseCase = processMiniShopItemUseCase;
    }

    @Override
    public CrawlTaskType supportedType() {
        return CrawlTaskType.MINI_SHOP;
    }

    @Override
    public ProcessingResult process(CrawlResult crawlResult, CrawlTask crawlTask) {
        log.debug(
                "MINI_SHOP 결과 처리 시작: taskId={}, schedulerId={}",
                crawlTask.getId().value(),
                crawlTask.getCrawlSchedulerId().value());

        // 1. 응답 파싱 - Domain VO 반환
        List<MiniShopItem> items = miniShopResponseParser.parse(crawlResult.getResponseBody());

        if (items.isEmpty()) {
            log.warn(
                    "MINI_SHOP 응답 파싱 실패 또는 상품 없음: taskId={}, responseBody 일부={}",
                    crawlTask.getId().value(),
                    truncate(crawlResult.getResponseBody()));
            return ProcessingResult.empty();
        }

        int parsedCount = items.size();
        long schedulerId = crawlTask.getCrawlSchedulerId().value();
        long sellerId = crawlTask.getSellerId().value();

        // 2. Assembler로 MiniShopItem → CrawledRaw 변환
        List<CrawledRaw> crawledRaws =
                crawledRawAssembler.toMiniShopRaws(schedulerId, sellerId, items);

        // 3. Manager로 벌크 저장
        List<CrawledRawId> savedIds = crawledRawManager.saveAll(crawledRaws);
        int savedCount = savedIds.size();

        log.info(
                "MINI_SHOP Raw 벌크 저장 완료: schedulerId={}, sellerId={}, parsedCount={},"
                        + " savedCount={}",
                schedulerId,
                sellerId,
                parsedCount,
                savedCount);

        // 4. CrawledProduct 처리 (정제된 도메인 객체 저장 + 이미지 Outbox 생성)
        SellerId sellerIdVO = SellerId.of(sellerId);
        int productProcessedCount = 0;
        for (MiniShopItem item : items) {
            try {
                processMiniShopItemUseCase.process(sellerIdVO, item);
                productProcessedCount++;
            } catch (Exception e) {
                log.warn(
                        "CrawledProduct 처리 실패: sellerId={}, itemNo={}, error={}",
                        sellerId,
                        item.itemNo(),
                        e.getMessage());
            }
        }
        log.info(
                "MINI_SHOP CrawledProduct 처리 완료: sellerId={}, processedCount={}",
                sellerId,
                productProcessedCount);

        // 5. 후속 DETAIL + OPTION Task 생성 (상품별)
        List<CreateCrawlTaskCommand> followUpCommands =
                createDetailAndOptionTaskCommands(crawlTask, items);

        return ProcessingResult.withFollowUp(followUpCommands, parsedCount, savedCount);
    }

    /** DETAIL + OPTION 후속 Task 커맨드 생성 */
    private List<CreateCrawlTaskCommand> createDetailAndOptionTaskCommands(
            CrawlTask crawlTask, List<MiniShopItem> items) {
        if (items.isEmpty()) {
            return Collections.emptyList();
        }

        Long schedulerId = crawlTask.getCrawlSchedulerId().value();
        Long sellerId = crawlTask.getSellerId().value();
        String mustItSellerName = crawlTask.getMustItSellerName();

        List<CreateCrawlTaskCommand> commands = new ArrayList<>();

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
        return commands;
    }
}
