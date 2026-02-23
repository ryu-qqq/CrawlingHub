package com.ryuqq.crawlinghub.application.seller.service.command;

import com.ryuqq.crawlinghub.application.common.dto.command.UpdateContext;
import com.ryuqq.crawlinghub.application.seller.factory.command.SellerCommandFactory;
import com.ryuqq.crawlinghub.application.seller.manager.SellerCommandManager;
import com.ryuqq.crawlinghub.application.seller.manager.SellerReadManager;
import com.ryuqq.crawlinghub.application.seller.port.in.command.UpdateSellerProductCountUseCase;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.exception.SellerNotFoundException;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import org.springframework.stereotype.Service;

/**
 * Update Seller Product Count Service
 *
 * <p>셀러 상품 수 업데이트 UseCase 구현
 *
 * <ul>
 *   <li>Factory: Command → UpdateContext 변환
 *   <li>ReadManager: 기존 셀러 조회
 *   <li>CommandManager: 트랜잭션 경계 내 persist
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class UpdateSellerProductCountService implements UpdateSellerProductCountUseCase {

    private final SellerReadManager readManager;
    private final SellerCommandFactory commandFactory;
    private final SellerCommandManager commandManager;

    public UpdateSellerProductCountService(
            SellerReadManager readManager,
            SellerCommandFactory commandFactory,
            SellerCommandManager commandManager) {
        this.readManager = readManager;
        this.commandFactory = commandFactory;
        this.commandManager = commandManager;
    }

    @Override
    public void execute(Long sellerId, int productCount) {
        UpdateContext<SellerId, Integer> context =
                commandFactory.createProductCountUpdateContext(sellerId, productCount);

        Seller seller =
                readManager
                        .findById(context.id())
                        .orElseThrow(() -> new SellerNotFoundException(sellerId));

        seller.updateProductCount(context.updateData(), context.changedAt());
        commandManager.persist(seller);
    }
}
