package com.ryuqq.crawlinghub.application.seller.service.query;

import com.ryuqq.crawlinghub.application.seller.assembler.SellerAssembler;
import com.ryuqq.crawlinghub.application.seller.dto.query.SellerSearchParams;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerPageResult;
import com.ryuqq.crawlinghub.application.seller.factory.query.SellerQueryFactory;
import com.ryuqq.crawlinghub.application.seller.manager.SellerReadManager;
import com.ryuqq.crawlinghub.application.seller.port.in.query.SearchSellerByOffsetUseCase;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.query.SellerQueryCriteria;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 셀러 오프셋 기반 다건 조회 Service
 *
 * <p><strong>트랜잭션</strong>: QueryService는 @Transactional 금지 (읽기 전용, 불필요)
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class SearchSellerByOffsetService implements SearchSellerByOffsetUseCase {

    private final SellerReadManager readManager;
    private final SellerQueryFactory queryFactory;
    private final SellerAssembler assembler;

    public SearchSellerByOffsetService(
            SellerReadManager readManager,
            SellerQueryFactory queryFactory,
            SellerAssembler assembler) {
        this.readManager = readManager;
        this.queryFactory = queryFactory;
        this.assembler = assembler;
    }

    @Override
    public SellerPageResult execute(SellerSearchParams params) {
        SellerQueryCriteria criteria = queryFactory.createCriteria(params);

        List<Seller> sellers = readManager.findByCriteria(criteria);
        long totalElements = readManager.countByCriteria(criteria);

        return assembler.toPageResult(sellers, criteria.page(), criteria.size(), totalElements);
    }
}
