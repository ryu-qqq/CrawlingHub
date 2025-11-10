// ========================================
// Adapter-Out: HTTP Client
// ========================================
// Outbound adapter for external HTTP API calls
// Implements HTTP client ports from application layer
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
    api(project(":application"))
    api(project(":domain"))

    // ========================================
    // Spring Web Client
    // ========================================
    implementation("org.springframework.boot:spring-boot-starter-web") {
        // RestTemplate만 사용, WebFlux 제외
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-webflux")
    }

    // Spring Context
    implementation(libs.spring.context)
    implementation("org.slf4j:slf4j-api")

    // JSON Processing
    implementation(libs.jackson.databind)

    // ========================================
    // Test Dependencies
    // ========================================
    testImplementation(libs.spring.boot.starter.test)
    testImplementation("org.springframework.cloud:spring-cloud-starter-contract-stub-runner:4.0.4")
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
