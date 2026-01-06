# Observability Spring Boot Starter 설계 문서

**버전**: v0.1.0 (Draft)
**작성일**: 2025-01-05
**상태**: 설계 단계

---

## 1. 프로젝트 개요

### 1.1 배경

MoniKit 프로젝트의 실패 경험을 바탕으로, **실용적이고 간결한** Observability SDK를 새로 설계합니다.

**MoniKit의 교훈**:
- ❌ "모든 것을 자동 로깅"은 실용적이지 않음
- ❌ 127개 파일, 6개 모듈은 과도한 복잡성
- ❌ SpEL 동적 규칙은 유지보수 악몽
- ❌ 개인정보 자동 노출 위험
- ✅ TraceId 전파, Request 로깅은 유용함
- ✅ Spring Boot AutoConfiguration 패턴은 좋음

### 1.2 프로젝트 이름

```
ryu-observability-spring-boot-starter
(또는 간단히: observability-starter)
```

### 1.3 목표

```
┌─────────────────────────────────────────────────────────────┐
│  핵심 목표                                                  │
├─────────────────────────────────────────────────────────────┤
│  1. 간결함: 30개 이하 파일, 단일 모듈                        │
│  2. 실용성: 진입점 자동 로깅, 나머지는 명시적                │
│  3. 표준 준수: W3C Trace Context, OpenTelemetry 호환        │
│  4. 안전성: 민감정보 마스킹 기본 내장                        │
│  5. 확장성: Sentry, OpenSearch, CloudWatch 연동 지원        │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. 철학 및 원칙

### 2.1 핵심 철학

```
"진입점은 자동으로, 비즈니스 로직은 명시적으로"

┌─────────────────────────────────────────────────────────────┐
│  자동 로깅 (SDK 담당)                                       │
│  • HTTP 요청/응답 (Filter)                                  │
│  • MQ 메시지 수신/발신 (Aspect)                             │
│  • 스케줄러 실행 (Aspect)                                   │
│  • 예외 발생 (ErrorHandler)                                 │
├─────────────────────────────────────────────────────────────┤
│  명시적 로깅 (개발자 담당)                                   │
│  • 비즈니스 이벤트: log.info("주문 생성: orderId={}", id)   │
│  • 상태 변경: log.info("주문 상태 변경: {} → {}", from, to) │
│  • 디버깅 정보: log.debug("계산 결과: {}", result)          │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 설계 원칙

| 원칙 | 설명 | MoniKit 교훈 |
|------|------|-------------|
| **KISS** | Keep It Simple, Stupid | 127개 파일 → 30개 파일 |
| **명시적 > 암묵적** | 개발자가 의도한 로그만 | AOP 모든 메서드 로깅 제거 |
| **표준 우선** | SLF4J, Micrometer, OTel 활용 | 커스텀 로그 모델 제거 |
| **안전 기본값** | 민감정보 마스킹 기본 ON | 개인정보 노출 방지 |
| **제로 설정** | 의존성 추가만으로 동작 | 복잡한 YAML 규칙 제거 |

### 2.3 안티패턴 (하지 않을 것)

```
❌ 모든 메서드 자동 로깅 (ExecutionLoggingAspect)
❌ SpEL 기반 동적 규칙 (DynamicMatcher)
❌ 커스텀 로그 모델 (LogEntry, ExecutionDetailLog)
❌ ThreadLocal 남용 (LogEntryContext)
❌ Hook/Sink 복잡한 패턴
❌ 6개 모듈 분리
```

---

## 3. 기능 범위

### 3.1 핵심 기능 (v1.0)

