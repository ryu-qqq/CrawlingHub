package com.ryuqq.crawlinghub.application.crawl.orchestration.service;


import com.ryuqq.crawlinghub.application.crawl.orchestration.dto.command.InitiateCrawlingCommand;
import com.ryuqq.crawlinghub.application.crawl.orchestration.port.in.InitiateCrawlingUseCase;
import com.ryuqq.crawlinghub.application.crawl.orchestration.port.out.IdempotencyKeyGeneratorPort;
import com.ryuqq.crawlinghub.application.crawl.orchestration.port.out.OutboxPort;
import com.ryuqq.crawlinghub.application.crawl.orchestration.port.out.SaveCrawlTaskPort;
import com.ryuqq.crawlinghub.application.mustit.seller.port.out.LoadSellerPort;
import com.ryuqq.crawlinghub.domain.crawl.task.CrawlTask;
import com.ryuqq.crawlinghub.domain.crawl.task.RequestUrl;
import com.ryuqq.crawlinghub.domain.crawl.task.TaskType;
import com.ryuqq.crawlinghub.domain.mustit.seller.MustitSeller;
import com.ryuqq.crawlinghub.domain.mustit.seller.MustitSellerId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 크롤링 시작 UseCase 구현체
 *
 * <p>초기 미니샵 태스크(page=0, size=1)를 생성하고 Outbox에 저장합니다.
 * Outbox 패턴을 사용하여 트랜잭션 안전성을 보장합니다.
 *
 * <p>⚠️ Transaction 경계:
 * <ul>
 *   <li>트랜잭션 내부: DB 작업 (태스크 저장, Outbox 저장)</li>
 *   <li>트랜잭션 외부: 실제 SQS 발행 (Polling Worker가 담당)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
@Service
public class InitiateCrawlingService implements InitiateCrawlingUseCase {

    private final LoadSellerPort loadSellerPort;
    private final SaveCrawlTaskPort saveCrawlTaskPort;
    private final IdempotencyKeyGeneratorPort idempotencyKeyGenerator;
    private final OutboxPort outboxPort;

    public InitiateCrawlingService(
        LoadSellerPort loadSellerPort,
        SaveCrawlTaskPort saveCrawlTaskPort,
        IdempotencyKeyGeneratorPort idempotencyKeyGenerator,
        OutboxPort outboxPort
    ) {
        this.loadSellerPort = loadSellerPort;
        this.saveCrawlTaskPort = saveCrawlTaskPort;
        this.idempotencyKeyGenerator = idempotencyKeyGenerator;
        this.outboxPort = outboxPort;
    }

    /**
     * 크롤링 시작
     *
     * <p>실행 순서:
     * 1. 셀러 상태 확인 (ACTIVE만 크롤링 가능)
     * 2. 초기 미니샵 태스크 생성 (page=0, size=1)
     * 3. 태스크 저장
     * 4. Outbox 저장 (트랜잭션 내부)
     * 5. [트랜잭션 외부] SQS 발행 (Polling Worker가 담당)
     *
     * @param command 시작할 셀러 정보
     * @throws IllegalArgumentException 셀러를 찾을 수 없거나 비활성 상태인 경우
     */
    @Override
    @Transactional
    public void execute(InitiateCrawlingCommand command) {
        MustitSellerId sellerId = MustitSellerId.of(command.sellerId());

        // 1. 셀러 상태 확인
        MustitSeller seller = loadSellerPort.findById(sellerId)
            .orElseThrow(() -> new IllegalArgumentException(
                "셀러를 찾을 수 없습니다: " + command.sellerId()
            ));

        if (!seller.isActive()) {
            throw new IllegalStateException(
                "비활성 셀러는 크롤링할 수 없습니다: " + command.sellerId()
            );
        }

        // 2. 초기 미니샵 태스크 생성 (page=0, size=1)
        // 첫 요청으로 총 상품 수를 파악합니다
        CrawlTask initialTask = createInitialMiniShopTask(sellerId);

        // 3. 태스크 저장
        CrawlTask savedTask = saveCrawlTaskPort.save(initialTask);

        // 4. 태스크 발행
        savedTask.publish();
        saveCrawlTaskPort.save(savedTask);

        // 5. Outbox 저장 (트랜잭션 내부)
        // 실제 SQS 발행은 별도 Polling Worker가 처리합니다
        saveToOutbox(savedTask);
    }

    /**
     * 초기 미니샵 태스크 생성
     */
    private CrawlTask createInitialMiniShopTask(MustitSellerId sellerId) {
        // 미니샵 API URL: page=0, size=1 (총 상품 수 확인용)
        String url = String.format(
            "https://api.smartstore.naver.com/seller/%d/products?page=0&size=1",
            sellerId.value()
        );
        RequestUrl requestUrl = RequestUrl.of(url);

        // 멱등성 키 생성
        String idempotencyKey = idempotencyKeyGenerator.generate(
            sellerId.value(),
            TaskType.MINI_SHOP,
            0,
            null
        );

        return CrawlTask.forNew(
            sellerId,
            TaskType.MINI_SHOP,
            requestUrl,
            0,
            idempotencyKey,
            LocalDateTime.now()
        );
    }

    /**
     * Outbox에 메시지 저장 (트랜잭션 내부)
     */
    private void saveToOutbox(CrawlTask task) {
        String payload = String.format(
            "{\"taskId\":%d,\"sellerId\":%d,\"taskType\":\"%s\",\"url\":\"%s\",\"pageNumber\":%d}",
            task.getIdValue(),
            task.getSellerIdValue(),
            task.getTaskType().name(),
            task.getRequestUrlValue(),
            task.getPageNumber()
        );

        outboxPort.saveOutboxMessage(
            "CrawlTask",
            task.getIdValue(),
            "CrawlTaskCreated",
            payload
        );
    }
}
