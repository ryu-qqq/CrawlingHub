package com.ryuqq.crawlinghub.application.seller.factory.query;

import com.ryuqq.crawlinghub.application.seller.dto.query.SearchSellersQuery;
import com.ryuqq.crawlinghub.domain.seller.vo.MustItSellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerQueryCriteria;
import org.springframework.stereotype.Component;

/**
 * Seller QueryFactory
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>Query → Criteria 변환
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
     * SearchSellersQuery → SellerQueryCriteria 변환
     *
     * @param query 셀러 검색 Query
     * @return Domain 조회 조건 객체
     */
    public SellerQueryCriteria createCriteria(SearchSellersQuery query) {
        MustItSellerName mustItSellerName =
                query.mustItSellerName() != null
                        ? MustItSellerName.of(query.mustItSellerName())
                        : null;
        SellerName sellerName =
                query.sellerName() != null ? SellerName.of(query.sellerName()) : null;

        return new SellerQueryCriteria(
                mustItSellerName,
                sellerName,
                query.sellerStatuses(),
                query.createdFrom(),
                query.createdTo(),
                query.page(),
                query.size());
    }
}

