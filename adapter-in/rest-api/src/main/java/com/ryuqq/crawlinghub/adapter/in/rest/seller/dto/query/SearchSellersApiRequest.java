package com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.query;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

/**
 * List Sellers API Request
 *
 * <p>셀러 목록 조회 API 요청 DTO
 *
 * <p><strong>Validation 규칙:</strong>
 *
 * <ul>
 *   <li>status: 선택, "ACTIVE" 또는 "INACTIVE"만 허용
 *   <li>page: 최소 0 (기본값: 0)
 *   <li>size: 1-100 (기본값: 20)
 * </ul>
 *
 * @param status 셀러 상태 필터 (선택)
 * @param page 페이지 번호 (0부터 시작, 기본값: 0)
 * @param size 페이지 크기 (기본값: 20, 최대: 100)
 * @author development-team
 * @since 1.0.0
 */
public record SearchSellersApiRequest(
        @Pattern(regexp = "^(ACTIVE|INACTIVE)?$", message = "상태값은 ACTIVE 또는 INACTIVE만 허용됩니다")
                String status,
        @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다") Integer page,
        @Min(value = 1, message = "페이지 크기는 최소 1이어야 합니다")
                @Max(value = 100, message = "페이지 크기는 최대 100까지 허용됩니다")
                Integer size) {

    /** 기본값 적용 생성자 */
    public SearchSellersApiRequest {
        if (page == null) {
            page = 0;
        }
        if (size == null) {
            size = 20;
        }
    }
}
