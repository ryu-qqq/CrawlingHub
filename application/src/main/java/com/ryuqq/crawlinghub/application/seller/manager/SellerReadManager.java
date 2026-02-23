package com.ryuqq.crawlinghub.application.seller.manager;

import com.ryuqq.crawlinghub.application.seller.port.out.query.SellerQueryPort;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import com.ryuqq.crawlinghub.domain.seller.query.SellerQueryCriteria;
import com.ryuqq.crawlinghub.domain.seller.vo.MustItSellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerName;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Seller 조회 전용 Manager
 *
 * <p><strong>책임</strong>: Seller 조회 작업 위임
 *
 * <p><strong>규칙</strong>: 단일 QueryPort만 의존, 트랜잭션 없음
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class SellerReadManager {

    private final SellerQueryPort sellerQueryPort;

    public SellerReadManager(SellerQueryPort sellerQueryPort) {
        this.sellerQueryPort = sellerQueryPort;
    }

    /**
     * ID로 Seller 조회
     *
     * @param sellerId Seller ID
     * @return Seller (없으면 Optional.empty())
     */
    public Optional<Seller> findById(SellerId sellerId) {
        return sellerQueryPort.findById(sellerId);
    }

    /**
     * ID 존재 여부 확인
     *
     * @param sellerId Seller ID
     * @return 존재하면 true
     */
    public boolean existsById(SellerId sellerId) {
        return sellerQueryPort.existsById(sellerId);
    }

    /**
     * MustItSellerName 중복 확인
     *
     * @param mustItSellerName 머스트잇 셀러명
     * @return 존재하면 true
     */
    public boolean existsByMustItSellerName(MustItSellerName mustItSellerName) {
        return sellerQueryPort.existsByMustItSellerName(mustItSellerName);
    }

    /**
     * SellerName 중복 확인
     *
     * @param sellerName 셀러명
     * @return 존재하면 true
     */
    public boolean existsBySellerName(SellerName sellerName) {
        return sellerQueryPort.existsBySellerName(sellerName);
    }

    /**
     * ID를 제외한 MustItSellerName 중복 확인
     *
     * @param mustItSellerName 머스트잇 셀러명
     * @param excludeSellerId 제외할 Seller ID
     * @return 존재하면 true
     */
    public boolean existsByMustItSellerNameExcludingId(
            MustItSellerName mustItSellerName, SellerId excludeSellerId) {
        return sellerQueryPort.existsByMustItSellerNameExcludingId(
                mustItSellerName, excludeSellerId);
    }

    /**
     * ID를 제외한 SellerName 중복 확인
     *
     * @param sellerName 셀러명
     * @param excludeSellerId 제외할 Seller ID
     * @return 존재하면 true
     */
    public boolean existsBySellerNameExcludingId(SellerName sellerName, SellerId excludeSellerId) {
        return sellerQueryPort.existsBySellerNameExcludingId(sellerName, excludeSellerId);
    }

    /**
     * 조건으로 Seller 목록 조회
     *
     * @param criteria 검색 조건
     * @return Seller 목록
     */
    public List<Seller> findByCriteria(SellerQueryCriteria criteria) {
        return sellerQueryPort.findByCriteria(criteria);
    }

    /**
     * 조건에 맞는 Seller 개수 조회
     *
     * @param criteria 검색 조건
     * @return 개수
     */
    public long countByCriteria(SellerQueryCriteria criteria) {
        return sellerQueryPort.countByCriteria(criteria);
    }
}
