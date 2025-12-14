package com.ryuqq.crawlinghub.application.seller.service.query;

import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.seller.assembler.SellerAssembler;
import com.ryuqq.crawlinghub.application.seller.dto.query.SearchSellersQuery;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerSummaryResponse;
import com.ryuqq.crawlinghub.application.seller.factory.query.SellerQueryFactory;
import com.ryuqq.crawlinghub.application.seller.manager.query.SellerReadManager;
import com.ryuqq.crawlinghub.application.seller.port.in.query.SearchSellersUseCase;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerQueryCriteria;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Search Sellers Service
 *
 * <p>셀러 목록 조회 UseCase 구현
 *
 * <ul>
 *   <li>조회 전용
 *   <li>Query DTO → Criteria 변환 (QueryFactory)
 *   <li>Domain → SummaryResponse 변환 (Assembler)
 * </ul>
 *
 * <p><strong>트랜잭션</strong>: QueryService는 @Transactional 금지 (읽기 전용, 불필요)
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class SearchSellersService implements SearchSellersUseCase {

    private final SellerReadManager readManager;
    private final SellerQueryFactory queryFactory;
    private final SellerAssembler assembler;

    public SearchSellersService(
            SellerReadManager readManager,
            SellerQueryFactory queryFactory,
            SellerAssembler assembler) {
        this.readManager = readManager;
        this.queryFactory = queryFactory;
        this.assembler = assembler;
    }

    @Override
    public PageResponse<SellerSummaryResponse> execute(SearchSellersQuery query) {
        // 1. Query → Criteria 변환 (QueryFactory)
        SellerQueryCriteria criteria = queryFactory.createCriteria(query);

        // 2. 조회
        List<Seller> sellers = readManager.findByCriteria(criteria);
        long totalElements = readManager.countByCriteria(criteria);

        // 3. Domain → PageResponse 변환 (Assembler)
        return assembler.toPageResponse(sellers, criteria.page(), criteria.size(), totalElements);
    }
}
