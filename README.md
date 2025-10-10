# 🕷️ CrawlingHub

**Hexagonal Architecture 기반 통합 크롤링 플랫폼**

CrawlingHub는 다양한 소스로부터 데이터를 수집하고 처리하는 Spring Boot 3.3.x + Java 21 기반 크롤링 플랫폼입니다.

---

## 📋 핵심 특징

### ✅ **헥사고날 아키텍처 (Ports & Adapters)**
- **Domain**: 순수 비즈니스 로직 (프레임워크 독립적)
- **Application**: 유즈케이스 및 서비스 계층
- **Adapter**: 외부 시스템 연동 (In/Out)
- **Bootstrap**: 실행 가능한 애플리케이션

### 🔒 **Level 3 엄격 규칙**
- ArchUnit 아키텍처 테스트
- Checkstyle 코드 스타일 강제
- SpotBugs 정적 분석
- **Lombok 전체 금지**
- 데드코드 자동 감지

### 🎯 **테스트 커버리지 (JaCoCo 자동 검증)**
- Domain: 90% 이상 (빌드 시 자동 검증)
- Application: 80% 이상 (빌드 시 자동 검증)
- Adapter: 70% 이상 (빌드 시 자동 검증)

### 🚀 **기술 스택**
- Java 21
- Spring Boot 3.3.0
- Gradle Kotlin DSL
- JPA + QueryDSL
- PostgreSQL
- AWS SDK v2

---

## 📁 프로젝트 구조

```
crawlinghub/
├── domain/                           # 순수 비즈니스 로직
│   └── src/main/java/
│       └── com/ryuqq/crawlinghub/domain/
│           ├── model/                # 도메인 엔티티
│           ├── vo/                   # Value Objects
│           ├── service/              # 도메인 서비스
│           └── exception/            # 도메인 예외
│
├── application/                      # 유즈케이스/서비스
│   └── src/main/java/
│       └── com/ryuqq/crawlinghub/application/
│           ├── port/
│           │   ├── in/               # Inbound Ports (Driving)
│           │   └── out/              # Outbound Ports (Driven)
│           ├── usecase/              # 유즈케이스 구현
│           └── service/              # 애플리케이션 서비스
│
├── adapter/                          # 외부 시스템 어댑터
│   ├── adapter-in-admin-web/         # REST API
│   ├── adapter-out-persistence-jpa/  # JPA 리포지토리
│   ├── adapter-out-aws-s3/           # S3 파일 저장소
│   └── adapter-out-aws-sqs/          # SQS 메시징
│
├── bootstrap/                        # 실행 가능 애플리케이션
│   └── bootstrap-web-api/            # Web API 부트스트랩
│       └── src/main/java/
│           └── com/ryuqq/crawlinghub/
│               ├── Application.java
│               └── config/           # Spring 설정
│
├── .claude/                          # Claude Code 설정
│   ├── README.md                     # Claude Code 가이드
│   ├── commands/                     # 슬래시 커맨드
│   │   └── gemini-review.md         # Gemini 리뷰 분석
│   ├── hooks/                        # 동적 훅
│   │   ├── user-prompt-submit.sh    # 코드 생성 전 규칙 주입
│   │   └── after-tool-use.sh        # 코드 생성 후 검증
│   └── agents/                       # 전문 에이전트
│       └── prompt-engineer.md       # 프롬프트 최적화 전문가
│
├── config/                           # 품질 게이트 설정
│   ├── checkstyle/
│   │   └── checkstyle.xml
│   └── spotbugs/
│       └── spotbugs-exclude.xml
│
├── hooks/                            # Git Hooks
│   ├── pre-commit                    # 마스터 훅
│   └── validators/                   # 모듈별 검증기
│       ├── domain-validator.sh
│       ├── application-validator.sh
│       ├── adapter-in-validator.sh
│       ├── adapter-out-validator.sh
│       ├── common-validator.sh
│       └── dead-code-detector.sh
│
└── terraform/                        # Infrastructure as Code
    └── (TODO: ECS, VPC, RDS 모듈)
```

---

## 🚀 시작하기

### 1. Git Hooks 설치

```bash
# 프로젝트 클론 후
ln -s ../../hooks/pre-commit .git/hooks/pre-commit
chmod +x .git/hooks/pre-commit
```

### 2. 빌드 및 테스트