```
┌─────────────────────────────────────────────────────────────┐
│  기능 범위                                                  │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  1️⃣ TraceId 관리                                            │
│     • 요청별 TraceId 자동 생성                              │
│     • X-Trace-Id, X-Request-Id 헤더 지원                    │
│     • MDC 자동 설정                                         │
│     • OpenTelemetry TraceId 연동                           │
│     • 응답 헤더에 TraceId 포함                              │
│                                                             │
│  2️⃣ HTTP 요청/응답 로깅                                     │
│     • 요청: Method, URI, Headers, Body (선택)              │
│     • 응답: Status, Duration, Body (선택)                  │
│     • 경로 정규화: /users/123 → /users/{id}                │
│     • 민감 헤더 마스킹: Authorization, Cookie              │
│                                                             │
│  3️⃣ 메시지 큐 로깅                                          │
│     • SQS 메시지 수신/발신 자동 로깅                        │
│     • Kafka Consumer/Producer 자동 로깅                    │
│     • 메시지 ID, 타입, 처리 시간 기록                       │
│                                                             │
│  4️⃣ 스케줄러 로깅                                           │
│     • @Scheduled 메서드 실행 자동 로깅                      │
│     • 시작/종료/실패 기록                                   │
│     • 실행 시간 측정                                        │
│                                                             │
│  5️⃣ 예외 처리 연동                                          │
│     • ERROR 레벨 자동 Sentry 전송                          │
│     • TraceId 태깅                                          │
│     • 스택트레이스 구조화                                   │
│                                                             │
│  6️⃣ 로그 포맷 표준화                                        │
│     • JSON 구조화 로깅                                      │
│     • 필수 필드 강제                                        │
│     • Logback 기본 설정 제공                               │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 3.2 범위 외 (하지 않을 것)

```
❌ 모든 메서드 실행 로깅
❌ SQL 쿼리 자동 로깅 (DataSource Proxy 사용 권장)
❌ 메트릭 수집 (Micrometer 직접 사용 권장)
❌ 분산 추적 구현 (OpenTelemetry Agent 사용 권장)
❌ 로그 전송 (Logback Appender 사용 권장)
```

### 3.3 확장 기능 (v1.1+)

```
• gRPC 요청 로깅 (Interceptor)
• WebSocket 연결 로깅
• Redis 명령 로깅 (선택적)
• 커스텀 마스킹 규칙 확장
```

---

## 4. 아키텍처

### 4.1 전체 구조

```
┌─────────────────────────────────────────────────────────────┐
│  Application (CrawlingHub, AuthHub, Gateway 등)            │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌───────────────────────────────────────────────────────┐ │
│  │  observability-spring-boot-starter                    │ │
│  │                                                       │ │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  │ │
│  │  │   Trace     │  │   Logging   │  │   Error     │  │ │
│  │  │   Module    │  │   Module    │  │   Module    │  │ │
│  │  │             │  │             │  │             │  │ │
│  │  │ • TraceId   │  │ • Request   │  │ • Sentry    │  │ │
│  │  │ • MDC       │  │ • Message   │  │ • Handler   │  │ │
│  │  │ • Headers   │  │ • Scheduler │  │ • Context   │  │ │
│  │  └─────────────┘  └─────────────┘  └─────────────┘  │ │
│  │                                                       │ │
│  │  ┌─────────────┐  ┌─────────────┐                   │ │
│  │  │   Masking   │  │   Config    │                   │ │
│  │  │   Module    │  │   Module    │                   │ │
│  │  │             │  │             │  ← AutoConfig     │ │
│  │  │ • PII       │  │ • Props     │                   │ │
│  │  │ • Patterns  │  │ • Defaults  │                   │ │
│  │  └─────────────┘  └─────────────┘                   │ │
│  └───────────────────────────────────────────────────────┘ │
│                          │                                  │
├──────────────────────────┼──────────────────────────────────┤
│                          ▼                                  │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐        │
│  │   SLF4J     │  │  Micrometer │  │    Sentry   │        │
│  │  (Logback)  │  │ (Optional)  │  │  (Optional) │        │
│  └─────────────┘  └─────────────┘  └─────────────┘        │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 4.2 요청 처리 흐름

```
HTTP 요청 도착
       │
       ▼
┌──────────────────┐
│ TraceIdFilter    │ ① TraceId 생성/추출 → MDC 설정
│ (Order: -100)    │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ RequestLogging   │ ② 요청 정보 로깅 (Method, URI, Headers)
│ Filter (-50)     │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ Controller       │ ③ 비즈니스 로직 실행
│ (개발자 코드)     │    → 개발자가 명시적 로깅
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ RequestLogging   │ ④ 응답 정보 로깅 (Status, Duration)
│ Filter (after)   │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ TraceIdFilter    │ ⑤ MDC 정리, 응답 헤더에 TraceId 추가
│ (finally)        │
└────────┬─────────┘
         │
         ▼
    HTTP 응답 반환
```

