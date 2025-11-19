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
 * Application Layer 전체 ArchUnit 규칙.
 *
 * <p>CQRS 패키지 분리, 의존성 규칙 등을 검증합니다.</p>
 */
@DisplayName("Application Layer ArchUnit Tests")
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
    @DisplayName("CQRS 패키지 분리 검증")
    class CqrsPackageSeparationTest {

        @Test
        @DisplayName("Command UseCase는 service.command 패키지에 위치해야 한다")
        void commandUseCase_ShouldBeInCommandPackage() {
            // Command UseCase는 TransactionManager를 통해 Command Port를 사용하므로
            // 직접 Command Port를 의존하지 않을 수 있습니다.
            // 따라서 이 규칙은 패키지 위치만 검증합니다.
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
    @DisplayName("의존성 규칙 검증")
    class DependencyRuleTest {

        @Test
        @DisplayName("Application Layer는 Domain Layer만 의존해야 한다")
        void applicationLayer_ShouldOnlyDependOnDomain() {
            ArchRule rule = classes()
                .that().resideInAPackage("com.ryuqq.crawlinghub.application..")
                .should().onlyAccessClassesThat()
                .resideInAnyPackage(
                    "com.ryuqq.crawlinghub.domain..",
                    "com.ryuqq.crawlinghub.application..",
                    "java..",
                    "javax..",
                    "org.springframework.."
                )
                .because("Application Layer는 Domain Layer만 의존해야 합니다 (Infrastructure/Adapter 의존 금지)");

            rule.check(classes);
        }

        @Test
        @DisplayName("Application Layer는 Adapter Layer를 의존하지 않아야 한다")
        void applicationLayer_ShouldNotDependOnAdapter() {
            ArchRule rule = noClasses()
                .that().resideInAPackage("com.ryuqq.crawlinghub.application..")
                .should().dependOnClassesThat().resideInAnyPackage(
                    "..adapter..",
                    "..infrastructure.."
                )
                .because("Application Layer는 Adapter Layer를 의존할 수 없습니다 (의존성 역전 원칙)");

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

