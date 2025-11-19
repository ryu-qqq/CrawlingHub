package com.ryuqq.crawlinghub.application.seller.port.out.query;

import java.util.List;
import java.util.Optional;

import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;

/**
 * Seller Aggregate 조회 전용 Outbound Port.
 *
 * <p>Persistence Adapter는 이 Port를 구현하여 CQRS Query 요구사항을 충족합니다.</p>
 *
 * @author development-team
 * @since 1.0.0
 */
public interface SellerQueryPort {

    /**
     * 식별자로 Seller를 조회한다.
     *
     * @param sellerId Seller ID
     * @return Seller Aggregate Optional
     */
    Optional<Seller> findById(SellerId sellerId);

    /**
     * Seller 존재 여부를 확인한다.
     *
     * @param sellerId Seller ID
     * @return 존재 여부
     */
    boolean existsById(SellerId sellerId);

    /**
     * 조회 조건에 맞는 Seller 목록을 조회한다.
     *
     * @param criteria 조회 조건
     * @return 조건에 맞는 Seller 리스트
     */
    List<Seller> findByCriteria(SellerQueryCriteria criteria);

    /**
     * 조회 조건에 맞는 Seller 총 개수를 반환한다.
     *
     * @param criteria 조회 조건
     * @return 조건에 맞는 Seller 수
     */
    long countByCriteria(SellerQueryCriteria criteria);
}