```bash
# 전체 빌드 (품질 게이트 포함)
./gradlew build

# 아키텍처 테스트만 실행
./gradlew :domain:test --tests "*HexagonalArchitectureTest"

# 데드코드 감지
./gradlew detectDeadCode

# Checkstyle 검사
./gradlew checkstyleMain

# SpotBugs 분석
./gradlew spotbugsMain

# JaCoCo 커버리지 리포트 생성
./gradlew jacocoTestReport

# JaCoCo 커버리지 검증 (최소 커버리지 체크)
./gradlew jacocoTestCoverageVerification
```

### 3. 애플리케이션 실행

```bash
./gradlew :bootstrap:bootstrap-web-api:bootRun
```

---

## 🏗️ 아키텍처 규칙

### ❌ **금지 사항 (Zero Tolerance)**

#### Domain 모듈
```java
// ❌ FORBIDDEN
import org.springframework.*;
import jakarta.persistence.*;
import lombok.*;

// ✅ ALLOWED
import java.util.*;
import org.apache.commons.lang3.*;
```

#### Application 모듈
```java
// ❌ FORBIDDEN
import com.company.template.adapter.*;  // 어댑터 직접 참조 금지

// ✅ ALLOWED
import com.company.template.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
```

#### Lombok 전체 금지
```java
// ❌ STRICTLY PROHIBITED
@Data
@Builder
@Getter
@Setter
@AllArgsConstructor

// ✅ REQUIRED - Use plain Java
public class Order {
    private final String id;
    private final Money amount;

    public Order(String id, Money amount) {
        this.id = id;
        this.amount = amount;
    }

    public String getId() {
        return id;
    }

    public Money getAmount() {
        return amount;
    }
}
```

---

## 📝 문서화 규칙

### Public API Javadoc 필수

```java
/**
 * 주문을 생성합니다.
 *
 * @param request 주문 생성 요청
 * @return 생성된 주문
 * @throws InvalidOrderException 주문 검증 실패시
 * @author 홍길동 (hong.gildong@company.com)
 * @since 2024-01-01
 */
public Order createOrder(CreateOrderRequest request) {
    // implementation
}
```

---

## 🔍 Git Pre-Commit Hook 흐름

```
커밋 시도
    ↓
마스터 훅 (pre-commit)
    ↓
파일 경로 분석 → 모듈 감지
    ↓
┌──────────────┬──────────────┬──────────────┐
│ Domain       │ Application  │ Adapter      │
│ Validator    │ Validator    │ Validator    │
└──────────────┴──────────────┴──────────────┘
    ↓
공통 검증 (Javadoc, @author 태그)
    ↓
데드코드 감지 (Utils/Helper 클래스)
    ↓
ArchUnit 테스트 실행
    ↓
✅ 통과 → 커밋 허용
❌ 실패 → 커밋 차단
```

---

## 🧪 테스트 전략

### Domain 테스트 (90% 커버리지)
```java
@Test
void 주문_생성_성공() {
    // given
    Money amount = Money.of(10000);

    // when
    Order order = Order.create("ORD-001", amount);

    // then
    assertThat(order.getId()).isEqualTo("ORD-001");
    assertThat(order.getAmount()).isEqualTo(amount);
}
```

### Application 테스트 (80% 커버리지)
```java
@Test
void 주문_생성_유즈케이스_성공() {
    // given
    CreateOrderUseCase useCase = new CreateOrderService(orderRepository);

    // when
    Order order = useCase.execute(request);

    // then
    verify(orderRepository).save(any(Order.class));
}
```

### Adapter 테스트 (70% 커버리지 + Testcontainers)
```java
@Testcontainers
class OrderJpaRepositoryTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Test
    void 주문_저장_성공() {
        // given
        OrderEntity entity = new OrderEntity("ORD-001", 10000);

        // when
        OrderEntity saved = repository.save(entity);

        // then
        assertThat(saved.getId()).isNotNull();
    }
}
```

---

## 🛠️ 개발 워크플로우

### 1. 새로운 기능 개발

```bash
# 1. Feature 브랜치 생성
git checkout -b feature/order-management

# 2. Domain부터 작성 (TDD)
# domain/src/main/java/com/company/template/domain/model/Order.java
# domain/src/test/java/com/company/template/domain/model/OrderTest.java

# 3. Application 계층 (유즈케이스)
# application/src/main/java/com/company/template/application/usecase/CreateOrderUseCase.java

# 4. Adapter 구현
# adapter/adapter-in-admin-web/src/main/java/...OrderController.java
# adapter/adapter-out-persistence-jpa/src/main/java/...OrderJpaRepository.java

# 5. 커밋 (자동 검증)
git add .
git commit -m "feat: 주문 생성 기능 구현"
# → Pre-commit hook 자동 실행
# → 모든 검증 통과시 커밋 완료
```

