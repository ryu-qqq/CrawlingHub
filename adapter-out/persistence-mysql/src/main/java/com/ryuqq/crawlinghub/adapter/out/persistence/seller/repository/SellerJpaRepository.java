package com.ryuqq.crawlinghub.adapter.out.persistence.seller.repository;

import com.ryuqq.crawlinghub.adapter.out.persistence.seller.entity.SellerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * SellerJpaRepository - Seller JPA Repository
 *
 * <p>Spring Data JPA Repository로서 Seller Entity의 기본 CRUD를 담당합니다.
 *
 * <p><strong>제공 메서드 (Command 전용):</strong>
 *
 * <ul>
 *   <li>save(entity): 저장/수정 (INSERT/UPDATE)
 *   <li>delete(entity): 삭제 (DELETE)
 *   <li>deleteById(id): ID로 삭제
 * </ul>
 *
 * <p><strong>Query 작업:</strong>
 *
 * <ul>
 *   <li>모든 Query 작업은 SellerQueryDslRepository 사용
 *   <li>findById(), findAll() 등도 QueryDslRepository에서
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public interface SellerJpaRepository extends JpaRepository<SellerJpaEntity, Long> {
    // ❌ Query Method 추가 금지
    // ❌ @Query 추가 금지
    // ❌ QuerydslPredicateExecutor 상속 금지
}
