package com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.query;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

/**
 * Search CrawlSchedulers API Request
 *
 * <p>크롤 스케줄러 목록 조회 API 요청 DTO
 *
 * <p><strong>Query Parameters:</strong>
 *
 * <ul>
 *   <li>sellerId: 셀러 ID 필터 (선택적)
 *   <li>status: 상태 필터 (선택적, ACTIVE/INACTIVE)
 *   <li>page: 페이지 번호 (선택적, 기본값: 0)
 *   <li>size: 페이지 크기 (선택적, 기본값: 20, 최대: 100)
 * </ul>
 *
 * @param sellerId 셀러 ID 필터 (선택적)
 * @param status 상태 필터 (선택적)
 * @param page 페이지 번호
 * @param size 페이지 크기
 * @author development-team
 * @since 1.0.0
 */
public record SearchCrawlSchedulersApiRequest(
        @Positive(message = "셀러 ID는 양수여야 합니다") Long sellerId,
        String status,
        @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다") Integer page,
        @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
                @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다")
                Integer size) {

    /**
     * 기본값 적용 생성자
     *
     * @param sellerId 셀러 ID
     * @param status 상태
     * @param page 페이지 번호 (null이면 0)
     * @param size 페이지 크기 (null이면 20)
     */
    public SearchCrawlSchedulersApiRequest {
        if (page == null) {
            page = 0;
        }
        if (size == null) {
            size = 20;
        }
    }
}
