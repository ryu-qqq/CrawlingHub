package com.ryuqq.crawlinghub.adapter.out.persistence.product.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.product.entity.CrawledProductJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.mapper.CrawledProductJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.repository.CrawledProductJpaRepository;
import com.ryuqq.crawlinghub.application.product.port.out.command.CrawledProductPersistencePort;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import org.springframework.stereotype.Component;

/**
 * CrawledProductCommandAdapter - CrawledProduct Command Adapter
 *
 * <p>CQRS의 Command(쓰기) 담당 Adapter입니다.
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>Domain Aggregate → JPA Entity 변환
 *   <li>JpaRepository.save() / deleteById() 호출
 *   <li>CrawledProductId 반환
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
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawledProductCommandAdapter implements CrawledProductPersistencePort {

    private final CrawledProductJpaRepository crawledProductJpaRepository;
    private final CrawledProductJpaEntityMapper crawledProductJpaEntityMapper;

    public CrawledProductCommandAdapter(
            CrawledProductJpaRepository crawledProductJpaRepository,
            CrawledProductJpaEntityMapper crawledProductJpaEntityMapper) {
        this.crawledProductJpaRepository = crawledProductJpaRepository;
        this.crawledProductJpaEntityMapper = crawledProductJpaEntityMapper;
    }

    /**
     * CrawledProduct 저장 (신규 생성 또는 수정)
     *
     * <p><strong>신규 생성 (ID 없음)</strong>: JPA가 ID 자동 할당 (INSERT)
     *
     * <p><strong>기존 수정 (ID 있음)</strong>: 더티체킹으로 자동 UPDATE
     *
     * @param crawledProduct 저장할 CrawledProduct Aggregate
     * @return 저장된 CrawledProduct의 ID
     */
    @Override
    public CrawledProductId persist(CrawledProduct crawledProduct) {
        CrawledProductJpaEntity entity = crawledProductJpaEntityMapper.toEntity(crawledProduct);
        CrawledProductJpaEntity savedEntity = crawledProductJpaRepository.save(entity);
        return CrawledProductId.of(savedEntity.getId());
    }

    /**
     * CrawledProduct 삭제
     *
     * @param crawledProductId 삭제할 CrawledProduct ID
     */
    @Override
    public void delete(CrawledProductId crawledProductId) {
        crawledProductJpaRepository.deleteById(crawledProductId.value());
    }
}
