package com.ryuqq.crawlinghub.application.seller.facade;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ryuqq.crawlinghub.application.seller.component.SellerManager;
import com.ryuqq.crawlinghub.application.seller.dto.command.RegisterSellerCommand;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.application.seller.port.in.RegisterSellerUseCase;
import com.ryuqq.crawlinghub.application.seller.port.in.UpdateSellerStatusUseCase;
import com.ryuqq.crawlinghub.domain.seller.MustitSeller;

/**
 * SellerCommandFacade - Seller Command 작업 조율
 *
 * <p><strong>Facade 패턴 적용 ⭐</strong></p>
 * <ul>
 *   <li>여러 UseCase 조율</li>
 *   <li>트랜잭션 경계 관리</li>
 *   <li>Controller 의존성 감소</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@Service
public class SellerCommandFacade {

    private final RegisterSellerUseCase registerSellerUseCase;
    private final UpdateSellerStatusUseCase updateSellerStatusUseCase;
    private final SellerManager sellerManager;

    public SellerCommandFacade(
        RegisterSellerUseCase registerSellerUseCase,
        UpdateSellerStatusUseCase updateSellerStatusUseCase,
        SellerManager sellerManager
    ) {
        this.registerSellerUseCase = registerSellerUseCase;
        this.updateSellerStatusUseCase = updateSellerStatusUseCase;
        this.sellerManager = sellerManager;
    }

    /**
     * 셀러 등록 + 초기 이력 생성
     *
     * <p>Facade가 여러 작업 조율:
     * <ol>
     *   <li>RegisterSellerUseCase 호출 (셀러 등록)</li>
     *   <li>SellerManager를 통한 초기 이력 생성 (상품 수 0)</li>
     * </ol>
     *
     * @param command 등록 Command
     * @return SellerResponse
     */
    @Transactional
    public SellerResponse registerSellerWithInitialHistory(RegisterSellerCommand command) {
        // 1. UseCase 호출
        SellerResponse response = registerSellerUseCase.execute(command);

        // 2. Manager를 통한 초기 이력 생성
        MustitSeller seller = sellerManager.loadSeller(response.sellerId());
        sellerManager.updateProductCountWithHistory(seller, 0);

        return response;
    }
}

