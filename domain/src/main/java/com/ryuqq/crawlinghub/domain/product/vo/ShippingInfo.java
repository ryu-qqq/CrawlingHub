package com.ryuqq.crawlinghub.domain.product.vo;

/**
 * 배송 정보 VO
 *
 * <p>DETAIL 크롤링의 ShippingModule에서 추출한 배송 정보
 *
 * @param shippingType 배송 타입 (DOMESTIC, INTERNATIONAL 등)
 * @param shippingFee 배송비 (원)
 * @param shippingFeeType 배송비 타입 (FREE, PAID 등)
 * @param averageDeliveryDays 평균 배송 소요일
 * @param freeShipping 무료 배송 여부
 * @author development-team
 * @since 1.0.0
 */
public record ShippingInfo(
        String shippingType,
        int shippingFee,
        String shippingFeeType,
        int averageDeliveryDays,
        boolean freeShipping) {

    public ShippingInfo {
        if (shippingType == null || shippingType.isBlank()) {
            shippingType = "DOMESTIC";
        }
        if (shippingFee < 0) {
            throw new IllegalArgumentException("shippingFee는 0 이상이어야 합니다.");
        }
        if (shippingFeeType == null || shippingFeeType.isBlank()) {
            shippingFeeType = "PAID";
        }
        if (averageDeliveryDays < 0) {
            averageDeliveryDays = 0;
        }
    }

    /**
     * 무료 배송 팩토리 메서드
     */
    public static ShippingInfo freeShipping(String shippingType, int averageDeliveryDays) {
        return new ShippingInfo(shippingType, 0, "FREE", averageDeliveryDays, true);
    }

    /**
     * 유료 배송 팩토리 메서드
     */
    public static ShippingInfo paidShipping(String shippingType, int shippingFee, int averageDeliveryDays) {
        return new ShippingInfo(shippingType, shippingFee, "PAID", averageDeliveryDays, false);
    }

    /**
     * ShippingModule JSON에서 생성
     *
     * @param shippingType 배송 타입
     * @param shippingFee 배송비
     * @param shippingFeeType 배송비 타입 (FREE, PAID 등)
     * @param averageDeliveryDayText 평균 배송일 텍스트 ("평균 배송 1일 소요" 형식)
     * @return ShippingInfo
     */
    public static ShippingInfo fromShippingModule(
            String shippingType,
            int shippingFee,
            String shippingFeeType,
            String averageDeliveryDayText) {
        int deliveryDays = parseDeliveryDays(averageDeliveryDayText);
        boolean isFree = "FREE".equalsIgnoreCase(shippingFeeType) || shippingFee == 0;
        return new ShippingInfo(shippingType, shippingFee, shippingFeeType, deliveryDays, isFree);
    }

    /**
     * 국내 배송인지 확인
     */
    public boolean isDomestic() {
        return "DOMESTIC".equalsIgnoreCase(shippingType);
    }

    /**
     * 해외 배송인지 확인
     */
    public boolean isInternational() {
        return "INTERNATIONAL".equalsIgnoreCase(shippingType);
    }

    /**
     * 배송비 변경 여부 확인
     *
     * @param other 비교 대상
     * @return 변경이 있으면 true
     */
    public boolean hasChanges(ShippingInfo other) {
        if (other == null) {
            return true;
        }
        return this.shippingFee != other.shippingFee
                || this.freeShipping != other.freeShipping
                || !this.shippingType.equals(other.shippingType);
    }

    /**
     * 평균 배송일 텍스트에서 일수 추출
     *
     * <p>"평균 배송 1일 소요" → 1
     *
     * @param text 배송일 텍스트
     * @return 배송 소요일
     */
    private static int parseDeliveryDays(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        // "평균 배송 1일 소요" 또는 "1일 소요" 형식에서 숫자 추출
        String digitsOnly = text.replaceAll("[^0-9]", "");
        if (digitsOnly.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(digitsOnly);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
