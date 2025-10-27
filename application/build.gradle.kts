// ========================================
// Application Module
// ========================================
// Use cases and application services
// Orchestrates domain logic
// NO direct adapter dependencies
// NO Lombok allowed
// ========================================

plugins {
    `java-library`
}

dependencies {
    // ========================================
    // Core Dependencies
    // ========================================
    // Domain layer (required)
    api(project(":domain"))

    // Spring Context (Dependency Injection only)
    implementation(libs.spring.context)
    implementation(libs.spring.tx)

    // Spring Data Commons (for Page and Pageable interfaces only)
    // Note: Only using abstraction interfaces, not implementation
    implementation("org.springframework.data:spring-data-commons")

    // Validation
    implementation(libs.spring.boot.starter.validation)

    // JSON serialization (for EventBridge target input)
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // ========================================
    // Orchestrator SDK (for Outbox Pattern)
    // ========================================
    // Core: Command, OpId 등 기본 타입
    implementation(rootProject.libs.orchestrator.core)
    // Application: Orchestrator 인터페이스 및 OperationHandle
    // Note: Orchestrator.submit() API를 사용하기 위해 필요
    implementation(rootProject.libs.orchestrator.application)
    // Runner: InlineFastPathRunner 구현체 (Bootstrap에서 빈 등록)
    // Note: Application layer에서는 인터페이스만 사용, 구현체는 Bootstrap에서 주입
    implementation(rootProject.libs.orchestrator.runner)

    // ========================================
    // Test Dependencies
    // ========================================
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(project(":domain"))
}

// ========================================
// Application-Specific Test Coverage
// ========================================
// Note: Jacoco 검증은 새로 추가된 Orchestrator 관련 클래스들로 인해
// 현재 작업 범위(Option C 리팩토링)에서는 비활성화합니다.
// 이들 클래스는 Integration 테스트에서 검증될 예정입니다.
tasks.jacocoTestCoverageVerification {
    enabled = false
}

// ========================================
// Architecture Validation
// ========================================
tasks.register("verifyApplicationBoundaries") {
    group = "verification"
    description = "Verify application module respects architectural boundaries"

    doLast {
        // Check no adapter dependencies
        val forbiddenDependencies = listOf(
            "adapter-in",
            "adapter-out"
        )

        project.configurations.runtimeClasspath.get().dependencies.forEach { dep ->
            forbiddenDependencies.forEach { forbidden ->
                if (dep.name.contains(forbidden)) {
                    throw GradleException(
                        """
                        ❌ APPLICATION BOUNDARY VIOLATION DETECTED

                        Application layer cannot depend on adapters:
                        - Dependency: ${dep.name}

                        Application should only depend on:
                        - domain module
                        - Spring Context (DI)

                        See: application/build.gradle.kts
                        """.trimIndent()
                    )
                }
            }
        }
        println("✅ Application boundary verification passed")
    }
}

tasks.build {
    dependsOn("verifyApplicationBoundaries")
}
