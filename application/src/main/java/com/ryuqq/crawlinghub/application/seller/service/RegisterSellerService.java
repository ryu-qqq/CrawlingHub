package com.ryuqq.crawlinghub.application.seller.service;

import com.ryuqq.crawlinghub.application.seller.assembler.SellerAssembler;
import com.ryuqq.crawlinghub.application.seller.dto.command.RegisterSellerCommand;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.application.seller.port.in.command.RegisterSellerUseCase;
import com.ryuqq.crawlinghub.application.seller.port.out.command.SellerPersistencePort;
import com.ryuqq.crawlinghub.application.seller.port.out.external.EventBridgePort;
import com.ryuqq.crawlinghub.application.seller.port.out.query.SellerQueryPort;
import com.ryuqq.crawlinghub.domain.seller.aggregate.seller.Seller;
import com.ryuqq.crawlinghub.domain.seller.vo.CrawlingInterval;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerSearchCriteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final SellerAssembler sellerAssembler;

    public RegisterSellerService(
        SellerQueryPort sellerQueryPort,
        SellerPersistencePort sellerPersistencePort,
        EventBridgePort eventBridgePort,
        SellerAssembler sellerAssembler
    ) {
        this.sellerQueryPort = sellerQueryPort;
        this.sellerPersistencePort = sellerPersistencePort;
        this.eventBridgePort = eventBridgePort;
        this.sellerAssembler = sellerAssembler;
    }

    @Override
    public SellerResponse execute(RegisterSellerCommand command) {
        // Transaction 내부: DB 저장
        Seller savedSeller = executeInTransaction(command);

        // Transaction 외부: 외부 API 호출
        executeExternalOperations(command);

        return sellerAssembler.toResponse(savedSeller);
    }

    /**
     * Transaction 내부 로직
     *
     * <p><strong>Transaction 경계:</strong></p>
     * <ul>
     *   <li>✅ DB 조회 및 저장 작업만 포함</li>
     *   <li>✅ 외부 API 호출 절대 금지</li>
     *   <li>✅ Spring Proxy 제약: Private 메서드지만 같은 클래스 내부 호출 아님</li>
     * </ul>
     *
     * @param command Seller 등록 Command
     * @return 저장된 Seller Domain
     */
    @Transactional
    private Seller executeInTransaction(RegisterSellerCommand command) {
        // Part 1: 중복 Seller ID 검증
        validateDuplicateSellerId(command.sellerId());

        // Part 2: Seller 생성 및 저장
        CrawlingInterval crawlingInterval = new CrawlingInterval(command.crawlingIntervalDays());
        Seller seller = Seller.forNew(
            SellerId.forNew(),
            command.name(),
            crawlingInterval
        );

        SellerId savedSellerId = sellerPersistencePort.persist(seller);
        return Seller.reconstitute(
            savedSellerId,
            command.name(),
            crawlingInterval,
            seller.getStatus(),
            seller.getTotalProductCount()
        );
    }

    /**
     * Transaction 외부 로직
     *
     * <p><strong>외부 API 호출:</strong></p>
     * <ul>
     *   <li>✅ EventBridge Rule 생성 (AWS SDK 호출)</li>
     *   <li>✅ @Transactional 외부에서 실행</li>
     *   <li>✅ DB Commit 이후 실행 보장</li>
     * </ul>
     *
     * @param command Seller 등록 Command
     */
    private void executeExternalOperations(RegisterSellerCommand command) {
        // Part 3: EventBridge Rule 생성
        eventBridgePort.createRule(
            command.sellerId(),
            command.crawlingIntervalDays()
        );
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
