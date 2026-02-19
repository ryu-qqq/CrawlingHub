package com.ryuqq.crawlinghub.application.seller.manager;

import com.ryuqq.crawlinghub.application.common.time.TimeProvider;
import com.ryuqq.crawlinghub.application.seller.port.out.command.SellerPersistencePort;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import com.ryuqq.crawlinghub.domain.seller.vo.MustItSellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seller Transaction Manager
 *
 * <p>Seller Aggregate 트랜잭션 경계 관리
 *
 * <ul>
 *   <li>상태 변경 + 영속화 캡슐화
 *   <li>TimeProvider 의존성 관리
 *   <li>QueryPort 의존성 없음 (Service에서 조회)
 *   <li>단일 PersistencePort 의존성만 보유
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class SellerTransactionManager {

    private final SellerPersistencePort sellerPersistencePort;
    private final TimeProvider timeProvider;

    public SellerTransactionManager(
            SellerPersistencePort sellerPersistencePort, TimeProvider timeProvider) {
        this.sellerPersistencePort = sellerPersistencePort;
        this.timeProvider = timeProvider;
    }

    /**
     * Seller 영속화
     *
     * <p>생성과 수정을 구분하지 않음 (JPA가 ID 유무로 판단)
     *
     * @param seller 영속화할 Seller Aggregate
     * @return 저장된 Seller의 ID
     */
    @Transactional
    public SellerId persist(Seller seller) {
        return sellerPersistencePort.persist(seller);
    }

    /**
     * Seller 정보 수정 + 영속화
     *
     * <p>TimeProvider를 사용하여 updatedAt 자동 설정
     *
     * @param seller 수정 대상 Seller
     * @param newMustItSellerName 새로운 머스트잇 셀러명 (null이면 변경 안 함)
     * @param newSellerName 새로운 셀러명 (null이면 변경 안 함)
     * @param newStatus 새로운 상태 (null이면 변경 안 함)
     */
    @Transactional
    public void update(
            Seller seller,
            MustItSellerName newMustItSellerName,
            SellerName newSellerName,
            SellerStatus newStatus) {
        seller.update(newMustItSellerName, newSellerName, newStatus, timeProvider.now());
        sellerPersistencePort.persist(seller);
    }

    /**
     * Seller 상품 수 업데이트 + 영속화
     *
     * <p>META 크롤링 결과에서 파싱된 총 상품 수를 업데이트합니다.
     *
     * @param seller 수정 대상 Seller
     * @param productCount 새로운 상품 수
     */
    @Transactional
    public void updateProductCount(Seller seller, int productCount) {
        seller.updateProductCount(productCount, timeProvider.now());
        sellerPersistencePort.persist(seller);
    }
}
