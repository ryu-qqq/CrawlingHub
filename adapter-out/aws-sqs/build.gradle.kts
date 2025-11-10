// ========================================
// Adapter-Out: AWS SQS
// ========================================
// Outbound adapter for AWS SQS message publishing
// Implements SQS client ports from application layer
// NO Lombok allowed
// NO @Transactional (외부 API 호출은 트랜잭션 밖에서)
// ========================================

plugins {
    `java-library`
}

dependencies {
    // ========================================
    // Core Dependencies
    // ========================================
    implementation(project(":application"))
    implementation(project(":domain"))

    // ========================================
    // AWS SDK v2 - SQS
    // ========================================
    implementation(platform(rootProject.libs.aws.bom))
    implementation(rootProject.libs.aws.sqs)
    implementation(rootProject.libs.aws.apache.client)

    // ========================================
    // Spring Context
    // ========================================
    implementation("org.springframework.boot:spring-boot-starter")
    implementation(rootProject.libs.spring.context)
    implementation("org.slf4j:slf4j-api")

    // JSON Processing
    implementation(rootProject.libs.jackson.databind)
    implementation(rootProject.libs.jackson.datatype.jsr310)

    // ========================================
    // Test Dependencies
    // ========================================
    testImplementation(rootProject.libs.spring.boot.starter.test)
    testImplementation(rootProject.libs.testcontainers.junit)
    testImplementation(rootProject.libs.testcontainers.localstack)
}

// ========================================
// Test Coverage (70% for adapters)
// ========================================
tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            element = "CLASS"
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.50".toBigDecimal()
            }
            excludes = listOf(
                "*.config.*",
                "*.dto.*",
                "*Response",
                "*Request"
            )
        }
    }
}

tasks.test {
    finalizedBy(tasks.jacocoTestCoverageVerification)
}
