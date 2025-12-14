package com.ryuqq.crawlinghub.application.architecture.factory;

import static com.tngtech.archunit.core.domain.JavaModifier.FINAL;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;

/**
 * CommandFactory ArchUnit 검증 테스트 (Zero-Tolerance)
 *
 * <p>핵심 철학: CommandFactory는 Command → Domain 순수 변환만, 비즈니스 로직/조회 금지
 */
@DisplayName("CommandFactory ArchUnit Tests (Zero-Tolerance)")
@Tag("architecture")
@Tag("factory")
class CommandFactoryArchTest {

    private static JavaClasses classes;
    private static boolean hasCommandFactoryClasses;

    @BeforeAll
    static void setUp() {
        classes = new ClassFileImporter().importPackages("com.ryuqq.crawlinghub.application");

        hasCommandFactoryClasses =
                classes.stream()
                        .anyMatch(
                                javaClass -> javaClass.getSimpleName().endsWith("CommandFactory"));
    }

    // ==================== 기본 구조 규칙 ====================

    @Nested
    @DisplayName("기본 구조 규칙")
    class BasicStructureRules {

        @Test
        @DisplayName("[필수] CommandFactory는 @Component 어노테이션을 가져야 한다")
        void commandFactory_MustHaveComponentAnnotation() {
            assumeTrue(hasCommandFactoryClasses, "CommandFactory 클래스가 없어 테스트를 스킵합니다");

            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("CommandFactory")
                            .should()
                            .beAnnotatedWith(Component.class)
                            .because("CommandFactory는 Spring Bean으로 등록되어야 합니다");

            rule.check(classes);
        }

        @Test
        @DisplayName("[필수] factory.command 패키지의 Factory 클래스는 'CommandFactory' 접미사를 가져야 한다")
        void factory_MustHaveCorrectSuffix() {
            assumeTrue(hasCommandFactoryClasses, "CommandFactory 클래스가 없어 테스트를 스킵합니다");

            // Factory 이름은 CommandFactory 또는 UpdateFactory 접미사 허용
            ArchRule rule =
                    classes()
                            .that()
                            .resideInAPackage("..application..factory.command..")
                            .and()
                            .haveSimpleNameContaining("Factory")
                            .and()
                            .haveSimpleNameNotEndingWith("Test") // 테스트 클래스 제외
                            .and()
                            .haveSimpleNameNotContaining("$") // 내부 클래스 제외
                            .and()
                            .areNotInterfaces()
                            .should()
                            .haveSimpleNameEndingWith("Factory")
                            .because(
                                    "Command Factory는 'Factory' 접미사를 사용해야 합니다 (CommandFactory,"
                                            + " UpdateFactory 등)");

            rule.check(classes);
        }

        @Test
        @DisplayName("[필수] Factory는 ..application..factory.. 패키지에 위치해야 한다")
        void factory_MustBeInCorrectPackage() {
            assumeTrue(hasCommandFactoryClasses, "CommandFactory 클래스가 없어 테스트를 스킵합니다");

            // Factory는 factory 또는 factory.command 패키지 모두 허용
            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("Factory")
                            .and()
                            .resideInAPackage("..application..")
                            .should()
                            .resideInAPackage("..application..factory..")
                            .because("Factory는 application.{bc}.factory 패키지에 위치해야 합니다");

            rule.check(classes);
        }

        @Test
        @DisplayName("[필수] CommandFactory는 final 클래스가 아니어야 한다")
        void commandFactory_MustNotBeFinal() {
            assumeTrue(hasCommandFactoryClasses, "CommandFactory 클래스가 없어 테스트를 스킵합니다");

            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("CommandFactory")
                            .should()
                            .notHaveModifier(FINAL)
                            .because("Spring 프록시 생성을 위해 CommandFactory가 final이 아니어야 합니다");

            rule.check(classes);
        }
    }

    // ==================== 메서드 규칙 ====================

    @Nested
    @DisplayName("메서드 규칙")
    class MethodRules {

        @Test
        @DisplayName("[권장] CommandFactory의 public 메서드는 create로 시작해야 한다")
        void commandFactory_MethodsShouldStartWithCreate() {
            assumeTrue(hasCommandFactoryClasses, "CommandFactory 클래스가 없어 테스트를 스킵합니다");

            ArchRule rule =
                    methods()
                            .that()
                            .areDeclaredInClassesThat()
                            .haveSimpleNameEndingWith("CommandFactory")
                            .and()
                            .arePublic()
                            .and()
                            .doNotHaveFullName(".*<init>.*")
                            .should()
                            .haveNameMatching("create.*|apply.*")
                            .because(
                                    "CommandFactory 메서드는 create*() (생성) 또는 apply*() (수정 적용) 네이밍을"
                                            + " 권장합니다");

            rule.check(classes);
        }
    }

    // ==================== 금지 규칙 (Zero-Tolerance) ====================

