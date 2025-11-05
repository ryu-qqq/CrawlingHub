package com.ryuqq.crawlinghub.adapter.out.persistence.seller.repository;

import com.ryuqq.crawlinghub.adapter.out.persistence.seller.entity.ProductCountHistoryEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * ProductCountHistoryJpaRepository - Command Repository (JPA)
 *
 * <p>Command 작업만 수행 (쓰기 전용) ⭐</p>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@Repository
public interface ProductCountHistoryJpaRepository extends JpaRepository<ProductCountHistoryEntity, Long> {
    // Command 작업만 수행 (save, delete 등)
    // 조회 메서드는 없음 ⭐
}

