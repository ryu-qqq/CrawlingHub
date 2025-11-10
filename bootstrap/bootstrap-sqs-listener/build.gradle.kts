// ========================================
// Bootstrap: SQS Listener Application
// ========================================
// Runnable Spring Boot application
// SQS Listener 전용 (ScheduleTriggerSqsListener 등)
// NO Lombok allowed
// ========================================

import java.time.Instant

plugins {
    java
    alias(libs.plugins.spring.boot)
    jacoco
}

dependencies {
    // ========================================
    // Core Modules
    // ========================================
    implementation(project(":domain"))
    implementation(project(":application"))

    // ========================================
    // Adapters
    // ========================================
    // Inbound (SQS Listener)
    implementation(project(":adapter-in:event"))

    // Outbound
    implementation(project(":adapter-out:persistence-mysql"))
    implementation(project(":adapter-out:persistence-redis"))
    implementation(project(":adapter-out:aws-eventbridge"))
    implementation(project(":adapter-out:aws-sqs"))
    implementation(project(":adapter-out:http-client"))

    // ========================================
    // Spring Boot Starters
    // ========================================
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // AWS SQS Starter (Spring Cloud AWS)
    implementation(libs.spring.cloud.aws.starter.sqs)

    // Configuration Processing
    annotationProcessor(libs.spring.boot.configuration.processor)

    // ========================================
    // Observability
    // ========================================
    // Micrometer for metrics
    implementation(libs.micrometer.prometheus)

    // Logging
    implementation(libs.logstash.logback.encoder)

    // ========================================
    // Database
    // ========================================
    runtimeOnly(libs.mysql)

    // ========================================
    // AWS SDK (from adapters)
    // ========================================
    implementation(platform(libs.aws.bom))

    // ========================================
    // Test Dependencies
    // ========================================
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.testcontainers.mysql)
    testImplementation(libs.testcontainers.junit)
}

// ========================================
// Spring Boot Configuration
// ========================================
tasks.bootJar {
    archiveFileName.set("${project.rootProject.name}-sqs-listener.jar")

    manifest {
        attributes(
            mapOf(
                "Implementation-Title" to "${project.rootProject.name}-sqs-listener",
                "Implementation-Version" to project.version,
                "Built-By" to System.getProperty("user.name"),
                "Built-JDK" to System.getProperty("java.version"),
                "Build-Timestamp" to Instant.now().toString()
            )
        )
    }
}

// ========================================
// Application Run Configuration
// ========================================
tasks.bootRun {
    jvmArgs = listOf(
        "-Xms512m",
        "-Xmx1024m",
        "-XX:+UseG1GC"
    )
}
