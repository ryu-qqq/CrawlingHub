package com.ryuqq.crawlinghub.adapter.out.persistence.image.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.image.mapper.CrawledProductImageJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.image.repository.CrawledProductImageQueryDslRepository;
import com.ryuqq.crawlinghub.application.product.port.out.query.CrawledProductImageQueryPort;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductImage;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.ImageType;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * CrawledProductImageQueryAdapter - 이미지 조회 Adapter
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawledProductImageQueryAdapter implements CrawledProductImageQueryPort {

    private final CrawledProductImageQueryDslRepository queryDslRepository;
    private final CrawledProductImageJpaEntityMapper mapper;

    public CrawledProductImageQueryAdapter(
            CrawledProductImageQueryDslRepository queryDslRepository,
            CrawledProductImageJpaEntityMapper mapper) {
        this.queryDslRepository = queryDslRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<CrawledProductImage> findById(Long imageId) {
        return queryDslRepository.findById(imageId).map(mapper::toDomain);
    }

    @Override
    public List<CrawledProductImage> findByCrawledProductId(CrawledProductId crawledProductId) {
        return queryDslRepository.findByCrawledProductId(crawledProductId.value()).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<CrawledProductImage> findByCrawledProductIdAndImageType(
            CrawledProductId crawledProductId, ImageType imageType) {
        return queryDslRepository
                .findByCrawledProductIdAndImageType(crawledProductId.value(), imageType)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<CrawledProductImage> findByCrawledProductIdAndOriginalUrl(
            CrawledProductId crawledProductId, String originalUrl) {
        return queryDslRepository
                .findByCrawledProductIdAndOriginalUrl(crawledProductId.value(), originalUrl)
                .map(mapper::toDomain);
    }

    @Override
    public List<String> findExistingOriginalUrls(
            CrawledProductId crawledProductId, List<String> originalUrls) {
        return queryDslRepository.findExistingOriginalUrls(crawledProductId.value(), originalUrls);
    }

    @Override
    public boolean existsByCrawledProductIdAndOriginalUrl(
            CrawledProductId crawledProductId, String originalUrl) {
        return queryDslRepository.existsByCrawledProductIdAndOriginalUrl(
                crawledProductId.value(), originalUrl);
    }
}
