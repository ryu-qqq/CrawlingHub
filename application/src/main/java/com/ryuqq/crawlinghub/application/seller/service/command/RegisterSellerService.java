package com.ryuqq.crawlinghub.application.seller.service.command;

import com.ryuqq.crawlinghub.application.seller.component.SellerPersistenceValidator;
import com.ryuqq.crawlinghub.application.seller.dto.command.RegisterSellerCommand;
import com.ryuqq.crawlinghub.application.seller.factory.command.SellerCommandFactory;
import com.ryuqq.crawlinghub.application.seller.manager.SellerCommandManager;
import com.ryuqq.crawlinghub.application.seller.port.in.command.RegisterSellerUseCase;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import org.springframework.stereotype.Service;

/**
 * Register Seller Service
 *
 * <p>셀러 등록 UseCase 구현
 *
 * <ul>
 *   <li>Validator: MustItSellerName, SellerName 중복 검증
 *   <li>CommandManager: 트랜잭션 경계 내 persist
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class RegisterSellerService implements RegisterSellerUseCase {

    private final SellerPersistenceValidator validator;
    private final SellerCommandFactory commandFactory;
    private final SellerCommandManager commandManager;

    public RegisterSellerService(
            SellerPersistenceValidator validator,
            SellerCommandFactory commandFactory,
            SellerCommandManager commandManager) {
        this.validator = validator;
        this.commandFactory = commandFactory;
        this.commandManager = commandManager;
    }

    @Override
    public long execute(RegisterSellerCommand command) {
        Seller seller = commandFactory.create(command);
        validator.validateForRegistration(seller);
        SellerId sellerId = commandManager.persist(seller);
        return sellerId.value();
    }
}
