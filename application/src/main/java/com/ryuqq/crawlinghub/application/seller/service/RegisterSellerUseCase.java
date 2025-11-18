package com.ryuqq.crawlinghub.application.seller.service;

import com.ryuqq.crawlinghub.application.seller.dto.command.RegisterSellerCommand;
import com.ryuqq.crawlinghub.application.seller.port.out.command.SellerCommandPort;
import com.ryuqq.crawlinghub.application.seller.port.out.query.SellerQueryPort;
import com.ryuqq.crawlinghub.domain.seller.aggregate.seller.Seller;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;
import com.ryuqq.crawlinghub.domain.seller.exception.SellerDuplicatedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seller 등록 UseCase
 */
@Service
public class RegisterSellerUseCase {

    private final SellerCommandPort sellerCommandPort;
    private final SellerQueryPort sellerQueryPort;

    public RegisterSellerUseCase(
            SellerCommandPort sellerCommandPort,
            SellerQueryPort sellerQueryPort
    ) {
        this.sellerCommandPort = sellerCommandPort;
        this.sellerQueryPort = sellerQueryPort;
    }

    /**
     * Seller 등록
     *
     * @param command RegisterSellerCommand
     */
    @Transactional
    public void execute(RegisterSellerCommand command) {
        // 1. 중복 name 검증
        if (sellerQueryPort.existsByName(command.name())) {
            throw new SellerDuplicatedException("이미 존재하는 Seller 이름입니다: " + command.name());
        }

        // 2. Seller 생성 (새 ID, INACTIVE 상태)
        Seller seller = Seller.forNew(
                SellerId.forNew(),
                command.name()
        );

        // 3. Seller 저장
        sellerCommandPort.save(seller);
    }
}
