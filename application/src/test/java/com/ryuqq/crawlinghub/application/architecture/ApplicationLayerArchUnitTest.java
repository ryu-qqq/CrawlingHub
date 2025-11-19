package com.ryuqq.crawlinghub.application.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

/**
 * Application Layer 전체 ArchUnit 검증 테스트
 *
 * <p><strong>Zero-Tolerance 규칙:</strong></p>
 * <ul>
 *   <li>✅ Application Layer는 Domain Layer만 의존</li>
 *   <li>✅ Adapter Layer 의존 금지</li>
 *   <li>✅ Port 네이밍 규칙 (`*PersistencePort`, `*QueryPort`, `*ClientPort`)</li>
 *   <li>✅ UseCase 네이밍 규칙 (`*UseCase`)</li>
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
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.ryuqq.crawlinghub.application");
    }

    /**
     * 규칙 1: Application Layer는 Domain Layer만 의존해야 함
     */
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

    /**
     * 규칙 2: Application Layer는 Adapter Layer를 의존하지 않아야 함
     */
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

    /**
     * 규칙 3: PersistencePort 네이밍 규칙
     */
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

    /**
     * 규칙 4: QueryPort 네이밍 규칙
     */
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

    /**
     * 규칙 5: ClientPort 네이밍 규칙
     */
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

    /**
     * 규칙 6: UseCase 네이밍 규칙
     */
    @Test
    @DisplayName("[필수] UseCase는 '*UseCase' 접미사를 가져야 한다")
    void useCase_MustFollowNamingConvention() {
        ArchRule rule = classes()
            .that().resideInAPackage("..usecase..")
            .and().areNotInterfaces()
            .and().areNotAnnotatedWith("org.springframework.scheduling.annotation.Scheduled")
            .and().haveSimpleNameNotContaining("Processor")
            .and().haveSimpleNameNotContaining("Fixture")
            .and().areNotInnerClasses()
            .should().haveSimpleNameEndingWith("UseCase")
            .because("UseCase는 '*UseCase' 네이밍을 따라야 합니다 (스케줄러, Processor, Fixture, 내부 클래스 제외)");

        rule.check(classes);
    }
}

