package com.ryuqq.crawlinghub.adapter.out.redis.architecture.adapter.cache;

import static com.ryuqq.crawlinghub.adapter.out.redis.architecture.ArchUnitPackageConstants.DOMAIN_ALL;
import static com.ryuqq.crawlinghub.adapter.out.redis.architecture.ArchUnitPackageConstants.REDIS;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * CacheAdapterArchTest - Cache Adapter 아키텍처 규칙 검증
 *
 * <p>cache-adapter-guide.md 규칙을 ArchUnit으로 검증합니다.
 *
 * <p><strong>검증 항목:</strong>
 *
 * <ul>
 *   <li>클래스 구조: @Component, CachePort 구현
 *   <li>의존성: RedisTemplate, ObjectMapper
 *   <li>금지 사항: @Transactional, KEYS 명령어
 *   <li>메서드: SCAN 기반 evictByPattern
 * </ul>
 *
 * @author Development Team
 * @since 1.0.0
 */
@DisplayName("CacheAdapter 아키텍처 규칙 검증")
class CacheAdapterArchTest {

    private static JavaClasses allClasses;
    private static JavaClasses cacheAdapterClasses;

    /**
     * 특화 Adapter 제외 목록
     *
     * <p>일반 CachePort 규칙을 따르지 않는 특화된 Adapter들:
     *
     * <ul>
     *   <li>RefreshTokenCacheAdapter - 토큰 전용 Adapter (RefreshTokenCacheCommandPort/QueryPort 구현)
     * </ul>
     */
    private static final java.util.Set<String> EXCLUDED_ADAPTERS =
            java.util.Set.of("RefreshTokenCacheAdapter");

    @BeforeAll
    static void setUp() {
        allClasses =
                new ClassFileImporter()
                        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                        .importPackages(REDIS);

        cacheAdapterClasses =
                allClasses.that(
                        DescribedPredicate.describe(
                                "Cache Adapter 클래스 (특화 Adapter 제외)",
                                javaClass ->
                                        javaClass.getSimpleName().endsWith("CacheAdapter")
                                                && !javaClass.isInterface()
                                                && !EXCLUDED_ADAPTERS.contains(
                                                        javaClass.getSimpleName())));
    }

    // ========================================================================
    // 1. 클래스 구조 규칙
    // ========================================================================

    @Nested
    @DisplayName("1. 클래스 구조 규칙")
    class ClassStructureRules {

        @Test
        @DisplayName("규칙 1-1: CacheAdapter는 클래스여야 합니다")
        void cacheAdapter_MustBeClass() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("CacheAdapter")
                            .and()
                            .resideInAPackage("..cache.adapter..")
                            .should()
                            .notBeInterfaces()
                            .allowEmptyShould(true)
                            .because("Cache Adapter는 클래스로 정의되어야 합니다");

            rule.check(cacheAdapterClasses);
        }

        @Test
        @DisplayName("규칙 1-2: @Component 어노테이션이 필수입니다")
        void cacheAdapter_MustHaveComponentAnnotation() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("CacheAdapter")
                            .and()
                            .resideInAPackage("..cache.adapter..")
                            .should()
                            .beAnnotatedWith(Component.class)
                            .allowEmptyShould(true)
                            .because("Cache Adapter는 @Component 어노테이션이 필수입니다");

