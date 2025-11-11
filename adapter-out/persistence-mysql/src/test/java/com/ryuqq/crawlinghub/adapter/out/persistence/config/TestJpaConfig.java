package com.ryuqq.crawlinghub.adapter.out.persistence.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * JPA 통합 테스트를 위한 설정
 *
 * <p>@DataJpaTest에서 JPAQueryFactory Bean을 제공합니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-11-11
 */
@TestConfiguration
public class TestJpaConfig {

    @PersistenceContext
    private EntityManager entityManager;

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }
}
