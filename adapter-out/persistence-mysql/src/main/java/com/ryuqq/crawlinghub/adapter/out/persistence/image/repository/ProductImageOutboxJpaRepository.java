package com.ryuqq.crawlinghub.adapter.out.persistence.image.repository;

import com.ryuqq.crawlinghub.adapter.out.persistence.image.entity.ProductImageOutboxJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * ProductImageOutboxJpaRepository - ImageOutbox JPA Repository
 *
 * <p>Spring Data JPA의 기본 CRUD 기능을 제공합니다.
 *
 * <p><strong>사용 범위:</strong>
 *
 * <ul>
 *   <li>CommandAdapter: save() 메서드로 신규/수정 저장
 *   <li>QueryDslRepository: 복잡한 조회는 QueryDSL 사용
 * </ul>
 *
 * <p><strong>금지 사항:</strong>
 *
 * <ul>
 *   <li>비즈니스 로직 포함 금지
 *   <li>복잡한 조회 쿼리 정의 금지 (QueryDslRepository 사용)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Repository
public interface ProductImageOutboxJpaRepository
        extends JpaRepository<ProductImageOutboxJpaEntity, Long> {
    // 기본 CRUD만 사용 - 복잡한 쿼리는 QueryDslRepository에서 처리
}
