package com.ryuqq.crawlinghub.adapter.out.persistence.seller.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.seller.entity.ProductCountHistoryEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.seller.mapper.ProductCountHistoryMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.seller.repository.ProductCountHistoryQueryRepository;
import com.ryuqq.crawlinghub.application.seller.port.out.LoadProductCountHistoryPort;
import com.ryuqq.crawlinghub.domain.seller.MustitSellerId;
import com.ryuqq.crawlinghub.domain.seller.history.ProductCountHistory;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ProductCountHistoryQueryAdapter - Query Adapter (읽기 전용)
 *
 * <p><strong>CQRS 패턴 적용 - Query 작업만 수행 ⭐</strong></p>
 * <ul>
 *   <li>QueryDSL 기반 최적화 조회</li>
 *   <li>N+1 문제 방지</li>
 *   <li>DTO Projection</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@Component
public class ProductCountHistoryQueryAdapter implements LoadProductCountHistoryPort {

    private final ProductCountHistoryQueryRepository queryRepository;
    private final ProductCountHistoryMapper mapper;

    public ProductCountHistoryQueryAdapter(
        ProductCountHistoryQueryRepository queryRepository,
        ProductCountHistoryMapper mapper
    ) {
        this.queryRepository = queryRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductCountHistory> loadHistories(MustitSellerId sellerId, int page, int size) {
        int offset = page * size;
        List<ProductCountHistoryEntity> entities = queryRepository.findHistoriesBySellerId(
            sellerId.value(),
            offset,
            size
        );
        return entities.stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countHistories(MustitSellerId sellerId) {
        return queryRepository.countHistoriesBySellerId(sellerId.value());
    }
}

