package com.ryuqq.crawlinghub.application.product.port.in.command;

import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.vo.MiniShopItem;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;

/**
 * MINI_SHOP 크롤링 결과 처리 UseCase
 *
 * <p>MiniShop에서 수집한 상품 목록 정보를 처리합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface ProcessMiniShopItemUseCase {

    /**
     * MINI_SHOP 크롤링 결과 처리
     *
     * <p>기존 상품이 있으면 업데이트, 없으면 신규 생성합니다. 썸네일 이미지 업로드 요청도 함께 처리합니다.
     *
     * @param sellerId 판매자 ID
     * @param item MINI_SHOP 파싱 결과
     * @return 저장/업데이트된 CrawledProduct
     */
    CrawledProduct process(SellerId sellerId, MiniShopItem item);
}
