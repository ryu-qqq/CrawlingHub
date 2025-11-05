package com.ryuqq.crawlinghub.adapter.in.rest.seller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Positive;

import java.util.Locale;

/**
 * 셀러 수정 API Request DTO
 * <p>
 * REST API Layer의 불변 Request 객체입니다.
 * Java Record로 구현하여 불변성을 보장합니다.
 * </p>
 * <p>
 * 모든 필드는 선택(Optional)입니다.
 * isActive만 변경하거나, intervalType과 intervalValue만 변경하거나,
 * 둘 다 변경할 수 있습니다.
 * </p>
 *
 * @param isActive      활성 상태 (선택)
 * @param intervalType  크롤링 주기 타입 (선택, intervalValue와 함께 있거나 둘 다 없어야 함)
 * @param intervalValue 크롤링 주기 값 (선택, 양수, intervalType과 함께 있거나 둘 다 없어야 함)
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
public record UpdateSellerApiRequest(
        @JsonProperty("isActive")
        Boolean isActive,

        @JsonProperty("intervalType")
        String intervalType,

        @JsonProperty("intervalValue")
        @Positive(message = "intervalValue는 양수여야 합니다")
        Integer intervalValue
) {
    /**
     * Compact Constructor - 추가 검증 및 데이터 정규화
     */
    public UpdateSellerApiRequest {
        if (intervalType != null) {
            intervalType = intervalType.trim().toUpperCase(Locale.ROOT);
            // intervalType 유효성 검증
            validateIntervalType(intervalType);
        }

        // intervalType과 intervalValue 일관성 검증
        validateCrawlIntervalConsistency(intervalType, intervalValue);
    }

    /**
     * 크롤링 주기 일관성 검증
     * intervalType과 intervalValue는 함께 설정되거나 둘 다 null이어야 함
     *
     * @param intervalTypeValue 크롤링 주기 타입
     * @param intervalValueParam 크롤링 주기 값
     * @throws IllegalArgumentException intervalType과 intervalValue 중 하나만 설정된 경우
     */
    private void validateCrawlIntervalConsistency(String intervalTypeValue, Integer intervalValueParam) {
        boolean typePresent = intervalTypeValue != null && !intervalTypeValue.isBlank();
        boolean valuePresent = intervalValueParam != null;

        if (typePresent != valuePresent) {
            throw new IllegalArgumentException(
                    "intervalType and intervalValue must be both present or both absent"
            );
        }
    }

    /**
     * intervalType 유효성 검증
     *
     * @param intervalTypeValue 검증할 intervalType 값
     * @throws IllegalArgumentException intervalType이 유효하지 않은 경우
     */
    private void validateIntervalType(String intervalTypeValue) {
        try {
            // TODO: CrawlIntervalType이 현재 구조에 없음, 기본 검증만 수행
            if (!intervalTypeValue.matches("HOURLY|DAILY|WEEKLY")) {
                throw new IllegalArgumentException("Invalid intervalType");
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "intervalType은 HOURLY, DAILY, WEEKLY 중 하나여야 합니다. 입력값: " + intervalTypeValue
            );
        }
    }

    /**
     * 어떤 필드라도 설정되었는지 확인합니다.
     *
     * @return 하나 이상의 필드가 설정된 경우 true
     */
    public boolean hasAnyUpdate() {
        return isActive != null
                || (intervalType != null && intervalValue != null);
    }
}
