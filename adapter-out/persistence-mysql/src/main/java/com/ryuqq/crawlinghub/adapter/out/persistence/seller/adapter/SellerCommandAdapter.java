package com.ryuqq.crawlinghub.adapter.out.persistence.seller.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.seller.entity.SellerJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.seller.mapper.SellerJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.seller.repository.SellerJpaRepository;
import com.ryuqq.crawlinghub.application.seller.port.out.command.SellerPersistencePort;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import org.springframework.stereotype.Component;

/**
 * SellerCommandAdapter - Seller Command Adapter
 *
 * <p>CQRS의 Command(쓰기) 담당 Adapter입니다.
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>Domain Aggregate → JPA Entity 변환
 *   <li>JpaRepository.save() 호출
 *   <li>SellerId 반환
 * </ul>
 *
 * <p><strong>금지 사항:</strong>
 *
 * <ul>
 *   <li>❌ 비즈니스 로직 (Domain에서 처리)
 *   <li>❌ 조회 로직 (QueryAdapter로 분리)
 *   <li>❌ @Transactional 어노테이션 (Application Layer에서 관리)
 * </ul>
 *
 * <p><strong>JPA 더티체킹 활용:</strong>
 *
 * <ul>
 *   <li>ID 없음 → INSERT
 *   <li>ID 있음 → 더티체킹으로 자동 UPDATE
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class SellerCommandAdapter implements SellerPersistencePort {

    private final SellerJpaRepository sellerJpaRepository;
    private final SellerJpaEntityMapper sellerJpaEntityMapper;

    public SellerCommandAdapter(
            SellerJpaRepository sellerJpaRepository, SellerJpaEntityMapper sellerJpaEntityMapper) {
        this.sellerJpaRepository = sellerJpaRepository;
        this.sellerJpaEntityMapper = sellerJpaEntityMapper;
    }

    /**
     * Seller 저장 (신규 생성 또는 수정)
     *
     * <p><strong>신규 생성 (ID 없음)</strong>: JPA가 ID 자동 할당 (INSERT)
     *
     * <p><strong>기존 수정 (ID 있음)</strong>: 더티체킹으로 자동 UPDATE
     *
     * @param seller 저장할 Seller Aggregate
     * @return 저장된 Seller의 ID
     */
    @Override
    public SellerId persist(Seller seller) {
        // 1. Domain → Entity 변환
        SellerJpaEntity entity = sellerJpaEntityMapper.toEntity(seller);

        // 2. JPA 저장 (신규/수정 JPA가 자동 판단)
        SellerJpaEntity savedEntity = sellerJpaRepository.save(entity);

        // 3. ID 반환
        return SellerId.of(savedEntity.getId());
    }
}
