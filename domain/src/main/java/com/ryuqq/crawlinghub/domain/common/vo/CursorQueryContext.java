package com.ryuqq.crawlinghub.domain.common.vo;

/**
 * CursorQueryContext - 정렬 + 커서 기반 페이징 조건을 묶은 공통 컨텍스트
 *
 * <p>CursorSliceCriteria에서 필수로 사용되는 조회 컨텍스트입니다. 정렬 키, 정렬 방향, 커서 기반 페이징 정보를 하나로 묶어 일관된 조회 조건을 강제합니다.
 *
 * <p><strong>설계 원칙:</strong>
 *
 * <ul>
 *   <li>CursorSliceCriteria는 반드시 CursorQueryContext를 포함해야 함 (ArchUnit 검증)
 *   <li>타입 안전한 정렬 키 (제네릭으로 BC별 SortKey 강제)
 *   <li>타입 안전한 커서 (제네릭으로 Long, String, Instant 등 지원)
 *   <li>기본값 적용으로 null 방어
 * </ul>
 *
 * <p><strong>사용 예시:</strong>
 *
 * <pre>{@code
 * // Criteria에서 사용
 * public record OrderSliceCriteria(
 *     Long memberId,
 *     DateRange dateRange,
 *     CursorQueryContext<OrderSortKey, Long> queryContext  // 필수!
 * ) {}
 *
 * // 생성
 * CursorQueryContext<OrderSortKey, Long> context = CursorQueryContext.of(
 *     OrderSortKey.ORDER_DATE,
 *     SortDirection.DESC,
 *     CursorPageRequest.afterId(lastId, 20)
 * );
 *
 * // 기본값 사용 (첫 페이지)
 * CursorQueryContext<OrderSortKey, Long> defaultContext =
 *         CursorQueryContext.defaultOf(OrderSortKey.ORDER_DATE);
 * }</pre>
 *
 * @param <K> 정렬 키 타입 (SortKey 구현체)
 * @param <C> 커서 타입 (Long, String, Instant 등)
 * @param sortKey 정렬 키
 * @param sortDirection 정렬 방향
 * @param cursorPageRequest 커서 기반 페이징 정보
 * @param includeDeleted 삭제된 항목 포함 여부
 * @author development-team
 * @since 1.0.0
 */
