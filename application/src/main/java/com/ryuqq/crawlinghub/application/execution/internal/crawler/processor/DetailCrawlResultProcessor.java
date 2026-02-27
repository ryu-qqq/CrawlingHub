package com.ryuqq.crawlinghub.application.execution.internal.crawler.processor;

import static com.ryuqq.crawlinghub.application.common.utils.StringTruncator.truncate;

import com.ryuqq.crawlinghub.application.execution.internal.crawler.parser.DetailResponseParser;
import com.ryuqq.crawlinghub.application.product.assembler.CrawledRawMapper;
import com.ryuqq.crawlinghub.application.product.internal.CrawledProductCoordinator;
import com.ryuqq.crawlinghub.application.product.manager.CrawledRawTransactionManager;
import com.ryuqq.crawlinghub.application.seller.manager.SellerReadManager;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlResult;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledRaw;
import com.ryuqq.crawlinghub.domain.product.id.CrawledRawId;
import com.ryuqq.crawlinghub.domain.product.vo.ProductDetailInfo;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import java.time.Instant;
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
    private final CrawledRawMapper crawledRawMapper;
    private final CrawledRawTransactionManager crawledRawTransactionManager;
    private final SellerReadManager sellerReadManager;
    private final CrawledProductCoordinator crawledProductCoordinator;

    public DetailCrawlResultProcessor(
            DetailResponseParser detailResponseParser,
            CrawledRawMapper crawledRawMapper,
            CrawledRawTransactionManager crawledRawTransactionManager,
            SellerReadManager sellerReadManager,
            CrawledProductCoordinator crawledProductCoordinator) {
        this.detailResponseParser = detailResponseParser;
        this.crawledRawMapper = crawledRawMapper;
        this.crawledRawTransactionManager = crawledRawTransactionManager;
        this.sellerReadManager = sellerReadManager;
        this.crawledProductCoordinator = crawledProductCoordinator;
    }

    @Override
    public CrawlTaskType supportedType() {
        return CrawlTaskType.DETAIL;
    }

    @Override
    public ProcessingResult process(CrawlResult crawlResult, CrawlTask crawlTask) {
        log.debug(
                "DETAIL 결과 처리 시작: taskId={}, schedulerId={}",
                crawlTask.getIdValue(),
                crawlTask.getCrawlSchedulerIdValue());

        Long itemNo = EndpointItemNoResolver.resolve(crawlTask);

        // 1. 응답 파싱 - Domain VO 반환
        Optional<ProductDetailInfo> parsedOpt =
                detailResponseParser.parse(crawlResult.responseBody(), itemNo);

        if (parsedOpt.isEmpty()) {
            log.warn(
                    "DETAIL 응답 파싱 실패: taskId={}, itemNo={}, responseBody 일부={}",
                    crawlTask.getIdValue(),
                    itemNo,
                    truncate(crawlResult.responseBody()));
            return ProcessingResult.empty();
        }

        ProductDetailInfo detailInfo = parsedOpt.get();
        long schedulerId = crawlTask.getCrawlSchedulerIdValue();
        long sellerId = crawlTask.getSellerIdValue();

        // 2. 셀러 검증 - 크롤링된 상품의 sellerId가 우리 셀러인지 확인
        if (!isMatchingSeller(sellerId, detailInfo.sellerId(), crawlTask.getIdValue(), itemNo)) {
            // 이미 MINI_SHOP에서 생성된 CrawledProduct가 있으면 soft-delete
            crawledProductCoordinator.softDeleteIfExists(
                    SellerId.of(sellerId), detailInfo.itemNo());
            return ProcessingResult.empty();
        }

        // 3. Assembler로 ProductDetailInfo → CrawledRaw 변환
        Instant now = Instant.now();
        CrawledRaw crawledRaw =
                crawledRawMapper.toDetailRaw(schedulerId, sellerId, detailInfo, now);

        // 4. Manager로 저장
        int savedCount = 0;
        if (crawledRaw != null) {
            CrawledRawId savedId = crawledRawTransactionManager.save(crawledRaw);
            savedCount = savedId != null ? 1 : 0;
        }

        log.info(
                "DETAIL Raw 저장 완료: schedulerId={}, sellerId={}, itemNo={}, itemName={}",
                schedulerId,
                sellerId,
                detailInfo.itemNo(),
                detailInfo.itemName());

        // 5. 후속 Task 없음 (PENDING 상태, 별도 스케줄러에서 가공)
        return ProcessingResult.completed(1, savedCount);
    }

    /**
     * 크롤링된 상품의 sellerId가 우리 셀러의 mustItSellerName과 일치하는지 검증
     *
     * @param sellerId DB 셀러 ID
     * @param crawledSellerId 크롤링 응답에서 파싱된 sellerId (예: "carte123")
     * @param taskId 로깅용 태스크 ID
     * @param itemNo 로깅용 상품 번호
     * @return 일치하면 true, 불일치 또는 셀러 조회 실패 시 false
     */
    private boolean isMatchingSeller(
            long sellerId, String crawledSellerId, Long taskId, Long itemNo) {
        Optional<Seller> sellerOpt = sellerReadManager.findById(SellerId.of(sellerId));

        if (sellerOpt.isEmpty()) {
            log.warn(
                    "셀러 조회 실패, DETAIL 저장 건너뜀: taskId={}, sellerId={}, itemNo={}",
                    taskId,
                    sellerId,
                    itemNo);
            return false;
        }

        String expectedSellerName = sellerOpt.get().getMustItSellerNameValue();
        if (!expectedSellerName.equals(crawledSellerId)) {
            log.warn(
                    "셀러 불일치 감지, DETAIL 저장 건너뜀: taskId={}, itemNo={}, " + "expected={}, actual={}",
                    taskId,
                    itemNo,
                    expectedSellerName,
                    crawledSellerId);
            return false;
        }

        return true;
    }
}
