package com.ryuqq.crawlinghub.application.seller.service.command;

import com.ryuqq.crawlinghub.application.seller.assembler.SellerAssembler;
import com.ryuqq.crawlinghub.application.seller.dto.command.UpdateSellerCommand;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.application.seller.facade.SellerCommandFacade;
import com.ryuqq.crawlinghub.application.seller.port.in.command.UpdateSellerUseCase;
import com.ryuqq.crawlinghub.application.seller.port.out.query.SellerQueryPort;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.exception.DuplicateMustItSellerIdException;
import com.ryuqq.crawlinghub.domain.seller.exception.DuplicateSellerNameException;
import com.ryuqq.crawlinghub.domain.seller.exception.SellerNotFoundException;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;
import org.springframework.stereotype.Service;

/**
 * Update Seller Service
 *
 * <p>셀러 수정 UseCase 구현
 *
 * <ul>
 *   <li>Assembler로 Command → Domain 변환
 *   <li>두 Seller 객체 비교로 변경 감지
 *   <li>Tell, Don't Ask 패턴: Seller.update()가 내부적으로 판단
 *   <li>Domain Event 수집 및 발행 (비활성화 시 스케줄러 중지)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class UpdateSellerService implements UpdateSellerUseCase {

    private final SellerCommandFacade sellerCommandFacade;
    private final SellerQueryPort sellerQueryPort;
    private final SellerAssembler assembler;

    public UpdateSellerService(
            SellerCommandFacade sellerCommandFacade,
            SellerQueryPort sellerQueryPort,
            SellerAssembler assembler) {
        this.sellerCommandFacade = sellerCommandFacade;
        this.sellerQueryPort = sellerQueryPort;
        this.assembler = assembler;
    }

    @Override
    public SellerResponse execute(UpdateSellerCommand command) {
        // 1. Command → Domain 변환 (Assembler)
        Seller requestedSeller = assembler.toDomain(command);

        // 2. 기존 Seller 조회
        Seller existingSeller =
                sellerQueryPort
                        .findById(SellerId.of(command.sellerId()))
                        .orElseThrow(() -> new SellerNotFoundException(command.sellerId()));

        // 3. 비즈니스 검증 (Seller 객체 간 비교)
        if (existingSeller.needsUpdateMustItSellerName(requestedSeller.getMustItSellerName())) {
            validateMustItSellerNameDuplicate(requestedSeller, existingSeller.getSellerId());
        }
        if (existingSeller.needsUpdateSellerName(requestedSeller.getSellerName())) {
            validateSellerNameDuplicate(requestedSeller, existingSeller.getSellerId());
        }

        // 4. Domain 로직 (Tell, Don't Ask)
        existingSeller.update(
                requestedSeller.getMustItSellerName(),
                requestedSeller.getSellerName(),
                requestedSeller.getStatus());

        // 5. 영속화
        sellerCommandFacade.persist(existingSeller);

        // 6. 응답 변환 (Assembler)
        return assembler.toResponse(existingSeller);
    }

    /**
     * MustItSellerName 중복 검증 (자기 자신 제외)
     *
     * @param requestedSeller 요청된 Seller (Command에서 변환)
     * @param excludeSellerId 제외할 Seller ID
     * @throws DuplicateMustItSellerIdException 중복 시
     */
    private void validateMustItSellerNameDuplicate(
            Seller requestedSeller, SellerId excludeSellerId) {
        if (sellerQueryPort.existsByMustItSellerNameExcludingId(
                requestedSeller.getMustItSellerName(), excludeSellerId)) {
            throw new DuplicateMustItSellerIdException(requestedSeller.getMustItSellerNameValue());
        }
    }

    /**
     * SellerName 중복 검증 (자기 자신 제외)
     *
     * @param requestedSeller 요청된 Seller (Command에서 변환)
     * @param excludeSellerId 제외할 Seller ID
     * @throws DuplicateSellerNameException 중복 시
     */
    private void validateSellerNameDuplicate(Seller requestedSeller, SellerId excludeSellerId) {
        if (sellerQueryPort.existsBySellerNameExcludingId(
                requestedSeller.getSellerName(), excludeSellerId)) {
            throw new DuplicateSellerNameException(requestedSeller.getSellerNameValue());
        }
    }
}
