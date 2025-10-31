package com.ryuqq.crawlinghub.application.product.sync.service;


import com.ryuqq.crawlinghub.application.product.sync.assembler.ProductSyncAssembler;
import com.ryuqq.crawlinghub.application.product.sync.dto.command.DetectChangeCommand;
import com.ryuqq.crawlinghub.application.product.sync.dto.response.ChangeDetectionResponse;
import com.ryuqq.crawlinghub.application.product.sync.port.in.DetectProductChangeUseCase;
import com.ryuqq.crawlinghub.application.product.sync.port.out.LoadProductPort;
import com.ryuqq.crawlinghub.application.product.sync.port.out.ProductHashCalculatorPort;
import com.ryuqq.crawlinghub.application.product.sync.port.out.SaveProductPort;
import com.ryuqq.crawlinghub.domain.product.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.DataHash;
import com.ryuqq.crawlinghub.domain.product.ProductId;
import org.springframework.transaction.annotation.Transactional;

/**
 * 상품 변경 감지 UseCase 구현체
 *
 * <p>크롤링된 상품 데이터가 변경되었는지 감지하고 버전을 관리합니다.
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
@Service
public class DetectProductChangeService implements DetectProductChangeUseCase {

    private final LoadProductPort loadProductPort;
    private final SaveProductPort saveProductPort;
    private final ProductHashCalculatorPort hashCalculatorPort;

    public DetectProductChangeService(
        LoadProductPort loadProductPort,
        SaveProductPort saveProductPort,
        ProductHashCalculatorPort hashCalculatorPort
    ) {
        this.loadProductPort = loadProductPort;
        this.saveProductPort = saveProductPort;
        this.hashCalculatorPort = hashCalculatorPort;
    }

    /**
     * 상품 변경 감지
     *
     * <p>실행 순서:
     * 1. 상품 조회
     * 2. 현재 해시 계산
     * 3. 이전 해시와 비교
     * 4. 변경 시: 해시 업데이트 + 버전 증가
     *
     * @param command 변경 감지 Command
     * @return 변경 감지 결과
     * @throws IllegalArgumentException 상품을 찾을 수 없는 경우
     */
    @Override
    @Transactional
    public ChangeDetectionResponse execute(DetectChangeCommand command) {
        ProductId productId = ProductId.of(command.productId());

        // 1. 상품 조회
        CrawledProduct product = loadProductPort.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException(
                "상품을 찾을 수 없습니다: " + command.productId()
            ));

        // 2. 상품이 완료 상태가 아니면 감지 불가
        if (!product.isComplete()) {
            throw new IllegalStateException(
                "완료되지 않은 상품은 변경 감지를 할 수 없습니다: " + command.productId()
            );
        }

        // 3. 현재 해시 계산
        DataHash currentHash = hashCalculatorPort.calculateHash(
            product.getMiniShopDataValue(),
            product.getDetailDataValue(),
            product.getOptionDataValue()
        );

        // 4. 변경 여부 확인
        boolean hasChanged = product.hasDataChanged(currentHash);

        DataHash previousHash = null;
        if (product.getDataHashValue() != null) {
            previousHash = DataHash.of(product.getDataHashValue());
        }

        // 5. 변경된 경우: 해시 업데이트 + 버전 증가
        if (hasChanged) {
            product.updateDataHash(currentHash);
            product.incrementVersion();
            saveProductPort.save(product);
        }

        // 6. 응답 생성
        return ProductSyncAssembler.toChangeDetectionResponse(
            product,
            previousHash,
            currentHash,
            hasChanged
        );
    }
}
