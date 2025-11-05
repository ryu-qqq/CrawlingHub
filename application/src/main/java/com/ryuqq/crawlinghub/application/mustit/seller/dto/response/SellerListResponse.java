package com.ryuqq.crawlinghub.application.mustit.seller.dto.response;

import java.util.List;

/**
 * 셀러 목록 응답 DTO
 *
 * @param sellers       셀러 목록
 * @param totalElements 전체 셀러 수
 * @param totalPages    전체 페이지 수
 * @param currentPage   현재 페이지 번호
 * @param pageSize      페이지 크기
 * @author ryu-qqq
 * @since 2025-11-02
 */
public record SellerListResponse(
    List<SellerResponse> sellers,
    long totalElements,
    int totalPages,
    int currentPage,
    int pageSize
) {
    /**
     * 빈 응답 생성
     */
    public static SellerListResponse empty(int page, int size) {
        return new SellerListResponse(
            List.of(),
            0L,
            0,
            page,
            size
        );
    }

    /**
     * 셀러 목록으로 응답 생성
     */
    public static SellerListResponse of(
        List<SellerResponse> sellers,
        long totalElements,
        int page,
        int size
    ) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        return new SellerListResponse(
            sellers,
            totalElements,
            totalPages,
            page,
            size
        );
    }
}
