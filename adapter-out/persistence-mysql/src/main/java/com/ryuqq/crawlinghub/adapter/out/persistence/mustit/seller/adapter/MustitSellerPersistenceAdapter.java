package com.ryuqq.crawlinghub.adapter.out.persistence.mustit.seller.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.mustit.seller.entity.MustitSellerEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.mustit.seller.mapper.MustitSellerMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.mustit.seller.repository.MustitSellerJpaRepository;
import com.ryuqq.crawlinghub.application.mustit.seller.port.out.LoadMustitSellerPort;
import com.ryuqq.crawlinghub.application.mustit.seller.port.out.SaveMustitSellerPort;
import com.ryuqq.crawlinghub.domain.common.DomainEvent;
import com.ryuqq.crawlinghub.domain.mustit.seller.MustitSeller;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

/**
 * 머스트잇 셀러 Persistence Adapter
 * <p>
 * Application Layer의 Port 인터페이스를 구현하여
 * Domain 객체와 JPA Repository 간의 변환 및 데이터 접근을 담당합니다.
 * </p>
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
@Component
public class MustitSellerPersistenceAdapter implements SaveMustitSellerPort, LoadMustitSellerPort {

    private final MustitSellerJpaRepository jpaRepository;
    private final MustitSellerMapper mapper;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Adapter 생성자
     *
     * @param jpaRepository  JPA Repository
     * @param mapper         Domain ↔ Entity 변환 Mapper
     * @param eventPublisher Spring Application Event Publisher
     */
    public MustitSellerPersistenceAdapter(
            MustitSellerJpaRepository jpaRepository,
            MustitSellerMapper mapper,
            ApplicationEventPublisher eventPublisher
    ) {
        this.jpaRepository = Objects.requireNonNull(jpaRepository, "jpaRepository must not be null");
        this.mapper = Objects.requireNonNull(mapper, "mapper must not be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher must not be null");
    }

    /**
     * 셀러를 저장합니다.
     * <p>
     * Domain Aggregate를 Entity로 변환하여 저장한 후,
     * 저장된 Entity를 다시 Domain Aggregate로 변환하여 반환합니다.
     * Domain Event는 트랜잭션 커밋 후 자동으로 발행됩니다.
     * </p>
     *
     * @param seller 저장할 셀러 Aggregate
     * @return 저장된 셀러 Aggregate
     * @throws IllegalArgumentException seller가 null인 경우
     */
    @Override
    public MustitSeller save(MustitSeller seller) {
        Objects.requireNonNull(seller, "seller must not be null");

        // 1. Domain → Entity 변환 및 저장
        MustitSellerEntity entity = mapper.toEntity(seller);
        MustitSellerEntity savedEntity = jpaRepository.save(entity);

        // 2. Domain Event 발행
        // 실제 발행 타이밍: 리스너 측에서 @TransactionalEventListener(phase = AFTER_COMMIT) 사용 시 트랜잭션 커밋 후 발행
        // 현재는 동기 방식이므로 즉시 발행되지만, 리스너 구현에 따라 비동기/지연 발행 가능
        publishDomainEvents(seller);

        // 3. 저장된 Entity를 Domain으로 변환하여 반환
        MustitSeller savedSeller = mapper.toDomain(savedEntity);

        // 4. 발행된 이벤트 제거 (중복 발행 방지)
        seller.clearDomainEvents();

        return savedSeller;
    }

    /**
     * Domain Event를 발행합니다.
     *
     * @param seller 이벤트를 포함한 셀러 Aggregate
     */
    private void publishDomainEvents(MustitSeller seller) {
        for (DomainEvent event : seller.getDomainEvents()) {
            eventPublisher.publishEvent(event);
        }
    }

    /**
     * sellerId로 셀러를 조회합니다.
     *
     * @param sellerId 셀러 고유 ID
     * @return 조회된 셀러 Aggregate (Optional)
     * @throws IllegalArgumentException sellerId가 null이거나 빈 문자열인 경우
     */
    @Override
    public Optional<MustitSeller> findBySellerId(String sellerId) {
        validateSellerId(sellerId);

        return jpaRepository.findBySellerId(sellerId)
                .map(mapper::toDomain);
    }

    /**
     * sellerId 존재 여부를 확인합니다.
     *
     * @param sellerId 셀러 고유 ID
     * @return 존재 여부
     * @throws IllegalArgumentException sellerId가 null이거나 빈 문자열인 경우
     */
    @Override
    public boolean existsBySellerId(String sellerId) {
        validateSellerId(sellerId);

        return jpaRepository.existsBySellerId(sellerId);
    }

    /**
     * sellerId 유효성 검증
     *
     * @param sellerId 검증할 셀러 ID
     * @throws IllegalArgumentException sellerId가 null이거나 빈 문자열인 경우
     */
    private void validateSellerId(String sellerId) {
        if (sellerId == null || sellerId.isBlank()) {
            throw new IllegalArgumentException("sellerId must not be null or blank");
        }
    }
}
