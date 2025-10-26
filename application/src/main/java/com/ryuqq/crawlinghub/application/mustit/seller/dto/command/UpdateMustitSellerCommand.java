package com.ryuqq.crawlinghub.application.mustit.seller.dto.command;

import com.ryuqq.crawlinghub.domain.mustit.seller.CrawlIntervalType;

/**
 * 머스트잇 셀러 수정 Command
 * <p>
 * UseCase의 입력 파라미터를 표현하는 DTO입니다.
 * Java 21의 Record 패턴을 활용하여 불변성을 보장합니다.
 * </p>
 *
 * @param sellerId 셀러 고유 ID (필수, 수정 불가)
 * @param isActive 활성 상태 (선택)
 * @param intervalType 크롤링 주기 타입 (선택)
 * @param intervalValue 크롤링 주기 값 (선택, 양수)
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
public record UpdateMustitSellerCommand(
        String sellerId,
        Boolean isActive,
        CrawlIntervalType intervalType,
        Integer intervalValue
) {

    /**
     * Compact Constructor로 입력 검증 수행
     *
     * @throws IllegalArgumentException sellerId가 null이거나 빈 문자열인 경우
     * @throws IllegalArgumentException intervalValue가 설정되었으나 0 이하인 경우
     * @throws IllegalArgumentException intervalType과 intervalValue 중 하나만 설정된 경우
     */
    public UpdateMustitSellerCommand {
        validateSellerId(sellerId);
        validateCrawlIntervalConsistency(intervalType, intervalValue);
        if (intervalValue != null) {
            validateIntervalValue(intervalValue);
        }
    }

    /**
     * sellerId 유효성 검증
     *
     * @param sellerIdValue 검증할 셀러 ID
     * @throws IllegalArgumentException sellerId가 null이거나 빈 문자열인 경우
     */
    private void validateSellerId(String sellerIdValue) {
        if (sellerIdValue == null || sellerIdValue.isBlank()) {
            throw new IllegalArgumentException("sellerId must not be null or blank");
        }
    }

    /**
     * 크롤링 주기 일관성 검증
     * intervalType과 intervalValue는 함께 설정되거나 둘 다 null이어야 함
     *
     * @param intervalTypeValue 크롤링 주기 타입
     * @param intervalValueParam 크롤링 주기 값
     * @throws IllegalArgumentException intervalType과 intervalValue 중 하나만 설정된 경우
     */
    private void validateCrawlIntervalConsistency(
            CrawlIntervalType intervalTypeValue,
            Integer intervalValueParam
    ) {
        boolean typePresent = intervalTypeValue != null;
        boolean valuePresent = intervalValueParam != null;

        if (typePresent != valuePresent) {
            throw new IllegalArgumentException(
                    "intervalType and intervalValue must be both present or both absent"
            );
        }
    }

    /**
     * intervalValue 유효성 검증
     *
     * @param intervalValueParam 검증할 주기 값
     * @throws IllegalArgumentException intervalValue가 0 이하인 경우
     */
    private void validateIntervalValue(int intervalValueParam) {
        if (intervalValueParam <= 0) {
            throw new IllegalArgumentException("intervalValue must be greater than 0");
        }
    }

    /**
     * 크롤링 주기 변경 여부를 확인합니다.
     *
     * @return 크롤링 주기가 설정된 경우 true
     */
    public boolean hasCrawlIntervalUpdate() {
        return intervalType != null && intervalValue != null;
    }

    /**
     * 활성 상태 변경 여부를 확인합니다.
     *
     * @return 활성 상태가 설정된 경우 true
     */
    public boolean hasActiveUpdate() {
        return isActive != null;
    }
}
