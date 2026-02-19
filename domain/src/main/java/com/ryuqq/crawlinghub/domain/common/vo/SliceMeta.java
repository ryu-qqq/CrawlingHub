package com.ryuqq.crawlinghub.domain.common.vo;

/**
 * SliceMeta - 슬라이스/커서 기반 페이지네이션 메타 정보
 *
 * <p>슬라이스 조회 결과의 메타 정보를 담는 불변 객체입니다. PageMeta와 달리 전체 개수를 조회하지 않아 성능상 이점이 있습니다. 무한 스크롤, 더보기 UI에
 * 적합합니다.
 *
 * <p><strong>사용 예시:</strong>
 *
 * <pre>{@code
 * // Repository에서 생성
 * List<Order> content = orderRepository.findNextOrders(cursor, size + 1);
 * boolean hasNext = content.size() > size;
 * if (hasNext) {
 *     content = content.subList(0, size);
 * }
 * String nextCursor = hasNext ? content.get(content.size() - 1).id().value().toString() : null;
 * SliceMeta meta = SliceMeta.withCursor(nextCursor, size, hasNext);
 * }</pre>
 *
 * <p><strong>PageMeta vs SliceMeta:</strong>
 *
 * <ul>
 *   <li>PageMeta: 전체 개수(totalElements) 포함, 페이지 번호 UI에 적합
 *   <li>SliceMeta: 전체 개수 없음, 무한 스크롤/더보기에 적합 (성능 이점)
 * </ul>
 *
 * @param size 페이지 크기
 * @param hasNext 다음 슬라이스 존재 여부
 * @param cursor 다음 페이지 조회용 커서 (nullable)
 * @param count 현재 슬라이스 항목 수
 * @author development-team
 * @since 1.0.0
 */
public record SliceMeta(int size, boolean hasNext, String cursor, int count) {

    /** 기본 슬라이스 크기 */
    public static final int DEFAULT_SIZE = 20;

    /** Compact Constructor - 값 정규화 */
    public SliceMeta {
        if (size <= 0) {
            size = DEFAULT_SIZE;
        }
        if (count < 0) {
            count = 0;
        }
    }

    /**
     * SliceMeta 생성 (커서 없음)
     *
     * @param size 페이지 크기
     * @param hasNext 다음 페이지 존재 여부
     * @return SliceMeta
     */
    public static SliceMeta of(int size, boolean hasNext) {
        return new SliceMeta(size, hasNext, null, 0);
    }

    /**
     * SliceMeta 생성 (커서 없음, count 포함)
     *
     * @param size 페이지 크기
     * @param hasNext 다음 페이지 존재 여부
     * @param count 현재 슬라이스 항목 수
     * @return SliceMeta
     */
    public static SliceMeta of(int size, boolean hasNext, int count) {
        return new SliceMeta(size, hasNext, null, count);
    }

    /**
     * SliceMeta 생성 (String 커서 포함)
     *
     * @param cursor 다음 페이지 조회용 커서
     * @param size 페이지 크기
     * @param hasNext 다음 페이지 존재 여부
     * @return SliceMeta
     */
    public static SliceMeta withCursor(String cursor, int size, boolean hasNext) {
        return new SliceMeta(size, hasNext, cursor, 0);
    }

    /**
     * SliceMeta 생성 (String 커서 포함, count 포함)
     *
     * @param cursor 다음 페이지 조회용 커서
     * @param size 페이지 크기
     * @param hasNext 다음 페이지 존재 여부
     * @param count 현재 슬라이스 항목 수
     * @return SliceMeta
     */
    public static SliceMeta withCursor(String cursor, int size, boolean hasNext, int count) {
        return new SliceMeta(size, hasNext, cursor, count);
    }

    /**
     * SliceMeta 생성 (Long ID 커서 포함)
     *
     * @param cursorId 다음 페이지 조회용 커서 (Long ID)
     * @param size 페이지 크기
     * @param hasNext 다음 페이지 존재 여부
     * @return SliceMeta
     */
    public static SliceMeta withCursor(Long cursorId, int size, boolean hasNext) {
        String cursor = cursorId != null ? cursorId.toString() : null;
        return new SliceMeta(size, hasNext, cursor, 0);
    }

    /**
     * SliceMeta 생성 (Long ID 커서 포함, count 포함)
     *
     * @param cursorId 다음 페이지 조회용 커서 (Long ID)
     * @param size 페이지 크기
     * @param hasNext 다음 페이지 존재 여부
     * @param count 현재 슬라이스 항목 수
     * @return SliceMeta
     */
    public static SliceMeta withCursor(Long cursorId, int size, boolean hasNext, int count) {
        String cursor = cursorId != null ? cursorId.toString() : null;
        return new SliceMeta(size, hasNext, cursor, count);
    }

    /**
     * 빈 결과용 SliceMeta 생성
     *
     * @return 빈 SliceMeta (hasNext=false, cursor=null, count=0)
     */
    public static SliceMeta empty() {
        return new SliceMeta(DEFAULT_SIZE, false, null, 0);
    }

    /**
     * 빈 결과용 SliceMeta 생성 (크기 지정)
     *
     * @param size 페이지 크기
     * @return 빈 SliceMeta
     */
    public static SliceMeta empty(int size) {
        return new SliceMeta(size, false, null, 0);
    }

    /**
     * 커서가 있는지 확인
     *
     * @return 커서가 있으면 true
     */
    public boolean hasCursor() {
        return cursor != null && !cursor.isEmpty();
    }

    /**
     * 마지막 슬라이스인지 확인
     *
     * @return 다음 슬라이스가 없으면 true
     */
    public boolean isLast() {
        return !hasNext;
    }

    /**
     * 결과가 비어있는지 확인
     *
     * @return 항목이 없으면 true
     */
    public boolean isEmpty() {
        return count == 0;
    }

    /**
     * 커서를 Long으로 변환
     *
     * @return Long 타입의 커서 (없거나 변환 불가시 null)
     */
    public Long cursorAsLong() {
        if (!hasCursor()) {
            return null;
        }
        try {
            return Long.parseLong(cursor);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 다음 SliceMeta 생성 (새로운 커서와 hasNext로)
     *
     * @param nextCursor 다음 커서
     * @param nextHasNext 다음 페이지 존재 여부
     * @return 새로운 SliceMeta
     */
    public SliceMeta next(String nextCursor, boolean nextHasNext) {
        return new SliceMeta(this.size, nextHasNext, nextCursor, 0);
    }

    /**
     * 다음 SliceMeta 생성 (Long 커서)
     *
     * @param nextCursorId 다음 커서 ID
     * @param nextHasNext 다음 페이지 존재 여부
     * @return 새로운 SliceMeta
     */
    public SliceMeta next(Long nextCursorId, boolean nextHasNext) {
        String nextCursor = nextCursorId != null ? nextCursorId.toString() : null;
        return new SliceMeta(this.size, nextHasNext, nextCursor, 0);
    }
}
