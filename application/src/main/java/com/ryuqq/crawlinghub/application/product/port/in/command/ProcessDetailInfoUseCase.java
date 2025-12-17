package com.ryuqq.crawlinghub.application.product.port.in.command;

import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.vo.ProductDetailInfo;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.util.Optional;

/**
 * DETAIL 크롤링 결과 처리 UseCase
 *
 * <p>상품 상세 페이지에서 수집한 정보를 처리합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface ProcessDetailInfoUseCase {

    /**
     * DETAIL 크롤링 결과 처리
     *
     * <p>상세 정보 업데이트 및 상세 이미지 업로드 요청을 처리합니다. 외부 서버 동기화 가능 여부도 확인합니다.
     *
     * @param sellerId 판매자 ID
     * @param itemNo 상품 번호
     * @param detailInfo DETAIL 파싱 결과
     * @return 업데이트된 CrawledProduct (상품이 없으면 empty)
     */
    Optional<CrawledProduct> process(SellerId sellerId, long itemNo, ProductDetailInfo detailInfo);
}