            rule.check(cacheAdapterClasses);
        }

        @Test
        @DisplayName("규칙 1-3: CachePort 인터페이스를 구현해야 합니다")
        void cacheAdapter_MustImplementCachePort() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("CacheAdapter")
                            .and()
                            .resideInAPackage("..cache.adapter..")
                            .should(
                                    ArchCondition.from(
                                            DescribedPredicate.describe(
                                                    "CachePort 인터페이스 구현",
                                                    javaClass ->
                                                            javaClass.getAllRawInterfaces().stream()
                                                                    .anyMatch(
                                                                            iface ->
                                                                                    iface.getName()
                                                                                            .contains(
                                                                                                    "CachePort")))))
                            .allowEmptyShould(true)
                            .because("Cache Adapter는 CachePort 인터페이스를 구현해야 합니다");

            rule.check(cacheAdapterClasses);
        }

        @Test
        @DisplayName("규칙 1-4: 모든 필드는 final이어야 합니다")
        void cacheAdapter_AllFieldsMustBeFinal() {
            ArchRule rule =
                    fields().that()
                            .areDeclaredInClassesThat()
                            .haveSimpleNameEndingWith("CacheAdapter")
                            .and()
                            .areDeclaredInClassesThat()
                            .resideInAPackage("..cache.adapter..")
                            .and()
                            .areNotStatic()
                            .should()
                            .beFinal()
                            .allowEmptyShould(true)
                            .because("Cache Adapter의 모든 인스턴스 필드는 final로 불변성을 보장해야 합니다");

            rule.check(cacheAdapterClasses);
        }
    }

    // ========================================================================
    // 2. 의존성 규칙
    // ========================================================================

    @Nested
    @DisplayName("2. 의존성 규칙")
    class DependencyRules {

        @Test
        @DisplayName("규칙 2-1: RedisTemplate 의존성이 필수입니다")
        void cacheAdapter_MustDependOnRedisTemplate() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("CacheAdapter")
                            .and()
                            .resideInAPackage("..cache.adapter..")
                            .should(
                                    ArchCondition.from(
                                            DescribedPredicate.describe(
                                                    "RedisTemplate 필드",
                                                    javaClass ->
                                                            javaClass.getAllFields().stream()
                                                                    .anyMatch(
                                                                            field ->
                                                                                    field.getRawType()
                                                                                            .getName()
                                                                                            .contains(
                                                                                                    "RedisTemplate")))))
                            .allowEmptyShould(true)
                            .because("Cache Adapter는 RedisTemplate 의존성이 필수입니다 (Lettuce)");

            rule.check(cacheAdapterClasses);
        }

        @Test
        @DisplayName("규칙 2-2: ObjectMapper 의존성이 필수입니다")
        void cacheAdapter_MustDependOnObjectMapper() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("CacheAdapter")
                            .and()
                            .resideInAPackage("..cache.adapter..")
                            .should(
                                    ArchCondition.from(
                                            DescribedPredicate.describe(
                                                    "ObjectMapper 필드",
                                                    javaClass ->
                                                            javaClass.getAllFields().stream()
                                                                    .anyMatch(
                                                                            field ->
                                                                                    field.getRawType()
                                                                                            .getName()
                                                                                            .contains(
                                                                                                    "ObjectMapper")))))
                            .allowEmptyShould(true)
                            .because("Cache Adapter는 ObjectMapper 의존성이 필수입니다 (JSON 직렬화)");

            rule.check(cacheAdapterClasses);
        }

        @Test
        @DisplayName("규칙 2-3: RedissonClient 의존성이 금지됩니다")
        void cacheAdapter_MustNotDependOnRedisson() {
            ArchRule rule =
                    noClasses()
                            .that()
                            .haveSimpleNameEndingWith("CacheAdapter")
                            .and()
                            .resideInAPackage("..cache.adapter..")
                            .should()
                            .dependOnClassesThat()
                            .haveNameMatching(".*Redisson.*")
                            .allowEmptyShould(true)
                            .because(
                                    "Cache Adapter는 Lettuce(RedisTemplate)만 사용해야 합니다. Redisson은"
                                            + " Lock 전용입니다");

            rule.check(cacheAdapterClasses);
        }
    }

    // ========================================================================
    // 3. 메서드 규칙
    // ========================================================================

    @Nested
    @DisplayName("3. 메서드 규칙")
    class MethodRules {

        @Test
        @DisplayName("규칙 3-1: evictByPattern 메서드가 필수입니다")
        void cacheAdapter_MustHaveEvictByPatternMethod() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("CacheAdapter")
                            .and()
                            .resideInAPackage("..cache.adapter..")
                            .should(
                                    ArchCondition.from(
                                            DescribedPredicate.describe(
                                                    "evictByPattern 메서드",
                                                    javaClass ->
                                                            javaClass.getMethods().stream()
                                                                    .anyMatch(
                                                                            method ->
                                                                                    method.getName()
                                                                                            .equals(
                                                                                                    "evictByPattern")))))
                            .allowEmptyShould(true)
                            .because("패턴 기반 캐시 무효화 메서드가 필수입니다");

            rule.check(cacheAdapterClasses);
        }

        @Test
        @DisplayName("규칙 3-2: scanKeys private 메서드가 존재해야 합니다 (SCAN 사용)")
        void cacheAdapter_MustHaveScanKeysMethod() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("CacheAdapter")
                            .and()
                            .resideInAPackage("..cache.adapter..")
                            .should(
                                    ArchCondition.from(
                                            DescribedPredicate.describe(
                                                    "scanKeys 메서드 (SCAN 기반)",
                                                    javaClass ->
                                                            javaClass.getMethods().stream()
                                                                    .anyMatch(
                                                                            method ->
                                                                                    method.getName()
                                                                                            .equals(
                                                                                                    "scanKeys")))))
                            .allowEmptyShould(true)
                            .because("KEYS 명령어 대신 SCAN을 사용하는 scanKeys 메서드가 필수입니다");

            rule.check(cacheAdapterClasses);
        }
    }

    // ========================================================================
    // 4. 금지 사항 규칙
    // ========================================================================

    @Nested
    @DisplayName("4. 금지 사항 규칙")
    class ProhibitionRules {

        @Test
        @DisplayName("규칙 4-1: @Transactional 사용이 금지됩니다")
        void cacheAdapter_MustNotBeTransactional() {
            ArchRule classRule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("CacheAdapter")
                            .and()
                            .resideInAPackage("..cache.adapter..")
                            .should()
                            .notBeAnnotatedWith(Transactional.class)
                            .allowEmptyShould(true)
                            .because("Cache Adapter에 @Transactional 사용 금지");

            ArchRule methodRule =
                    methods()
                            .that()
                            .areDeclaredInClassesThat()
                            .haveSimpleNameEndingWith("CacheAdapter")
                            .should()
                            .notBeAnnotatedWith(Transactional.class)
                            .allowEmptyShould(true)
                            .because("Cache Adapter 메서드에 @Transactional 사용 금지");

            classRule.check(cacheAdapterClasses);
            methodRule.check(cacheAdapterClasses);
        }

        @Test
        @DisplayName("규칙 4-2: 비즈니스 로직 포함이 금지됩니다")
        void cacheAdapter_MustNotContainBusinessLogic() {
            ArchRule rule =
                    noClasses()
                            .that()
                            .haveSimpleNameEndingWith("CacheAdapter")
                            .and()
                            .resideInAPackage("..cache.adapter..")
                            .should()
                            .dependOnClassesThat()
                            .resideInAPackage(DOMAIN_ALL)
                            .allowEmptyShould(true)
                            .because("Cache Adapter는 비즈니스 로직을 포함하지 않아야 합니다");

            rule.check(cacheAdapterClasses);
        }

        @Test
        @DisplayName("규칙 4-3: DB 접근이 금지됩니다")
        void cacheAdapter_MustNotAccessDatabase() {
            ArchRule rule =
                    noClasses()
                            .that()
                            .haveSimpleNameEndingWith("CacheAdapter")
                            .and()
                            .resideInAPackage("..cache.adapter..")
                            .should()
                            .dependOnClassesThat()
                            .haveNameMatching(".*(Repository|JpaRepository|EntityManager).*")
                            .allowEmptyShould(true)
                            .because("Cache Adapter는 DB에 직접 접근하지 않습니다");

            rule.check(cacheAdapterClasses);
        }

        @Test
        @DisplayName("규칙 4-4: 로깅이 금지됩니다")
        void cacheAdapter_MustNotContainLogging() {
            ArchRule rule =
                    noClasses()
                            .that()
                            .haveSimpleNameEndingWith("CacheAdapter")
                            .and()
                            .resideInAPackage("..cache.adapter..")
                            .should()
                            .accessClassesThat()
                            .haveNameMatching(".*Logger.*")
                            .allowEmptyShould(true)
                            .because("Cache Adapter는 로깅을 포함하지 않습니다. AOP로 처리하세요");

            rule.check(cacheAdapterClasses);
        }
    }
}
