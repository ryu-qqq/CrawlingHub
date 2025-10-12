// ========================================
// Adapter-Out: AWS EventBridge
// ========================================
// Outbound adapter for AWS EventBridge scheduled events
// Implements EventBridge ports from application layer
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

    // AWS SDK v2 - EventBridge
    implementation(platform(libs.aws.bom))
    implementation(libs.aws.eventbridge)

    // Spring Context & Transaction
    implementation(libs.spring.context)
    implementation(libs.spring.tx)
    implementation(libs.spring.boot.autoconfigure)

    // Configuration Properties
    implementation("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

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
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                exclude("**/config/**")
            }
        })
    )
}

tasks.test {
    finalizedBy(tasks.jacocoTestCoverageVerification)
}
