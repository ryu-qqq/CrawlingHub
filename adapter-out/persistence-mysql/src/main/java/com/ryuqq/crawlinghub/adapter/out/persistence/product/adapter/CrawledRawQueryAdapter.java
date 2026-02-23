package com.ryuqq.crawlinghub.adapter.out.persistence.product.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.product.entity.CrawledRawJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.mapper.CrawledRawJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.repository.CrawledRawQueryDslRepository;
import com.ryuqq.crawlinghub.application.product.port.out.query.CrawledRawQueryPort;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledRaw;
import com.ryuqq.crawlinghub.domain.product.id.CrawledRawId;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlType;
import com.ryuqq.crawlinghub.domain.product.vo.RawDataStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * CrawledRawQueryAdapter - CrawledRaw Query Adapter
 *
 * <p>CQRS의 Query(읽기) 담당 Adapter입니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawledRawQueryAdapter implements CrawledRawQueryPort {

    private final CrawledRawQueryDslRepository queryDslRepository;
    private final CrawledRawJpaEntityMapper mapper;

    public CrawledRawQueryAdapter(
            CrawledRawQueryDslRepository queryDslRepository, CrawledRawJpaEntityMapper mapper) {
        this.queryDslRepository = queryDslRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<CrawledRaw> findById(CrawledRawId id) {
        return queryDslRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public List<CrawledRaw> findByStatusAndType(
            RawDataStatus status, CrawlType crawlType, int limit) {
        List<CrawledRawJpaEntity> entities =
                queryDslRepository.findByStatusAndType(status, crawlType, limit);
        return entities.stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<CrawledRaw> findBySellerIdAndItemNoAndType(
            long sellerId, long itemNo, CrawlType crawlType) {
        return queryDslRepository
                .findBySellerIdAndItemNoAndType(sellerId, itemNo, crawlType)
                .map(mapper::toDomain);
    }
}
