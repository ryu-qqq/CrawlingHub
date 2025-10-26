package com.ryuqq.crawlinghub.application.mustit.seller.dto.command;

import com.ryuqq.crawlinghub.domain.mustit.seller.CrawlIntervalType;

/**
 * 머스트잇 셀러 등록 Command
 * <p>
 * UseCase의 입력 파라미터를 표현하는 DTO입니다.
 * Java 21의 Record 패턴을 활용하여 불변성을 보장합니다.
 * </p>
 *
 * @param sellerId 셀러 고유 ID (필수)
 * @param name 셀러명 (필수)
 * @param intervalType 크롤링 주기 타입 (필수)
 * @param intervalValue 크롤링 주기 값 (양수, 필수)
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
public record RegisterMustitSellerCommand(
        String sellerId,
        String name,
        CrawlIntervalType intervalType,
        int intervalValue
) {

    /**
     * Compact Constructor로 입력 검증 수행
     *
     * @throws IllegalArgumentException sellerId 또는 name이 null이거나 빈 문자열인 경우
     * @throws IllegalArgumentException intervalValue가 0 이하인 경우
     * @throws NullPointerException intervalType이 null인 경우
     */
    public RegisterMustitSellerCommand {
        validateSellerId(sellerId);
        validateName(name);
        validateIntervalType(intervalType);
        validateIntervalValue(intervalValue);
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
     * name 유효성 검증
     *
     * @param nameValue 검증할 셀러명
     * @throws IllegalArgumentException name이 null이거나 빈 문자열인 경우
     */
    private void validateName(String nameValue) {
        if (nameValue == null || nameValue.isBlank()) {
            throw new IllegalArgumentException("name must not be null or blank");
        }
    }

    /**
     * intervalType 유효성 검증
     *
     * @param intervalTypeValue 검증할 주기 타입
     * @throws NullPointerException intervalType이 null인 경우
     */
    private void validateIntervalType(CrawlIntervalType intervalTypeValue) {
        if (intervalTypeValue == null) {
            throw new NullPointerException("intervalType must not be null");
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
}
