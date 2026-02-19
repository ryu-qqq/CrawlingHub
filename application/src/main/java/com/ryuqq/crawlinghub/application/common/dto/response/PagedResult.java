package com.ryuqq.crawlinghub.application.common.dto.response;

import com.ryuqq.crawlinghub.domain.common.vo.PageMeta;
import java.util.List;

/**
 * 페이징 결과 응답 래퍼
 *
 * @param <T> 콘텐츠 타입
 * @param content 결과 목록
 * @param pageMeta 페이징 메타 정보
 */
public record PagedResult<T>(List<T> content, PageMeta pageMeta) {

    public static <T> PagedResult<T> of(List<T> content, PageMeta pageMeta) {
        return new PagedResult<>(content, pageMeta);
    }

    public static <T> PagedResult<T> of(List<T> content, int page, int size, long totalElements) {
        return new PagedResult<>(content, PageMeta.of(page, size, totalElements));
    }

    public static <T> PagedResult<T> empty(int size) {
        return new PagedResult<>(List.of(), PageMeta.empty(size));
    }

    public boolean isEmpty() {
        return content == null || content.isEmpty();
    }

    public int size() {
        return content != null ? content.size() : 0;
    }
}
