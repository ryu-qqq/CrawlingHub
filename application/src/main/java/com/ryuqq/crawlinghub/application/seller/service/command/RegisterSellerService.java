package com.ryuqq.crawlinghub.application.seller.service.command;

import com.ryuqq.crawlinghub.application.seller.assembler.SellerAssembler;
import com.ryuqq.crawlinghub.application.seller.dto.command.RegisterSellerCommand;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.application.seller.manager.SellerTransactionManager;
import com.ryuqq.crawlinghub.application.seller.port.in.command.RegisterSellerUseCase;
import com.ryuqq.crawlinghub.application.seller.port.out.query.SellerQueryPort;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.exception.DuplicateMustItSellerIdException;
import com.ryuqq.crawlinghub.domain.seller.exception.DuplicateSellerNameException;
import com.ryuqq.crawlinghub.domain.seller.vo.MustItSellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerName;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Register Seller Service
 *
 * <p>셀러 등록 UseCase 구현
 *
 * <ul>
 *   <li>비즈니스 규칙: MustItSellerName, SellerName 중복 불가
 *   <li>Service에서 QueryPort로 중복 검사 수행
 *   <li>Manager는 영속화만 담당
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class RegisterSellerService implements RegisterSellerUseCase {

    private final SellerTransactionManager transactionManager;
    private final SellerQueryPort sellerQueryPort;
    private final SellerAssembler assembler;

    public RegisterSellerService(
            SellerTransactionManager transactionManager,
            SellerQueryPort sellerQueryPort,
            SellerAssembler assembler) {
        this.transactionManager = transactionManager;
        this.sellerQueryPort = sellerQueryPort;
        this.assembler = assembler;
    }

    @Override
    @Transactional
    public SellerResponse execute(RegisterSellerCommand command) {
        // 1. 비즈니스 검증 (Service 책임)
        validateMustItSellerNameDuplicate(command.mustItSellerName());
        validateSellerNameDuplicate(command.sellerName());

        // 2. Domain 생성 (Assembler)
        Seller seller = assembler.toDomain(command);

        // 3. 영속화 (Manager 책임)
        transactionManager.persist(seller);

        // 4. 응답 변환 (Assembler)
        return assembler.toResponse(seller);
    }

    /**
     * MustItSellerName 중복 검증
     *
     * @param mustItSellerName 머스트잇 셀러명
     * @throws DuplicateMustItSellerIdException 중복 시
     */
    private void validateMustItSellerNameDuplicate(String mustItSellerName) {
        if (sellerQueryPort.existsByMustItSellerName(MustItSellerName.of(mustItSellerName))) {
            throw new DuplicateMustItSellerIdException(mustItSellerName);
        }
    }

    /**
     * SellerName 중복 검증
     *
     * @param sellerName 셀러명
     * @throws DuplicateSellerNameException 중복 시
     */
    private void validateSellerNameDuplicate(String sellerName) {
        if (sellerQueryPort.existsBySellerName(SellerName.of(sellerName))) {
            throw new DuplicateSellerNameException(sellerName);
        }
    }
}
