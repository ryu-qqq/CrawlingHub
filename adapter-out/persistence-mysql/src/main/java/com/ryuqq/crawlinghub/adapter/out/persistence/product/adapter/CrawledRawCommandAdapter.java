package com.ryuqq.crawlinghub.adapter.out.persistence.product.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.product.entity.CrawledRawJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.mapper.CrawledRawJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.repository.CrawledRawJpaRepository;
import com.ryuqq.crawlinghub.application.product.port.out.command.CrawledRawPersistencePort;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledRaw;
import com.ryuqq.crawlinghub.domain.product.id.CrawledRawId;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * CrawledRawCommandAdapter - CrawledRaw Command Adapter
 *
 * <p>CQRS의 Command(쓰기) 담당 Adapter입니다.
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>Domain Aggregate → JPA Entity 변환
 *   <li>JpaRepository.save() / saveAll() 호출
 *   <li>CrawledRawId 반환
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
public class CrawledRawCommandAdapter implements CrawledRawPersistencePort {

    private final CrawledRawJpaRepository crawledRawJpaRepository;
    private final CrawledRawJpaEntityMapper crawledRawJpaEntityMapper;

    public CrawledRawCommandAdapter(
            CrawledRawJpaRepository crawledRawJpaRepository,
            CrawledRawJpaEntityMapper crawledRawJpaEntityMapper) {
        this.crawledRawJpaRepository = crawledRawJpaRepository;
        this.crawledRawJpaEntityMapper = crawledRawJpaEntityMapper;
    }

    /**
     * CrawledRaw 저장 (신규 생성 또는 수정)
     *
     * <p><strong>신규 생성 (ID 없음)</strong>: JPA가 ID 자동 할당 (INSERT)
     *
     * <p><strong>기존 수정 (ID 있음)</strong>: 더티체킹으로 자동 UPDATE
     *
     * @param crawledRaw 저장할 CrawledRaw Aggregate
     * @return 저장된 CrawledRaw의 ID
     */
    @Override
    public CrawledRawId persist(CrawledRaw crawledRaw) {
        CrawledRawJpaEntity entity = crawledRawJpaEntityMapper.toEntity(crawledRaw);
        CrawledRawJpaEntity savedEntity = crawledRawJpaRepository.save(entity);
        return CrawledRawId.of(savedEntity.getId());
    }

    /**
     * CrawledRaw 벌크 저장
     *
     * <p>여러 CrawledRaw를 한 번에 저장합니다.
     *
     * @param crawledRaws 저장할 CrawledRaw 목록
     * @return 저장된 CrawledRaw ID 목록
     */
    @Override
    public List<CrawledRawId> persistAll(List<CrawledRaw> crawledRaws) {
        List<CrawledRawJpaEntity> entities =
                crawledRaws.stream().map(crawledRawJpaEntityMapper::toEntity).toList();

        List<CrawledRawJpaEntity> savedEntities = crawledRawJpaRepository.saveAll(entities);

        return savedEntities.stream().map(entity -> CrawledRawId.of(entity.getId())).toList();
    }
}
