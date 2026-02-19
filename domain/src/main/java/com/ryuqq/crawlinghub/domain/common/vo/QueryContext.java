package com.ryuqq.crawlinghub.domain.common.vo;

/**
 * QueryContext - 정렬 + 페이징 조건을 묶은 공통 컨텍스트
 *
 * <p>SearchCriteria에서 필수로 사용되는 조회 컨텍스트입니다.
 * 정렬 키, 정렬 방향, 페이징 정보를 하나로 묶어 일관된 조회 조건을 강제합니다.
 *
 * <p><strong>설계 원칙:</strong>
 *
 * <ul>
 *   <li>SearchCriteria는 반드시 QueryContext를 포함해야 함 (ArchUnit 검증)
 *   <li>타입 안전한 정렬 키 (제네릭으로 BC별 SortKey 강제)
 *   <li>기본값 적용으로 null 방어
 * </ul>
 *
 * <p><strong>사용 예시:</strong>
 *
 * <pre>{@code
 * // Criteria에서 사용
 * public record OrderSearchCriteria(
 *     Long memberId,
 *     DateRange dateRange,
 *     QueryContext<OrderSortKey> queryContext  // 필수!
 * ) {}
 *
 * // 생성
 * QueryContext<OrderSortKey> context = QueryContext.of(
 *     OrderSortKey.ORDER_DATE,
 *     SortDirection.DESC,
 *     PageRequest.first(20)
 * );
 *
 * // 기본값 사용
 * QueryContext<OrderSortKey> defaultContext = QueryContext.defaultOf(OrderSortKey.ORDER_DATE);
 * }</pre>
 *
 * @param <K> 정렬 키 타입 (SortKey 구현체)
 * @param sortKey 정렬 키
 * @param sortDirection 정렬 방향
 * @param pageRequest 페이징 정보
 * @param includeDeleted 삭제된 항목 포함 여부
 * @author development-team
 * @since 1.0.0
 */
public record QueryContext<K extends SortKey>(
        K sortKey,
        SortDirection sortDirection,
        PageRequest pageRequest,
        boolean includeDeleted) {

    /** Compact Constructor - null 방어 */
    public QueryContext {
        if (sortKey == null) {
            throw new IllegalArgumentException("sortKey must not be null");
        }
        if (sortDirection == null) {
            sortDirection = SortDirection.defaultDirection();
        }
        if (pageRequest == null) {
            pageRequest = PageRequest.defaultPage();
        }
    }

    /**
     * QueryContext 생성 (기본값 적용)
     *
     * @param sortKey 정렬 키 (필수)
     * @param sortDirection 정렬 방향 (null이면 DESC)
     * @param pageRequest 페이징 정보 (null이면 기본값)
     * @param <K> 정렬 키 타입
     * @return QueryContext
     */
    public static <K extends SortKey> QueryContext<K> of(
            K sortKey, SortDirection sortDirection, PageRequest pageRequest) {
        return new QueryContext<>(sortKey, sortDirection, pageRequest, false);
    }

    /**
     * QueryContext 생성 (includeDeleted 포함)
     *
     * @param sortKey 정렬 키 (필수)
     * @param sortDirection 정렬 방향 (null이면 DESC)
     * @param pageRequest 페이징 정보 (null이면 기본값)
     * @param includeDeleted 삭제된 항목 포함 여부
     * @param <K> 정렬 키 타입
     * @return QueryContext
     */
    public static <K extends SortKey> QueryContext<K> of(
            K sortKey, SortDirection sortDirection, PageRequest pageRequest, boolean includeDeleted) {
        return new QueryContext<>(sortKey, sortDirection, pageRequest, includeDeleted);
    }

    /**
     * 기본 QueryContext 생성 (첫 페이지, 기본 사이즈, 내림차순, 삭제 제외)
     *
     * @param sortKey 정렬 키 (필수)
     * @param <K> 정렬 키 타입
     * @return QueryContext (기본 설정)
     */
    public static <K extends SortKey> QueryContext<K> defaultOf(K sortKey) {
        return new QueryContext<>(sortKey, SortDirection.defaultDirection(), PageRequest.defaultPage(), false);
    }

    /**
     * 첫 페이지 QueryContext 생성
     *
     * @param sortKey 정렬 키
     * @param sortDirection 정렬 방향
     * @param size 페이지 크기
     * @param <K> 정렬 키 타입
     * @return QueryContext (첫 페이지)
     */
    public static <K extends SortKey> QueryContext<K> firstPage(
            K sortKey, SortDirection sortDirection, int size) {
        return new QueryContext<>(sortKey, sortDirection, PageRequest.first(size), false);
    }

    /**
     * 다음 페이지로 이동한 새 QueryContext 반환
     *
     * @return 다음 페이지 QueryContext
     */
    public QueryContext<K> nextPage() {
        return new QueryContext<>(sortKey, sortDirection, pageRequest.next(), includeDeleted);
    }

    /**
     * 이전 페이지로 이동한 새 QueryContext 반환
     *
     * @return 이전 페이지 QueryContext
     */
    public QueryContext<K> previousPage() {
        return new QueryContext<>(sortKey, sortDirection, pageRequest.previous(), includeDeleted);
    }

    /**
     * 정렬 방향을 반전한 새 QueryContext 반환
     *
     * @return 정렬 반전된 QueryContext
     */
    public QueryContext<K> reverseSortDirection() {
        return new QueryContext<>(sortKey, sortDirection.reverse(), pageRequest, includeDeleted);
    }

    /**
     * 정렬 키를 변경한 새 QueryContext 반환
     *
     * @param newSortKey 새 정렬 키
     * @return 정렬 키가 변경된 QueryContext
     */
    public QueryContext<K> withSortKey(K newSortKey) {
        return new QueryContext<>(newSortKey, sortDirection, pageRequest, includeDeleted);
    }

    /**
     * 페이지 크기를 변경한 새 QueryContext 반환
     *
     * @param newSize 새 페이지 크기
     * @return 페이지 크기가 변경된 QueryContext
     */
    public QueryContext<K> withPageSize(int newSize) {
        return new QueryContext<>(sortKey, sortDirection, PageRequest.of(pageRequest.page(), newSize), includeDeleted);
    }

    /**
     * 삭제된 항목 포함 여부를 변경한 새 QueryContext 반환
     *
     * @param newIncludeDeleted 새 삭제 포함 여부
     * @return 삭제 포함 여부가 변경된 QueryContext
     */
    public QueryContext<K> withIncludeDeleted(boolean newIncludeDeleted) {
        return new QueryContext<>(sortKey, sortDirection, pageRequest, newIncludeDeleted);
    }

    /**
     * SQL OFFSET 값 반환
     *
     * @return offset
     */
    public long offset() {
        return pageRequest.offset();
    }

    /**
     * 페이지 크기 반환
     *
     * @return size
     */
    public int size() {
        return pageRequest.size();
    }

    /**
     * 현재 페이지 번호 반환
     *
     * @return page (0-based)
     */
    public int page() {
        return pageRequest.page();
    }

    /**
     * 첫 페이지인지 확인
     *
     * @return 첫 페이지이면 true
     */
    public boolean isFirstPage() {
        return pageRequest.isFirst();
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
