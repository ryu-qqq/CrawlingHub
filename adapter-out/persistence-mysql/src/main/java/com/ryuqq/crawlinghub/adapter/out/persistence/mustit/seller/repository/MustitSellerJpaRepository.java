package com.ryuqq.crawlinghub.adapter.out.persistence.mustit.seller.repository;

import com.ryuqq.crawlinghub.adapter.out.persistence.mustit.seller.entity.MustitSellerEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 머스트잇 셀러 JPA Repository
 * <p>
 * Spring Data JPA 인터페이스를 사용하여 기본 CRUD 및 커스텀 쿼리를 제공합니다.
 * </p>
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
@Repository
public interface MustitSellerJpaRepository extends JpaRepository<MustitSellerEntity, Long> {

    /**
     * sellerId로 셀러 조회
     *
     * @param sellerId 셀러 고유 ID
     * @return 조회된 셀러 Entity (Optional)
     */
    Optional<MustitSellerEntity> findBySellerId(String sellerId);

    /**
     * sellerId 존재 여부 확인
     *
     * @param sellerId 셀러 고유 ID
     * @return 존재 여부
     */
    boolean existsBySellerId(String sellerId);
}
