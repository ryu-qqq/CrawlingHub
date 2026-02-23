package com.ryuqq.crawlinghub.application.seller.service.query;

import com.ryuqq.crawlinghub.application.seller.dto.composite.SellerDetailResult;
import com.ryuqq.crawlinghub.application.seller.manager.SellerCompositionReadManager;
import com.ryuqq.crawlinghub.application.seller.port.in.query.GetSellerUseCase;
import org.springframework.stereotype.Service;

/**
 * 셀러 단건 조회 UseCase 구현체
 *
 * <p><strong>책임</strong>: Composite ReadManager를 통한 셀러 상세 정보 조회
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class GetSellerService implements GetSellerUseCase {

    private final SellerCompositionReadManager compositionReadManager;

    public GetSellerService(SellerCompositionReadManager compositionReadManager) {
        this.compositionReadManager = compositionReadManager;
    }

    @Override
    public SellerDetailResult execute(Long sellerId) {
        return compositionReadManager.getSellerDetail(sellerId);
    }
}
