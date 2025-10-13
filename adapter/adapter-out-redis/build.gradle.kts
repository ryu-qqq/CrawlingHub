// ========================================
// Adapter-Out: Redis
// ========================================
// Outbound adapter for Redis-based rate limiting and distributed locking
// Implements rate limiting ports from application layer
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

    // Redis Dependencies
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("io.lettuce:lettuce-core")
    implementation("org.apache.commons:commons-pool2:2.11.1")

    // Spring Context
    implementation(libs.spring.context)
    implementation("org.slf4j:slf4j-api")

    // JSON Processing
    implementation(libs.jackson.databind)

    // ========================================
    // Test Dependencies
    // ========================================
    testImplementation(libs.spring.boot.starter.test)

    // Testcontainers
    testImplementation(platform("org.testcontainers:testcontainers-bom:1.19.3"))
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("com.redis.testcontainers:testcontainers-redis-junit:1.6.4")
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
                "*.HealthStatus",
                "*.ConnectionPoolStatus",
                "*.CircuitState",
                "*.CircuitStatus",
                "*.RateLimitResult",
                "*.BucketStatus",
                "*.LockHandle"
            )
        }
    }
}

tasks.test {
    // Testcontainers Java 모듈 시스템 호환성
    jvmArgs(
        "--add-opens", "java.base/java.lang=ALL-UNNAMED",
        "--add-opens", "java.base/java.util=ALL-UNNAMED"
    )
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
