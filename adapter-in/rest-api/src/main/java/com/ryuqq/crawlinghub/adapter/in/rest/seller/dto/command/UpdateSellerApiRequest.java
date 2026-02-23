package com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Update Seller API Request
 *
 * <p>셀러 수정 API 요청 DTO (모든 필드 필수)
 *
 * @param mustItSellerName 머스트잇 셀러명
 * @param sellerName 커머스 셀러명
 * @param active 활성화 여부 (true=ACTIVE, false=INACTIVE)
 * @author development-team
 * @since 1.0.0
 */
public record UpdateSellerApiRequest(
        @NotBlank(message = "머스트잇 셀러명은 필수입니다")
                @Size(min = 1, max = 100, message = "머스트잇 셀러명은 1~100자 이내여야 합니다")
                String mustItSellerName,
        @NotBlank(message = "셀러명은 필수입니다")
                @Size(min = 1, max = 100, message = "셀러명은 1~100자 이내여야 합니다")
                String sellerName,
        @NotNull(message = "활성화 여부는 필수입니다") Boolean active) {}
