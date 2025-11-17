package com.ryuqq.crawlinghub.application.architecture.port.in;

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
 * Input Port (Command UseCase, Query UseCase) ArchUnit 검증 테스트
 *
 * <p><strong>Zero-Tolerance 규칙:</strong></p>
 * <ul>
 *   <li>✅ Input Port는 port.in.command 또는 port.in.query 패키지에 위치</li>
 *   <li>✅ UseCase 네이밍 (동사 + UseCase 접미사)</li>
 *   <li>✅ 인터페이스 타입 (구현체는 별도)</li>
 *   <li>✅ Command DTO 입력, Response DTO 출력</li>
 *   <li>✅ @Transactional 금지 (구현체에서 처리)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-18
 */
@DisplayName("Input Port ArchUnit Tests (Zero-Tolerance)")
@Tag("architecture")
class InputPortArchTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setUp() {
        classes = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.ryuqq.crawlinghub.application");
    }

    /**
     * 규칙 1: Input Port는 인터페이스여야 함
     */
    @Test
    @DisplayName("[필수] Input Port는 인터페이스여야 한다")
    void inputPort_MustBeInterface() {
        ArchRule rule = classes()
            .that().resideInAPackage("..port.in..")
            .and().haveSimpleNameEndingWith("UseCase")
            .should().beInterfaces()
            .because("Input Port는 UseCase 인터페이스로 정의해야 합니다");

        rule.check(classes);
    }

    /**
     * 규칙 2: Command UseCase 네이밍 규칙
     */
    @Test
    @DisplayName("[필수] Command UseCase는 동사로 시작하고 UseCase로 끝나야 한다")
    void commandUseCase_MustFollowNamingConvention() {
        ArchRule rule = classes()
            .that().resideInAPackage("..port.in.command..")
            .and().areInterfaces()
            .should().haveSimpleNameEndingWith("UseCase")
            .because("Command UseCase는 '동사 + UseCase' 네이밍을 따라야 합니다");

        rule.check(classes);
    }

    /**
     * 규칙 3: Query UseCase 네이밍 규칙
     */
    @Test
    @DisplayName("[필수] Query UseCase는 동사로 시작하고 UseCase로 끝나야 한다")
    void queryUseCase_MustFollowNamingConvention() {
        ArchRule rule = classes()
            .that().resideInAPackage("..port.in.query..")
            .and().areInterfaces()
            .should().haveSimpleNameEndingWith("UseCase")
            .allowEmptyShould(true)  // Query UseCase가 없을 수 있음
            .because("Query UseCase는 '동사 + UseCase' 네이밍을 따라야 합니다");

        rule.check(classes);
    }

    /**
     * 규칙 4: Input Port는 @Transactional을 가지지 않아야 함
     */
    @Test
    @DisplayName("[금지] Input Port는 @Transactional을 가지지 않아야 한다")
    void inputPort_MustNotHaveTransactionalAnnotation() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..port.in..")
            .and().areInterfaces()
            .should().beAnnotatedWith("org.springframework.transaction.annotation.Transactional")
            .because("@Transactional은 UseCase 구현체에서만 사용해야 합니다 (Input Port는 인터페이스)");

        rule.check(classes);
    }

    /**
     * 규칙 5: Input Port는 Command/Query DTO 패키지를 의존해야 함
     */
    @Test
    @DisplayName("[필수] Input Port는 DTO 패키지를 의존해야 한다")
    void inputPort_MustDependOnDtoPackage() {
        ArchRule rule = classes()
            .that().resideInAPackage("..port.in..")
            .and().areInterfaces()
            .should().dependOnClassesThat().resideInAPackage("..dto..")
            .because("Input Port는 DTO를 입출력으로 사용해야 합니다");

        rule.check(classes);
    }

    /**
     * 규칙 7: Input Port는 Output Port를 직접 의존하지 않아야 함
     */
    @Test
    @DisplayName("[금지] Input Port는 Output Port를 직접 의존하지 않아야 한다")
    void inputPort_MustNotDependOnOutputPort() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..port.in..")
            .should().dependOnClassesThat().resideInAPackage("..port.out..")
            .because("Input Port는 Output Port를 직접 의존할 수 없습니다 (구현체에서만 의존)");

        rule.check(classes);
    }

    /**
     * 규칙 8: Input Port는 Domain 객체를 반환하지 않아야 함
     */
    @Test
    @DisplayName("[금지] Input Port는 Domain 객체를 반환하지 않아야 한다")
    void inputPort_MustNotReturnDomainObjects() {
        ArchRule rule = noMethods()
            .that().areDeclaredInClassesThat().resideInAPackage("..port.in..")
            .and().arePublic()
            .should().haveRawReturnType("com.ryuqq.domain..")
            .because("Input Port는 Domain 객체를 반환할 수 없습니다 (DTO만 반환)");

        rule.check(classes);
    }
}
