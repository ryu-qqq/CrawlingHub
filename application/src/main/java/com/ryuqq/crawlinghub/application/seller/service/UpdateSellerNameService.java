package com.ryuqq.crawlinghub.application.seller.service;

import com.ryuqq.crawlinghub.application.seller.dto.command.UpdateSellerNameCommand;
import com.ryuqq.crawlinghub.application.seller.manager.SellerManager;
import com.ryuqq.crawlinghub.application.seller.port.in.command.UpdateSellerNameUseCase;
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

    public UpdateSellerNameService(SellerManager sellerManager) {
        this.sellerManager = sellerManager;
    }

    @Override
    public void execute(UpdateSellerNameCommand command) {
        // SellerManager에서 조회 + 수정 + 저장 (하나의 트랜잭션)
        sellerManager.updateName(command.sellerId(), command.newName());
    }
}
