package com.ryuqq.crawlinghub.application.crawl.parser;

import com.ryuqq.crawlinghub.domain.product.vo.MiniShopItem;
import java.util.List;

/**
 * Search API 파싱 결과 VO
 *
 * <p>SEARCH 크롤링 응답 파싱 결과를 담는 VO입니다.
 *
 * <p><strong>무한스크롤 종료 조건</strong>:
 *
 * <ul>
 *   <li>moduleList가 빈 배열 AND nextApiUrl이 없음
 *   <li>이 경우 {@link #shouldStopPagination()}가 true 반환
 * </ul>
 *
 * @param items 파싱된 상품 목록 (MiniShopItem으로 변환된)
 * @param nextApiUrl 다음 페이지 API URL (없으면 null)
 * @author development-team
 * @since 1.0.0
 */
public record SearchParseResult(List<MiniShopItem> items, String nextApiUrl) {

    public SearchParseResult {
        if (items == null) {
            items = List.of();
        } else {
            items = List.copyOf(items);
        }
    }

    /**
     * 빈 결과 생성
     *
     * @return 빈 상품 목록과 nextApiUrl이 null인 결과
     */
    public static SearchParseResult empty() {
        return new SearchParseResult(List.of(), null);
    }

    /**
     * 상품 목록이 비어있는지 확인
     *
     * @return 상품이 없으면 true
     */
    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }

    /**
     * 다음 페이지가 있는지 확인
     *
     * @return nextApiUrl이 있으면 true
     */
    public boolean hasNextPage() {
        return nextApiUrl != null && !nextApiUrl.isBlank();
    }

    /**
     * 무한스크롤 페이지네이션 중단 여부 확인
     *
     * <p><strong>종료 조건</strong>: moduleList 비어있음 AND nextApiUrl 없음
     *
     * @return 페이지네이션을 중단해야 하면 true
     */
    public boolean shouldStopPagination() {
        return isEmpty() && !hasNextPage();
    }

    /**
     * 상품 개수 반환
     *
     * @return 파싱된 상품 개수
     */
    public int size() {
        return items != null ? items.size() : 0;
    }
}