public record CursorQueryContext<K extends SortKey, C>(
        K sortKey,
        SortDirection sortDirection,
        CursorPageRequest<C> cursorPageRequest,
        boolean includeDeleted) {

    /** Compact Constructor - null 방어 */
    public CursorQueryContext {
        if (sortKey == null) {
            throw new IllegalArgumentException("sortKey must not be null");
        }
        if (sortDirection == null) {
            sortDirection = SortDirection.defaultDirection();
        }
        if (cursorPageRequest == null) {
            cursorPageRequest = CursorPageRequest.defaultPage();
        }
    }

    /**
     * CursorQueryContext 생성 (기본값 적용)
     *
     * @param sortKey 정렬 키 (필수)
     * @param sortDirection 정렬 방향 (null이면 DESC)
     * @param cursorPageRequest 커서 페이징 정보 (null이면 기본값)
     * @param <K> 정렬 키 타입
     * @param <C> 커서 타입
     * @return CursorQueryContext
     */
    public static <K extends SortKey, C> CursorQueryContext<K, C> of(
            K sortKey, SortDirection sortDirection, CursorPageRequest<C> cursorPageRequest) {
        return new CursorQueryContext<>(sortKey, sortDirection, cursorPageRequest, false);
    }

    /**
     * CursorQueryContext 생성 (includeDeleted 포함)
     *
     * @param sortKey 정렬 키 (필수)
     * @param sortDirection 정렬 방향 (null이면 DESC)
     * @param cursorPageRequest 커서 페이징 정보 (null이면 기본값)
     * @param includeDeleted 삭제된 항목 포함 여부
     * @param <K> 정렬 키 타입
     * @param <C> 커서 타입
     * @return CursorQueryContext
     */
    public static <K extends SortKey, C> CursorQueryContext<K, C> of(
            K sortKey,
            SortDirection sortDirection,
            CursorPageRequest<C> cursorPageRequest,
            boolean includeDeleted) {
        return new CursorQueryContext<>(sortKey, sortDirection, cursorPageRequest, includeDeleted);
    }

    /**
     * 기본 CursorQueryContext 생성 (첫 페이지, 기본 사이즈, 내림차순, 삭제 제외)
     *
     * @param sortKey 정렬 키 (필수)
     * @param <K> 정렬 키 타입
     * @param <C> 커서 타입
     * @return CursorQueryContext (기본 설정)
     */
    public static <K extends SortKey, C> CursorQueryContext<K, C> defaultOf(K sortKey) {
        return new CursorQueryContext<>(
                sortKey, SortDirection.defaultDirection(), CursorPageRequest.defaultPage(), false);
    }

    /**
     * 첫 페이지 CursorQueryContext 생성
     *
     * @param sortKey 정렬 키
     * @param sortDirection 정렬 방향
     * @param size 페이지 크기
     * @param <K> 정렬 키 타입
     * @param <C> 커서 타입
     * @return CursorQueryContext (첫 페이지)
     */
    public static <K extends SortKey, C> CursorQueryContext<K, C> firstPage(
            K sortKey, SortDirection sortDirection, int size) {
        return new CursorQueryContext<>(
                sortKey, sortDirection, CursorPageRequest.first(size), false);
    }

    /**
     * 다음 페이지로 이동한 새 CursorQueryContext 반환
     *
     * @param nextCursor 다음 커서 값
     * @return 다음 페이지 CursorQueryContext
     */
    public CursorQueryContext<K, C> nextPage(C nextCursor) {
        return new CursorQueryContext<>(
                sortKey, sortDirection, cursorPageRequest.next(nextCursor), includeDeleted);
    }

    /**
     * 정렬 방향을 반전한 새 CursorQueryContext 반환
     *
     * @return 정렬 반전된 CursorQueryContext
     */
    public CursorQueryContext<K, C> reverseSortDirection() {
        return new CursorQueryContext<>(
                sortKey, sortDirection.reverse(), cursorPageRequest, includeDeleted);
    }

    /**
     * 정렬 키를 변경한 새 CursorQueryContext 반환
     *
     * @param newSortKey 새 정렬 키
     * @return 정렬 키가 변경된 CursorQueryContext
     */
    public CursorQueryContext<K, C> withSortKey(K newSortKey) {
        return new CursorQueryContext<>(
                newSortKey, sortDirection, cursorPageRequest, includeDeleted);
    }

    /**
     * 페이지 크기를 변경한 새 CursorQueryContext 반환
     *
     * @param newSize 새 페이지 크기
     * @return 페이지 크기가 변경된 CursorQueryContext
     */
    public CursorQueryContext<K, C> withPageSize(int newSize) {
        return new CursorQueryContext<>(
                sortKey,
                sortDirection,
                CursorPageRequest.of(cursorPageRequest.cursor(), newSize),
                includeDeleted);
    }

    /**
     * 삭제된 항목 포함 여부를 변경한 새 CursorQueryContext 반환
     *
     * @param newIncludeDeleted 새 삭제 포함 여부
     * @return 삭제 포함 여부가 변경된 CursorQueryContext
     */
    public CursorQueryContext<K, C> withIncludeDeleted(boolean newIncludeDeleted) {
        return new CursorQueryContext<>(
                sortKey, sortDirection, cursorPageRequest, newIncludeDeleted);
    }

    /**
     * 첫 페이지인지 확인
     *
     * @return cursor가 null이면 true
     */
    public boolean isFirstPage() {
        return cursorPageRequest.isFirstPage();
    }

    /**
     * 커서가 있는지 확인
     *
     * @return cursor가 있으면 true
     */
    public boolean hasCursor() {
        return cursorPageRequest.hasCursor();
    }

    /**
     * 커서 값 반환
     *
     * @return 현재 커서 값
     */
    public C cursor() {
        return cursorPageRequest.cursor();
    }

    /**
     * 페이지 크기 반환
     *
     * @return size
     */
    public int size() {
        return cursorPageRequest.size();
    }

    /**
     * 실제 조회 크기 반환 (hasNext 판단용 +1)
     *
     * @return size + 1
     */
    public int fetchSize() {
        return cursorPageRequest.fetchSize();
    }

    /**
     * 오름차순인지 확인
     *
     * @return ASC이면 true
     */
    public boolean isAscending() {
        return sortDirection.isAscending();
    }
}
