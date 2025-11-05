package com.ryuqq.crawlinghub.application.task.service;


import com.ryuqq.crawlinghub.application.task.assembler.command.MiniShopResultCommand;
import com.ryuqq.crawlinghub.application.task.port.in.ProcessMiniShopResultUseCase;
import com.ryuqq.crawlinghub.application.task.port.out.IdempotencyKeyGeneratorPort;
import com.ryuqq.crawlinghub.application.task.port.out.LoadCrawlTaskPort;
import com.ryuqq.crawlinghub.application.task.port.out.OutboxPort;
import com.ryuqq.crawlinghub.application.task.port.out.SaveCrawlTaskPort;
import com.ryuqq.crawlinghub.application.seller.port.out.LoadSellerPort;
import com.ryuqq.crawlinghub.application.seller.port.out.SaveSellerPort;
import com.ryuqq.crawlinghub.domain.task.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.CrawlTaskId;
import com.ryuqq.crawlinghub.domain.task.RequestUrl;
import com.ryuqq.crawlinghub.domain.task.TaskType;
import com.ryuqq.crawlinghub.domain.seller.MustitSeller;
import com.ryuqq.crawlinghub.domain.seller.MustitSellerId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 미니샵 크롤링 결과 처리 UseCase 구현체
 *
 * <p>총 상품 수를 추출하고 후속 태스크를 생성합니다:
 * <ul>
 *   <li>페이징된 미니샵 태스크 (totalCount / 500)</li>
 *   <li>상품별 상세 태스크</li>
 *   <li>상품별 옵션 태스크</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
@Service
public class ProcessMiniShopResultService implements ProcessMiniShopResultUseCase {

    private static final int PAGE_SIZE = 500;

    private final LoadCrawlTaskPort loadCrawlTaskPort;
    private final SaveCrawlTaskPort saveCrawlTaskPort;
    private final LoadSellerPort loadSellerPort;
    private final SaveSellerPort saveSellerPort;
    private final IdempotencyKeyGeneratorPort idempotencyKeyGenerator;
    private final OutboxPort outboxPort;
    private final com.ryuqq.crawlinghub.application.seller.assembler.SellerAssembler sellerAssembler;

    public ProcessMiniShopResultService(
        LoadCrawlTaskPort loadCrawlTaskPort,
        SaveCrawlTaskPort saveCrawlTaskPort,
        LoadSellerPort loadSellerPort,
        SaveSellerPort saveSellerPort,
        IdempotencyKeyGeneratorPort idempotencyKeyGenerator,
        OutboxPort outboxPort,
        com.ryuqq.crawlinghub.application.seller.assembler.SellerAssembler sellerAssembler
    ) {
        this.loadCrawlTaskPort = loadCrawlTaskPort;
        this.sellerAssembler = sellerAssembler;
        this.saveCrawlTaskPort = saveCrawlTaskPort;
        this.loadSellerPort = loadSellerPort;
        this.saveSellerPort = saveSellerPort;
        this.idempotencyKeyGenerator = idempotencyKeyGenerator;
        this.outboxPort = outboxPort;
    }

    /**
     * 미니샵 결과 처리
     *
     * <p>실행 순서:
     * 1. 현재 태스크 조회 및 완료 처리
     * 2. 총 상품 수 추출 (responseData에서 JSON 파싱)
     * 3. 페이지 계산 (totalCount / 500)
     * 4. 후속 미니샵 태스크 생성
     * 5. 상품별 상세/옵션 태스크 생성
     * 6. 셀러 상품 수 업데이트
     * 7. Outbox 저장
     *
     * @param command 미니샵 API 응답 데이터
     * @throws IllegalArgumentException 태스크를 찾을 수 없는 경우
     */
    @Override
    @Transactional
    public void execute(MiniShopResultCommand command) {
        CrawlTaskId taskId = CrawlTaskId.of(command.taskId());

        // 1. 현재 태스크 조회
        CrawlTask currentTask = loadCrawlTaskPort.findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException(
                "태스크를 찾을 수 없습니다: " + command.taskId()
            ));

        // 2. 현재 태스크 완료 처리
        currentTask.completeSuccessfully();
        saveCrawlTaskPort.save(currentTask);

        // 3. 총 상품 수 추출 (JSON 파싱)
        // TODO: JSON 파싱 유틸 구현 필요
        int totalProductCount = extractTotalCount(command.responseData());
        List<String> itemNos = extractItemNos(command.responseData());

        // 4. 페이지 계산 및 후속 미니샵 태스크 생성
        List<CrawlTask> subsequentTasks = createSubsequentMiniShopTasks(
            currentTask.getSellerIdValue(),
            totalProductCount
        );

        // 5. 상품별 상세/옵션 태스크 생성
        List<CrawlTask> productTasks = createProductTasks(
            currentTask.getSellerIdValue(),
            itemNos
        );