### 4.3 MQ 메시지 처리 흐름

```
SQS/Kafka 메시지 도착
       │
       ▼
┌──────────────────┐
│ MessageLogging   │ ① TraceId 추출/생성 → MDC 설정
│ Aspect (Before)  │ ② 메시지 수신 로깅
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ @SqsListener     │ ③ 메시지 처리 로직
│ (개발자 코드)     │    → 개발자가 명시적 로깅
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ MessageLogging   │ ④ 처리 완료/실패 로깅
│ Aspect (After)   │ ⑤ MDC 정리
└────────┬─────────┘
         │
         ▼
    메시지 처리 완료
```

---

## 5. 모듈 구조

### 5.1 디렉토리 구조

```
observability-spring-boot-starter/
├── build.gradle.kts
├── settings.gradle.kts
├── README.md
│
├── src/main/java/com/ryuqq/observability/
│   │
│   ├── ObservabilityAutoConfiguration.java      # 메인 AutoConfig
│   │
│   ├── trace/                                    # TraceId 관리
│   │   ├── TraceIdFilter.java                   # Filter (Order: -100)
│   │   ├── TraceIdProvider.java                 # TraceId 생성/관리
│   │   ├── TraceIdHolder.java                   # ThreadLocal + MDC
│   │   └── TraceIdHeaders.java                  # 헤더 상수
│   │
│   ├── logging/                                  # 자동 로깅
│   │   ├── http/
│   │   │   ├── HttpLoggingFilter.java           # HTTP 요청/응답 로깅
│   │   │   ├── CachedBodyRequestWrapper.java    # Request Body 캐싱
│   │   │   ├── CachedBodyResponseWrapper.java   # Response Body 캐싱
│   │   │   └── PathNormalizer.java              # 경로 정규화
│   │   │
│   │   ├── message/
│   │   │   ├── MessageLoggingAspect.java        # MQ 메시지 로깅
│   │   │   └── MessageLogContext.java           # 메시지 컨텍스트
│   │   │
│   │   └── scheduler/
│   │       └── SchedulerLoggingAspect.java      # @Scheduled 로깅
│   │
│   ├── masking/                                  # 민감정보 마스킹
│   │   ├── LogMasker.java                       # 마스킹 엔진
│   │   ├── MaskingPattern.java                  # 패턴 정의
│   │   └── MaskingPatterns.java                 # 기본 패턴들
│   │
│   ├── error/                                    # 에러 처리
│   │   ├── SentryErrorReporter.java             # Sentry 연동
│   │   └── ErrorContext.java                    # 에러 컨텍스트
│   │
│   ├── config/                                   # 설정
│   │   ├── ObservabilityProperties.java         # 설정 프로퍼티
│   │   ├── HttpLoggingProperties.java           # HTTP 로깅 설정
│   │   ├── MessageLoggingProperties.java        # MQ 로깅 설정
│   │   └── MaskingProperties.java               # 마스킹 설정
│   │
│   └── support/                                  # 유틸리티
│       ├── JsonLogger.java                      # JSON 로깅 헬퍼
│       └── LogConstants.java                    # 상수 정의
│
├── src/main/resources/
│   ├── META-INF/spring/
│   │   └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
│   │
│   └── logback/
│       ├── logback-json-defaults.xml            # JSON 로그 기본 설정
│       └── logback-console-defaults.xml         # 콘솔 로그 기본 설정
│
└── src/test/java/com/ryuqq/observability/
    ├── trace/
    │   └── TraceIdFilterTest.java
    ├── logging/
    │   └── HttpLoggingFilterTest.java
    └── masking/
        └── LogMaskerTest.java
```

### 5.2 파일 개수 목표

