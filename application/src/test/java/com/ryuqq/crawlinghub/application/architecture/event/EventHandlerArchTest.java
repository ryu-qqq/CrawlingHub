package com.ryuqq.crawlinghub.application.architecture.event;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Event Handler ArchUnit 규칙.
 *
 * <p>Event Handler는 Domain Event를 처리하며, 트랜잭션 커밋 후에 실행되어야 합니다.</p>
 */
@DisplayName("EventHandler ArchUnit Tests")
@Tag("architecture")
class EventHandlerArchTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setUp() {
        classes = new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages("com.ryuqq.crawlinghub.application");
    }

    @Test
    @DisplayName("EventHandler는 @Component 어노테이션을 가져야 한다")
    void eventHandler_ShouldHaveComponentAnnotation() {
        ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("EventHandler")
            .and().areNotInterfaces()
            .should().beAnnotatedWith(Component.class)
            .because("EventHandler는 Spring Bean으로 등록되어야 합니다");

        rule.check(classes);
    }

    @Test
    @DisplayName("EventHandler는 event 패키지에 위치해야 한다")
    void eventHandler_ShouldBeInEventPackage() {
        ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("EventHandler")
            .and().areNotInterfaces()
            .should().resideInAPackage("..application..event..")
            .because("EventHandler는 event 패키지에 위치해야 합니다");

        rule.check(classes);
    }

    @Test
    @DisplayName("EventHandler의 이벤트 처리 메서드는 @TransactionalEventListener를 가져야 한다")
    void eventHandler_ShouldHaveTransactionalEventListener() {
        ArchRule rule = methods()
            .that().areDeclaredInClassesThat().haveSimpleNameEndingWith("EventHandler")
            .and().arePublic()
            .and().haveNameMatching("handle.*")
            .should().beAnnotatedWith(TransactionalEventListener.class)
            .because("EventHandler는 트랜잭션 커밋 후에 실행되어야 합니다");

        rule.check(classes);
    }

    @Test
    @DisplayName("EventHandler는 Domain Event만 의존해야 한다")
    void eventHandler_ShouldOnlyDependOnDomainEvents() {
        ArchRule rule = noClasses()
            .that().haveSimpleNameEndingWith("EventHandler")
            .should().dependOnClassesThat().resideInAnyPackage(
                "..adapter..",
                "..infrastructure.."
            )
            .because("EventHandler는 Domain Event만 의존해야 하며, Adapter나 Infrastructure에 의존할 수 없습니다");

        rule.check(classes);
    }
}

