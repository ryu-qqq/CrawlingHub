package com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.command;

import jakarta.validation.constraints.Size;

/**
 * Update Seller API Request
 *
 * <p>셀러 수정 API 요청 DTO
 *
 * <p><strong>선택적 업데이트:</strong> null인 필드는 변경하지 않음
 *
 * @param mustItSellerName 머스트잇 셀러명 (선택적)
 * @param sellerName 커머스 셀러명 (선택적)
 * @param active 활성화 여부 (선택적, true=ACTIVE, false=INACTIVE)
 * @author development-team
 * @since 1.0.0
 */
public record UpdateSellerApiRequest(
        @Size(max = 100, message = "머스트잇 셀러명은 100자 이하여야 합니다") String mustItSellerName,
        @Size(max = 100, message = "셀러명은 100자 이하여야 합니다") String sellerName,
        Boolean active) {}
