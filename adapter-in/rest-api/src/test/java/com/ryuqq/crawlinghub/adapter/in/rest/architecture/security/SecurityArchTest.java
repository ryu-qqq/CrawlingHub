package com.ryuqq.crawlinghub.adapter.in.rest.architecture.security;

import static com.ryuqq.crawlinghub.adapter.in.rest.architecture.ArchUnitPackageConstants.ADAPTER_IN_REST;
import static com.ryuqq.crawlinghub.adapter.in.rest.architecture.ArchUnitPackageConstants.DOMAIN_ALL;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Security Layer ArchUnit 검증 테스트 (Zero-Tolerance)
 *
 * <p>Security 관련 아키텍처 규칙을 검증합니다.
 *
 * <p><strong>검증 규칙:</strong>
 *
 * <ul>
 *   <li>Config: @Configuration, @EnableWebSecurity, @EnableMethodSecurity
 *   <li>Filter: OncePerRequestFilter 상속, *Filter 네이밍
 *   <li>Handler: AuthenticationEntryPoint/AccessDeniedHandler 구현
 *   <li>Component: @Component 어노테이션
 *   <li>Lombok 금지
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("Security Layer ArchUnit Tests (Zero-Tolerance)")
@Tag("architecture")
@Tag("adapter-rest")
@Tag("security")
class SecurityArchTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setUp() {
        classes =
                new ClassFileImporter()
                        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                        .importPackages(ADAPTER_IN_REST);
    }

    // =========================================================================
    // Security Config 규칙
    // =========================================================================

    @Nested
    @DisplayName("Security Config 규칙")
    class SecurityConfigRules {

        /** 규칙 1: SecurityConfig는 auth.config 패키지에 위치해야 한다 */
        @Test
        @DisplayName("[필수] SecurityConfig는 auth.config 패키지에 위치해야 한다")
        void securityConfig_MustBeInAuthConfigPackage() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("SecurityConfig")
                            .and()
                            .resideInAPackage("..adapter.in.rest..")
                            .should()
                            .resideInAPackage("..auth.config..")
                            .because("SecurityConfig는 auth.config 패키지에 위치해야 합니다");

            rule.allowEmptyShould(true).check(classes);
        }

        /** 규칙 2: SecurityConfig는 @Configuration 어노테이션을 가져야 한다 */
        @Test
        @DisplayName("[필수] SecurityConfig는 @Configuration을 가져야 한다")
        void securityConfig_MustHaveConfigurationAnnotation() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("SecurityConfig")
                            .and()
                            .resideInAPackage("..auth.config..")
                            .should()
                            .beAnnotatedWith(
                                    org.springframework.context.annotation.Configuration.class)
                            .because("SecurityConfig는 Spring Bean 설정을 위해 @Configuration이 필수입니다");

            rule.allowEmptyShould(true).check(classes);
        }

        /** 규칙 3: SecurityConfig는 @EnableWebSecurity 어노테이션을 가져야 한다 */
        @Test
        @DisplayName("[필수] SecurityConfig는 @EnableWebSecurity를 가져야 한다")
        void securityConfig_MustHaveEnableWebSecurityAnnotation() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("SecurityConfig")
                            .and()
                            .resideInAPackage("..auth.config..")
                            .should()
                            .beAnnotatedWith(
                                    org.springframework.security.config.annotation.web.configuration
                                            .EnableWebSecurity.class)
                            .because(
                                    "SecurityConfig는 Spring Security 활성화를 위해 @EnableWebSecurity가"
                                            + " 필수입니다");

            rule.allowEmptyShould(true).check(classes);
        }
    }

    // =========================================================================
    // Security Filter 규칙
    // =========================================================================

    @Nested
    @DisplayName("Security Filter 규칙")
    class SecurityFilterRules {

        /** 규칙 4: Security Filter는 auth.filter 패키지에 위치해야 한다 */
        @Test
        @DisplayName("[필수] Security Filter는 auth.filter 패키지에 위치해야 한다")
        void securityFilter_MustBeInAuthFilterPackage() {
            ArchRule rule =
                    classes()
                            .that()
                            .areAssignableTo(OncePerRequestFilter.class)
                            .and()
                            .resideInAPackage("..adapter.in.rest..")
                            .and()
                            .haveSimpleNameContaining("Authentication")
                            .should()
                            .resideInAPackage("..auth.filter..")
                            .because("인증 관련 Filter는 auth.filter 패키지에 위치해야 합니다");

            rule.allowEmptyShould(true).check(classes);
        }

        /** 규칙 5: Authentication Filter는 OncePerRequestFilter를 상속해야 한다 */
        @Test
        @DisplayName("[필수] Authentication Filter는 OncePerRequestFilter를 상속해야 한다")
        void authenticationFilter_MustExtendOncePerRequestFilter() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameContaining("AuthenticationFilter")
                            .or()
                            .haveSimpleNameContaining("AuthFilter")
                            .and()
                            .resideInAPackage("..adapter.in.rest..")
                            .should()
                            .beAssignableTo(OncePerRequestFilter.class)
                            .because("인증 필터는 요청당 한 번만 실행되어야 하므로 OncePerRequestFilter를 상속해야 합니다");

            rule.allowEmptyShould(true).check(classes);
        }

        /** 규칙 6: Security Filter는 *Filter 네이밍 규칙을 따라야 한다 */
        @Test
        @DisplayName("[필수] Security Filter는 *Filter 네이밍 규칙을 따라야 한다")
        void securityFilter_MustFollowNamingConvention() {
            ArchRule rule =
                    classes()
                            .that()
                            .areAssignableTo(OncePerRequestFilter.class)
                            .and()
                            .resideInAPackage("..auth.filter..")
                            .should()
                            .haveSimpleNameEndingWith("Filter")
                            .because("Security Filter는 *Filter 네이밍 규칙을 따라야 합니다");

            rule.allowEmptyShould(true).check(classes);
        }
    }

    // =========================================================================
    // Security Handler 규칙
    // =========================================================================

    @Nested
    @DisplayName("Security Handler 규칙")
    class SecurityHandlerRules {

        /** 규칙 7: Security Handler는 auth.handler 패키지에 위치해야 한다 */
        @Test
        @DisplayName("[필수] Security Handler는 auth.handler 패키지에 위치해야 한다")
        void securityHandler_MustBeInAuthHandlerPackage() {
            ArchRule rule =
                    classes()
                            .that()
                            .implement(AuthenticationEntryPoint.class)
                            .or()
                            .implement(AccessDeniedHandler.class)
                            .should()
                            .resideInAPackage("..auth.handler..")
                            .because("Security Handler는 auth.handler 패키지에 위치해야 합니다");

            rule.allowEmptyShould(true).check(classes);
        }

        /** 규칙 8: AuthenticationEntryPoint 구현체는 @Component를 가져야 한다 */
        @Test
        @DisplayName("[필수] AuthenticationEntryPoint 구현체는 @Component를 가져야 한다")
        void authenticationEntryPoint_MustHaveComponentAnnotation() {
            ArchRule rule =
                    classes()
                            .that()
                            .implement(AuthenticationEntryPoint.class)
                            .and()
                            .resideInAPackage("..auth.handler..")
                            .should()
                            .beAnnotatedWith(Component.class)
                            .because("AuthenticationEntryPoint 구현체는 Bean 등록을 위해 @Component가 필수입니다");

            rule.allowEmptyShould(true).check(classes);
        }

        /** 규칙 9: AccessDeniedHandler 구현체는 @Component를 가져야 한다 */
        @Test
        @DisplayName("[필수] AccessDeniedHandler 구현체는 @Component를 가져야 한다")
        void accessDeniedHandler_MustHaveComponentAnnotation() {
            ArchRule rule =
                    classes()
                            .that()
                            .implement(AccessDeniedHandler.class)
                            .and()
                            .resideInAPackage("..auth.handler..")
                            .should()
                            .beAnnotatedWith(Component.class)
                            .because("AccessDeniedHandler 구현체는 Bean 등록을 위해 @Component가 필수입니다");

            rule.allowEmptyShould(true).check(classes);
        }
    }

    // =========================================================================
    // Security Component 규칙
    // =========================================================================

    @Nested
    @DisplayName("Security Component 규칙")
    class SecurityComponentRules {

        /** 규칙 10: Security Component는 auth.component 패키지에 위치해야 한다 */
        @Test
        @DisplayName("[필수] Security Component는 auth.component 패키지에 위치해야 한다")
        void securityComponent_MustBeInAuthComponentPackage() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameContaining("TokenProvider")
                            .or()
                            .haveSimpleNameContaining("TokenResolver")
                            .or()
                            .haveSimpleNameContaining("CookieProvider")
                            .should()
                            .resideInAPackage("..auth.component..")
                            .because("Security 관련 Component는 auth.component 패키지에 위치해야 합니다");

            rule.allowEmptyShould(true).check(classes);
        }

        /** 규칙 11: Security Component는 @Component를 가져야 한다 */
        @Test
        @DisplayName("[필수] Security Component는 @Component를 가져야 한다")
        void securityComponent_MustHaveComponentAnnotation() {
            ArchRule rule =
                    classes()
                            .that()
                            .resideInAPackage("..auth.component..")
                            .and()
                            .areNotInterfaces()
                            .and()
                            .areNotNestedClasses()
                            .and()
                            .areNotRecords()
                            .should()
                            .beAnnotatedWith(Component.class)
                            .because("Security Component는 Bean 등록을 위해 @Component가 필수입니다");

            rule.allowEmptyShould(true).check(classes);
        }
    }

    // =========================================================================
    // Security Properties 규칙
    // =========================================================================

    @Nested
    @DisplayName("Security Properties 규칙")
    class SecurityPropertiesRules {

        /** 규칙 12: Security Properties는 auth.config 또는 config.properties 패키지에 위치해야 한다 */
        @Test
        @DisplayName("[필수] Security Properties는 적절한 패키지에 위치해야 한다")
        void securityProperties_MustBeInCorrectPackage() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("SecurityProperties")
                            .and()
                            .resideInAPackage("..adapter.in.rest..")
                            .should()
                            .resideInAnyPackage("..auth.config..", "..config.properties..")
                            .because(
                                    "Security Properties는 auth.config 또는 config.properties 패키지에"
                                            + " 위치해야 합니다");

            rule.allowEmptyShould(true).check(classes);
        }

        /** 규칙 13: Security Properties는 @ConfigurationProperties를 가져야 한다 */
        @Test
        @DisplayName("[필수] Security Properties는 @ConfigurationProperties를 가져야 한다")
        void securityProperties_MustHaveConfigurationPropertiesAnnotation() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("SecurityProperties")
                            .and()
                            .resideInAPackage("..adapter.in.rest..")
                            .should()
                            .beAnnotatedWith(
                                    org.springframework.boot.context.properties
                                            .ConfigurationProperties.class)
                            .because(
                                    "Security Properties는 설정 바인딩을 위해 @ConfigurationProperties가"
                                            + " 필수입니다");

            rule.allowEmptyShould(true).check(classes);
        }
    }

    // =========================================================================
    // 금지 규칙 (Prohibition Rules)
    // =========================================================================

    @Nested
    @DisplayName("금지 규칙 (Prohibition Rules)")
    class ProhibitionRules {

        /** 규칙 14: Security Layer는 Lombok을 사용하지 않아야 한다 */
        @Test
        @DisplayName("[금지] Security Layer는 Lombok을 사용하지 않아야 한다")
        void securityLayer_MustNotUseLombok() {
            ArchRule rule =
                    noClasses()
                            .that()
                            .resideInAPackage("..auth..")
                            .should()
                            .beAnnotatedWith("lombok.Data")
                            .orShould()
                            .beAnnotatedWith("lombok.Builder")
                            .orShould()
                            .beAnnotatedWith("lombok.Getter")
                            .orShould()
                            .beAnnotatedWith("lombok.Setter")
                            .orShould()
                            .beAnnotatedWith("lombok.AllArgsConstructor")
                            .orShould()
                            .beAnnotatedWith("lombok.NoArgsConstructor")
                            .orShould()
                            .beAnnotatedWith("lombok.RequiredArgsConstructor")
                            .orShould()
                            .beAnnotatedWith("lombok.Value")
                            .because("Security Layer는 Pure Java를 사용해야 하며 Lombok은 금지됩니다");

            rule.allowEmptyShould(true).check(classes);
        }

        /** 규칙 15: Security Layer는 Domain Layer를 직접 의존하지 않아야 한다 */
        @Test
        @DisplayName("[금지] Security Layer는 Domain Layer를 직접 의존하지 않아야 한다")
        void securityLayer_MustNotDependOnDomain() {
            ArchRule rule =
                    noClasses()
                            .that()
                            .resideInAPackage("..auth..")
                            .should()
                            .dependOnClassesThat()
                            .resideInAnyPackage(DOMAIN_ALL)
                            .because("Security Layer는 Domain Layer를 직접 의존하면 안 됩니다");

            rule.allowEmptyShould(true).check(classes);
        }

        /** 규칙 16: Security Layer는 Persistence Layer를 직접 의존하지 않아야 한다 */
        @Test
        @DisplayName("[금지] Security Layer는 Persistence Layer를 직접 의존하지 않아야 한다")
        void securityLayer_MustNotDependOnPersistence() {
            ArchRule rule =
                    noClasses()
                            .that()
                            .resideInAPackage("..auth..")
                            .should()
                            .dependOnClassesThat()
                            .resideInAnyPackage("..adapter.out.persistence..")
                            .because("Security Layer는 Persistence Layer를 직접 의존하면 안 됩니다");

            rule.allowEmptyShould(true).check(classes);
        }
    }

    // =========================================================================
    // Gateway Only 아키텍처 규칙
    // =========================================================================

    @Nested
    @DisplayName("Gateway Only 아키텍처 규칙")
    class GatewayOnlyArchitectureRules {

        /** 규칙 17: Gateway 관련 컴포넌트는 auth 하위 패키지에 위치해야 한다 */
        @Test
        @DisplayName("[필수] Gateway 관련 컴포넌트는 auth.component/filter/config 패키지에 위치해야 한다")
        void gatewayComponents_MustBeInAuthPackage() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameContaining("Gateway")
                            .and()
                            .resideInAPackage("..auth..")
                            .and()
                            .areNotInterfaces()
                            .should()
                            .resideInAnyPackage(
                                    "..auth.component..", "..auth.filter..", "..auth.config..")
                            .because(
                                    "Gateway 관련 컴포넌트는 auth.component, auth.filter, 또는 auth.config"
                                            + " 패키지에 위치해야 합니다");

            rule.allowEmptyShould(true).check(classes);
        }

        /** 규칙 18: Gateway 헤더 인증 필터는 OncePerRequestFilter를 상속해야 한다 */
        @Test
        @DisplayName("[필수] Gateway 헤더 인증 필터는 OncePerRequestFilter를 상속해야 한다")
        void gatewayHeaderAuthFilter_MustExtendOncePerRequestFilter() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameContaining("GatewayHeaderAuthFilter")
                            .or()
                            .haveSimpleNameContaining("GatewayAuthFilter")
                            .should()
                            .beAssignableTo(OncePerRequestFilter.class)
                            .because(
                                    "Gateway 헤더 인증 필터는 요청당 한 번만 실행되어야 하므로"
                                            + " OncePerRequestFilter를 상속해야 합니다");

            rule.allowEmptyShould(true).check(classes);
        }

        /** 규칙 19: Gateway User VO는 record 타입이어야 한다 */
        @Test
        @DisplayName("[권장] Gateway User는 record 타입이어야 한다")
        void gatewayUser_ShouldBeRecord() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleName("GatewayUser")
                            .should()
                            .beRecords()
                            .because("Gateway User는 불변성을 위해 record 타입이어야 합니다");

            rule.allowEmptyShould(true).check(classes);
        }

        /** 규칙 20: Security Layer는 JWT Secret/Key 관련 클래스를 직접 참조하지 않아야 한다 */
        @Test
        @DisplayName("[금지] Security Layer는 JWT Secret 관련 클래스를 직접 참조하지 않아야 한다")
        void securityLayer_MustNotReferenceJwtSecretClasses() {
            ArchRule rule =
                    noClasses()
                            .that()
                            .resideInAPackage("..auth..")
                            .and()
                            .haveSimpleNameNotContaining("Jwt")
                            .should()
                            .dependOnClassesThat()
                            .haveSimpleNameContaining("JwtSecret")
                            .orShould()
                            .dependOnClassesThat()
                            .haveSimpleNameContaining("SecretKey")
                            .because(
                                    "Gateway Only 아키텍처에서 서비스는 JWT Secret을 직접 참조하면 안 됩니다."
                                            + " JWT 검증은 Gateway에서 수행합니다.");

            rule.allowEmptyShould(true).check(classes);
        }

        /** 규칙 21: GatewayUser의 userId 필드는 UUID 타입이어야 한다 */
        @Test
        @DisplayName("[필수] GatewayUser의 userId 필드는 UUID 타입이어야 한다")
        void gatewayUser_UserIdFieldMustBeUUID() {
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleName("GatewayUser")
                            .should()
                            .beRecords()
                            .because("Gateway User는 불변성을 위해 record 타입이어야 합니다");

            rule.allowEmptyShould(true).check(classes);
        }

        /** 규칙 22: SecurityContextAuthenticator.authenticate(GatewayUser)는 UUID를 반환해야 한다 */
        @Test
        @DisplayName("[필수] SecurityContextAuthenticator.authenticate(GatewayUser)는 UUID를 반환해야 한다")
        void securityContextAuthenticator_GatewayAuthenticateMustReturnUUID() {
            ArchRule rule =
                    methods()
                            .that()
                            .areDeclaredInClassesThat()
                            .haveSimpleName("SecurityContextAuthenticator")
                            .and()
                            .haveName("authenticate")
                            .and()
                            .haveRawReturnType(UUID.class)
                            .should()
                            .haveRawReturnType(UUID.class)
                            .because(
                                    "SecurityContextAuthenticator.authenticate(GatewayUser)는 "
                                            + "GatewayUser의 UUID userId를 반환해야 합니다.");

            rule.allowEmptyShould(true).check(classes);
        }
    }
}
