package com.ryuqq.crawlinghub.application.mustit.seller.component;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ryuqq.crawlinghub.application.mustit.seller.port.out.LoadSellerPort;
import com.ryuqq.crawlinghub.application.mustit.seller.port.out.SaveProductCountHistoryPort;
import com.ryuqq.crawlinghub.application.mustit.seller.port.out.SaveSellerPort;
import com.ryuqq.crawlinghub.domain.mustit.seller.MustitSeller;
import com.ryuqq.crawlinghub.domain.mustit.seller.MustitSellerId;
import com.ryuqq.crawlinghub.domain.mustit.seller.history.ProductCountHistory;

/**
 * SellerManager - Seller Bounded Context 상태 관리
 *
 * <p><strong>Manager 패턴 적용 ⭐</strong></p>
 * <ul>
 *   <li>횡단 관심사 처리 (상품 수 업데이트 + 이력 자동 저장)</li>
 *   <li>트랜잭션 조율</li>
 *   <li>Bounded Context 내 상태 변경 관리</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@Component
public class SellerManager {

    private final SaveSellerPort saveSellerPort;
    private final LoadSellerPort loadSellerPort;
    private final SaveProductCountHistoryPort saveHistoryPort;

    public SellerManager(
        SaveSellerPort saveSellerPort,
        LoadSellerPort loadSellerPort,
        SaveProductCountHistoryPort saveHistoryPort
    ) {
        this.saveSellerPort = saveSellerPort;
        this.loadSellerPort = loadSellerPort;
        this.saveHistoryPort = saveHistoryPort;
    }

    /**
     * 상품 수 업데이트 + 자동 이력 저장
     *
     * <p>Manager가 횡단 관심사 처리:
     * <ol>
     *   <li>Seller Domain 업데이트</li>
     *   <li>ProductCountHistory 자동 생성</li>
     *   <li>하나의 트랜잭션으로 조율</li>
     * </ol>
     *
     * @param seller Seller Domain 객체
     * @param newCount 새로운 상품 수
     */
    @Transactional
    public void updateProductCountWithHistory(MustitSeller seller, Integer newCount) {
        // 1. Seller 업데이트
        seller.updateProductCount(newCount);
        saveSellerPort.save(seller);

        // 2. 이력 자동 저장
        ProductCountHistory history = ProductCountHistory.record(
            MustitSellerId.of(seller.getIdValue()),
            newCount,
            LocalDateTime.now()
        );
        saveHistoryPort.saveHistory(history);
    }

    /**
     * 셀러 등록 (Manager가 일관된 저장 방식 제공)
     *
     * @param seller Seller Domain 객체
     * @return 저장된 MustitSeller
     */
    @Transactional
    public MustitSeller registerSeller(MustitSeller seller) {
        return saveSellerPort.save(seller);
    }

    /**
     * 셀러 조회
     *
     * @param sellerId 셀러 ID
     * @return 조회된 MustitSeller
     */
    public MustitSeller loadSeller(Long sellerId) {
        return loadSellerPort.findById(MustitSellerId.of(sellerId))
            .orElseThrow(() -> new IllegalArgumentException("셀러를 찾을 수 없습니다: " + sellerId));
    }
}