| 구분 | 파일 수 | 설명 |
|------|--------|------|
| AutoConfiguration | 1 | 메인 설정 |
| Trace | 4 | TraceId 관리 |
| HTTP Logging | 4 | HTTP 로깅 |
| Message Logging | 2 | MQ 로깅 |
| Scheduler Logging | 1 | 스케줄러 로깅 |
| Masking | 3 | 마스킹 |
| Error | 2 | 에러 처리 |
| Config | 4 | 설정 클래스 |
| Support | 2 | 유틸리티 |
| Resources | 3 | 설정 파일 |
| **합계** | **~26개** | 목표: 30개 이하 |

---

## 6. API 설계

### 6.1 설정 프로퍼티

```yaml
# application.yml
observability:
  # 전역 설정
  enabled: true                              # 전체 활성화/비활성화
  service-name: ${spring.application.name}   # 서비스 이름

  # TraceId 설정
  trace:
    enabled: true
    header-names:                            # TraceId 헤더 이름들
      - X-Trace-Id
      - X-Request-Id
      - traceparent                          # W3C Trace Context
    include-in-response: true                # 응답에 TraceId 포함
    generate-if-missing: true                # 없으면 생성

  # HTTP 로깅 설정
  http:
    enabled: true
    log-request-body: false                  # 요청 본문 로깅 (기본 OFF)
    log-response-body: false                 # 응답 본문 로깅 (기본 OFF)
    max-body-length: 1000                    # 본문 최대 길이
    exclude-paths:                           # 제외할 경로
      - /actuator/**
      - /health
      - /favicon.ico
    exclude-headers:                         # 로깅 제외 헤더
      - Authorization
      - Cookie
      - Set-Cookie
    path-patterns:                           # 경로 정규화 패턴
      - pattern: "/users/[0-9]+"
        replacement: "/users/{id}"
      - pattern: "/orders/[A-Z0-9-]+"
        replacement: "/orders/{orderId}"

  # 메시지 로깅 설정
  message:
    enabled: true
    log-payload: false                       # 페이로드 로깅 (기본 OFF)
    max-payload-length: 500

  # 스케줄러 로깅 설정
  scheduler:
    enabled: true

  # 마스킹 설정
  masking:
    enabled: true
    patterns:                                # 커스텀 마스킹 패턴
      - name: credit-card
        pattern: "\\d{4}-\\d{4}-\\d{4}-\\d{4}"
        replacement: "****-****-****-$4"
      - name: phone
        pattern: "010-\\d{4}-\\d{4}"
        replacement: "010-****-****"
    mask-fields:                             # 마스킹할 필드명
      - password
      - secret
      - token
      - apiKey
      - creditCard

  # Sentry 설정
  sentry:
    enabled: true                            # Sentry 연동 활성화
    include-trace-id: true                   # TraceId 태그 추가
    environment: ${spring.profiles.active}
```

### 6.2 프로그래매틱 API

```java
// 1. TraceId 접근 (어디서든 사용 가능)
String traceId = TraceIdHolder.get();

// 2. TraceId 수동 설정 (테스트용)
TraceIdHolder.set("custom-trace-id");

// 3. 컨텍스트 추가 (로그에 추가 정보)
TraceIdHolder.addContext("userId", "USR-12345");
TraceIdHolder.addContext("orderId", "ORD-67890");

// 4. 마스킹 유틸리티
String masked = LogMasker.mask("card=1234-5678-9012-3456");
// 결과: "card=****-****-****-3456"

// 5. JSON 로깅 헬퍼
JsonLogger.info("order.created")
    .field("orderId", order.getId())
    .field("amount", order.getAmount())
    .field("userId", order.getUserId())
    .log();
// 결과: {"event":"order.created","orderId":"ORD-123","amount":50000,"userId":"USR-456"}
```

### 6.3 커스텀 확장 포인트

```java
// 1. TraceId 생성 전략 커스터마이징
@Bean
public TraceIdProvider customTraceIdProvider() {
    return new TraceIdProvider() {
        @Override
        public String generate() {
            return "CUSTOM-" + UUID.randomUUID().toString();
        }

        @Override
        public String extractFromRequest(HttpServletRequest request) {
            // OpenTelemetry traceparent 헤더에서 추출
            String traceparent = request.getHeader("traceparent");
            if (traceparent != null) {
                return parseTraceId(traceparent);
            }
            return null;
        }
    };
}

// 2. 마스킹 패턴 추가
@Bean
public MaskingPatternCustomizer maskingCustomizer() {
    return patterns -> {
        patterns.add(MaskingPattern.of(
            "rrn",
            "\\d{6}-[1-4]\\d{6}",
            "******-*******"
        ));
    };
}

// 3. 경로 정규화 규칙 추가
@Bean
public PathNormalizerCustomizer pathCustomizer() {
    return normalizer -> {
        normalizer.addPattern("/products/[A-Z]{3}\\d+", "/products/{sku}");
    };
}
```

