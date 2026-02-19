package com.ryuqq.crawlinghub.domain.common.vo;

/**
 * CursorPageRequest - 커서 기반 페이징 Value Object (제네릭)
 *
 * <p>전체 개수를 세지 않고 다음 페이지 존재 여부만 확인하는 효율적인 페이징 방식입니다.
 *
 * <p><strong>사용 시나리오:</strong>
 *
 * <ul>
 *   <li>무한 스크롤 UI
 *   <li>대량의 데이터에서 COUNT 쿼리 부담이 큰 경우
 *   <li>실시간 피드, 타임라인 등
 *   <li>모바일 앱 목록 조회
 * </ul>
 *
 * <p><strong>커서 전략:</strong>
 *
 * <ul>
 *   <li>ID 기반: 마지막 항목의 ID를 커서로 사용 (Long, String 등)
 *   <li>복합 커서: ID + 정렬키를 인코딩하여 사용
 *   <li>타임스탬프: 마지막 항목의 시간을 커서로 사용 (Instant, LocalDateTime 등)
 * </ul>
 *
 * <p><strong>사용 예시:</strong>
 *
 * <pre>{@code
 * // ID 기반 커서 (Long)
 * CursorPageRequest<Long> firstPage = CursorPageRequest.first(20);
 * CursorPageRequest<Long> nextPage = CursorPageRequest.afterId(lastId, 20);
 *
 * // String 기반 커서
 * CursorPageRequest<String> stringCursor = CursorPageRequest.of("cursor-xyz", 20);
 *
 * // Criteria에서 사용
 * TechStackSliceCriteria criteria =
 *         new TechStackSliceCriteria(CursorPageRequest.afterId(lastId, 20));
 * }</pre>
 *
 * @param <C> 커서 타입 (Long, String, Instant 등)
 * @param cursor 커서 값 (마지막 항목의 식별자, null이면 첫 페이지)
 * @param size 페이지 크기
 * @author development-team
 * @since 1.0.0
 */
public record CursorPageRequest<C>(C cursor, int size) {

    /** 기본 페이지 크기 */
    public static final int DEFAULT_SIZE = 20;

    /** 최대 페이지 크기 */
    public static final int MAX_SIZE = 100;

    /** Compact Constructor - 유효성 검증 및 정규화 */
    public CursorPageRequest {
        if (size <= 0) {
            size = DEFAULT_SIZE;
        }
        if (size > MAX_SIZE) {
            size = MAX_SIZE;
        }
        // String 타입인 경우 빈 문자열을 null로 정규화
        if (cursor instanceof String str && str.isBlank()) {
            cursor = null;
        }
    }

    /**
     * CursorPageRequest 생성
     *
     * @param <C> 커서 타입
     * @param cursor 커서 값
     * @param size 페이지 크기
     * @return CursorPageRequest
     */
    public static <C> CursorPageRequest<C> of(C cursor, int size) {
        return new CursorPageRequest<>(cursor, size);
    }

    /**
     * 첫 페이지 요청 생성
     *
     * @param <C> 커서 타입
     * @param size 페이지 크기
     * @return CursorPageRequest (cursor=null)
     */
    public static <C> CursorPageRequest<C> first(int size) {
        return new CursorPageRequest<>(null, size);
    }

    /**
     * 기본 설정 CursorPageRequest 생성
     *
     * @param <C> 커서 타입
     * @return CursorPageRequest (cursor=null, size=DEFAULT_SIZE)
     */
    public static <C> CursorPageRequest<C> defaultPage() {
        return new CursorPageRequest<>(null, DEFAULT_SIZE);
    }

    /**
     * ID 기반 커서 요청 생성 (Long)
     *
     * @param lastId 마지막 항목의 ID
     * @param size 페이지 크기
     * @return CursorPageRequest<Long>
     */
    public static CursorPageRequest<Long> afterId(Long lastId, int size) {
        return new CursorPageRequest<>(lastId, size);
    }

    /**
     * String 기반 커서 요청 생성
     *
     * @param cursor 커서 값 (String)
     * @param size 페이지 크기
     * @return CursorPageRequest<String>
     */
    public static CursorPageRequest<String> ofString(String cursor, int size) {
        return new CursorPageRequest<>(cursor, size);
    }

    /**
     * 첫 페이지 요청인지 확인
     *
     * @return cursor가 null이면 true
     */
    public boolean isFirstPage() {
        return cursor == null;
    }

    /**
     * 커서가 있는지 확인
     *
     * @return cursor가 있으면 true
     */
    public boolean hasCursor() {
        return cursor != null;
    }

    /**
     * 다음 페이지 요청 생성
     *
     * @param nextCursor 다음 커서 값
     * @return 다음 페이지 CursorPageRequest
     */
    public CursorPageRequest<C> next(C nextCursor) {
        return new CursorPageRequest<>(nextCursor, size);
    }

    /**
     * 실제 조회 크기 반환 (hasNext 판단용 +1)
     *
     * <p>LIMIT+1 전략: size+1개를 조회하여 다음 페이지 존재 여부 판단
     *
     * @return size + 1
     */
    public int fetchSize() {
        return size + 1;
    }
}
