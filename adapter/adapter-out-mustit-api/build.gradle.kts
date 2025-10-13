// ========================================
// Adapter-Out: Mustit API
// ========================================
// Outbound adapter for Mustit API token management
// Implements MustitTokenPort from application layer
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

    // HTTP Client Dependencies
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("io.projectreactor:reactor-core")
    implementation("io.projectreactor.netty:reactor-netty")

    // Spring Context
    implementation(libs.spring.context)
    implementation("org.slf4j:slf4j-api")

    // JSON Processing
    implementation(libs.jackson.databind)

    // Retry & Resilience
    implementation("org.springframework.retry:spring-retry:2.0.4")

    // ========================================
    // Test Dependencies
    // ========================================
    testImplementation(libs.spring.boot.starter.test)

    // MockWebServer for HTTP mocking
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    testImplementation("com.squareup.okhttp3:okhttp:4.12.0")
}

// ========================================
// Test Coverage (70% for adapters)
// ========================================
tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            element = "BUNDLE"
            limit {
                counter = "INSTRUCTION"
                value = "COVEREDRATIO"
                minimum = "0.65".toBigDecimal()
            }
        }
        rule {
            element = "CLASS"
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.50".toBigDecimal()
            }
            excludes = listOf(
                "*TokenRequest",
                "*TokenApiResponse",
                "*MustitApiConfig"
            )
        }
    }
}

tasks.test {
    finalizedBy(tasks.jacocoTestCoverageVerification)
}

// ========================================
// SpotBugs Configuration
// ========================================
spotbugs {
    excludeFilter.set(file("spotbugs-excludes.xml"))
}

tasks.withType<com.github.spotbugs.snom.SpotBugsTask>().configureEach {
    reports.create("html") {
        required.set(true)
    }
}

// ========================================
// Checkstyle Configuration
// ========================================
checkstyle {
    configFile = rootProject.file("config/checkstyle/checkstyle.xml")
    configProperties = mapOf(
        "suppressionFile" to file("checkstyle-suppressions.xml").absolutePath
    )
}
