package com.ryuqq.crawlinghub.application.architecture.service;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Query UseCase 전용 ArchUnit 규칙.
 *
 * <p>읽기 전용 UseCase는 service.query 패키지에만 존재하며, Command Port나 Command Service에 의존하지 않는다.</p>
 */
@DisplayName("Query UseCase ArchUnit Tests")
@Tag("architecture")
class QueryUseCaseArchTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setUp() {
        classes = new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages("com.ryuqq.crawlinghub.application");
    }

    @Test
    @DisplayName("Query UseCase는 service.query 패키지 내 Service Bean이어야 한다")
    void queryUseCase_ShouldBeServiceInQueryPackage() {
        ArchRule rule = classes()
            .that().resideInAPackage("..application..service.query..")
            .and().areNotInterfaces()
            .and().haveSimpleNameEndingWith("Service")
            .should().beAnnotatedWith(Service.class)
            .because("Query UseCase는 읽기 전용 트랜잭션을 가진 서비스 Bean이어야 한다");

        rule.check(classes);
    }

    @Test
    @DisplayName("Query UseCase의 public 메서드는 @Transactional을 사용하지 않아야 한다")
    void queryUseCase_PublicMethodsMustNotBeTransactional() {
        // TransactionManager 패턴 적용: UseCase Service는 @Transactional을 사용하지 않고
        // TransactionManager를 통해 트랜잭션을 관리합니다.
        ArchRule rule = methods()
            .that().areDeclaredInClassesThat().resideInAPackage("..application..service.query..")
            .and().arePublic()
            .should().notBeAnnotatedWith(Transactional.class)
            .because("Query UseCase는 TransactionManager 패턴을 사용하므로 @Transactional을 직접 사용하지 않습니다");

        rule.check(classes);
    }

    @Test
    @DisplayName("Query UseCase는 Command Port/Service를 의존하지 않아야 한다")
    void queryUseCase_ShouldNotDependOnCommandPackages() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..application..service.query..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "..application..service.command..",
                "..application..port.out.command..",
                "..application..port.in.command.."
            )
            .because("Query UseCase는 CQRS 분리를 위해 Command 패키지에 의존할 수 없다");

        rule.check(classes);
    }
}

