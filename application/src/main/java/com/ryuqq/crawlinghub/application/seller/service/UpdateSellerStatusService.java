package com.ryuqq.crawlinghub.application.seller.service;

import com.ryuqq.crawlinghub.application.seller.assembler.SellerAssembler;
import com.ryuqq.crawlinghub.application.seller.dto.command.UpdateSellerStatusCommand;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.application.seller.port.in.UpdateSellerStatusUseCase;
import com.ryuqq.crawlinghub.application.seller.port.out.LoadSellerPort;
import com.ryuqq.crawlinghub.application.seller.port.out.SaveSellerPort;
import com.ryuqq.crawlinghub.domain.seller.MustitSeller;
import com.ryuqq.crawlinghub.domain.seller.MustitSellerId;
import com.ryuqq.crawlinghub.domain.seller.exception.SellerNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 셀러 상태 변경 UseCase 구현체
 *
 * <p>셀러의 상태를 변경하고 도메인 이벤트를 발행합니다.
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
@Service
public class UpdateSellerStatusService implements UpdateSellerStatusUseCase {

    private final LoadSellerPort loadSellerPort;
    private final SaveSellerPort saveSellerPort;
    private final SellerAssembler sellerAssembler;

    public UpdateSellerStatusService(
        LoadSellerPort loadSellerPort,
        SaveSellerPort saveSellerPort,
        SellerAssembler sellerAssembler
    ) {
        this.loadSellerPort = loadSellerPort;
        this.saveSellerPort = saveSellerPort;
        this.sellerAssembler = sellerAssembler;
    }

    /**
     * 셀러 상태 변경
     *
     * <p>트랜잭션 내에서:
     * 1. 셀러 조회
     * 2. 상태 변경 (도메인 메서드)
     * 3. 저장
     *
     * @param command 변경할 상태 정보
     * @return 변경된 셀러 정보
     * @throws SellerNotFoundException 셀러를 찾을 수 없는 경우
     */
    @Override
    @Transactional
    public SellerResponse execute(UpdateSellerStatusCommand command) {
        // 1. 셀러 조회 (DTO → Domain Model 변환)
        MustitSellerId sellerId = MustitSellerId.of(command.sellerId());
        MustitSeller seller = loadSellerPort.findById(sellerId)
            .map(sellerAssembler::toDomain)
            .orElseThrow(() -> new SellerNotFoundException(command.sellerId()));

        // 2. 상태 변경 (도메인 메서드)
        switch (command.status()) {
            case ACTIVE -> seller.activate();
            case PAUSED -> seller.pause();
            case DISABLED -> seller.disable();
        }

        // 3. 저장
        MustitSeller updatedSeller = saveSellerPort.save(seller);

        // 4. 응답 변환
        return SellerAssembler.toResponse(updatedSeller);
    }
}
