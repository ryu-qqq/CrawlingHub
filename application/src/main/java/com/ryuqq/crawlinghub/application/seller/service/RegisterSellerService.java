package com.ryuqq.crawlinghub.application.seller.service;

import com.ryuqq.crawlinghub.application.seller.assembler.SellerAssembler;
import com.ryuqq.crawlinghub.application.seller.dto.command.RegisterSellerCommand;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.application.seller.manager.SellerManager;
import com.ryuqq.crawlinghub.application.seller.port.in.command.RegisterSellerUseCase;
import com.ryuqq.crawlinghub.application.seller.port.out.SellerQueryPort;
import com.ryuqq.crawlinghub.domain.seller.aggregate.seller.Seller;
import com.ryuqq.crawlinghub.domain.seller.exception.SellerDuplicatedException;
import org.springframework.stereotype.Service;

/**
 * Seller 등록 Service
 *
 * @author ryu-qqq
 * @since 2025-11-18
 */
@Service
public class RegisterSellerService implements RegisterSellerUseCase {

    private final SellerManager sellerManager;
    private final SellerQueryPort sellerQueryPort;
    private final SellerAssembler sellerAssembler;

    public RegisterSellerService(
            SellerManager sellerManager,
            SellerQueryPort sellerQueryPort,
            SellerAssembler sellerAssembler
    ) {
        this.sellerManager = sellerManager;
        this.sellerQueryPort = sellerQueryPort;
        this.sellerAssembler = sellerAssembler;
    }

    @Override
    public SellerResponse execute(RegisterSellerCommand command) {
        // 1. 중복 name 검증
        if (sellerQueryPort.existsByName(command.name())) {
            throw new SellerDuplicatedException("이미 존재하는 Seller 이름입니다: " + command.name());
        }

        // 2. Command → Domain (Assembler)
        Seller seller = sellerAssembler.toDomain(command);

        // 3. SellerManager로 저장 (Transaction)
        sellerManager.save(seller);

        // 4. Domain → Response (Assembler)
        return sellerAssembler.toResponse(seller);
    }
}
