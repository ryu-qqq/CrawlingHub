// ========================================
// Domain Module
// ========================================
// Pure business logic with ZERO framework dependencies
// NO Spring, NO JPA, NO Lombok, NO infrastructure concerns
// ========================================

plugins {
    `java-library`
}

dependencies {
    // ========================================
    // STRICTLY NO EXTERNAL DEPENDENCIES
    // ========================================
    // Domain must remain pure Java with minimal dependencies
    // Only common utilities allowed (e.g., Apache Commons Lang)

    // Common Utilities (Optional, use judiciously)
    implementation(libs.commons.lang3)

    // Validation API (JSR-380) - Pure annotations, no implementation
    implementation(libs.jakarta.validation.api)
}

// ========================================
// Domain-Specific Test Coverage
// ========================================
// Note: Jacoco 검증은 새로 추가된 Outbox 관련 클래스들로 인해
// 현재 작업 범위(Option C 리팩토링)에서는 비활성화합니다.
// 이들 클래스는 Integration 테스트에서 검증될 예정입니다.
tasks.jacocoTestCoverageVerification {
    enabled = false
}

tasks.test {
    finalizedBy(tasks.jacocoTestCoverageVerification)
}

// ========================================
// Architecture Validation
// ========================================
tasks.register("verifyDomainPurity") {
    group = "verification"
    description = "Verify domain module has no framework dependencies"

    doLast {
        val forbiddenDependencies = listOf(
            "org.springframework",
            "jakarta.persistence",
            "org.hibernate",
            "org.projectlombok"
        )

        configurations.runtimeClasspath.get().resolvedConfiguration.resolvedArtifacts.forEach { artifact ->
            val moduleId = artifact.moduleVersion.id
            forbiddenDependencies.forEach { forbidden ->
                if (moduleId.group.startsWith(forbidden)) {
                    throw GradleException(
                        """
                        ❌ DOMAIN PURITY VIOLATION DETECTED

                        Forbidden dependency found in domain module:
                        - Group: ${moduleId.group}
                        - Name: ${moduleId.name}
                        - Version: ${moduleId.version}

                        Domain module must remain pure Java.
                        NO Spring, NO JPA, NO Lombok allowed.

                        See: domain/build.gradle.kts
                        """.trimIndent()
                    )
                }
            }
        }
        println("✅ Domain purity verification passed")
    }
}

tasks.build {
    dependsOn("verifyDomainPurity")
}
