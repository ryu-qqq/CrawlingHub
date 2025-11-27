package com.ryuqq.crawlinghub.application.seller.port.in.command;

/**
 * 셀러 상품 수 업데이트 UseCase
 *
 * <p>META 크롤링 결과에서 파싱된 총 상품 수를 셀러에 업데이트합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface UpdateSellerProductCountUseCase {

    /**
     * 셀러 상품 수 업데이트
     *
     * @param sellerId 셀러 ID
     * @param productCount 총 상품 수
     */
    void execute(Long sellerId, int productCount);
}
