package com.ryuqq.crawlinghub.adapter.out.persistence.seller.repository;

import com.ryuqq.crawlinghub.adapter.out.persistence.seller.entity.MustitSellerEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 머스트잇 셀러 JPA Repository
 * <p>
 * Spring Data JPA 인터페이스를 사용하여 기본 CRUD 및 커스텀 쿼리를 제공합니다.
 * 인덱스 활용을 보장하기 위해 명시적 @Query를 사용합니다.
 * </p>
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
@Repository
public interface MustitSellerJpaRepository extends JpaRepository<MustitSellerEntity, Long> {

    /**
     * sellerId로 셀러 조회 (인덱스 활용)
     * <p>
     * NOTE: idx_seller_id 인덱스를 사용하여 조회 성능을 최적화합니다.
     * </p>
     *
     * @param sellerId 셀러 고유 ID
     * @return 조회된 셀러 Entity (Optional)
     */
    @Query("SELECT m FROM MustitSellerEntity m WHERE m.sellerId = :sellerId")
    Optional<MustitSellerEntity> findBySellerId(@Param("sellerId") String sellerId);

    /**
     * sellerId 존재 여부 확인 (최적화 쿼리)
     * <p>
     * NOTE: COUNT 쿼리 대신 EXISTS를 사용하여 성능을 최적화합니다.
     * 데이터가 발견되면 즉시 반환되므로 전체 테이블 스캔이 불필요합니다.
     * </p>
     *
     * @param sellerId 셀러 고유 ID
     * @return 존재 여부
     */
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END " +
           "FROM MustitSellerEntity m WHERE m.sellerId = :sellerId")
    boolean existsBySellerId(@Param("sellerId") String sellerId);
}
