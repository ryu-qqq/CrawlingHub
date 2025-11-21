package com.ryuqq.crawlinghub.application.seller.service.command;

import com.ryuqq.crawlinghub.application.assembler.SellerAssembler;
import com.ryuqq.crawlinghub.application.seller.dto.command.RegisterSellerCommand;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.application.seller.manager.SellerTransactionManager;
import com.ryuqq.crawlinghub.application.seller.port.in.command.RegisterSellerUseCase;
import com.ryuqq.crawlinghub.application.seller.port.out.query.SellerQueryCriteria;
import com.ryuqq.crawlinghub.application.seller.port.out.query.SellerQueryPort;
import com.ryuqq.crawlinghub.domain.seller.exception.DuplicateMustItSellerIdException;
import com.ryuqq.crawlinghub.domain.seller.exception.DuplicateSellerNameException;
import com.ryuqq.crawlinghub.domain.seller.vo.MustItSellerId;
import java.util.List;
import org.springframework.stereotype.Service;

/** 셀러 등록 UseCase 구현체. */
@Service
public class RegisterSellerService implements RegisterSellerUseCase {

    private final SellerTransactionManager transactionManager;
    private final SellerQueryPort sellerQueryPort;
    private final SellerAssembler sellerAssembler;

    public RegisterSellerService(
            SellerTransactionManager transactionManager,
            SellerQueryPort sellerQueryPort,
            SellerAssembler sellerAssembler) {
        this.transactionManager = transactionManager;
        this.sellerQueryPort = sellerQueryPort;
        this.sellerAssembler = sellerAssembler;
    }

    /**
     * 셀러 등록.
     *
     * @param command 등록 명령
     * @return 등록 결과 응답
     */
    @Override
    public SellerResponse register(RegisterSellerCommand command) {
        ensureNoDuplicateMustItSellerId(command.mustItSellerId());
        ensureNoDuplicateSellerName(command.sellerName());

        Seller seller =
                Seller.forNew(MustItSellerId.of(command.mustItSellerId()), command.sellerName());

        Seller savedSeller = transactionManager.persist(seller);
        return sellerAssembler.toSellerResponse(savedSeller);
    }

    private void ensureNoDuplicateMustItSellerId(Long mustItSellerId) {
        SellerQueryCriteria criteria = new SellerQueryCriteria(null, null, mustItSellerId);
        List<Seller> sellers = sellerQueryPort.findByCriteria(criteria);
        if (!sellers.isEmpty()) {
            throw new DuplicateMustItSellerIdException(mustItSellerId);
        }
    }

    private void ensureNoDuplicateSellerName(String sellerName) {
        SellerQueryCriteria criteria = new SellerQueryCriteria(null, sellerName, null);
        List<Seller> sellers = sellerQueryPort.findByCriteria(criteria);
        if (!sellers.isEmpty()) {
            throw new DuplicateSellerNameException(sellerName);
        }
    }
}
