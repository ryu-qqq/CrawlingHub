package com.ryuqq.crawlinghub.adapter.out.persistence.image.repository;

import com.ryuqq.crawlinghub.adapter.out.persistence.image.entity.CrawledProductImageJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * CrawledProductImageJpaRepository - 크롤링 상품 이미지 JPA Repository
 *
 * <p>기본 CRUD 및 단순 조회를 제공합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface CrawledProductImageJpaRepository
        extends JpaRepository<CrawledProductImageJpaEntity, Long> {}
