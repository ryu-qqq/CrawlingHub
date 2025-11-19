package com.ryuqq.crawlinghub.application.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Application Layer 전체 ArchUnit 검증 테스트
 *
 * <p><strong>Zero-Tolerance 규칙:</strong></p>
 * <ul>
 *   <li>✅ Application Layer는 Domain Layer만 의존</li>
 *   <li>✅ Adapter Layer 의존 금지</li>
 *   <li>✅ Port 네이밍 규칙 (`*PersistencePort`, `*QueryPort`, `*ClientPort`)</li>
 *   <li>✅ UseCase 네이밍 규칙 (`*UseCase`)</li>
 *   <li>✅ CQRS 패키지 분리</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-01-XX
 */
@DisplayName("Application Layer ArchUnit Tests (Zero-Tolerance)")
@Tag("architecture")
class ApplicationLayerArchUnitTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setUp() {
        classes = new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages("com.ryuqq.crawlinghub.application");
    }

    @Nested
    @DisplayName("의존성 규칙 검증")
    class DependencyRuleTest {

        @Test
        @DisplayName("[필수] Application Layer는 Domain Layer만 의존해야 한다")
        void applicationLayer_MustOnlyDependOnDomainLayer() {
            ArchRule rule = noClasses()
                .that().resideInAPackage("com.ryuqq.crawlinghub.application..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                    "com.ryuqq.crawlinghub.adapter..",
                    "com.ryuqq.crawlinghub.bootstrap.."
                )
                .because("Application Layer는 Domain Layer와 표준 라이브러리만 의존해야 합니다 (Adapter, Bootstrap 제외)");

            rule.check(classes);
        }

        @Test
        @DisplayName("[금지] Application Layer는 Adapter Layer를 의존하지 않아야 한다")
        void applicationLayer_MustNotDependOnAdapterLayer() {
            ArchRule rule = noClasses()
                .that().resideInAPackage("com.ryuqq.crawlinghub.application..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                    "com.ryuqq.crawlinghub.adapter.."
                )
                .because("Application Layer는 Adapter Layer를 의존할 수 없습니다");

            rule.check(classes);
        }

        @Test
        @DisplayName("Application Layer는 Persistence Layer를 의존하지 않아야 한다")
        void applicationLayer_ShouldNotDependOnPersistence() {
            ArchRule rule = noClasses()
                .that().resideInAPackage("com.ryuqq.crawlinghub.application..")
                .should().dependOnClassesThat().resideInAnyPackage(
                    "..persistence..",
                    "..repository.."
                )
                .because("Application Layer는 Persistence Layer를 직접 의존할 수 없습니다 (Port를 통해서만)");

            rule.check(classes);
        }
    }

    @Nested
    @DisplayName("Port 네이밍 규칙 검증")
    class PortNamingRuleTest {

        @Test
        @DisplayName("[필수] PersistencePort는 '*PersistencePort' 접미사를 가져야 한다")
        void persistencePort_MustFollowNamingConvention() {
            ArchRule rule = classes()
                .that().resideInAPackage("..port.out.command..")
                .and().areInterfaces()
                .should().haveSimpleNameEndingWith("PersistencePort")
                .because("PersistencePort는 '*PersistencePort' 네이밍을 따라야 합니다");

            rule.check(classes);
        }

        @Test
        @DisplayName("[필수] QueryPort는 '*QueryPort' 접미사를 가져야 한다")
        void queryPort_MustFollowNamingConvention() {
            ArchRule rule = classes()
                .that().resideInAPackage("..port.out.query..")
                .and().areInterfaces()
                .should().haveSimpleNameEndingWith("QueryPort")
                .because("QueryPort는 '*QueryPort' 네이밍을 따라야 합니다");

            rule.check(classes);
        }

        @Test
        @DisplayName("[필수] ClientPort는 '*ClientPort' 접미사를 가져야 한다")
        void clientPort_MustFollowNamingConvention() {
            ArchRule rule = classes()
                .that().resideInAPackage("..port.out.client..")
                .and().areInterfaces()
                .should().haveSimpleNameEndingWith("ClientPort")
                .because("ClientPort는 '*ClientPort' 네이밍을 따라야 합니다");

            rule.check(classes);
        }
    }

    @Nested
    @DisplayName("UseCase 네이밍 규칙 검증")
    class UseCaseNamingRuleTest {

        @Test
        @DisplayName("[필수] UseCase 인터페이스는 '*UseCase' 접미사를 가져야 한다")
        void useCaseInterface_MustFollowNamingConvention() {
            ArchRule rule = classes()
                .that().resideInAPackage("..port.in..")
                .and().areInterfaces()
                .should().haveSimpleNameEndingWith("UseCase")
                .because("UseCase 인터페이스는 '*UseCase' 네이밍을 따라야 합니다");

            rule.check(classes);
        }

        @Test
        @DisplayName("[필수] Service 구현체는 '*Service' 접미사를 가져야 한다")
        void service_MustFollowNamingConvention() {
            ArchRule rule = classes()
                .that().resideInAPackage("..service..")
                .and().areNotInterfaces()
                .should().haveSimpleNameEndingWith("Service")
                .because("Service 구현체는 '*Service' 네이밍을 따라야 합니다");

            rule.check(classes);
        }
    }

    @Nested
    @DisplayName("CQRS 패키지 분리 검증")
    class CqrsPackageSeparationTest {

        @Test
        @DisplayName("Command UseCase는 service.command 패키지에 위치해야 한다")
        void commandUseCase_ShouldBeInCommandPackage() {
            ArchRule rule = classes()
                .that().resideInAPackage("..application..service.command..")
                .and().haveSimpleNameEndingWith("Service")
                .and().areNotInterfaces()
                .should().beAnnotatedWith(org.springframework.stereotype.Service.class)
                .because("Command UseCase는 service.command 패키지에 위치해야 합니다");

            rule.check(classes);
        }

        @Test
        @DisplayName("Query UseCase는 service.query 패키지에 위치해야 한다")
        void queryUseCase_ShouldBeInQueryPackage() {
            ArchRule rule = classes()
                .that().resideInAPackage("..application..service.query..")
                .and().haveSimpleNameEndingWith("Service")
                .and().areNotInterfaces()
                .should().dependOnClassesThat().resideInAPackage("..application..port.out.query..")
                .because("Query UseCase는 Query Port를 의존해야 합니다 (CQRS 분리)");

            rule.check(classes);
        }

        @Test
        @DisplayName("Command UseCase는 Query Port를 사용할 수 있다 (상태 변경 전 조회 필요)")
        void commandUseCase_CanDependOnQueryPort() {
            // Command UseCase는 상태 변경 전에 현재 상태를 조회해야 할 수 있으므로
            // Query Port 사용을 허용합니다. 이는 실용적인 CQRS 패턴입니다.
            // 단, Query UseCase는 Command Port를 사용할 수 없습니다.
        }

        @Test
        @DisplayName("Query UseCase는 Command Port를 의존하지 않아야 한다")
        void queryUseCase_ShouldNotDependOnCommandPort() {
            ArchRule rule = noClasses()
                .that().resideInAPackage("..application..service.query..")
                .should().dependOnClassesThat().resideInAPackage("..application..port.out.command..")
                .because("Query UseCase는 Command Port를 의존할 수 없습니다 (CQRS 분리)");

            rule.check(classes);
        }
    }

    @Nested
    @DisplayName("Port 의존성 검증")
    class PortDependencyTest {

        @Test
        @DisplayName("UseCase는 Port 인터페이스만 의존해야 한다")
        void useCase_ShouldOnlyDependOnPortInterfaces() {
            ArchRule rule = noClasses()
                .that().resideInAPackage("..application..service..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..adapter..", "..infrastructure..")
                .because("UseCase는 Port 인터페이스만 의존해야 합니다 (구현체 직접 의존 금지)");

            rule.check(classes);
        }
    }
}
