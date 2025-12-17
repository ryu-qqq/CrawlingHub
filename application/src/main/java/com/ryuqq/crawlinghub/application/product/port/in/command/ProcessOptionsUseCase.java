package com.ryuqq.crawlinghub.application.product.port.in.command;

import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOption;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.util.List;
import java.util.Optional;

/**
 * OPTION 크롤링 결과 처리 UseCase
 *
 * <p>상품 옵션 정보(사이즈, 색상, 재고 등)를 처리합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface ProcessOptionsUseCase {

    /**
     * OPTION 크롤링 결과 처리
     *
     * <p>옵션 정보 업데이트 후 외부 서버 동기화 가능 여부를 확인합니다.
     *
     * @param sellerId 판매자 ID
     * @param itemNo 상품 번호
     * @param options 옵션 목록
     * @return 업데이트된 CrawledProduct (상품이 없으면 empty)
     */
    Optional<CrawledProduct> process(SellerId sellerId, long itemNo, List<ProductOption> options);
}
