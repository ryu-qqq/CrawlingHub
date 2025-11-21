package com.ryuqq.crawlinghub.application.seller.service.query;

import com.ryuqq.crawlinghub.application.seller.assembler.SellerAssembler;
import com.ryuqq.crawlinghub.application.seller.dto.query.GetSellerQuery;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.application.seller.port.in.query.GetSellerUseCase;
import com.ryuqq.crawlinghub.application.seller.port.out.query.SellerQueryPort;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.exception.SellerNotFoundException;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;
import org.springframework.stereotype.Service;

/** 셀러 단건 조회 UseCase 구현체. */
@Service
public class GetSellerService implements GetSellerUseCase {

    private final SellerQueryPort sellerQueryPort;
    private final SellerAssembler sellerAssembler;

    public GetSellerService(SellerQueryPort sellerQueryPort, SellerAssembler sellerAssembler) {
        this.sellerQueryPort = sellerQueryPort;
        this.sellerAssembler = sellerAssembler;
    }

    @Override
    public SellerResponse execute(GetSellerQuery query) {
        Seller seller =
                sellerQueryPort
                        .findById(SellerId.of(query.sellerId()))
                        .orElseThrow(() -> new SellerNotFoundException(query.sellerId()));

        return sellerAssembler.toResponse(seller);
    }
}