---

## 7. 로그 포맷 표준

### 7.1 JSON 로그 구조

```json
{
  "@timestamp": "2025-01-05T10:30:00.123Z",
  "level": "INFO",
  "logger": "com.example.OrderService",
  "message": "주문 생성 완료",
  "traceId": "abc123def456",
  "spanId": "789xyz",
  "service": "crawlinghub-web-api",
  "environment": "prod",
  "thread": "http-nio-8080-exec-1",
  "context": {
    "userId": "USR-12345",
    "orderId": "ORD-67890"
  },
  "error": {
    "type": "java.lang.IllegalArgumentException",
    "message": "Invalid order amount",
    "stackTrace": "..."
  }
}
```

### 7.2 HTTP 요청 로그

```json
{
  "@timestamp": "2025-01-05T10:30:00.000Z",
  "level": "INFO",
  "logger": "observability.http",
  "message": "HTTP Request",
  "traceId": "abc123def456",
  "service": "crawlinghub-web-api",
  "http": {
    "direction": "REQUEST",
    "method": "POST",
    "uri": "/api/v1/orders",
    "normalizedUri": "/api/v1/orders",
    "headers": {
      "Content-Type": "application/json",
      "User-Agent": "Mozilla/5.0..."
    },
    "clientIp": "192.168.1.100"
  }
}
```

### 7.3 HTTP 응답 로그

```json
{
  "@timestamp": "2025-01-05T10:30:00.150Z",
  "level": "INFO",
  "logger": "observability.http",
  "message": "HTTP Response",
  "traceId": "abc123def456",
  "service": "crawlinghub-web-api",
  "http": {
    "direction": "RESPONSE",
    "method": "POST",
    "uri": "/api/v1/orders",
    "status": 201,
    "durationMs": 150
  }
}
```

### 7.4 메시지 로그

```json
{
  "@timestamp": "2025-01-05T10:30:00.000Z",
  "level": "INFO",
  "logger": "observability.message",
  "message": "Message Received",
  "traceId": "abc123def456",
  "service": "crawlinghub-worker",
  "messaging": {
    "direction": "RECEIVE",
    "system": "SQS",
    "queue": "order-events",
    "messageId": "msg-12345",
    "messageType": "OrderCreatedEvent"
  }
}
```

---

## 8. 통합 가이드

### 8.1 의존성 추가

```kotlin
// build.gradle.kts
dependencies {
    implementation("com.ryuqq:observability-spring-boot-starter:1.0.0")

    // 선택적 의존성
    implementation("io.sentry:sentry-spring-boot-starter-jakarta:7.3.0")  // Sentry 사용 시
}
```

### 8.2 기본 사용 (Zero Configuration)

```yaml
# application.yml
spring:
  application:
    name: crawlinghub-web-api

# 끝! 자동으로 활성화됨
```

### 8.3 Logback 설정

```xml
<!-- logback-spring.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <!-- SDK 제공 JSON 기본 설정 포함 -->
    <include resource="logback/logback-json-defaults.xml"/>

    <!-- 필요시 커스터마이징 -->
    <root level="INFO">
        <appender-ref ref="JSON_CONSOLE"/>
    </root>
</configuration>
```

### 8.4 멀티모듈 프로젝트 적용

```
project-root/
├── core-domain/           # 의존성 없음
├── core-application/      # 의존성 없음
├── adapter-in-web/        # ← observability-starter 추가
├── adapter-in-message/    # ← observability-starter 추가
├── adapter-out-persistence/  # 의존성 없음
└── bootstrap-api/         # ← observability-starter 추가 (진입점)
```

