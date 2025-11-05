package com.ryuqq.crawlinghub.adapter.out.persistence.seller.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.seller.entity.MustitSellerEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.seller.mapper.MustitSellerMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.seller.repository.MustitSellerJpaRepository;
import com.ryuqq.crawlinghub.application.seller.port.out.SaveSellerPort;
import com.ryuqq.crawlinghub.domain.seller.MustitSeller;

import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seller Command Adapter - CQRS Command Adapter (쓰기 전용)
 *
 * <p><strong>CQRS 패턴 적용 - Command 작업만 수행 ⭐</strong></p>
 * <ul>
 *   <li>✅ Write 작업 전용 (save)</li>
 *   <li>✅ Domain Aggregate를 Entity로 변환하여 저장</li>
 *   <li>✅ 저장된 Entity를 Domain Aggregate로 변환하여 반환</li>
 * </ul>
 *
 * <p><strong>주의사항:</strong></p>
 * <ul>
 *   <li>❌ Read 작업은 SellerQueryAdapter에서 처리</li>
 *   <li>❌ Query 작업은 이 Adapter에서 금지</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@Component
public class SellerCommandAdapter implements SaveSellerPort {

    private final MustitSellerJpaRepository jpaRepository;
    private final MustitSellerMapper mapper;

    /**
     * Adapter 생성자
     *
     * @param jpaRepository JPA Repository
     * @param mapper Domain ↔ Entity 변환 Mapper
     */
    public SellerCommandAdapter(
        MustitSellerJpaRepository jpaRepository,
        MustitSellerMapper mapper
    ) {
        this.jpaRepository = Objects.requireNonNull(jpaRepository, "jpaRepository must not be null");
        this.mapper = Objects.requireNonNull(mapper, "mapper must not be null");
    }

    /**
     * 셀러 저장 (신규 생성 또는 수정)
     *
     * <p>Domain Aggregate를 Entity로 변환하여 저장한 후,
     * 저장된 Entity를 다시 Domain Aggregate로 변환하여 반환합니다.</p>
     *
     * @param seller 저장할 셀러 Aggregate (null 불가)
     * @return 저장된 셀러 Aggregate (ID 포함)
     * @throws IllegalArgumentException seller가 null인 경우
     */
    @Override
    @Transactional
    public MustitSeller save(MustitSeller seller) {
        Objects.requireNonNull(seller, "seller must not be null");

        MustitSellerEntity entity = mapper.toEntity(seller);
        MustitSellerEntity savedEntity = jpaRepository.save(entity);

        return mapper.toDomain(savedEntity);
    }
}

