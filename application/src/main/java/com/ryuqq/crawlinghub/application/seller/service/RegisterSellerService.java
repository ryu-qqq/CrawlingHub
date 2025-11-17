package com.ryuqq.crawlinghub.application.seller.service;

import com.ryuqq.crawlinghub.application.seller.dto.command.RegisterSellerCommand;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.application.seller.port.in.command.RegisterSellerUseCase;
import com.ryuqq.crawlinghub.application.seller.port.out.command.SellerPersistencePort;
import com.ryuqq.crawlinghub.application.seller.port.out.external.EventBridgePort;
import com.ryuqq.crawlinghub.application.seller.port.out.query.SellerQueryPort;
import com.ryuqq.crawlinghub.domain.seller.aggregate.seller.Seller;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerSearchCriteria;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Seller 등록 Service (UseCase 구현체)
 *
 * <p><strong>구현 책임:</strong></p>
 * <ul>
 *   <li>중복 Seller ID 검증 (findByCriteria 사용)</li>
 *   <li>Seller Domain 생성 및 저장</li>
 *   <li>EventBridge Rule 생성 (크롤링 스케줄)</li>
 *   <li>SellerResponse 반환</li>
 * </ul>
 *
 * <p><strong>Zero-Tolerance 규칙:</strong></p>
 * <ul>
 *   <li>✅ @Service 어노테이션 사용 (UseCase 구현체)</li>
 *   <li>✅ Port 의존성 주입 (생성자)</li>
 *   <li>✅ @Transactional 내 외부 API 호출 금지</li>
 *   <li>✅ VO 기반 중복 체크 (SellerSearchCriteria)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-18
 */
@Service
public class RegisterSellerService implements RegisterSellerUseCase {

    private final SellerQueryPort sellerQueryPort;
    private final SellerPersistencePort sellerPersistencePort;
    private final EventBridgePort eventBridgePort;

    public RegisterSellerService(
        SellerQueryPort sellerQueryPort,
        SellerPersistencePort sellerPersistencePort,
        EventBridgePort eventBridgePort
    ) {
        this.sellerQueryPort = sellerQueryPort;
        this.sellerPersistencePort = sellerPersistencePort;
        this.eventBridgePort = eventBridgePort;
    }

    @Override
    public SellerResponse execute(RegisterSellerCommand command) {
        // Part 1: 중복 Seller ID 검증
        validateDuplicateSellerId(command.sellerId());

        // TODO: Part 2 - Seller 생성 및 저장
        // TODO: Part 3 - EventBridge Rule 생성

        return null; // 임시
    }

    /**
     * 중복 Seller ID 검증
     *
     * @param sellerId Seller ID (String)
     * @throws IllegalArgumentException 이미 존재하는 Seller ID인 경우
     */
    private void validateDuplicateSellerId(String sellerId) {
        SellerSearchCriteria criteria = SellerSearchCriteria.of(
            sellerId,
            null,
            null,
            null,
            null
        );

        List<Seller> existingSellers = sellerQueryPort.findByCriteria(criteria);

        if (!existingSellers.isEmpty()) {
            throw new IllegalArgumentException("이미 존재하는 Seller ID입니다: " + sellerId);
        }
    }
}
