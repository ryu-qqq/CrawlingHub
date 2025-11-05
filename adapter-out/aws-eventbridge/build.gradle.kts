plugins {
    id("java")
    `java-test-fixtures`  // TestFixtures 플러그인
    id("io.spring.dependency-management") version "1.1.5"
}

group = "com.ryuqq.crawlinghub"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    // ========================================
    // Internal Modules
    // ========================================
    // Application Layer (Port 인터페이스 의존)
    implementation(project(":application"))

    // ========================================
    // Orchestrator SDK (JitPack)
    // ========================================
    implementation(rootProject.libs.orchestrator.core)
    implementation(rootProject.libs.orchestrator.application)
    implementation(rootProject.libs.orchestrator.runner)
    testImplementation(rootProject.libs.orchestrator.testkit)

    // ========================================
    // AWS SDK v2 - EventBridge
    // ========================================
    implementation(platform(rootProject.libs.aws.bom))
    implementation(rootProject.libs.aws.eventbridge)
    implementation(rootProject.libs.aws.apache.client)

    // ========================================
    // Resilience4j (Circuit Breaker, Retry)
    // ========================================
    implementation(rootProject.libs.resilience4j.spring.boot3)
    implementation(rootProject.libs.resilience4j.circuitbreaker)
    implementation(rootProject.libs.resilience4j.retry)
    implementation(rootProject.libs.resilience4j.timelimiter)

    // ========================================
    // Spring Boot
    // ========================================
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework:spring-context")

    // ========================================
    // Utilities
    // ========================================
    implementation(rootProject.libs.commons.lang3)
    implementation(rootProject.libs.guava)
    implementation(rootProject.libs.jackson.databind)
    implementation(rootProject.libs.jackson.datatype.jsr310)

    // ========================================
    // Test Dependencies
    // ========================================
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.mockito:mockito-junit-jupiter")

    // LocalStack for AWS Integration Tests
    testImplementation(rootProject.libs.testcontainers.junit)
    testImplementation(rootProject.libs.testcontainers.localstack)
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.5.6")
        mavenBom("org.testcontainers:testcontainers-bom:1.19.7")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
