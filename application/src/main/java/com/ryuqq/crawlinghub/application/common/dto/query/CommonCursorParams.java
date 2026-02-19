package com.ryuqq.crawlinghub.application.common.dto.query;

/**
 * 커서 기반 페이징 공통 파라미터
 *
 * <p>API 경계에서 커서는 String으로 통일합니다. 도메인별 실제 타입 변환은 Factory에서 수행합니다.
 *
 * @param cursor 커서 값 (nullable, null이면 첫 페이지)
 * @param size 페이지 크기
 */
public record CommonCursorParams(String cursor, Integer size) {

    private static final Integer DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    /** Compact Constructor - null/범위 방어 */
    public CommonCursorParams {
        if (cursor != null && cursor.isBlank()) {
            cursor = null;
        }
        if (size == null || size <= 0) {
            size = DEFAULT_SIZE;
        } else if (size > MAX_SIZE) {
            size = MAX_SIZE;
        }
    }

    public static CommonCursorParams of(String cursor, Integer size) {
        return new CommonCursorParams(cursor, size);
    }

    public static CommonCursorParams first(int size) {
        return new CommonCursorParams(null, size);
    }

    public static CommonCursorParams defaultPage() {
        return new CommonCursorParams(null, DEFAULT_SIZE);
    }

    public boolean isFirstPage() {
        return cursor == null;
    }

    public boolean hasCursor() {
        return cursor != null;
    }
}
