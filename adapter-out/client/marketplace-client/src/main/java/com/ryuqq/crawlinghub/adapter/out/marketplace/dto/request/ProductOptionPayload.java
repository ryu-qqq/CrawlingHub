package com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request;

/**
 * 상품 옵션 페이로드
 *
 * <p>옵션/재고 등록/수정 API에서 공통으로 사용하는 독립 객체
 *
 * @param optionNo 옵션 번호
 * @param color 색상
 * @param size 사이즈
 * @param stock 재고 수량
 * @param sizeGuide 사이즈 가이드
 * @author development-team
 * @since 1.0.0
 */
public record ProductOptionPayload(
        long optionNo, String color, String size, int stock, String sizeGuide) {}
