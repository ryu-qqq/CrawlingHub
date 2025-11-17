package com.ryuqq.crawlinghub.domain.product.vo;

/**
 * ProductOutbox 이벤트 타입 Enum
 *
 * <p>ProductOutbox에서 발행하는 이벤트 종류를 정의합니다.</p>
 *
 * <p>이벤트 타입:</p>
 * <ul>
 *   <li>{@link #PRODUCT_CREATED} - 상품 생성 이벤트</li>
 *   <li>{@link #PRODUCT_UPDATED} - 상품 업데이트 이벤트</li>
 * </ul>
 */
public enum OutboxEventType {

    /**
     * 상품 생성 이벤트
     */
    PRODUCT_CREATED,

    /**
     * 상품 업데이트 이벤트
     */
    PRODUCT_UPDATED;

    /**
     * String 값으로부터 OutboxEventType 생성 (표준 패턴)
     *
     * @param value 문자열 값
     * @return OutboxEventType enum
     * @throws IllegalArgumentException value가 null이거나 유효하지 않은 경우
     */
    public static OutboxEventType of(String value) {
        if (value == null) {
            throw new IllegalArgumentException("OutboxEventType cannot be null");
        }
        return valueOf(value.toUpperCase());
    }
}