        // 6. 일괄 저장
        List<CrawlTask> allTasks = new ArrayList<>();
        allTasks.addAll(subsequentTasks);
        allTasks.addAll(productTasks);
        List<CrawlTask> savedTasks = saveCrawlTaskPort.saveAll(allTasks);

        // 7. 태스크 발행 및 Outbox 저장
        savedTasks.forEach(task -> {
            task.publish();
            saveCrawlTaskPort.save(task);
            saveToOutbox(task);
        });

        // 8. 셀러 상품 수 업데이트
        updateSellerProductCount(currentTask.getSellerIdValue(), totalProductCount);
    }

    /**
     * 총 상품 수 추출 (JSON 파싱)
     */
    private int extractTotalCount(String responseData) {
        // TODO: JSON 파싱 라이브러리 사용 (Jackson, Gson 등)
        // 예시: {"totalCount": 1234, "items": [...]}
        return 1000; // 임시값
    }

    /**
     * 상품 번호 목록 추출 (JSON 파싱)
     */
    private List<String> extractItemNos(String responseData) {
        // TODO: JSON 파싱 라이브러리 사용
        return List.of(); // 임시값
    }

    /**
     * 후속 미니샵 태스크 생성 (페이징)
     */
    private List<CrawlTask> createSubsequentMiniShopTasks(Long sellerId, int totalCount) {
        List<CrawlTask> tasks = new ArrayList<>();
        MustitSellerId sellerIdObj = MustitSellerId.of(sellerId);

        int totalPages = (int) Math.ceil((double) totalCount / PAGE_SIZE);

        for (int page = 1; page < totalPages; page++) {
            String url = String.format(
                "https://api.smartstore.naver.com/seller/%d/products?page=%d&size=%d",
                sellerId,
                page,
                PAGE_SIZE
            );

            String idempotencyKey = idempotencyKeyGenerator.generate(
                sellerId,
                TaskType.MINI_SHOP,
                page,
                null
            );

            CrawlTask task = CrawlTask.forNew(
                sellerIdObj,
                TaskType.MINI_SHOP,
                RequestUrl.of(url),
                page,
                idempotencyKey,
                LocalDateTime.now()
            );

            tasks.add(task);
        }

        return tasks;
    }

    /**
     * 상품별 상세/옵션 태스크 생성
     */
    private List<CrawlTask> createProductTasks(Long sellerId, List<String> itemNos) {
        List<CrawlTask> tasks = new ArrayList<>();
        MustitSellerId sellerIdObj = MustitSellerId.of(sellerId);

        for (String itemNo : itemNos) {
            // 상품 상세 태스크
            String detailUrl = String.format(
                "https://api.smartstore.naver.com/products/%s/detail",
                itemNo
            );

            String detailKey = idempotencyKeyGenerator.generate(
                sellerId,
                TaskType.PRODUCT_DETAIL,
                null,
                itemNo
            );

            CrawlTask detailTask = CrawlTask.forNew(
                sellerIdObj,
                TaskType.PRODUCT_DETAIL,
                RequestUrl.of(detailUrl),
                null,
                detailKey,
                LocalDateTime.now()
            );

            tasks.add(detailTask);

            // 상품 옵션 태스크
            String optionUrl = String.format(
                "https://api.smartstore.naver.com/products/%s/options",
                itemNo
            );

            String optionKey = idempotencyKeyGenerator.generate(
                sellerId,
                TaskType.PRODUCT_OPTION,
                null,
                itemNo
            );

            CrawlTask optionTask = CrawlTask.forNew(
                sellerIdObj,
                TaskType.PRODUCT_OPTION,
                RequestUrl.of(optionUrl),
                null,
                optionKey,
                LocalDateTime.now()
            );

            tasks.add(optionTask);
        }

        return tasks;
    }

    /**
     * 셀러 상품 수 업데이트
     */
    private void updateSellerProductCount(Long sellerId, int totalProductCount) {
        MustitSellerId sellerIdObj = MustitSellerId.of(sellerId);

        MustitSeller seller = loadSellerPort.findById(sellerIdObj)
            .map(sellerAssembler::toDomain)
            .orElseThrow(() -> new IllegalArgumentException(
                "셀러를 찾을 수 없습니다: " + sellerId
            ));

        seller.updateProductCount(totalProductCount);
        saveSellerPort.save(seller);
    }

    /**
     * Outbox에 메시지 저장
     */
    private void saveToOutbox(CrawlTask task) {
        String payload = String.format(
            "{\"taskId\":%d,\"sellerId\":%d,\"taskType\":\"%s\",\"url\":\"%s\"}",
            task.getIdValue(),
            task.getSellerIdValue(),
            task.getTaskType().name(),
            task.getRequestUrlValue()
        );

        outboxPort.saveOutboxMessage(
            "CrawlTask",
            task.getIdValue(),
            "CrawlTaskCreated",
            payload
        );
    }
}
