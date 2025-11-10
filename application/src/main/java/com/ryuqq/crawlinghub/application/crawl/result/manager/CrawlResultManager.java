package com.ryuqq.crawlinghub.application.crawl.result.manager;

import com.ryuqq.crawlinghub.application.crawl.result.port.out.SaveCrawlResultPort;
import com.ryuqq.crawlinghub.domain.crawl.result.CrawlResult;
import com.ryuqq.crawlinghub.domain.seller.MustitSellerId;
import com.ryuqq.crawlinghub.domain.task.TaskId;
import com.ryuqq.crawlinghub.domain.task.TaskType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * CrawlResult 비즈니스 로직 Manager
 *
 * <p>핵심 책임:
 * <ul>
 *   <li>크롤링 원본 결과를 JSON 문자열로 저장</li>
 *   <li>Task 실행 결과 추적 및 감사(Audit)</li>
 * </ul>
 *
 * <p>Transaction 관리:
 * - 각 메서드는 독립적인 Transaction
 * - 크롤링 결과 저장은 Task 처리와 독립적
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
@Component
public class CrawlResultManager {

    private static final Logger log = LoggerFactory.getLogger(CrawlResultManager.class);

    private final SaveCrawlResultPort saveCrawlResultPort;

    public CrawlResultManager(SaveCrawlResultPort saveCrawlResultPort) {
        this.saveCrawlResultPort = saveCrawlResultPort;
    }

    /**
     * 크롤링 결과 저장
     *
     * <p>Task 실행 결과를 JSON 문자열로 저장합니다.
     *
     * @param taskId Task ID
     * @param taskType Task 타입 (META, MINI_SHOP, PRODUCT_DETAIL, PRODUCT_OPTION)
     * @param sellerId Seller ID
     * @param rawData 크롤링 원본 JSON 데이터
     * @return 저장된 CrawlResult ID
     */
    @Transactional
    public Long saveCrawlResult(
        TaskId taskId,
        TaskType taskType,
        MustitSellerId sellerId,
        String rawData
    ) {
        log.info("크롤링 결과 저장 시작. taskId={}, taskType={}, sellerId={}",
            taskId.value(), taskType, sellerId.value());

        // 1. CrawlResult Domain 생성
        CrawlResult crawlResult = CrawlResult.create(
            taskId,
            taskType,
            sellerId,
            rawData,
            LocalDateTime.now()
        );

        // 2. 저장
        CrawlResult savedResult = saveCrawlResultPort.save(crawlResult);

        log.info("크롤링 결과 저장 완료. crawlResultId={}, taskId={}, dataSize={}",
            savedResult.getIdValue(), taskId.value(), rawData.length());

        return savedResult.getIdValue();
    }
}
