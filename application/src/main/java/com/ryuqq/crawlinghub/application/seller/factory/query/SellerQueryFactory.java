package com.ryuqq.crawlinghub.application.seller.factory.query;

import com.ryuqq.crawlinghub.application.seller.dto.query.SellerSearchParams;
import com.ryuqq.crawlinghub.domain.seller.query.SellerQueryCriteria;
import com.ryuqq.crawlinghub.domain.seller.vo.MustItSellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Seller QueryFactory
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>SearchParams → Criteria 변환
 *   <li>String 상태값 → SellerStatus Enum 파싱
 * </ul>
 *
 * <p><strong>금지</strong>:
 *
 * <ul>
 *   <li>@Transactional 금지 (변환만, 트랜잭션 불필요)
 *   <li>Port 의존 금지
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class SellerQueryFactory {

    /**
     * SellerSearchParams → SellerQueryCriteria 변환
     *
     * @param params 셀러 검색 파라미터
     * @return Domain 조회 조건 객체
     */
    public SellerQueryCriteria createCriteria(SellerSearchParams params) {
        MustItSellerName mustItSellerName =
                params.mustItSellerName() != null
                        ? MustItSellerName.of(params.mustItSellerName())
                        : null;
        SellerName sellerName =
                params.sellerName() != null ? SellerName.of(params.sellerName()) : null;

        List<SellerStatus> sellerStatuses = parseStatuses(params.statuses());

        return new SellerQueryCriteria(
                mustItSellerName,
                sellerName,
                sellerStatuses,
                params.createdFrom(),
                params.createdTo(),
                params.page(),
                params.size());
    }

    /**
     * 상태 문자열 목록 → SellerStatus Enum 목록 변환
     *
     * @param statuses 상태 문자열 목록
     * @return SellerStatus 목록 (null이거나 빈 리스트면 null)
     */
    private List<SellerStatus> parseStatuses(List<String> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return null;
        }
        return statuses.stream()
                .filter(s -> s != null && !s.isBlank())
                .map(SellerStatus::valueOf)
                .toList();
    }
}
