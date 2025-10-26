package com.ryuqq.crawlinghub.adapter.out.persistence.mustit.seller.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.mustit.seller.entity.MustitSellerEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.mustit.seller.mapper.MustitSellerMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.mustit.seller.repository.MustitSellerJpaRepository;
import com.ryuqq.crawlinghub.application.mustit.seller.port.out.LoadMustitSellerPort;
import com.ryuqq.crawlinghub.application.mustit.seller.port.out.SaveMustitSellerPort;
import com.ryuqq.crawlinghub.domain.mustit.seller.MustitSeller;
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

    /**
     * Adapter 생성자
     *
     * @param jpaRepository JPA Repository
     * @param mapper        Domain ↔ Entity 변환 Mapper
     */
    public MustitSellerPersistenceAdapter(
            MustitSellerJpaRepository jpaRepository,
            MustitSellerMapper mapper
    ) {
        this.jpaRepository = Objects.requireNonNull(jpaRepository, "jpaRepository must not be null");
        this.mapper = Objects.requireNonNull(mapper, "mapper must not be null");
    }

    /**
     * 셀러를 저장합니다.
     * <p>
     * Domain Aggregate를 Entity로 변환하여 저장한 후,
     * 저장된 Entity를 다시 Domain Aggregate로 변환하여 반환합니다.
     * </p>
     *
     * @param seller 저장할 셀러 Aggregate
     * @return 저장된 셀러 Aggregate
     * @throws IllegalArgumentException seller가 null인 경우
     */
    @Override
    public MustitSeller save(MustitSeller seller) {
        Objects.requireNonNull(seller, "seller must not be null");

        MustitSellerEntity entity = mapper.toEntity(seller);
        MustitSellerEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
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
