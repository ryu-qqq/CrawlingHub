package com.ryuqq.crawlinghub.application.seller.service.query;

import com.ryuqq.crawlinghub.application.seller.assembler.SellerAssembler;
import com.ryuqq.crawlinghub.application.seller.dto.query.GetSellerQuery;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.application.seller.manager.query.SellerReadManager;
import com.ryuqq.crawlinghub.application.seller.port.in.query.GetSellerUseCase;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.exception.SellerNotFoundException;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import org.springframework.stereotype.Service;

/** 셀러 단건 조회 UseCase 구현체. */
@Service
public class GetSellerService implements GetSellerUseCase {

    private final SellerReadManager sellerReadManager;
    private final SellerAssembler sellerAssembler;

    public GetSellerService(SellerReadManager sellerReadManager, SellerAssembler sellerAssembler) {
        this.sellerReadManager = sellerReadManager;
        this.sellerAssembler = sellerAssembler;
    }

    @Override
    public SellerResponse execute(GetSellerQuery query) {
        Seller seller =
                sellerReadManager
                        .findById(SellerId.of(query.sellerId()))
                        .orElseThrow(() -> new SellerNotFoundException(query.sellerId()));

        return sellerAssembler.toResponse(seller);
    }
}
