package com.ryuqq.crawlinghub.application.seller.service.command;

import com.ryuqq.crawlinghub.application.seller.manager.SellerTransactionManager;
import com.ryuqq.crawlinghub.application.seller.port.in.command.UpdateSellerProductCountUseCase;
import com.ryuqq.crawlinghub.application.seller.port.out.query.SellerQueryPort;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.exception.SellerNotFoundException;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 셀러 상품 수 업데이트 Service
 *
 * <p>META 크롤링 결과에서 파싱된 총 상품 수를 셀러에 업데이트합니다.
 *
 * <p><strong>TODO</strong>: 실제 DB 업데이트 로직 구현 필요
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class UpdateSellerProductCountService implements UpdateSellerProductCountUseCase {

    private static final Logger log = LoggerFactory.getLogger(UpdateSellerProductCountService.class);

    private final SellerTransactionManager sellerTransactionManager;
    private final SellerQueryPort sellerQueryPort;

    public UpdateSellerProductCountService(SellerTransactionManager sellerTransactionManager,
                                           SellerQueryPort sellerQueryPort) {
        this.sellerTransactionManager = sellerTransactionManager;
        this.sellerQueryPort = sellerQueryPort;
    }

    @Override
    public void execute(Long sellerId, int productCount) {
        Seller seller = sellerQueryPort.findById(SellerId.of(sellerId))
            .orElseThrow(() -> new SellerNotFoundException(sellerId));

        seller.updateProductCount(productCount);

        sellerTransactionManager.persist(seller);
        log.info("셀러 상품 수 업데이트: sellerId={}, productCount={}", sellerId, productCount);
    }
}
