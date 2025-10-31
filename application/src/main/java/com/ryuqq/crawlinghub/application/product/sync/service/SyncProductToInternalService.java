package com.ryuqq.crawlinghub.application.product.sync.service;


import com.ryuqq.crawlinghub.application.product.sync.assembler.ProductSyncAssembler;
import com.ryuqq.crawlinghub.application.product.sync.dto.command.SyncProductCommand;
import com.ryuqq.crawlinghub.application.product.sync.dto.response.SyncResultResponse;
import com.ryuqq.crawlinghub.application.product.sync.port.in.SyncProductToInternalUseCase;
import com.ryuqq.crawlinghub.application.product.sync.port.out.InternalProductApiPort;
import com.ryuqq.crawlinghub.application.product.sync.port.out.LoadProductPort;
import com.ryuqq.crawlinghub.domain.product.CompletionStatus;
import com.ryuqq.crawlinghub.domain.product.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.ProductId;

import java.time.LocalDateTime;

/**
 * 상품 내부 동기화 UseCase 구현체
 *
 * <p>⚠️ Transaction 경계:
 * <ul>
 *   <li>외부 API 호출이 메인 로직이므로 트랜잭션 사용 안 함</li>
 *   <li>조회는 읽기 전용 트랜잭션으로 실행</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
@Service
public class SyncProductToInternalService implements SyncProductToInternalUseCase {

    private final LoadProductPort loadProductPort;
    private final InternalProductApiPort internalProductApiPort;

    public SyncProductToInternalService(
        LoadProductPort loadProductPort,
        InternalProductApiPort internalProductApiPort
    ) {
        this.loadProductPort = loadProductPort;
        this.internalProductApiPort = internalProductApiPort;
    }

    /**
     * 상품 내부 동기화
     *
     * <p>⚠️ 트랜잭션 없음 - 외부 API 호출이 메인 로직
     *
     * <p>실행 순서:
     * 1. 상품 조회 (읽기 전용)
     * 2. 완료 상태 검증
     * 3. 강제 동기화 아니면 변경 여부 확인
     * 4. 내부 API Payload 생성
     * 5. 외부 API 호출 (트랜잭션 밖)
     *
     * @param command 동기화 Command
     * @return 동기화 결과
     */
    @Override
    public SyncResultResponse execute(SyncProductCommand command) {
        ProductId productId = ProductId.of(command.productId());

        // 1. 상품 조회
        CrawledProduct product = loadProductPort.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException(
                "상품을 찾을 수 없습니다: " + command.productId()
            ));

        // 2. 완료 상태 검증
        if (!product.hasStatus(CompletionStatus.COMPLETE)) {
            return SyncResultResponse.failure(
                command.productId(),
                "완료되지 않은 상품은 동기화할 수 없습니다"
            );
        }

        // 3. 강제 동기화가 아니면 변경 여부 확인
        if (!command.forceSync() && product.getDataHashValue() == null) {
            return SyncResultResponse.failure(
                command.productId(),
                "해시값이 없는 상품은 변경 감지 후 동기화해야 합니다"
            );
        }

        try {
            // 4. 내부 API Payload 생성
            String payload = ProductSyncAssembler.toInternalApiPayload(product);

            // 5. 외부 API 호출 (트랜잭션 밖)
            InternalProductApiPort.SyncResult result = internalProductApiPort.syncProduct(payload);

            // 6. 결과 반환
            if (result.isSuccess()) {
                return SyncResultResponse.success(command.productId(), LocalDateTime.now());
            } else {
                return SyncResultResponse.failure(
                    command.productId(),
                    result.message()
                );
            }

        } catch (Exception e) {
            return SyncResultResponse.failure(
                command.productId(),
                "내부 API 호출 실패: " + e.getMessage()
            );
        }
    }
}
