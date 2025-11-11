// ========================================
// Bootstrap Module (Architecture Tests Only)
// ========================================
// Purpose: ArchUnit 아키텍처 검증 테스트
// - Application Layer 규칙
// - Domain Layer 규칙
// - Hexagonal Architecture 의존성 규칙
// - JPA Entity 컨벤션
// - Mapper 컨벤션
// - Orchestration Pattern 컨벤션
// ========================================

plugins {
    java
}

dependencies {
    // ========================================
    // Test Dependencies for ArchUnit
    // ========================================
    testImplementation(project(":domain"))
    testImplementation(project(":application"))
    testImplementation(project(":adapter-in:rest-api"))
    testImplementation(project(":adapter-out:persistence-mysql"))

    // Spring Boot Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    // ArchUnit
    testImplementation("com.tngtech.archunit:archunit:1.2.1")
    testImplementation("com.tngtech.archunit:archunit-junit5:1.2.1")

    // Spring Framework (ArchUnit 테스트에서 어노테이션 검증용)
    testImplementation("org.springframework:spring-context:6.1.1")
    testImplementation("org.springframework:spring-tx:6.1.1")
    testImplementation("org.springframework.boot:spring-boot-autoconfigure:3.2.0")

    // Jakarta Persistence (JPA 어노테이션 검증용)
    testImplementation("jakarta.persistence:jakarta.persistence-api:3.1.0")
}

tasks.test {
    useJUnitPlatform()
}
