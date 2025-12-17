package com.ryuqq.crawlinghub.adapter.out.persistence.image.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.image.entity.ProductImageOutboxJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.image.mapper.ProductImageOutboxJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.image.repository.ProductImageOutboxJpaRepository;
import com.ryuqq.crawlinghub.application.product.port.out.command.ImageOutboxPersistencePort;
import com.ryuqq.crawlinghub.domain.product.aggregate.ProductImageOutbox;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * ImageOutboxCommandAdapter - Outbox Command Adapter
 *
 * <p>CQRS의 Command(쓰기) 담당 Adapter입니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class ImageOutboxCommandAdapter implements ImageOutboxPersistencePort {

    private final ProductImageOutboxJpaRepository jpaRepository;
    private final ProductImageOutboxJpaEntityMapper mapper;

    public ImageOutboxCommandAdapter(
            ProductImageOutboxJpaRepository jpaRepository,
            ProductImageOutboxJpaEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public void persist(ProductImageOutbox outbox) {
        ProductImageOutboxJpaEntity entity = mapper.toEntity(outbox);
        jpaRepository.save(entity);
    }

    @Override
    public void persistAll(List<ProductImageOutbox> outboxes) {
        List<ProductImageOutboxJpaEntity> entities =
                outboxes.stream().map(mapper::toEntity).toList();
        jpaRepository.saveAll(entities);
    }

    @Override
    public void update(ProductImageOutbox outbox) {
        ProductImageOutboxJpaEntity entity = mapper.toEntity(outbox);
        jpaRepository.save(entity);
    }
}
