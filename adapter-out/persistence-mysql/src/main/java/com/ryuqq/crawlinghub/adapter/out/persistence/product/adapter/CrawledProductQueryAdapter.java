package com.ryuqq.crawlinghub.adapter.out.persistence.product.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.product.entity.CrawledProductJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.mapper.CrawledProductJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.repository.CrawledProductQueryDslRepository;
import com.ryuqq.crawlinghub.application.product.port.out.query.CrawledProductQueryPort;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.id.CrawledProductId;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * CrawledProductQueryAdapter - CrawledProduct Query Adapter
 *
 * <p>CQRS의 Query(읽기) 담당 Adapter입니다.
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>QueryDslRepository를 통한 조회
 *   <li>JPA Entity → Domain Aggregate 변환
 *   <li>Domain 조회 결과 반환
 * </ul>
 *
 * <p><strong>금지 사항:</strong>
 *
 * <ul>
 *   <li>❌ 비즈니스 로직 (Domain에서 처리)
 *   <li>❌ 저장/수정 로직 (CommandAdapter로 분리)
 *   <li>❌ @Transactional 어노테이션 (Application Layer에서 관리)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawledProductQueryAdapter implements CrawledProductQueryPort {

    private final CrawledProductQueryDslRepository queryDslRepository;
    private final CrawledProductJpaEntityMapper mapper;

    public CrawledProductQueryAdapter(
            CrawledProductQueryDslRepository queryDslRepository,
            CrawledProductJpaEntityMapper mapper) {
        this.queryDslRepository = queryDslRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<CrawledProduct> findById(CrawledProductId crawledProductId) {
        return queryDslRepository.findById(crawledProductId.value()).map(mapper::toDomain);
    }

    @Override
    public Optional<CrawledProduct> findBySellerIdAndItemNo(SellerId sellerId, long itemNo) {
        return queryDslRepository
                .findBySellerIdAndItemNo(sellerId.value(), itemNo)
                .map(mapper::toDomain);
    }

    @Override
    public List<CrawledProduct> findBySellerId(SellerId sellerId) {
        List<CrawledProductJpaEntity> entities =
                queryDslRepository.findBySellerId(sellerId.value());
        return entities.stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<CrawledProduct> findNeedsSyncProducts(int limit) {
        List<CrawledProductJpaEntity> entities = queryDslRepository.findNeedsSyncProducts(limit);
        return entities.stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsBySellerIdAndItemNo(SellerId sellerId, long itemNo) {
        return queryDslRepository.existsBySellerIdAndItemNo(sellerId.value(), itemNo);
    }

    /**
     * 셀러별 CrawledProduct 개수 조회
     *
     * @param sellerId 셀러 ID
     * @return 해당 셀러의 상품 개수
     */
    @Override
    public long countBySellerId(SellerId sellerId) {
        return queryDslRepository.countBySellerId(sellerId.value());
    }
}
