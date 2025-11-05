package com.ryuqq.crawlinghub.application.seller.port.in;

import com.ryuqq.crawlinghub.application.seller.dto.query.GetSellerQuery;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerDetailResponse;

/**
 * 셀러 상세 조회 UseCase
 *
 * <p>셀러 기본 정보와 통계 정보를 함께 조회합니다.
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public interface GetSellerDetailUseCase {

    /**
     * 셀러 상세 조회 (기존 메서드)
     *
     * @param query 조회할 셀러 ID
     * @return 셀러 상세 정보 (통계 포함)
     */
    SellerDetailResponse execute(GetSellerQuery query);

    /**
     * 셀러 상세 조회 (확장된 메서드) ⭐
     *
     * <p>셀러 기본 정보 + 상품 수 이력 + 스케줄 정보 + 스케줄 실행 이력
     *
     * @param sellerId 셀러 ID
     * @return 셀러 상세 정보 (확장된 정보 포함)
     */
    SellerDetailResponse getDetail(Long sellerId);
}