### 2. 품질 게이트 통과 체크리스트

- [ ] ArchUnit 테스트 통과
- [ ] Checkstyle 위반 없음
- [ ] SpotBugs 버그 없음
- [ ] 테스트 커버리지 달성
- [ ] Javadoc 작성 (Public API)
- [ ] @author 태그 포함
- [ ] Lombok 미사용
- [ ] 데드코드 없음

---

## ⚙️ 설정 파일

### application.yml (bootstrap-web-api)

```yaml
spring:
  application:
    name: spring-hexagonal-template

  datasource:
    url: jdbc:postgresql://localhost:5432/template
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: true
        show_sql: false

  flyway:
    enabled: true
    locations: classpath:db/migration

logging:
  level:
    com.company.template: DEBUG
    org.springframework.web: INFO

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

---

## 📚 문서

### 핵심 문서
- **[코딩 표준 (87개 규칙)](docs/CODING_STANDARDS.md)** - Domain, Application, Adapter 계층별 상세 규칙
- **[버전 관리 가이드](docs/VERSION_MANAGEMENT_GUIDE.md)** - Gradle Version Catalog 사용법
- **[동적 훅 가이드](docs/DYNAMIC_HOOKS_GUIDE.md)** - Claude Code 동적 훅 시스템
- **[Gemini 리뷰 분석 가이드](docs/GEMINI_REVIEW_GUIDE.md)** - AI 코드 리뷰 체계적 분석 및 리팩토링 전략

### Claude Code 설정
- **[Claude Code 가이드](.claude/README.md)** - 프로젝트별 Claude Code 설정 및 사용법
- **[슬래시 커맨드](.claude/commands/)** - Gemini 리뷰 분석 등 자동화 커맨드
- **[동적 훅](.claude/hooks/)** - 코드 생성 시 자동 규칙 주입 및 검증
- **[전문 에이전트](.claude/agents/)** - 프롬프트 최적화 등 특화 에이전트

### 품질 도구 가이드
- **[Checkstyle 설정 가이드](config/checkstyle/README.md)** - 코드 스타일 검증 규칙
- **[SpotBugs 설정 가이드](config/spotbugs/README.md)** - 정적 분석 및 버그 탐지

### 설정 및 프롬프트
- **[Spring 표준 프롬프트](docs/ENTERPRISE_SPRING_STANDARDS_PROMPT.md)** - AI 코드 생성 표준
- **[설정 요약](docs/SETUP_SUMMARY.md)** - 프로젝트 설정 가이드

### 예외 관리 시스템

⚠️ **현재 상태**: **미구현** (향후 개선 후보)

현재 프로젝트에서 규칙 예외를 허용하려면 **validator 스크립트를 직접 수정**해야 합니다:
- Git Hook Validators: `hooks/validators/*.sh`
- Claude Code Hooks: `.claude/hooks/*.sh`

**향후 개선 계획**:
- `.claude/exceptions.json` 기반 예외 관리 시스템
- 경로 패턴 기반 규칙 예외 허용
- 예외 승인자 및 만료일 관리

**현재 우회 방법**:
```bash
# 예시: JPA Entity에서 Lombok 허용이 필요한 경우
# hooks/validators/domain-validator.sh 수정
if [[ "$file" == *"/entity/"* ]]; then
    echo "⚠️  JPA Entity 예외 허용"
    continue
fi
```

자세한 내용은 [factcheck-todo.md](factcheck-todo.md)의 "SECTION 3: 예외 관리 시스템" 참조

---

### 아키텍처 가이드 (계획)
- [헥사고날 아키텍처 심화](docs/architecture/hexagonal-architecture.md) (TODO)
- [도메인 주도 설계 패턴](docs/architecture/ddd-patterns.md) (TODO)
- [테스트 전략](docs/testing/testing-strategy.md) (TODO)

---



**🎯 목표**: 어떤 프로젝트에서도 동일한 품질의 규격화된 코드 생성

© 2024 Ryu-qqq. All Rights Reserved.
