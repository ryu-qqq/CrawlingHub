package com.ryuqq.crawlinghub.application.seller.service;

import com.ryuqq.crawlinghub.application.seller.assembler.SellerAssembler;
import com.ryuqq.crawlinghub.application.seller.dto.command.UpdateSellerNameCommand;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.application.seller.manager.SellerManager;
import com.ryuqq.crawlinghub.application.seller.port.in.command.UpdateSellerNameUseCase;
import com.ryuqq.crawlinghub.application.seller.port.out.SellerQueryPort;
import com.ryuqq.crawlinghub.domain.seller.aggregate.seller.Seller;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;
import com.ryuqq.crawlinghub.domain.seller.exception.SellerNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Seller 이름 변경 Service
 *
 * @author ryu-qqq
 * @since 2025-11-18
 */
@Service
public class UpdateSellerNameService implements UpdateSellerNameUseCase {

    private final SellerManager sellerManager;
    private final SellerQueryPort sellerQueryPort;
    private final SellerAssembler sellerAssembler;

    public UpdateSellerNameService(
            SellerManager sellerManager,
            SellerQueryPort sellerQueryPort,
            SellerAssembler sellerAssembler
    ) {
        this.sellerManager = sellerManager;
        this.sellerQueryPort = sellerQueryPort;
        this.sellerAssembler = sellerAssembler;
    }

    @Override
    public SellerResponse execute(UpdateSellerNameCommand command) {
        // 1. Seller 조회 (Service에서 검증)
        Seller seller = sellerQueryPort.findBySellerId(SellerId.of(command.sellerId()))
                .orElseThrow(() -> new SellerNotFoundException("Seller를 찾을 수 없습니다: " + command.sellerId()));

        // 2. 도메인 로직 실행
        seller.updateName(command.newName());

        // 3. SellerManager로 저장 (Transaction)
        sellerManager.save(seller);

        // 4. Domain → Response (Assembler)
        return sellerAssembler.toResponse(seller);
    }
}
