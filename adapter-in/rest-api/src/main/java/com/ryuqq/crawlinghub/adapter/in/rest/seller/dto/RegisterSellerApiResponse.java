package com.ryuqq.crawlinghub.adapter.in.rest.seller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * 셀러 등록 API Response DTO
 * <p>
 * REST API Layer의 불변 Response 객체입니다.
 * Java Record로 구현하여 불변성을 보장합니다.
 * </p>
 *
 * @param sellerId      셀러 ID
 * @param name          셀러명
 * @param isActive      활성 상태
 * @param intervalType  크롤링 주기 타입
 * @param intervalValue 크롤링 주기 값
 * @param createdAt     생성 시각
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
public record RegisterSellerApiResponse(
        @JsonProperty("sellerId")
        String sellerId,

        @JsonProperty("name")
        String name,

        @JsonProperty("isActive")
        boolean isActive,

        @JsonProperty("intervalType")
        String intervalType,

        @JsonProperty("intervalValue")
        int intervalValue,

        @JsonProperty("createdAt")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt
) {
}
