package com.ryuqq.crawlinghub.adapter.out.persistence.product.image.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.product.image.entity.ProductImageOutboxJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.image.mapper.ProductImageOutboxJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.image.repository.ProductImageOutboxJpaRepository;
import com.ryuqq.crawlinghub.application.product.port.out.command.ImageOutboxPersistencePort;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductImageOutbox;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * ImageOutboxCommandAdapter - ImageOutbox Command Adapter
 *
 * <p>CQRS의 Command(쓰기) 담당 Adapter입니다.
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>Domain Aggregate -> JPA Entity 변환
 *   <li>JpaRepository.save() / saveAll() 호출
 * </ul>
 *
 * <p><strong>금지 사항:</strong>
 *
 * <ul>
 *   <li>비즈니스 로직 (Domain에서 처리)
 *   <li>조회 로직 (QueryAdapter로 분리)
 *   <li>@Transactional 어노테이션 (Application Layer에서 관리)
 * </ul>
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

    /**
     * ImageOutbox 저장 (신규 생성)
     *
     * @param outbox 저장할 ImageOutbox
     */
    @Override
    public void persist(CrawledProductImageOutbox outbox) {
        ProductImageOutboxJpaEntity entity = mapper.toEntity(outbox);
        jpaRepository.save(entity);
    }

    /**
     * ImageOutbox 일괄 저장
     *
     * @param outboxes 저장할 ImageOutbox 목록
     */
    @Override
    public void persistAll(List<CrawledProductImageOutbox> outboxes) {
        List<ProductImageOutboxJpaEntity> entities =
                outboxes.stream().map(mapper::toEntity).toList();
        jpaRepository.saveAll(entities);
    }

    /**
     * ImageOutbox 상태 업데이트
     *
     * <p>ID가 있는 Entity를 저장하면 JPA가 UPDATE를 수행합니다.
     *
     * @param outbox 업데이트할 ImageOutbox
     */
    @Override
    public void update(CrawledProductImageOutbox outbox) {
        ProductImageOutboxJpaEntity entity = mapper.toEntity(outbox);
        jpaRepository.save(entity);
    }
}
