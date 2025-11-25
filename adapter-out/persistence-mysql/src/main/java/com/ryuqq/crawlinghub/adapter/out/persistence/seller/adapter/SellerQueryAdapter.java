package com.ryuqq.crawlinghub.adapter.out.persistence.seller.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.seller.entity.SellerJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.seller.mapper.SellerJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.seller.repository.SellerQueryDslRepository;
import com.ryuqq.crawlinghub.application.seller.port.out.query.SellerQueryPort;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import com.ryuqq.crawlinghub.domain.seller.vo.MustItSellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerQueryCriteria;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * SellerQueryAdapter - Seller Query Adapter
 *
 * <p>CQRS의 Query(읽기) 담당 Adapter입니다.
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>단건 조회 (findById)
 *   <li>존재 여부 확인 (existsById, existsByMustItSellerName, existsBySellerName)
 *   <li>목록 조회 (findByCriteria)
 *   <li>카운트 조회 (countByCriteria)
 *   <li>QueryDslRepository 호출
 *   <li>Mapper를 통한 Entity → Domain 변환
 * </ul>
 *
 * <p><strong>금지 사항:</strong>
 *
 * <ul>
 *   <li>❌ 비즈니스 로직
 *   <li>❌ 저장/수정/삭제 (CommandAdapter로 분리)
 *   <li>❌ JPAQueryFactory 직접 사용 (QueryDslRepository에서 처리)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class SellerQueryAdapter implements SellerQueryPort {

    private final SellerQueryDslRepository queryDslRepository;
    private final SellerJpaEntityMapper sellerJpaEntityMapper;

    public SellerQueryAdapter(
            SellerQueryDslRepository queryDslRepository,
            SellerJpaEntityMapper sellerJpaEntityMapper) {
        this.queryDslRepository = queryDslRepository;
        this.sellerJpaEntityMapper = sellerJpaEntityMapper;
    }

    /**
     * ID로 Seller 단건 조회
     *
     * @param sellerId Seller ID
     * @return Seller Domain (Optional)
     */
    @Override
    public Optional<Seller> findById(SellerId sellerId) {
        return queryDslRepository.findById(sellerId.value()).map(sellerJpaEntityMapper::toDomain);
    }

    /**
     * ID로 Seller 존재 여부 확인
     *
     * @param sellerId Seller ID
     * @return 존재 여부
     */
    @Override
    public boolean existsById(SellerId sellerId) {
        return queryDslRepository.existsById(sellerId.value());
    }

    /**
     * MustItSellerName 중복 확인
     *
     * @param mustItSellerName 머스트잇 셀러명
     * @return 존재하면 true
     */
    @Override
    public boolean existsByMustItSellerName(MustItSellerName mustItSellerName) {
        return queryDslRepository.existsByMustItSellerName(mustItSellerName.value());
    }

    /**
     * SellerName 중복 확인
     *
     * @param sellerName 셀러명
     * @return 존재하면 true
     */
    @Override
    public boolean existsBySellerName(SellerName sellerName) {
        return queryDslRepository.existsBySellerName(sellerName.value());
    }

    /**
     * ID를 제외한 MustItSellerName 중복 확인
     *
     * <p>수정 시 자기 자신을 제외하고 중복 검사
     *
     * @param mustItSellerName 머스트잇 셀러명
     * @param excludeSellerId 제외할 Seller ID
     * @return 존재하면 true
     */
    @Override
    public boolean existsByMustItSellerNameExcludingId(
            MustItSellerName mustItSellerName, SellerId excludeSellerId) {
        return queryDslRepository.existsByMustItSellerNameExcludingId(
                mustItSellerName.value(), excludeSellerId.value());
    }

    /**
     * ID를 제외한 SellerName 중복 확인
     *
     * <p>수정 시 자기 자신을 제외하고 중복 검사
     *
     * @param sellerName 셀러명
     * @param excludeSellerId 제외할 Seller ID
     * @return 존재하면 true
     */
    @Override
    public boolean existsBySellerNameExcludingId(SellerName sellerName, SellerId excludeSellerId) {
        return queryDslRepository.existsBySellerNameExcludingId(
                sellerName.value(), excludeSellerId.value());
    }

    /**
     * 검색 조건으로 Seller 목록 조회
     *
     * @param criteria 검색 조건
     * @return Seller Domain 목록
     */
    @Override
    public List<Seller> findByCriteria(SellerQueryCriteria criteria) {
        List<SellerJpaEntity> entities = queryDslRepository.findByCriteria(criteria);
        return entities.stream().map(sellerJpaEntityMapper::toDomain).toList();
    }

    /**
     * 검색 조건으로 Seller 개수 조회
     *
     * @param criteria 검색 조건
     * @return Seller 개수
     */
    @Override
    public long countByCriteria(SellerQueryCriteria criteria) {
        return queryDslRepository.countByCriteria(criteria);
    }
}
