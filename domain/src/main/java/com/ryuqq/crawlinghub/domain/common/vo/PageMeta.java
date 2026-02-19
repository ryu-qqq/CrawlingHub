package com.ryuqq.crawlinghub.domain.common.vo;

/**
 * PageMeta - 오프셋 기반 페이지네이션 메타 정보
 *
 * <p>페이지 조회 결과의 메타 정보를 담는 불변 객체입니다. Application Layer와 REST API Layer에서 공통으로 사용됩니다.
 *
 * <p><strong>사용 예시:</strong>
 *
 * <pre>{@code
 * // Repository에서 생성
 * PageMeta meta = PageMeta.of(0, 20, 150);
 *
 * // Application Response에서 사용
 * public record OrderListResponse(
 *     List<OrderDto> content,
 *     PageMeta pageMeta
 * ) {}
 * }</pre>
 *
 * @param page 현재 페이지 번호 (0-based)
 * @param size 페이지 크기
 * @param totalElements 전체 요소 수
 * @param totalPages 전체 페이지 수
 * @author development-team
 * @since 1.0.0
 */
public record PageMeta(int page, int size, long totalElements, int totalPages) {

    /** 기본 페이지 크기 */
    public static final int DEFAULT_SIZE = 20;

    /** Compact Constructor - 값 정규화 */
    public PageMeta {
        if (page < 0) {
            page = 0;
        }
        if (size <= 0) {
            size = DEFAULT_SIZE;
        }
        if (totalElements < 0) {
            totalElements = 0;
        }
        if (totalPages < 0) {
            totalPages = 0;
        }
    }

    /**
     * PageMeta 생성 (totalPages 자동 계산)
     *
     * @param page 현재 페이지 번호 (0-based)
     * @param size 페이지 크기
     * @param totalElements 전체 요소 수
     * @return PageMeta
     */
    public static PageMeta of(int page, int size, long totalElements) {
        int effectiveSize = size <= 0 ? DEFAULT_SIZE : size;
        int totalPages = (int) Math.ceil((double) totalElements / effectiveSize);
        return new PageMeta(page, effectiveSize, totalElements, totalPages);
    }

    /**
     * PageMeta 생성 (모든 값 직접 지정)
     *
     * @param page 현재 페이지 번호 (0-based)
     * @param size 페이지 크기
     * @param totalElements 전체 요소 수
     * @param totalPages 전체 페이지 수
     * @return PageMeta
     */
    public static PageMeta of(int page, int size, long totalElements, int totalPages) {
        return new PageMeta(page, size, totalElements, totalPages);
    }

    /**
     * 빈 결과용 PageMeta 생성
     *
     * @param size 페이지 크기
     * @return 빈 PageMeta (page=0, totalElements=0, totalPages=0)
     */
    public static PageMeta empty(int size) {
        return new PageMeta(0, size, 0, 0);
    }

    /**
     * 빈 결과용 PageMeta 생성 (기본 크기)
     *
     * @return 빈 PageMeta
     */
    public static PageMeta empty() {
        return empty(DEFAULT_SIZE);
    }

    /**
     * 다음 페이지가 있는지 확인
     *
     * @return 다음 페이지가 있으면 true
     */
    public boolean hasNext() {
        return page < totalPages - 1;
    }

    /**
     * 이전 페이지가 있는지 확인
     *
     * @return 이전 페이지가 있으면 true
     */
    public boolean hasPrevious() {
        return page > 0;
    }

    /**
     * 첫 페이지인지 확인
     *
     * @return 첫 페이지이면 true
     */
    public boolean isFirst() {
        return page == 0;
    }

    /**
     * 마지막 페이지인지 확인
     *
     * @return 마지막 페이지이면 true
     */
    public boolean isLast() {
        return !hasNext();
    }

    /**
     * 결과가 비어있는지 확인
     *
     * @return 전체 요소가 0이면 true
     */
    public boolean isEmpty() {
        return totalElements == 0;
    }

    /**
     * 현재 페이지의 요소 시작 번호 (1-based, UI 표시용)
     *
     * @return 시작 번호 (예: 21번째 요소부터)
     */
    public long startElement() {
        if (isEmpty()) {
            return 0;
        }
        return (long) page * size + 1;
    }

    /**
     * 현재 페이지의 요소 끝 번호 (1-based, UI 표시용)
     *
     * @return 끝 번호 (예: 40번째 요소까지)
     */
    public long endElement() {
        if (isEmpty()) {
            return 0;
        }
        return Math.min((long) (page + 1) * size, totalElements);
    }

    /**
     * SQL OFFSET 값 반환
     *
     * @return offset
     */
    public long offset() {
        return (long) page * size;
    }
}
