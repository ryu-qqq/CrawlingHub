package com.ryuqq.crawlinghub.application.seller.service;

import com.ryuqq.crawlinghub.application.seller.dto.command.UpdateSellerNameCommand;
import com.ryuqq.crawlinghub.application.seller.port.in.command.UpdateSellerNameUseCase;
import com.ryuqq.crawlinghub.application.seller.port.out.command.SellerPersistencePort;
import com.ryuqq.crawlinghub.application.seller.port.out.query.SellerQueryPort;
import com.ryuqq.crawlinghub.domain.seller.aggregate.seller.Seller;
import com.ryuqq.crawlinghub.domain.seller.exception.SellerNotFoundException;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seller 이름 변경 Service 구현
 *
 * <p>기존 Seller의 이름을 변경하는 UseCase 구현입니다.</p>
 *
 * <p><strong>Zero-Tolerance 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ @Service 어노테이션 사용</li>
 *   <li>✅ UseCase 인터페이스 구현</li>
 *   <li>✅ 생성자 주입 (의존성 주입)</li>
 *   <li>✅ @Transactional은 메서드에만 (클래스 레벨 금지)</li>
 *   <li>✅ Transaction 내 외부 API 호출 금지</li>
 * </ul>
 *
 * <p><strong>비즈니스 로직:</strong></p>
 * <ol>
 *   <li>Seller 조회 (SellerQueryPort.findById)</li>
 *   <li>이름 변경 (Seller.updateName)</li>
 *   <li>DB 저장 (SellerPersistencePort.persist)</li>
 * </ol>
 *
 * <p><strong>Transaction 경계:</strong></p>
 * <ul>
 *   <li>✅ 단일 트랜잭션 (DB 작업만)</li>
 *   <li>✅ 외부 API 호출 없음</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
@Service
public class UpdateSellerNameService implements UpdateSellerNameUseCase {

    private final SellerQueryPort sellerQueryPort;
    private final SellerPersistencePort sellerPersistencePort;

    public UpdateSellerNameService(
            SellerQueryPort sellerQueryPort,
            SellerPersistencePort sellerPersistencePort
    ) {
        this.sellerQueryPort = sellerQueryPort;
        this.sellerPersistencePort = sellerPersistencePort;
    }

    /**
     * Seller 이름 변경
     *
     * @param command UpdateSellerNameCommand (sellerId, newName)
     * @throws SellerNotFoundException Seller가 존재하지 않는 경우
     * @throws IllegalArgumentException 이름이 유효하지 않은 경우
     */
    @Override
    @Transactional
    public void execute(UpdateSellerNameCommand command) {
        // 1. Seller 조회
        SellerId sellerId = new SellerId(command.sellerId());
        Seller seller = sellerQueryPort.findById(sellerId)
            .orElseThrow(() -> new SellerNotFoundException("Seller를 찾을 수 없습니다: " + command.sellerId()));

        // 2. 이름 변경
        seller.updateName(command.newName());

        // 3. DB 저장
        sellerPersistencePort.persist(seller);
    }
}

