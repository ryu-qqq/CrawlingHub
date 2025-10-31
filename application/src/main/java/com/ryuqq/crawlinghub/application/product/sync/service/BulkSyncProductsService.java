package com.ryuqq.crawlinghub.application.product.sync.service;


import com.ryuqq.crawlinghub.application.product.sync.dto.command.BulkSyncCommand;
import com.ryuqq.crawlinghub.application.product.sync.dto.command.SyncProductCommand;
import com.ryuqq.crawlinghub.application.product.sync.dto.response.BulkSyncResponse;
import com.ryuqq.crawlinghub.application.product.sync.dto.response.SyncResultResponse;
import com.ryuqq.crawlinghub.application.product.sync.port.in.BulkSyncProductsUseCase;
import com.ryuqq.crawlinghub.application.product.sync.port.in.SyncProductToInternalUseCase;
import com.ryuqq.crawlinghub.application.product.sync.port.out.LoadProductPort;
import com.ryuqq.crawlinghub.domain.mustit.seller.MustitSellerId;
import com.ryuqq.crawlinghub.domain.product.CrawledProduct;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 대량 상품 동기화 UseCase 구현체
 *
 * <p>⚠️ Transaction 경계:
 * <ul>
 *   <li>조회는 읽기 전용 트랜잭션</li>
 *   <li>각 상품 동기화는 SyncProductToInternalUseCase에 위임 (외부 API 호출)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
@Service
public class BulkSyncProductsService implements BulkSyncProductsUseCase {

    private final LoadProductPort loadProductPort;
    private final SyncProductToInternalUseCase syncProductToInternalUseCase;

    public BulkSyncProductsService(
        LoadProductPort loadProductPort,
        SyncProductToInternalUseCase syncProductToInternalUseCase
    ) {
        this.loadProductPort = loadProductPort;
        this.syncProductToInternalUseCase = syncProductToInternalUseCase;
    }

    /**
     * 대량 동기화 실행
     *
     * <p>⚠️ 트랜잭션 없음 - 외부 API 호출이 메인 로직
     *
     * <p>실행 순서:
     * 1. 조건에 맞는 상품 목록 조회
     * 2. 배치 단위로 동기화 실행
     * 3. 각 상품별로 SyncProductToInternalUseCase 호출
     * 4. 성공/실패 집계
     *
     * @param command 대량 동기화 Command
     * @return 대량 동기화 결과
     */
    @Override
    public BulkSyncResponse execute(BulkSyncCommand command) {
        LocalDateTime startedAt = LocalDateTime.now();

        // 1. 조건에 맞는 상품 목록 조회
        List<CrawledProduct> products = fetchProductsByCondition(command);

        // 2. 동기화 결과 집계 변수
        int totalCount = products.size();
        int successCount = 0;
        int failureCount = 0;
        List<Long> failedProducts = new ArrayList<>();

        // 3. 각 상품별 동기화 실행
        for (CrawledProduct product : products) {
            SyncProductCommand syncCommand = SyncProductCommand.forceSync(product.getIdValue());
            SyncResultResponse result = syncProductToInternalUseCase.execute(syncCommand);

            if (result.success()) {
                successCount++;
            } else {
                failureCount++;
                failedProducts.add(product.getIdValue());
            }
        }

        LocalDateTime completedAt = LocalDateTime.now();

        // 4. 결과 반환
        return new BulkSyncResponse(
            totalCount,
            successCount,
            failureCount,
            startedAt,
            completedAt,
            failedProducts
        );
    }

    /**
     * 조건에 맞는 상품 목록 조회
     */
    private List<CrawledProduct> fetchProductsByCondition(BulkSyncCommand command) {
        // 셀러 필터 + 시간 필터
        if (command.hasSellerFilter() && command.hasTimeFilter()) {
            MustitSellerId sellerId = MustitSellerId.of(command.sellerId());
            return loadProductPort.findChangedProductsBySellerAfter(
                sellerId,
                command.changedAfter(),
                command.batchSize()
            );
        }

        // 시간 필터만
        if (command.hasTimeFilter()) {
            return loadProductPort.findChangedProductsAfter(
                command.changedAfter(),
                command.batchSize()
            );
        }

        // 셀러 필터만
        if (command.hasSellerFilter()) {
            MustitSellerId sellerId = MustitSellerId.of(command.sellerId());
            List<CrawledProduct> allProducts = loadProductPort.findCompletedProductsBySellerId(sellerId);
            // 배치 크기만큼 제한
            return allProducts.stream()
                .limit(command.batchSize())
                .toList();
        }

        // 필터 없음: 전체 조회는 위험하므로 빈 리스트 반환
        throw new IllegalArgumentException(
            "대량 동기화는 최소한 셀러 또는 시간 필터가 필요합니다"
        );
    }
}
