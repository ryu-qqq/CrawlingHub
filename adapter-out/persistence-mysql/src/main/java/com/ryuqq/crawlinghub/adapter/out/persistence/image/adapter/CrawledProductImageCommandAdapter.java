package com.ryuqq.crawlinghub.adapter.out.persistence.image.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.image.entity.CrawledProductImageJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.image.mapper.CrawledProductImageJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.image.repository.CrawledProductImageJpaRepository;
import com.ryuqq.crawlinghub.application.product.port.out.command.CrawledProductImagePersistencePort;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductImage;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * CrawledProductImageCommandAdapter - 이미지 저장 Adapter
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawledProductImageCommandAdapter implements CrawledProductImagePersistencePort {

    private final CrawledProductImageJpaRepository repository;
    private final CrawledProductImageJpaEntityMapper mapper;

    public CrawledProductImageCommandAdapter(
            CrawledProductImageJpaRepository repository,
            CrawledProductImageJpaEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public CrawledProductImage save(CrawledProductImage image) {
        CrawledProductImageJpaEntity entity = mapper.toEntity(image);
        CrawledProductImageJpaEntity savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public List<CrawledProductImage> saveAll(List<CrawledProductImage> images) {
        List<CrawledProductImageJpaEntity> entities =
                images.stream().map(mapper::toEntity).toList();
        List<CrawledProductImageJpaEntity> savedEntities = repository.saveAll(entities);
        return savedEntities.stream().map(mapper::toDomain).toList();
    }

    @Override
    public void update(CrawledProductImage image) {
        CrawledProductImageJpaEntity entity = mapper.toEntity(image);
        repository.save(entity);
    }
}
