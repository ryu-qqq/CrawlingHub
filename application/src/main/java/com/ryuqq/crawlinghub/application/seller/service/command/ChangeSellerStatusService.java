package com.ryuqq.crawlinghub.application.seller.service.command;

import com.ryuqq.crawlinghub.application.assembler.SellerAssembler;
import com.ryuqq.crawlinghub.application.seller.dto.command.ChangeSellerStatusCommand;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.application.seller.manager.SellerTransactionManager;
import com.ryuqq.crawlinghub.application.seller.port.in.command.ChangeSellerStatusUseCase;
import com.ryuqq.crawlinghub.application.seller.port.out.query.SchedulerQueryPort;
import com.ryuqq.crawlinghub.application.seller.port.out.query.SellerQueryPort;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.exception.SellerHasActiveSchedulersException;
import com.ryuqq.crawlinghub.domain.seller.exception.SellerNotFoundException;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import org.springframework.stereotype.Service;

/**
 * 셀러 상태 변경 UseCase 구현체.
 */
@Service
public class ChangeSellerStatusService implements ChangeSellerStatusUseCase {

    private final SellerTransactionManager transactionManager;
    private final SellerQueryPort sellerQueryPort;
    private final SchedulerQueryPort schedulerQueryPort;
    private final SellerAssembler sellerAssembler;

    public ChangeSellerStatusService(
        SellerTransactionManager transactionManager,
        SellerQueryPort sellerQueryPort,
        SchedulerQueryPort schedulerQueryPort,
        SellerAssembler sellerAssembler
    ) {
        this.transactionManager = transactionManager;
        this.sellerQueryPort = sellerQueryPort;
        this.schedulerQueryPort = schedulerQueryPort;
        this.sellerAssembler = sellerAssembler;
    }

    @Override
    public SellerResponse changeStatus(ChangeSellerStatusCommand command) {
        Seller seller = sellerQueryPort.findById(SellerId.of(command.sellerId()))
            .orElseThrow(() -> new SellerNotFoundException(command.sellerId()));

        if (command.targetStatus() == SellerStatus.INACTIVE) {
            deactivateSeller(seller, command.sellerId());
        } else {
            seller.activate();
        }

        Seller savedSeller = transactionManager.persist(seller);
        return sellerAssembler.toSellerResponse(savedSeller);
    }

    private void deactivateSeller(Seller seller, Long sellerId) {
        int activeSchedulerCount = schedulerQueryPort.countActiveSchedulersBySellerId(sellerId);
        if (activeSchedulerCount > 0) {
            throw new SellerHasActiveSchedulersException(sellerId, activeSchedulerCount);
        }
        seller.deactivate(0);
    }
}

