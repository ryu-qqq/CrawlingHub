package com.ryuqq.crawlinghub.application.seller.dto.response;

import com.ryuqq.crawlinghub.domain.common.vo.PageMeta;
import java.util.List;

/**
 * 셀러 페이지 조회 결과 (PageMeta 기반)
 *
 * @param results 셀러 결과 목록
 * @param pageMeta 페이지 메타 정보
 * @author development-team
 * @since 1.0.0
 */
public record SellerPageResult(List<SellerResult> results, PageMeta pageMeta) {

    public SellerPageResult {
        results = results != null ? List.copyOf(results) : List.of();
    }

    public static SellerPageResult of(List<SellerResult> results, PageMeta pageMeta) {
        return new SellerPageResult(results, pageMeta);
    }

    public static SellerPageResult empty() {
        return new SellerPageResult(List.of(), PageMeta.empty());
    }

    public boolean isEmpty() {
        return results.isEmpty();
    }

    public int size() {
        return results.size();
    }
}