```kotlin
// adapter-in-web/build.gradle.kts
dependencies {
    implementation("com.ryuqq:observability-spring-boot-starter:1.0.0")
}

// adapter-in-message/build.gradle.kts
dependencies {
    implementation("com.ryuqq:observability-spring-boot-starter:1.0.0")
}

// bootstrap-api/build.gradle.kts
dependencies {
    implementation("com.ryuqq:observability-spring-boot-starter:1.0.0")
}
```

---

## 9. 기존 시스템 연동

### 9.1 OpenSearch 연동

```
┌─────────────────────────────────────────────────────────────┐
│  Application                                                │
│  └─ observability-starter                                  │
│     └─ JSON 로그 출력 (stdout)                             │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│  CloudWatch Logs                                            │
│  └─ /aws/ecs/{service}/application                         │
└──────────────────────────┬──────────────────────────────────┘
                           │ Subscription Filter
                           ▼
┌─────────────────────────────────────────────────────────────┐
│  Kinesis Data Streams → Lambda → OpenSearch                │
│  (기존 log-subscription-filter-v2 모듈 사용)                │
└─────────────────────────────────────────────────────────────┘
```

### 9.2 Sentry 연동

```yaml
# application.yml
observability:
  sentry:
    enabled: true
    include-trace-id: true

sentry:
  dsn: ${SENTRY_DSN}
  environment: ${SPRING_PROFILES_ACTIVE}
```

### 9.3 X-Ray/ADOT 연동

```
┌─────────────────────────────────────────────────────────────┐
│  ECS Task                                                   │
│  ┌─────────────────┐  ┌─────────────────────────────────┐  │
│  │  Application    │  │  ADOT Sidecar                   │  │
│  │  (with SDK)     │  │  • Prometheus scraping          │  │
│  │                 │──│  • X-Ray trace collection       │  │
│  │  TraceId from   │  │                                 │  │
│  │  X-Ray header   │  │                                 │  │
│  └─────────────────┘  └─────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

SDK는 `X-Amzn-Trace-Id` 헤더에서 TraceId를 추출하여 로그에 포함:

```java
// TraceIdProvider 구현
@Override
public String extractFromRequest(HttpServletRequest request) {
    // 1. W3C traceparent (OpenTelemetry)
    String traceparent = request.getHeader("traceparent");
    if (traceparent != null) {
        return parseW3CTraceId(traceparent);
    }

    // 2. AWS X-Ray
    String xrayHeader = request.getHeader("X-Amzn-Trace-Id");
    if (xrayHeader != null) {
        return parseXRayTraceId(xrayHeader);
    }

    // 3. 커스텀 헤더
    return request.getHeader("X-Trace-Id");
}
```

---

## 10. 테스트 전략

### 10.1 단위 테스트

```java
@ExtendWith(MockitoExtension.class)
class TraceIdFilterTest {

    @Test
    void shouldGenerateTraceIdWhenNotPresent() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        filter.doFilter(request, response, chain);

        // then
        assertThat(MDC.get("traceId")).isNotNull();
        assertThat(response.getHeader("X-Trace-Id")).isNotNull();
    }

    @Test
    void shouldUseExistingTraceIdFromHeader() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Trace-Id", "existing-trace-id");

        // when
        filter.doFilter(request, response, chain);

        // then
        assertThat(MDC.get("traceId")).isEqualTo("existing-trace-id");
    }
}
```

### 10.2 통합 테스트

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class HttpLoggingIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldLogHttpRequestAndResponse() {
        // given
        // Logback ListAppender로 로그 캡처 설정

        // when
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/api/v1/health", String.class);

        // then
        assertThat(response.getHeaders().get("X-Trace-Id")).isNotEmpty();
        // 로그 검증
        assertThat(capturedLogs)
            .anyMatch(log -> log.contains("HTTP Request"))
            .anyMatch(log -> log.contains("HTTP Response"));
    }
}
```

---

## 11. 마이그레이션 가이드

### 11.1 MoniKit에서 마이그레이션