    @Nested
    @DisplayName("금지 규칙 (Zero-Tolerance)")
    class ProhibitionRules {

        @Test
        @DisplayName("[금지] CommandFactory는 @Service 어노테이션을 가지지 않아야 한다")
        void commandFactory_MustNotHaveServiceAnnotation() {
            assumeTrue(hasCommandFactoryClasses, "CommandFactory 클래스가 없어 테스트를 스킵합니다");

            ArchRule rule =
                    noClasses()
                            .that()
                            .haveSimpleNameEndingWith("CommandFactory")
                            .should()
                            .beAnnotatedWith("org.springframework.stereotype.Service")
                            .because("CommandFactory는 @Service가 아닌 @Component를 사용해야 합니다");

            rule.check(classes);
        }

        @Test
        @DisplayName("[금지] CommandFactory는 @Transactional을 가지지 않아야 한다")
        void commandFactory_MustNotHaveTransactionalAnnotation() {
            assumeTrue(hasCommandFactoryClasses, "CommandFactory 클래스가 없어 테스트를 스킵합니다");

            ArchRule rule =
                    noClasses()
                            .that()
                            .haveSimpleNameEndingWith("CommandFactory")
                            .should()
                            .beAnnotatedWith(
                                    "org.springframework.transaction.annotation.Transactional")
                            .because(
                                    "CommandFactory는 @Transactional을 가지지 않아야 합니다. "
                                            + "트랜잭션 경계는 Service 책임입니다.");

            rule.check(classes);
        }

        @Test
        @DisplayName("[금지] CommandFactory는 Lombok 어노테이션을 가지지 않아야 한다")
        void commandFactory_MustNotUseLombok() {
            assumeTrue(hasCommandFactoryClasses, "CommandFactory 클래스가 없어 테스트를 스킵합니다");

            ArchRule rule =
                    noClasses()
                            .that()
                            .haveSimpleNameEndingWith("CommandFactory")
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
                            .because("CommandFactory는 Plain Java를 사용해야 합니다 (Lombok 금지)");

            rule.check(classes);
        }

        @Test
        @DisplayName("[허용] CommandFactory는 조회 Port는 금지하지만 Client Port(PasswordEncoderPort 등)는 허용한다")
        void commandFactory_MustNotDependOnQueryOrPersistencePorts() {
            assumeTrue(hasCommandFactoryClasses, "CommandFactory 클래스가 없어 테스트를 스킵합니다");

            // Client Port(예: PasswordEncoderPort)는 도메인 객체 생성에 필요할 수 있으므로 허용
            // 조회/영속화 Port만 금지
            ArchRule rule =
                    noClasses()
                            .that()
                            .haveSimpleNameEndingWith("CommandFactory")
                            .should()
                            .dependOnClassesThat()
                            .haveNameMatching(".*QueryPort|.*PersistencePort")
                            .because(
                                    "CommandFactory는 조회/영속화 Port를 주입받지 않아야 합니다. "
                                            + "Client Port(PasswordEncoderPort 등)는 허용됩니다.");

            rule.check(classes);
        }

        @Test
        @DisplayName("[금지] CommandFactory는 Repository를 의존하지 않아야 한다")
        void commandFactory_MustNotDependOnRepositories() {
            assumeTrue(hasCommandFactoryClasses, "CommandFactory 클래스가 없어 테스트를 스킵합니다");

            ArchRule rule =
                    noClasses()
                            .that()
                            .haveSimpleNameEndingWith("CommandFactory")
                            .should()
                            .dependOnClassesThat()
                            .haveNameMatching(".*Repository")
                            .because(
                                    "CommandFactory는 Repository를 주입받지 않아야 합니다. "
                                            + "조회 없이 순수 변환만 수행합니다.");

            rule.check(classes);
        }
    }

    // ==================== 의존성 규칙 ====================

    @Nested
    @DisplayName("의존성 규칙")
    class DependencyRules {

        @Test
        @DisplayName("[필수] CommandFactory는 Application Layer와 Domain Layer만 의존해야 한다")
        void commandFactory_MustOnlyDependOnApplicationAndDomainLayers() {
            assumeTrue(hasCommandFactoryClasses, "CommandFactory 클래스가 없어 테스트를 스킵합니다");

            ArchRule rule =
                    classes()
                            .that()
                            .haveSimpleNameEndingWith("CommandFactory")
                            .should()
                            .onlyAccessClassesThat()
                            .resideInAnyPackage(
                                    "com.ryuqq.crawlinghub.application..",
                                    "com.ryuqq.crawlinghub.domain..",
                                    "org.springframework..",
                                    "java..",
                                    "jakarta..",
                                    "com.github.f4b6a3..") // UUID v7 라이브러리 허용
                            .because("CommandFactory는 Application Layer와 Domain Layer만 의존해야 합니다");

            rule.check(classes);
        }
    }
}
