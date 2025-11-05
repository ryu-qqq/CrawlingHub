package com.ryuqq.crawlinghub.adapter.in.rest.common.dto;

import java.util.List;

/**
 * PageApiResponse - REST API 페이징 응답 공통 DTO
 *
 * <p><strong>Application PageResponse → REST API 변환 ⭐</strong></p>
 * <ul>
 *   <li>Application Layer PageResponse를 REST API로 변환</li>
 *   <li>JSON 응답 포맷 제공</li>
 * </ul>
 *
 * @param <T> 콘텐츠 타입
 * @param content 데이터 리스트
 * @param page 페이지 번호
 * @param size 페이지 크기
 * @param totalElements 전체 개수
 * @param totalPages 전체 페이지 수
 * @param first 첫 페이지 여부
 * @param last 마지막 페이지 여부
 * @author ryu-qqq
 * @since 2025-11-05
 */
public record PageApiResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last
) {

    /**
     * Static Factory Method
     *
     * @param content 데이터 리스트
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param totalElements 전체 개수
     * @param totalPages 전체 페이지 수
     * @param first 첫 페이지 여부
     * @param last 마지막 페이지 여부
     * @param <T> 콘텐츠 타입
     * @return PageApiResponse
     */
    public static <T> PageApiResponse<T> of(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
    ) {
        return new PageApiResponse<>(content, page, size, totalElements, totalPages, first, last);
    }
}

