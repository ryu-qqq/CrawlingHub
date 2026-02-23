package com.ryuqq.crawlinghub.application.seller.service.command;

import com.ryuqq.crawlinghub.application.common.dto.command.UpdateContext;
import com.ryuqq.crawlinghub.application.seller.component.SellerPersistenceValidator;
import com.ryuqq.crawlinghub.application.seller.dto.command.UpdateSellerCommand;
import com.ryuqq.crawlinghub.application.seller.factory.command.SellerCommandFactory;
import com.ryuqq.crawlinghub.application.seller.manager.SellerCommandManager;
import com.ryuqq.crawlinghub.application.seller.manager.SellerReadManager;
import com.ryuqq.crawlinghub.application.seller.port.in.command.UpdateSellerUseCase;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.exception.SellerNotFoundException;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerUpdateData;
import org.springframework.stereotype.Service;

/**
 * Update Seller Service
 *
 * <p>셀러 수정 UseCase 구현
 *
 * <ul>
 *   <li>Factory: Command → UpdateContext 변환
 *   <li>Validator: 중복 검증 + 비활성화 검증
 *   <li>CommandManager: 도메인 update() + 트랜잭션 경계 내 persist
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class UpdateSellerService implements UpdateSellerUseCase {

    private final SellerPersistenceValidator validator;
    private final SellerReadManager readManager;
    private final SellerCommandFactory commandFactory;
    private final SellerCommandManager commandManager;

    public UpdateSellerService(
            SellerPersistenceValidator validator,
            SellerReadManager readManager,
            SellerCommandFactory commandFactory,
            SellerCommandManager commandManager) {
        this.validator = validator;
        this.readManager = readManager;
        this.commandFactory = commandFactory;
        this.commandManager = commandManager;
    }

    @Override
    public void execute(UpdateSellerCommand command) {
        UpdateContext<SellerId, SellerUpdateData> context =
                commandFactory.createUpdateContext(command);

        Seller existingSeller =
                readManager
                        .findById(context.id())
                        .orElseThrow(() -> new SellerNotFoundException(command.sellerId()));

        validator.validateForUpdate(existingSeller, context.updateData());

        existingSeller.update(context.updateData(), context.changedAt());
        commandManager.persist(existingSeller);
    }
}
