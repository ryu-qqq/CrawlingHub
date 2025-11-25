package com.ryuqq.crawlinghub.application.seller.port.out.query;

import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import com.ryuqq.crawlinghub.domain.seller.vo.MustItSellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerQueryCriteria;
import java.util.List;
import java.util.Optional;

/**
 * Seller Query Port
 *
 * <p>Seller 조회 전용 Port
 *
 * @author development-team
 * @since 1.0.0
 */
public interface SellerQueryPort {

    /**
     * ID로 Seller 조회
     *
     * @param sellerId Seller ID
     * @return Seller (없으면 Optional.empty())
     */
    Optional<Seller> findById(SellerId sellerId);

    /**
     * ID 존재 여부 확인
     *
     * @param sellerId Seller ID
     * @return 존재하면 true
     */
    boolean existsById(SellerId sellerId);

    /**
     * MustItSellerName 중복 확인
     *
     * @param mustItSellerName 머스트잇 셀러명
     * @return 존재하면 true
     */
    boolean existsByMustItSellerName(MustItSellerName mustItSellerName);

    /**
     * SellerName 중복 확인
     *
     * @param sellerName 셀러명
     * @return 존재하면 true
     */
    boolean existsBySellerName(SellerName sellerName);

    /**
     * ID를 제외한 MustItSellerName 중복 확인
     *
     * <p>수정 시 자기 자신을 제외하고 중복 검사
     *
     * @param mustItSellerName 머스트잇 셀러명
     * @param excludeSellerId 제외할 Seller ID
     * @return 존재하면 true
     */
    boolean existsByMustItSellerNameExcludingId(
            MustItSellerName mustItSellerName, SellerId excludeSellerId);

    /**
     * ID를 제외한 SellerName 중복 확인
     *
     * <p>수정 시 자기 자신을 제외하고 중복 검사
     *
     * @param sellerName 셀러명
     * @param excludeSellerId 제외할 Seller ID
     * @return 존재하면 true
     */
    boolean existsBySellerNameExcludingId(SellerName sellerName, SellerId excludeSellerId);

    /**
     * 조건으로 Seller 목록 조회
     *
     * @param criteria 검색 조건
     * @return Seller 목록
     */
    List<Seller> findByCriteria(SellerQueryCriteria criteria);

    /**
     * 조건에 맞는 Seller 개수 조회
     *
     * @param criteria 검색 조건
     * @return 개수
     */
    long countByCriteria(SellerQueryCriteria criteria);
}