```kotlin
// Before (MoniKit)
dependencies {
    implementation("com.ryuqq:monikit-starter:1.1.0")
    implementation("com.ryuqq:monikit-starter-web:1.1.0")
    implementation("com.ryuqq:monikit-metric:1.1.0")
}

// After (New SDK)
dependencies {
    implementation("com.ryuqq:observability-spring-boot-starter:1.0.0")
    // 메트릭은 Micrometer 직접 사용
    implementation("io.micrometer:micrometer-registry-prometheus")
}
```

### 11.2 설정 마이그레이션

```yaml
# Before (MoniKit)
monikit:
  logging:
    log-enabled: true
    datasource-logging-enabled: true
    slow-query-threshold-ms: 1000
    allowed-packages:
      - "com.myapp"
    dynamic-matching:
      - classNamePattern: ".*Service"
        when: "#executionTime > 500"

# After (New SDK)
observability:
  enabled: true
  http:
    enabled: true
  # 복잡한 동적 규칙 제거!
  # SQL 로깅은 p6spy 또는 datasource-proxy 사용
```

### 11.3 코드 마이그레이션

```java
// Before (MoniKit - LogEntryContext 사용)
LogEntryContext.addLog(LogEntry.builder()...);

// After (New SDK - 표준 SLF4J 사용)
log.info("주문 생성: orderId={}", orderId);

// 또는 JSON 헬퍼 사용
JsonLogger.info("order.created")
    .field("orderId", orderId)
    .log();
```

---

## 12. 로드맵

### v1.0.0 (초기 릴리스)
- [x] 설계 문서 작성
- [ ] 프로젝트 셋업
- [ ] TraceId 관리
- [ ] HTTP 요청/응답 로깅
- [ ] 민감정보 마스킹
- [ ] Logback 기본 설정
- [ ] 단위/통합 테스트
- [ ] README 작성

### v1.1.0
- [ ] MQ 메시지 로깅 (SQS, Kafka)
- [ ] 스케줄러 로깅
- [ ] Sentry 연동
- [ ] 경로 정규화 고도화

### v1.2.0
- [ ] OpenTelemetry 네이티브 연동
- [ ] gRPC 지원
- [ ] 커스텀 마스킹 규칙 UI

---

## 13. 참고 자료

### 13.1 관련 문서
- [log-streaming-setup-guide.md](../guide/log-streaming-setup-guide.md)
- [sentry-observability-guide.md](../guide/sentry-observability-guide.md)

### 13.2 외부 참고
- [SLF4J Documentation](http://www.slf4j.org/docs.html)
- [Logback Configuration](https://logback.qos.ch/manual/configuration.html)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer Documentation](https://micrometer.io/docs)
- [OpenTelemetry Java](https://opentelemetry.io/docs/instrumentation/java/)
- [W3C Trace Context](https://www.w3.org/TR/trace-context/)

---

## 부록 A: MoniKit에서 재사용할 코드

### A.1 TraceIdFilter 패턴

```java
// moni-kit/monitoring-starter-web/filter/TraceIdFilter.java
// 핵심 로직만 추출하여 단순화
```

### A.2 CachedBodyRequestWrapper

```java
// moni-kit/monitoring-starter-web/filter/RequestWrapper.java
// Request Body 캐싱 로직 재사용
```

### A.3 경로 정규화 로직

```java
// moni-kit/monitoring-metric/PathNormalizer.java (있다면)
// /api/users/123 → /api/users/{id} 변환 로직
```

---

## 부록 B: 체크리스트

### B.1 구현 체크리스트

- [ ] TraceIdFilter 구현
- [ ] TraceIdHolder (MDC 연동) 구현
- [ ] HttpLoggingFilter 구현
- [ ] CachedBodyRequestWrapper 구현
- [ ] LogMasker 구현
- [ ] ObservabilityProperties 구현
- [ ] ObservabilityAutoConfiguration 구현
- [ ] logback-json-defaults.xml 작성
- [ ] README.md 작성
- [ ] 단위 테스트 작성
- [ ] 통합 테스트 작성

### B.2 품질 체크리스트

- [ ] 30개 이하 파일
- [ ] 테스트 커버리지 80% 이상
- [ ] Javadoc 작성
- [ ] 예제 코드 작성
- [ ] 성능 테스트 (오버헤드 측정)

---

**문서 끝**
