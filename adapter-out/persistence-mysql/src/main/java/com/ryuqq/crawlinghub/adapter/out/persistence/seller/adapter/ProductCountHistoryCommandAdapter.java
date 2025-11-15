package com.ryuqq.crawlinghub.adapter.out.persistence.seller.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.seller.entity.ProductCountHistoryEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.seller.mapper.ProductCountHistoryMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.seller.repository.ProductCountHistoryJpaRepository;
import com.ryuqq.crawlinghub.application.seller.port.out.SaveProductCountHistoryPort;
import com.ryuqq.crawlinghub.domain.seller.history.ProductCountHistory;

import org.springframework.stereotype.Component;

/**
 * ProductCountHistoryCommandAdapter - Command Adapter (쓰기 전용)
 *
 * <p>CQRS 패턴 적용 - Command 작업만 수행 ⭐</p>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@Component
public class ProductCountHistoryCommandAdapter implements SaveProductCountHistoryPort {

    private final ProductCountHistoryJpaRepository jpaRepository;
    private final ProductCountHistoryMapper mapper;

    public ProductCountHistoryCommandAdapter(
        ProductCountHistoryJpaRepository jpaRepository,
        ProductCountHistoryMapper mapper
    ) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public ProductCountHistory saveHistory(ProductCountHistory history) {
        ProductCountHistoryEntity entity = mapper.toEntity(history);
        ProductCountHistoryEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }
}

