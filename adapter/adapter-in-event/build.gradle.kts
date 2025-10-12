// ========================================
// Adapter-In: Event Handler
// ========================================
// Inbound adapter for domain event handling
// Listens to domain events and coordinates external services
// NO Lombok allowed
// ========================================

plugins {
    `java-library`
}

dependencies {
    // ========================================
    // Core Dependencies
    // ========================================
    api(project(":application"))
    api(project(":domain"))

    // Spring Context & Events
    implementation(libs.spring.context)
    implementation(libs.spring.tx)

    // Logging
    implementation(libs.slf4j.api)

    // ========================================
    // Test Dependencies
    // ========================================
    testImplementation(libs.spring.boot.starter.test)
}

// ========================================
// Test Coverage (70% for adapters)
// ========================================
tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.70".toBigDecimal()
            }
        }
    }
}

tasks.test {
    finalizedBy(tasks.jacocoTestCoverageVerification)
}
