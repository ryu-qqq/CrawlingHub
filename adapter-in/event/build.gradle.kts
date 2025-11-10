// ========================================
// Adapter-In Event (Inbound Adapter)
// ========================================
// Purpose: EventBridge/SQS 메시지 리스너 (Driving Adapter)
// - SQS FIFO Listener
// - Event Request DTOs
// - Assembler (DTO → Command 변환)
//
// Dependencies:
// - application (Use Case 호출)
// - domain (Domain 모델 참조)
// - Spring Cloud AWS SQS
//
// Policy:
// - Listener는 thin layer (비즈니스 로직 없음)
// - DTO ↔ Command 변환은 Assembler로 위임
// - SQS FIFO 사용 (sellerId 기반 중복 방지)
// ========================================

plugins {
    java
    `java-test-fixtures`  // TestFixtures 플러그인
}

dependencies {
    // ========================================
    // Core Dependencies
    // ========================================
    implementation(project(":domain"))
    implementation(project(":application"))

    // ========================================
    // Spring Boot Core
    // ========================================
    // @Component, @Configuration, @Bean, @Value 등 Spring 기본 어노테이션
    // REST API 없으므로 spring-boot-starter만 사용 (Tomcat 기동 방지)
    implementation("org.springframework.boot:spring-boot-starter")

    // ========================================
    // Spring Transaction
    // ========================================
    // @TransactionalEventListener, TransactionPhase 등
    implementation("org.springframework:spring-tx")

    // ========================================
    // Spring Cloud AWS SQS
    // ========================================
    // @SqsListener, SqsTemplate, SqsAsyncClient 등
    implementation(libs.spring.cloud.aws.starter.sqs)

    // ========================================
    // JSON Processing
    // ========================================
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // ========================================
    // Logging
    // ========================================
    implementation("org.slf4j:slf4j-api")

    // ========================================
    // Test Dependencies
    // ========================================
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.awaitility:awaitility")
}
