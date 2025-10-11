package com.ryuqq.crawlinghub.adapter.persistence.jpa.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * QueryDSL Configuration
 * Provides JPAQueryFactory bean for QueryDSL repositories
 */
@Configuration
public class QueryDslConfig {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Creates JPAQueryFactory bean
     * Used by all QueryDSL repositories for type-safe queries
     *
     * @return JPAQueryFactory instance
     */
    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }

}
