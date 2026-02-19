package com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * SliceApiResponse - 슬라이스 조회 REST API 응답 DTO (Cursor 기반)
 *
 * @param <T> 콘텐츠 타입
 * @author windsurf
 * @since 1.0.0
 */
@Schema(description = "Cursor 기반 슬라이스 응답")
public record SliceApiResponse<T>(
        @Schema(description = "현재 슬라이스의 데이터 목록") List<T> content,
        @Schema(description = "슬라이스 크기", example = "20") int size,
        @Schema(description = "다음 슬라이스 존재 여부") boolean hasNext,
        @Schema(description = "다음 슬라이스 조회를 위한 커서") String nextCursor) {

    /** Compact Constructor - Defensive Copy */
    public SliceApiResponse {
        content = List.copyOf(content);
    }

    /**
     * REST API SliceApiResponse 생성
     *
     * @param content 데이터 목록
     * @param size 슬라이스 크기
     * @param hasNext 다음 슬라이스 존재 여부
     * @param nextCursor 다음 커서
     * @param <T> 콘텐츠 타입
     * @return SliceApiResponse
     */
    public static <T> SliceApiResponse<T> of(
            List<T> content, int size, boolean hasNext, String nextCursor) {
        return new SliceApiResponse<>(content, size, hasNext, nextCursor);
    }
}
