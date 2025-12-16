package com.ryuqq.crawlinghub.adapter.in.rest.product.dto.query;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * SearchCrawledProducts API Request
 *
 * <p>크롤링 상품 목록 조회 API 요청 DTO
 *
 * <p><strong>Validation 규칙:</strong>
 *
 * <ul>
 *   <li>sellerId: 선택, 양수
 *   <li>itemNo: 선택, 양수
 *   <li>itemName: 선택, 부분 일치 검색
 *   <li>brandName: 선택, 부분 일치 검색
 *   <li>needsSync: 선택, 동기화 필요 여부
 *   <li>allCrawled: 선택, 모든 크롤링 완료 여부
 *   <li>hasExternalId: 선택, 외부 상품 ID 존재 여부
 *   <li>page: 최소 0 (기본값: 0)
 *   <li>size: 1-100 (기본값: 20)
 * </ul>
 *
 * @param sellerId 셀러 ID 필터 (선택)
 * @param itemNo 상품 번호 필터 (선택)
 * @param itemName 상품명 부분 일치 검색 (선택)
 * @param brandName 브랜드명 부분 일치 검색 (선택)
 * @param needsSync 동기화 필요 여부 필터 (선택)
 * @param allCrawled 모든 크롤링 완료 여부 필터 (선택)
 * @param hasExternalId 외부 상품 ID 존재 여부 필터 (선택)
 * @param page 페이지 번호 (0부터 시작, 기본값: 0)
 * @param size 페이지 크기 (기본값: 20, 최대: 100)
 * @author development-team
 * @since 1.0.0
 */
public record SearchCrawledProductsApiRequest(
        @Min(value = 1, message = "셀러 ID는 1 이상이어야 합니다") Long sellerId,
        @Min(value = 1, message = "상품 번호는 1 이상이어야 합니다") Long itemNo,
        String itemName,
        String brandName,
        Boolean needsSync,
        Boolean allCrawled,
        Boolean hasExternalId,
        @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다") Integer page,
        @Min(value = 1, message = "페이지 크기는 최소 1이어야 합니다")
                @Max(value = 100, message = "페이지 크기는 최대 100까지 허용됩니다")
                Integer size) {

    /** 기본값 적용 생성자 */
    public SearchCrawledProductsApiRequest {
        if (page == null) {
            page = 0;
        }
        if (size == null) {
            size = 20;
        }
    }
}
