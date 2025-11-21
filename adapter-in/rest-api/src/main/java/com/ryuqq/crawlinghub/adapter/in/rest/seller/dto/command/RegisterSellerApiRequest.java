package com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Register Seller API Request
 *
 * <p>셀러 등록 API 요청 DTO
 *
 * <p><strong>Validation 규칙:</strong>
 *
 * <ul>
 *   <li>mustItSellerName: 필수, 1-100자
 *   <li>sellerName: 필수, 1-100자
 * </ul>
 *
 * @param mustItSellerName 머스트잇 셀러 이름 (MustIt 시스템에 등록된 이름)
 * @param sellerName 셀러 이름 (자사 커머스에 등록된 이름)
 * @author development-team
 * @since 1.0.0
 */
public record RegisterSellerApiRequest(
        @NotBlank(message = "머스트잇 셀러명은 필수입니다")
                @Size(min = 1, max = 100, message = "머스트잇 셀러명은 1-100자여야 합니다")
                String mustItSellerName,
        @NotBlank(message = "셀러명은 필수입니다") @Size(min = 1, max = 100, message = "셀러명은 1-100자여야 합니다")
                String sellerName) {}
