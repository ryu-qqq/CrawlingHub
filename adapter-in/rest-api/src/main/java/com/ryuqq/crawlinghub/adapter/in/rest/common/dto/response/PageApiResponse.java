package com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * PageApiResponse - 페이지 조회 REST API 응답 DTO (Offset 기반)
 *
 * @param <T> 콘텐츠 타입
 * @author windsurf
 * @since 1.0.0
 */
@Schema(description = "Offset 기반 페이지네이션 응답")
public record PageApiResponse<T>(
        @Schema(description = "현재 페이지의 데이터 목록") List<T> content,
        @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0") int page,
        @Schema(description = "페이지 크기", example = "20") int size,
        @Schema(description = "전체 데이터 개수", example = "100") long totalElements,
        @Schema(description = "전체 페이지 수", example = "5") int totalPages,
        @Schema(description = "첫 페이지 여부") boolean first,
        @Schema(description = "마지막 페이지 여부") boolean last) {

    /** Compact Constructor - Defensive Copy */
    public PageApiResponse {
        content = List.copyOf(content);
    }

    /**
     * REST API PageApiResponse 생성
     *
     * @param content 데이터 목록
     * @param page 현재 페이지 번호
     * @param size 페이지 크기
     * @param totalElements 전체 데이터 개수
     * @param <T> 콘텐츠 타입
     * @return PageApiResponse (totalPages, first, last 자동 계산)
     */
    public static <T> PageApiResponse<T> of(
            List<T> content, int page, int size, long totalElements) {
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
        boolean first = page == 0;
        boolean last = page >= totalPages - 1 || totalPages == 0;
        return new PageApiResponse<>(content, page, size, totalElements, totalPages, first, last);
    }
}
