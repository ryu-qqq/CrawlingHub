package com.ryuqq.crawlinghub.application.seller.service;

import com.ryuqq.crawlinghub.application.seller.assembler.SellerAssembler;
import com.ryuqq.crawlinghub.application.seller.dto.command.UpdateSellerIntervalCommand;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.application.seller.port.in.command.UpdateSellerIntervalUseCase;
import com.ryuqq.crawlinghub.application.seller.port.out.command.SellerPersistencePort;
import com.ryuqq.crawlinghub.application.seller.port.out.external.EventBridgePort;
import com.ryuqq.crawlinghub.application.seller.port.out.query.SellerQueryPort;
import com.ryuqq.crawlinghub.domain.seller.aggregate.seller.Seller;
import com.ryuqq.crawlinghub.domain.seller.vo.CrawlingInterval;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerSearchCriteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Seller 크롤링 주기 업데이트 Service (UseCase 구현체)
 *
 * <p><strong>구현 책임:</strong></p>
 * <ul>
 *   <li>Seller 조회 (findByCriteria 사용)</li>
 *   <li>changeInterval() 호출 (Domain 메서드)</li>
 *   <li>Seller 저장</li>
 *   <li>EventBridge Rule 업데이트 (크롤링 스케줄 변경)</li>
 *   <li>SellerResponse 반환</li>
 * </ul>
 *
 * <p><strong>Zero-Tolerance 규칙:</strong></p>
 * <ul>
 *   <li>✅ @Service 어노테이션 사용 (UseCase 구현체)</li>
 *   <li>✅ Port 의존성 주입 (생성자)</li>
 *   <li>✅ @Transactional 내 외부 API 호출 금지</li>
 *   <li>✅ VO 기반 조회 (SellerSearchCriteria)</li>
 *   <li>✅ Domain 메서드 활용 (changeInterval)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-18
 */
@Service
public class UpdateSellerIntervalService implements UpdateSellerIntervalUseCase {

    private final SellerQueryPort sellerQueryPort;
    private final SellerPersistencePort sellerPersistencePort;
    private final EventBridgePort eventBridgePort;
    private final SellerAssembler sellerAssembler;

    public UpdateSellerIntervalService(
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
    public SellerResponse execute(UpdateSellerIntervalCommand command) {
        // Transaction 내부: DB 조회 및 저장
        Seller updatedSeller = executeInTransaction(command);

        // Transaction 외부: 외부 API 호출
        executeExternalOperations(command);

        return sellerAssembler.toResponse(updatedSeller);
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
     * @param command Seller 주기 업데이트 Command
     * @return 업데이트된 Seller Domain
     */
    @Transactional
    private Seller executeInTransaction(UpdateSellerIntervalCommand command) {
        // Part 1: Seller 조회
        SellerSearchCriteria criteria = SellerSearchCriteria.of(
            command.sellerId(),
            null,
            null,
            null,
            null
        );

        List<Seller> sellers = sellerQueryPort.findByCriteria(criteria);

        if (sellers.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 Seller ID입니다: " + command.sellerId());
        }

        Seller seller = sellers.get(0);

        // Part 2: updateInterval() 호출 (Domain 메서드)
        seller.updateInterval(command.newIntervalDays());

        // Part 3: Seller 저장
        sellerPersistencePort.persist(seller);

        return seller;
    }

    /**
     * Transaction 외부 로직
     *
     * <p><strong>외부 API 호출:</strong></p>
     * <ul>
     *   <li>✅ EventBridge Rule 업데이트 (AWS SDK 호출)</li>
     *   <li>✅ @Transactional 외부에서 실행</li>
     *   <li>✅ DB Commit 이후 실행 보장</li>
     * </ul>
     *
     * @param command Seller 주기 업데이트 Command
     */
    private void executeExternalOperations(UpdateSellerIntervalCommand command) {
        // Part 4: EventBridge Rule 업데이트
        eventBridgePort.updateRule(
            command.sellerId(),
            command.newIntervalDays()
        );
    }
}
