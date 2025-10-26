package com.ryuqq.crawlinghub.adapter.in.rest.seller;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 셀러 등록 API Request DTO
 * <p>
 * REST API Layer의 불변 Request 객체입니다.
 * Java Record로 구현하여 불변성을 보장합니다.
 * </p>
 *
 * @param sellerId      셀러 ID (머스트잇 고유 식별자, 필수)
 * @param name          셀러명 (필수)
 * @param intervalType  크롤링 주기 타입 (HOURLY, DAILY, WEEKLY, 필수)
 * @param intervalValue 크롤링 주기 값 (양수, 필수)
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
public record RegisterSellerApiRequest(
        @JsonProperty("sellerId")
        @NotBlank(message = "sellerId는 필수입니다")
        String sellerId,

        @JsonProperty("name")
        @NotBlank(message = "name은 필수입니다")
        String name,

        @JsonProperty("intervalType")
        @NotBlank(message = "intervalType은 필수입니다")
        String intervalType,

        @JsonProperty("intervalValue")
        @NotNull(message = "intervalValue는 필수입니다")
        @Positive(message = "intervalValue는 양수여야 합니다")
        Integer intervalValue
) {
    /**
     * Compact Constructor - 추가 검증 및 null 방어
     */
    public RegisterSellerApiRequest {
        if (sellerId != null) {
            sellerId = sellerId.trim();
        }
        if (name != null) {
            name = name.trim();
        }
        if (intervalType != null) {
            intervalType = intervalType.trim().toUpperCase();
        }
    }
}
