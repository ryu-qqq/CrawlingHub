package com.ryuqq.crawlinghub.application.seller.service;

import com.ryuqq.crawlinghub.application.seller.assembler.SellerAssembler;
import com.ryuqq.crawlinghub.application.seller.dto.command.RegisterSellerCommand;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.application.seller.port.in.RegisterSellerUseCase;
import com.ryuqq.crawlinghub.application.seller.port.out.LoadSellerPort;
import com.ryuqq.crawlinghub.application.seller.port.out.SaveSellerPort;
import com.ryuqq.crawlinghub.domain.seller.MustitSeller;
import com.ryuqq.crawlinghub.domain.seller.exception.DuplicateSellerCodeException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 셀러 등록 UseCase 구현체
 *
 * <p>신규 셀러를 등록하고 도메인 이벤트를 발행합니다.
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
@Service
public class RegisterSellerService implements RegisterSellerUseCase {

    private final LoadSellerPort loadSellerPort;
    private final SaveSellerPort saveSellerPort;

    public RegisterSellerService(
        LoadSellerPort loadSellerPort,
        SaveSellerPort saveSellerPort
    ) {
        this.loadSellerPort = loadSellerPort;
        this.saveSellerPort = saveSellerPort;
    }

    /**
     * 셀러 등록
     *
     * <p>트랜잭션 내에서:
     * 1. 중복 체크 (sellerCode 기준)
     * 2. 도메인 객체 생성 (Factory 메서드 사용)
     * 3. 저장
     *
     * @param command 등록할 셀러 정보
     * @return 등록된 셀러 정보
     * @throws DuplicateSellerCodeException 이미 존재하는 셀러 코드인 경우
     */
    @Override
    @Transactional
    public SellerResponse execute(RegisterSellerCommand command) {
        // 1. 중복 체크
        loadSellerPort.findByCode(command.sellerCode())
            .ifPresent(existingSeller -> {
                throw new DuplicateSellerCodeException(command.sellerCode());
            });

        // 2. 도메인 객체 생성 (Factory 메서드)
        MustitSeller seller = MustitSeller.forNew(
            command.sellerCode(),
            command.sellerName()
        );

        // 3. 저장
        MustitSeller savedSeller = saveSellerPort.save(seller);

        // 4. 응답 변환
        return SellerAssembler.toResponse(savedSeller);
    }
}
