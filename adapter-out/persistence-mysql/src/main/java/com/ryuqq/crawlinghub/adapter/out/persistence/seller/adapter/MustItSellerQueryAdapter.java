package com.ryuqq.crawlinghub.adapter.out.persistence.seller.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.seller.repository.MustItSellerQueryDslRepository;
import com.ryuqq.crawlinghub.application.seller.dto.query.SellerQueryDto;
import com.ryuqq.crawlinghub.application.seller.port.out.LoadSellerPort;
import com.ryuqq.crawlinghub.domain.seller.MustItSellerId;

import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seller Query Adapter - CQRS Query Adapter (읽기 전용)
 *
 * <p><strong>CQRS 패턴 적용 - Query 작업만 수행 ⭐</strong></p>
 * <ul>
 *   <li>✅ Read 작업 전용 (findById, findByCode)</li>
 *   <li>✅ QueryDSL DTO Projection으로 직접 조회 (Domain Model 거치지 않음)</li>
 *   <li>✅ N+1 문제 방지</li>
 *   <li>✅ SellerQueryDslRepository 사용</li>
 * </ul>
 *
 * <p><strong>주의사항:</strong></p>
 * <ul>
 *   <li>❌ Write 작업은 SellerCommandAdapter에서 처리</li>
 *   <li>❌ Command 작업은 이 Adapter에서 금지</li>
 *   <li>❌ Domain Model 변환 없이 DTO 직접 반환</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@Component
public class MustItSellerQueryAdapter implements LoadSellerPort {

    private final MustItSellerQueryDslRepository queryDslRepository;

    /**
     * Adapter 생성자
     *
     * @param queryDslRepository QueryDSL Repository
     */
    public MustItSellerQueryAdapter(MustItSellerQueryDslRepository queryDslRepository) {
        this.queryDslRepository = Objects.requireNonNull(queryDslRepository, "queryDslRepository must not be null");
    }

    /**
     * ID로 셀러 조회
     *
     * <p>QueryDSL DTO Projection으로 직접 조회하여 Domain Model을 거치지 않습니다.</p>
     *
     * @param id 셀러 ID (null 불가)
     * @return 셀러 Query DTO (없으면 Optional.empty())
     * @throws IllegalArgumentException id가 null인 경우
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<SellerQueryDto> findById(MustItSellerId id) {
        Objects.requireNonNull(id, "id must not be null");

        return queryDslRepository.findById(id.value());
    }

    /**
     * 셀러 코드로 셀러 조회
     *
     * <p>인덱스(idx_seller_id)를 활용한 최적화 쿼리로 QueryDSL DTO Projection으로 직접 조회합니다.</p>
     *
     * @param code 셀러 코드 (null, blank 불가)
     * @return 셀러 Query DTO (없으면 Optional.empty())
     * @throws IllegalArgumentException code가 null 또는 blank인 경우
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<SellerQueryDto> findByCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("code must not be null or blank");
        }

        return queryDslRepository.findByCode(code);
    }

    /**
     * 셀러 코드 존재 여부 확인
     *
     * <p>QueryDSL selectOne() + fetchFirst()로 EXISTS 쿼리를 최적화합니다.</p>
     * <p>COUNT 쿼리 대신 EXISTS를 사용하여 성능을 최적화합니다.</p>
     *
     * @param code 셀러 코드 (null, blank 불가)
     * @return 존재 여부
     * @throws IllegalArgumentException code가 null 또는 blank인 경우
     */
    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("code must not be null or blank");
        }

        return queryDslRepository.existsByCode(code);
    }
}

