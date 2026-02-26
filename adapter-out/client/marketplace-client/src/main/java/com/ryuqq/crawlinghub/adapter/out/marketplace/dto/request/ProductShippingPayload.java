package com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request;

/**
 * 배송 정보 페이로드
 *
 * <p>상품 등록/수정 API에서 공통으로 사용하는 독립 객체
 *
 * @param shippingType 배송 타입 (DOMESTIC, INTERNATIONAL)
 * @param shippingFee 배송비
 * @param shippingFeeType 배송비 타입 (FREE, PAID)
 * @param averageDeliveryDays 평균 배송 소요일
 * @param freeShipping 무료 배송 여부
 * @author development-team
 * @since 1.0.0
 */
public record ProductShippingPayload(
        String shippingType,
        int shippingFee,
        String shippingFeeType,
        int averageDeliveryDays,
        boolean freeShipping) {}
