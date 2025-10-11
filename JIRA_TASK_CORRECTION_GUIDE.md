# Jira Task Correction Guide

**CrawlingHub 프로젝트의 Jira 태스크와 코딩 규약 간 불일치 수정 가이드**

---

## 1. Executive Summary

### 1.1 Why Jira Tasks Need Correction

현재 CrawlingHub 프로젝트의 일부 Jira 태스크 설명이 프로젝트의 핵심 아키텍처 원칙 및 코딩 표준과 충돌하고 있습니다. 이러한 불일치는 다음과 같은 문제를 야기합니다:

**문제점:**
- Domain 레이어에 JPA 애노테이션이 포함된 Entity 구현 요구
- Lombok 사용 지시 (프로젝트 전체 금지)
- Hexagonal Architecture의 의존성 규칙 위반
- 순수 도메인 모델과 영속성 계층의 혼재

**영향:**
- 신규 개발자가 잘못된 태스크 설명을 따라 규칙 위반 코드 작성
- Pre-commit hook 및 빌드 실패로 개발 생산성 저하
- 아키텍처 원칙 훼손 및 기술 부채 증가
- Git hook 및 ArchUnit 테스트가 커밋 차단

### 1.2 Impact of Current Misalignment

**심각도 분류:**
- **🔴 Critical**: CRAW-68 (Domain Entity Implementation) - JPA 및 Lombok 사용 지시
- **🟡 Important**: CRAW-76 (Test Code Writing) - 테스트 전략 혼재

**즉시 영향:**
1. 개발자가 태스크 설명대로 코드 작성 시 빌드 실패 (`checkNoLombok` task)
2. Git pre-commit hook이 Domain 레이어의 JPA import 감지하여 커밋 차단
3. ArchUnit 테스트가 의존성 규칙 위반 감지 (`HexagonalArchitectureTest`)

### 1.3 Correction Priority and Scope

**우선순위:**

| 우선순위 | Jira Task | 심각도 | 수정 범위 |
|---------|-----------|--------|-----------|
| 1 | CRAW-68 | 🔴 Critical | 태스크 설명 전면 수정 필요 |
| 2 | CRAW-76 | 🟡 Important | 테스트 전략 부분 수정 |
| 3 | Future Tasks | 🟢 Prevention | 신규 태스크 생성 템플릿 적용 |

**수정 범위:**
- 기존 태스크 설명 재작성 (CRAW-68, CRAW-76)
- 향후 태스크 생성 시 참고할 표준 템플릿 제공
- 태스크 검증 체크리스트 배포

---

## 2. Hexagonal Architecture Principles Review

### 2.1 Dependency Rules

```
┌─────────────────────────────────────────────────┐
│           Hexagonal Architecture                │
│                                                 │
│   Bootstrap → Adapter → Application → Domain   │
│                  ↓           ↓                  │
│              (구현)      (인터페이스)              │
└─────────────────────────────────────────────────┘
```

**의존성 방향:**
- **Domain**: 아무것도 의존하지 않음 (완전 독립)
- **Application**: Domain만 의존
- **Adapter-In**: Application (Port) + Domain 의존
- **Adapter-Out**: Application (Port) + Domain 의존
- **Bootstrap**: 모든 레이어 의존 (조립 목적)

**금지되는 의존성:**
- ❌ Adapter → Adapter (Adapter 간 직접 의존 절대 금지)
- ❌ Application → Adapter (구체 구현 의존 금지)
- ❌ Domain → 모든 외부 의존성 (완전 순수성)

### 2.2 Domain vs Adapter Layer Responsibilities

#### Domain Layer (순수 비즈니스 로직)

**책임:**
- 비즈니스 규칙 및 로직
- 도메인 객체 (Aggregate, Entity, Value Object)
- 도메인 서비스 (여러 Aggregate 간 로직)
- 도메인 예외

**허용:**
```java
// ✅ ALLOWED
import java.util.*;
import java.time.*;
import jakarta.validation.*;  // 표준 검증만
import org.apache.commons.lang3.StringUtils;  // 순수 유틸리티만
```

**금지:**
```java
// ❌ FORBIDDEN
import org.springframework.*;
import jakarta.persistence.*;
import org.hibernate.*;
import lombok.*;
import com.amazonaws.*;
```

#### Persistence Adapter Layer (영속성 계층)

**책임:**
- JPA Entity 관리
- Repository 구현
- Domain ↔ Entity 매핑
- 데이터베이스 상호작용

**허용:**
```java
// ✅ ALLOWED
import jakarta.persistence.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Component;
```

**핵심 원칙:**
- JPA Entity는 **Adapter Layer에만** 존재
- Domain Model은 **순수 Java**로만 작성
- Mapper를 통해 Domain ↔ Entity 변환

### 2.3 Why JPA Belongs in Adapter Layer, Not Domain

**이유:**

1. **프레임워크 독립성**: Domain은 어떤 영속성 기술에도 종속되지 않아야 함
2. **테스트 용이성**: Domain 로직은 데이터베이스 없이 단위 테스트 가능
3. **유연성**: 영속성 기술 교체 시 Domain 코드는 변경 불필요
4. **비즈니스 로직 집중**: Domain은 기술 세부사항 없이 비즈니스 규칙에만 집중

**잘못된 접근:**
```java
// ❌ BAD - Domain Layer에 JPA Entity
package com.ryuqq.crawlinghub.domain.site;

import jakarta.persistence.*;  // ❌ Domain에 JPA 의존성!

@Entity  // ❌ Domain에 @Entity 금지!
@Table(name = "crawl_site")
public class CrawlSite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long siteId;

    @ManyToOne  // ❌ Domain에 연관관계 금지!
    private SiteProfile profile;
}
```

**올바른 접근:**
```java
// ✅ GOOD - Domain Layer에 순수 Java
package com.ryuqq.crawlinghub.domain.site;

public class CrawlSite {
    private final Long siteId;
    private final String siteName;

    private CrawlSite(Long siteId, String siteName) {
        this.siteId = siteId;
        this.siteName = siteName;
    }

    public static CrawlSite create(String siteName) {
        validateSiteName(siteName);
        return new CrawlSite(null, siteName);
    }

    public static CrawlSite reconstitute(Long siteId, String siteName) {
        return new CrawlSite(siteId, siteName);
    }

    private static void validateSiteName(String siteName) {
        if (siteName == null || siteName.isBlank()) {
            throw new IllegalArgumentException("Site name cannot be blank");
        }
    }

    public Long getSiteId() {
        return siteId;
    }

    public String getSiteName() {
        return siteName;
    }
}
```

---

## 3. Specific Task Corrections

### 3.1 CRAW-68: Domain Entity Implementation

#### 3.1.1 Current Task Description (문제점)

**현재 태스크 설명 (추정):**
```
21개의 JPA Entity 클래스 구현
- Lombok 어노테이션 적용 (@Entity, @Table, @Id, @GeneratedValue)
- JPA 연관관계 매핑 (@OneToMany, @ManyToOne)
- Getter/Setter 자동 생성
```

#### 3.1.2 Issues Identified

| 문제점 | 심각도 | 위반 규칙 |
|-------|--------|----------|
| Lombok 사용 지시 | 🔴 Critical | `build.gradle.kts` lines 159-182 (Lombok 전체 금지) |
| Domain에 JPA 애노테이션 | 🔴 Critical | CODING_STANDARDS.md Domain Layer 규칙 |
| JPA 연관관계 사용 | 🔴 Critical | CODING_STANDARDS.md Persistence Adapter 규칙 |
| Domain과 Entity 혼동 | 🔴 Critical | Hexagonal Architecture 의존성 규칙 |

**자동 차단:**
- `./gradlew build` → `checkNoLombok` task 실패
- `git commit` → `domain-validator.sh` 차단
- Architecture Test → `HexagonalArchitectureTest` 실패

#### 3.1.3 Corrected Task Description

**제목:** Domain Model 구현 (21개 순수 도메인 객체)

**설명:**

CrawlingHub의 핵심 도메인 모델을 **순수 Java**로 구현합니다.

**구현 범위:**
- 21개의 Domain 객체 (Aggregate Root, Entity, Value Object)
- 도메인 로직 및 비즈니스 규칙
- Domain 전용 예외 클래스

**구현 위치:**
- `domain/src/main/java/com/ryuqq/crawlinghub/domain/`
- Aggregate별 서브패키지 구조 사용 (예: `site/`, `schedule/`, `workflow/`)

**구현 규칙:**
1. **순수 Java만 사용** - 모든 프레임워크 의존성 금지
2. **불변성 (Immutability)** - 모든 필드는 `private final` (일부 상태 변경 필드 제외)
3. **정적 팩토리 메서드** 사용
   - `create()`: 신규 도메인 객체 생성
   - `reconstitute()`: 영속성 계층에서 복원 시 사용
4. **비즈니스 로직 포함** - 상태 전이, 검증, 계산은 Domain 객체 내부에 위치
5. **Setter 금지** - 상태 변경은 명시적 메서드로 (`updateStatus()`, `enable()` 등)
6. **Private 생성자** - 외부에서 직접 생성 금지

**금지 사항:**
- ❌ Lombok (`@Data`, `@Builder`, `@Getter`, `@Setter` 등 모든 애노테이션)
- ❌ JPA (`@Entity`, `@Table`, `@Id`, `@GeneratedValue`, `@OneToMany` 등)
- ❌ Spring Framework (`@Component`, `@Service` 등)
- ❌ Public 생성자
- ❌ Setter 메서드

**코드 예시:**

```java
package com.ryuqq.crawlinghub.domain.site;

public class CrawlSite {

    private final Long siteId;
    private final String siteName;
    private final String siteUrl;
    private boolean isActive;

    // Private 생성자
    private CrawlSite(Long siteId, String siteName, String siteUrl, boolean isActive) {
        this.siteId = siteId;
        this.siteName = siteName;
        this.siteUrl = siteUrl;
        this.isActive = isActive;
    }

    // 정적 팩토리 - 신규 생성
    public static CrawlSite create(String siteName, String siteUrl) {
        validateSiteName(siteName);
        validateSiteUrl(siteUrl);
        return new CrawlSite(null, siteName, siteUrl, true);
    }

    // 정적 팩토리 - 영속성 계층에서 복원
    public static CrawlSite reconstitute(Long siteId, String siteName, String siteUrl, boolean isActive) {
        return new CrawlSite(siteId, siteName, siteUrl, isActive);
    }

    // 비즈니스 로직
    public void activate() {
        if (this.isActive) {
            throw new IllegalStateException("Site is already active");
        }
        this.isActive = true;
    }

    public void deactivate() {
        if (!this.isActive) {
            throw new IllegalStateException("Site is already inactive");
        }
        this.isActive = false;
    }

    // 검증 로직
    private static void validateSiteName(String siteName) {
        if (siteName == null || siteName.isBlank()) {
            throw new IllegalArgumentException("Site name cannot be null or blank");
        }
        if (siteName.length() > 200) {
            throw new IllegalArgumentException("Site name exceeds maximum length of 200");
        }
    }

    private static void validateSiteUrl(String siteUrl) {
        if (siteUrl == null || siteUrl.isBlank()) {
            throw new IllegalArgumentException("Site URL cannot be null or blank");
        }
        // URL 형식 검증 로직
    }

    // Getter만 (Setter 금지)
    public Long getSiteId() {
        return siteId;
    }

    public String getSiteName() {
        return siteName;
    }

    public String getSiteUrl() {
        return siteUrl;
    }

    public boolean isActive() {
        return isActive;
    }
}
```

**Value Object 예시 (record 사용):**

```java
package com.ryuqq.crawlinghub.domain.site;

public record SiteId(Long value) {
    public SiteId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Site ID must be positive");
        }
    }
}
```

**테스트 커버리지:**
- Domain 레이어: 90% 이상 필수
- 단위 테스트만 작성 (순수 JUnit, 프레임워크 의존성 없음)

**검증:**
- `./gradlew :domain:test` - 단위 테스트 실행
- `./gradlew :domain:test --tests "*HexagonalArchitectureTest"` - 아키텍처 테스트
- `git commit` - Pre-commit hook 자동 검증

**완료 조건 (Definition of Done):**
- [ ] 21개 도메인 객체 구현 완료
- [ ] 모든 필드 `private final` (또는 명시적 상태 변경 필드)
- [ ] `create()` 및 `reconstitute()` 정적 팩토리 메서드 구현
- [ ] 비즈니스 로직 및 검증 로직 포함
- [ ] Lombok, JPA, Spring 의존성 없음
- [ ] 단위 테스트 커버리지 90% 이상
- [ ] ArchUnit 아키텍처 테스트 통과
- [ ] Pre-commit hook 검증 통과

---

#### 3.1.4 Implementation Guidance

**Step-by-Step Approach:**

**1단계: Domain 객체 설계 (TDD)**

각 Domain 객체에 대해 테스트부터 작성:

```java
// domain/src/test/java/com/ryuqq/crawlinghub/domain/site/CrawlSiteTest.java
package com.ryuqq.crawlinghub.domain.site;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CrawlSite 도메인 모델 테스트")
class CrawlSiteTest {

    @Test
    @DisplayName("유효한 정보로 CrawlSite를 생성할 수 있다")
    void create_WithValidInfo_ShouldSucceed() {
        // given
        String siteName = "Example Site";
        String siteUrl = "https://example.com";

        // when
        CrawlSite site = CrawlSite.create(siteName, siteUrl);

        // then
        assertThat(site.getSiteName()).isEqualTo(siteName);
        assertThat(site.getSiteUrl()).isEqualTo(siteUrl);
        assertThat(site.isActive()).isTrue();
        assertThat(site.getSiteId()).isNull();  // 신규 생성 시 ID는 null
    }

    @Test
    @DisplayName("사이트 이름이 null이면 예외 발생")
    void create_WithNullName_ShouldThrowException() {
        // given
        String siteName = null;
        String siteUrl = "https://example.com";

        // when & then
        assertThatThrownBy(() -> CrawlSite.create(siteName, siteUrl))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Site name cannot be null");
    }

    @Test
    @DisplayName("비활성 사이트를 활성화할 수 있다")
    void activate_WhenInactive_ShouldSucceed() {
        // given
        CrawlSite site = CrawlSite.reconstitute(1L, "Example", "https://example.com", false);

        // when
        site.activate();

        // then
        assertThat(site.isActive()).isTrue();
    }
}
```

**2단계: Domain 객체 구현**

테스트를 통과하도록 순수 Java로 구현:

```java
// domain/src/main/java/com/ryuqq/crawlinghub/domain/site/CrawlSite.java
package com.ryuqq.crawlinghub.domain.site;

public class CrawlSite {

    private final Long siteId;
    private final String siteName;
    private final String siteUrl;
    private boolean isActive;

    private CrawlSite(Long siteId, String siteName, String siteUrl, boolean isActive) {
        this.siteId = siteId;
        this.siteName = siteName;
        this.siteUrl = siteUrl;
        this.isActive = isActive;
    }

    public static CrawlSite create(String siteName, String siteUrl) {
        validateSiteName(siteName);
        validateSiteUrl(siteUrl);
        return new CrawlSite(null, siteName, siteUrl, true);
    }

    public static CrawlSite reconstitute(Long siteId, String siteName, String siteUrl, boolean isActive) {
        return new CrawlSite(siteId, siteName, siteUrl, isActive);
    }

    public void activate() {
        if (this.isActive) {
            throw new IllegalStateException("Site is already active");
        }
        this.isActive = true;
    }

    public void deactivate() {
        if (!this.isActive) {
            throw new IllegalStateException("Site is already inactive");
        }
        this.isActive = false;
    }

    private static void validateSiteName(String siteName) {
        if (siteName == null || siteName.isBlank()) {
            throw new IllegalArgumentException("Site name cannot be null or blank");
        }
    }

    private static void validateSiteUrl(String siteUrl) {
        if (siteUrl == null || siteUrl.isBlank()) {
            throw new IllegalArgumentException("Site URL cannot be null or blank");
        }
    }

    public Long getSiteId() { return siteId; }
    public String getSiteName() { return siteName; }
    public String getSiteUrl() { return siteUrl; }
    public boolean isActive() { return isActive; }
}
```

**3단계: 검증**

```bash
# 단위 테스트 실행
./gradlew :domain:test

# 아키텍처 테스트 실행
./gradlew :domain:test --tests "*HexagonalArchitectureTest"

# 커버리지 확인
./gradlew :domain:jacocoTestReport
# 리포트: domain/build/reports/jacoco/test/html/index.html
```

**4단계: 21개 객체 구현 완료**

각 Aggregate별로 위 과정 반복:
- `site/` - CrawlSite, SiteProfile, SiteConfig 등
- `schedule/` - CrawlSchedule, ScheduleInputParam 등
- `workflow/` - CrawlWorkflow, WorkflowStep 등
- `task/` - CrawlTask, TaskResult 등
- `endpoint/` - CrawlEndpoint, EndpointConfig 등

---

### 3.2 CRAW-76: Test Code Writing

#### 3.2.1 Current Task Description (문제점)

**현재 태스크 설명 (추정):**
```
단위 테스트, 통합 테스트, E2E 테스트 작성
- Domain: Spring @DataJpaTest 사용
- Repository: H2 in-memory database 사용
- Controller: MockMvc 사용
```

#### 3.2.2 Issues Identified

| 문제점 | 심각도 | 위반 규칙 |
|-------|--------|----------|
| Domain 테스트에 Spring 사용 | 🟡 Important | Domain은 순수 JUnit만 사용 |
| H2 사용 권장 | 🟡 Important | Testcontainers 권장 (PostgreSQL) |
| 테스트 전략 혼재 | 🟡 Important | 레이어별 명확한 테스트 전략 필요 |

#### 3.2.3 Corrected Task Description

**제목:** 레이어별 테스트 코드 작성

**설명:**

Hexagonal Architecture 레이어별로 적절한 테스트 전략을 적용하여 테스트 코드를 작성합니다.

**테스트 커버리지 목표:**
- Domain: 90% 이상
- Application: 80% 이상
- Adapter: 70% 이상

**레이어별 테스트 전략:**

**1. Domain 테스트 (Unit Test)**
- **도구**: 순수 JUnit 5 + AssertJ
- **특징**: 프레임워크 의존성 없음, 빠른 실행
- **대상**: 비즈니스 로직, 검증 로직, 상태 전이

```java
@DisplayName("CrawlSite 도메인 모델 테스트")
class CrawlSiteTest {

    @Test
    @DisplayName("유효한 정보로 사이트 생성")
    void create_WithValidInfo_ShouldSucceed() {
        // given
        String siteName = "Example";

        // when
        CrawlSite site = CrawlSite.create(siteName, "https://example.com");

        // then
        assertThat(site.getSiteName()).isEqualTo(siteName);
        assertThat(site.isActive()).isTrue();
    }
}
```

**2. Application 테스트 (Service Test)**
- **도구**: JUnit 5 + Test Double (Inner Static Class)
- **특징**: Port 인터페이스의 Test Double 구현
- **대상**: UseCase 로직, 트랜잭션 경계, Port 조합

```java
@DisplayName("CreateSiteService 테스트")
class CreateSiteServiceTest {

    private CreateSiteService service;
    private TestSaveSitePort saveSitePort;

    @BeforeEach
    void setUp() {
        saveSitePort = new TestSaveSitePort();
        service = new CreateSiteService(saveSitePort);
    }

    @Test
    @DisplayName("사이트 생성 성공")
    void execute_WithValidCommand_ShouldSucceed() {
        // given
        CreateSiteCommand command = new CreateSiteCommand("Example", "https://example.com");

        // when
        SiteResponse response = service.execute(command);

        // then
        assertThat(response).isNotNull();
        assertThat(saveSitePort.getSavedSite()).isNotNull();
    }

    // Test Double (Inner Static Class)
    static class TestSaveSitePort implements SaveSitePort {
        private CrawlSite savedSite;

        @Override
        public CrawlSite save(CrawlSite site) {
            this.savedSite = site;
            return CrawlSite.reconstitute(1L, site.getSiteName(), site.getSiteUrl(), site.isActive());
        }

        CrawlSite getSavedSite() {
            return savedSite;
        }
    }
}
```

**3. Adapter 테스트 (Integration Test)**
- **도구**: Spring Boot Test + Testcontainers (PostgreSQL)
- **특징**: 실제 데이터베이스 사용, Spring Context 로딩
- **대상**: JPA Repository, Mapper, 외부 시스템 연동

```java
@SpringBootTest
@Testcontainers
@DisplayName("SitePersistenceAdapter 통합 테스트")
class SitePersistenceAdapterTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
        .withDatabaseName("test")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private SitePersistenceAdapter adapter;

    @Test
    @DisplayName("사이트 저장 및 조회")
    void save_AndLoad_ShouldSucceed() {
        // given
        CrawlSite site = CrawlSite.create("Example", "https://example.com");

        // when
        CrawlSite saved = adapter.save(site);
        CrawlSite loaded = adapter.loadById(saved.getSiteId()).orElseThrow();

        // then
        assertThat(loaded.getSiteName()).isEqualTo(site.getSiteName());
    }
}
```

**4. E2E 테스트 (Controller Test)**
- **도구**: REST Assured + Testcontainers
- **특징**: 전체 애플리케이션 실행, API 테스트
- **대상**: REST API, 인증/인가, 응답 형식

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@DisplayName("Site API E2E 테스트")
class SiteApiE2ETest {

    @LocalServerPort
    private int port;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("사이트 생성 API 성공")
    void createSite_WithValidRequest_ShouldReturn201() {
        // given
        CreateSiteRequest request = new CreateSiteRequest("Example", "https://example.com");

        // when & then
        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/v1/sites")
        .then()
            .statusCode(201)
            .body("siteName", equalTo("Example"));
    }
}
```

**금지 사항:**
- ❌ Domain 테스트에 Spring 의존성 사용
- ❌ Application 테스트에 Mockito 사용 (Test Double 권장)
- ❌ H2 in-memory database (Testcontainers 사용)

**완료 조건:**
- [ ] Domain 테스트 커버리지 90% 이상
- [ ] Application 테스트 커버리지 80% 이상
- [ ] Adapter 테스트 커버리지 70% 이상
- [ ] Testcontainers를 사용한 PostgreSQL 통합 테스트
- [ ] E2E 테스트 작성 (주요 API)

---

## 4. Lombok Prohibition Rationale

### 4.1 Why Lombok is Prohibited

**정책:** CrawlingHub 프로젝트는 Lombok을 **전면 금지**합니다.

**근거 (`build.gradle.kts` lines 159-182):**

```kotlin
// ========================================
// Lombok 금지 검증
// ========================================
tasks.register("checkNoLombok") {
    doLast {
        val lombokFound = configurations.flatMap { config ->
            config.dependencies.filter { dep ->
                dep.group == "org.projectlombok" && dep.name == "lombok"
            }
        }

        if (lombokFound.isNotEmpty()) {
            throw GradleException(
                """
                ❌ LOMBOK DETECTED: Lombok is strictly prohibited in this project.
                Found in: ${project.name}

                Policy: All modules must use pure Java without Lombok.
                """.trimIndent()
            )
        }
    }
}

tasks.build {
    dependsOn("checkNoLombok")
}
```

**빌드 시 자동 검증:** 빌드 시 Lombok 의존성이 발견되면 즉시 빌드 실패

### 4.2 Technical Reasons

**1. 컴파일 타임 코드 조작 (Bytecode Manipulation)**
- Lombok은 컴파일러를 해킹하여 코드 생성
- IDE와 빌드 도구 간 불일치 가능성
- 디버깅 시 생성된 코드가 보이지 않아 혼란

**2. 가독성 저하**
```java
// ❌ Lombok - 실제 필드와 메서드가 숨겨져 있음
@Data
public class Order {
    private String id;
    private Money amount;
}
// → Getter, Setter, equals, hashCode, toString이 어떻게 생성되는지 불명확

// ✅ Pure Java - 모든 것이 명시적
public class Order {
    private final String id;
    private final Money amount;

    public Order(String id, Money amount) {
        this.id = id;
        this.amount = amount;
    }

    public String getId() { return id; }
    public Money getAmount() { return amount; }

    @Override
    public boolean equals(Object o) {
        // 명시적 구현
    }
}
```

**3. 의존성 오염**
- Lombok은 컴파일 타임에만 필요하지만 런타임에도 영향
- 프로젝트의 순수성 저해

**4. 도메인 모델 설계 원칙 위반**
```java
// ❌ Lombok @Data - 모든 필드에 Setter 생성 (불변성 위반)
@Data
public class Order {
    private OrderStatus status;  // Setter가 자동 생성됨
}

// ✅ Pure Java - 의도적인 상태 변경 메서드만 제공
public class Order {
    private OrderStatus status;

    public void confirm() {  // 비즈니스 의미가 명확한 메서드
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("Cannot confirm non-pending order");
        }
        this.status = OrderStatus.CONFIRMED;
    }
}
```

**5. Java 21 기능으로 충분**
- **Record**: Value Object 구현
- **Text Blocks**: 긴 문자열 처리
- **Pattern Matching**: 타입 검사 및 캐스팅
- Lombok의 주요 기능을 Java 표준으로 대체 가능

### 4.3 Alternatives and Best Practices

**Lombok 기능별 대체 방법:**

#### `@Getter` / `@Setter`
```java
// ❌ Lombok
@Getter
@Setter
public class Order {
    private String id;
}

// ✅ Pure Java
public class Order {
    private final String id;

    public String getId() {
        return id;
    }

    // Setter 금지 - 불변성 유지
}
```

#### `@AllArgsConstructor` / `@NoArgsConstructor`
```java
// ❌ Lombok
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    private String id;
    private Money amount;
}

// ✅ Pure Java
public class Order {
    private final String id;
    private final Money amount;

    // Private 생성자 + 정적 팩토리 메서드
    private Order(String id, Money amount) {
        this.id = id;
        this.amount = amount;
    }

    public static Order create(String id, Money amount) {
        validateId(id);
        validateAmount(amount);
        return new Order(id, amount);
    }
}
```

#### `@Builder`
```java
// ❌ Lombok @Builder
@Builder
public class Order {
    private String id;
    private Money amount;
    private List<OrderItem> items;
}

// ✅ Pure Java - Builder Pattern (수동 구현)
public class Order {
    private final String id;
    private final Money amount;
    private final List<OrderItem> items;

    private Order(String id, Money amount, List<OrderItem> items) {
        this.id = id;
        this.amount = amount;
        this.items = List.copyOf(items);  // 방어적 복사
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private Money amount;
        private List<OrderItem> items = new ArrayList<>();

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder amount(Money amount) {
            this.amount = amount;
            return this;
        }

        public Builder items(List<OrderItem> items) {
            this.items = new ArrayList<>(items);
            return this;
        }

        public Order build() {
            return new Order(id, amount, items);
        }
    }
}

// 더 나은 대안: 정적 팩토리 메서드
public static Order create(String id, Money amount, List<OrderItem> items) {
    return new Order(id, amount, items);
}
```

#### `@Data` (Value Object)
```java
// ❌ Lombok @Data
@Data
public class Money {
    private BigDecimal amount;
    private Currency currency;
}

// ✅ Java Record (Java 21)
public record Money(BigDecimal amount, Currency currency) {
    // Compact Constructor - 검증 로직
    public Money {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        if (currency == null) {
            throw new IllegalArgumentException("Currency is required");
        }
    }

    // 추가 메서드
    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot add different currencies");
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }
}
```

#### `@Slf4j`
```java
// ❌ Lombok
@Slf4j
public class OrderService {
    public void process() {
        log.info("Processing order");
    }
}

// ✅ Pure Java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    public void process() {
        log.info("Processing order");
    }
}
```

### 4.4 Examples of Correct Pure Java Implementations

**Domain Model (Aggregate Root):**

```java
package com.ryuqq.crawlinghub.domain.site;

import java.time.LocalDateTime;
import java.util.Objects;

public class CrawlSite {

    private final Long siteId;
    private final String siteName;
    private final String siteUrl;
    private boolean isActive;
    private final LocalDateTime createdAt;

    // Private 생성자
    private CrawlSite(Long siteId, String siteName, String siteUrl,
                      boolean isActive, LocalDateTime createdAt) {
        this.siteId = siteId;
        this.siteName = siteName;
        this.siteUrl = siteUrl;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    // 정적 팩토리 - 신규 생성
    public static CrawlSite create(String siteName, String siteUrl) {
        validateSiteName(siteName);
        validateSiteUrl(siteUrl);
        return new CrawlSite(null, siteName, siteUrl, true, LocalDateTime.now());
    }

    // 정적 팩토리 - 영속성 복원
    public static CrawlSite reconstitute(Long siteId, String siteName, String siteUrl,
                                        boolean isActive, LocalDateTime createdAt) {
        return new CrawlSite(siteId, siteName, siteUrl, isActive, createdAt);
    }

    // 비즈니스 로직
    public void activate() {
        if (this.isActive) {
            throw new IllegalStateException("Site is already active");
        }
        this.isActive = true;
    }

    public void deactivate() {
        if (!this.isActive) {
            throw new IllegalStateException("Site is already inactive");
        }
        this.isActive = false;
    }

    // 검증 로직
    private static void validateSiteName(String siteName) {
        if (siteName == null || siteName.isBlank()) {
            throw new IllegalArgumentException("Site name cannot be null or blank");
        }
        if (siteName.length() > 200) {
            throw new IllegalArgumentException("Site name exceeds maximum length");
        }
    }

    private static void validateSiteUrl(String siteUrl) {
        if (siteUrl == null || siteUrl.isBlank()) {
            throw new IllegalArgumentException("Site URL cannot be null or blank");
        }
    }

    // Getter만 (Setter 금지)
    public Long getSiteId() {
        return siteId;
    }

    public String getSiteName() {
        return siteName;
    }

    public String getSiteUrl() {
        return siteUrl;
    }

    public boolean isActive() {
        return isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // equals & hashCode (ID 기반)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CrawlSite that = (CrawlSite) o;
        return Objects.equals(siteId, that.siteId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(siteId);
    }

    @Override
    public String toString() {
        return "CrawlSite{" +
                "siteId=" + siteId +
                ", siteName='" + siteName + '\'' +
                ", siteUrl='" + siteUrl + '\'' +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                '}';
    }
}
```

**Value Object (Record):**

```java
package com.ryuqq.crawlinghub.domain.site;

import java.util.Objects;

public record SiteId(Long value) {

    // Compact Constructor - 검증 로직
    public SiteId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Site ID must be positive");
        }
    }

    // 정적 팩토리 메서드 (선택사항)
    public static SiteId of(Long value) {
        return new SiteId(value);
    }
}
```

---

## 5. Domain vs JPA Entity Separation Strategy

### 5.1 Overview

**핵심 원칙:** Domain Model과 JPA Entity는 **완전히 분리**되어야 합니다.

```
┌──────────────────────────────────────────────────┐
│          Domain Layer (순수 비즈니스)              │
│  ┌────────────────────────────────────────────┐  │
│  │  CrawlSite (Domain Model)                  │  │
│  │  - 순수 Java                               │  │
│  │  - 비즈니스 로직 포함                       │  │
│  │  - 프레임워크 독립적                        │  │
│  └────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────┘
                        ↕
                    Mapper
                        ↕
┌──────────────────────────────────────────────────┐
│     Persistence Adapter Layer (JPA)              │
│  ┌────────────────────────────────────────────┐  │
│  │  CrawlSiteEntity (JPA Entity)              │  │
│  │  - @Entity, @Table                         │  │
│  │  - 외래키 (Long 타입)                      │  │
│  │  - Getter만 (Setter 금지)                  │  │
│  │  - 비즈니스 로직 없음                       │  │
│  └────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────┘
```

### 5.2 Domain Model (Pure Java)

**위치:** `domain/src/main/java/com/ryuqq/crawlinghub/domain/site/`

**특징:**
- 순수 Java (프레임워크 의존성 없음)
- 비즈니스 로직 및 규칙 포함
- 불변성 (Immutability) 지향
- 정적 팩토리 메서드 사용

**예시:**

```java
package com.ryuqq.crawlinghub.domain.site;

public class CrawlSite {

    private final Long siteId;
    private final String siteName;
    private final String siteUrl;
    private boolean isActive;

    // Private 생성자
    private CrawlSite(Long siteId, String siteName, String siteUrl, boolean isActive) {
        this.siteId = siteId;
        this.siteName = siteName;
        this.siteUrl = siteUrl;
        this.isActive = isActive;
    }

    // 정적 팩토리 - 신규 생성
    public static CrawlSite create(String siteName, String siteUrl) {
        validateSiteName(siteName);
        validateSiteUrl(siteUrl);
        return new CrawlSite(null, siteName, siteUrl, true);
    }

    // 정적 팩토리 - 영속성 복원
    public static CrawlSite reconstitute(Long siteId, String siteName, String siteUrl, boolean isActive) {
        return new CrawlSite(siteId, siteName, siteUrl, isActive);
    }

    // 비즈니스 로직
    public void activate() {
        if (this.isActive) {
            throw new IllegalStateException("Site is already active");
        }
        this.isActive = true;
    }

    public void deactivate() {
        if (!this.isActive) {
            throw new IllegalStateException("Site is already inactive");
        }
        this.isActive = false;
    }

    // 검증 로직
    private static void validateSiteName(String siteName) {
        if (siteName == null || siteName.isBlank()) {
            throw new IllegalArgumentException("Site name cannot be null or blank");
        }
    }

    private static void validateSiteUrl(String siteUrl) {
        if (siteUrl == null || siteUrl.isBlank()) {
            throw new IllegalArgumentException("Site URL cannot be null or blank");
        }
    }

    // Getter만 (Setter 금지)
    public Long getSiteId() { return siteId; }
    public String getSiteName() { return siteName; }
    public String getSiteUrl() { return siteUrl; }
    public boolean isActive() { return isActive; }
}
```

### 5.3 JPA Entity (Adapter Layer)

**위치:** `adapter/adapter-out-persistence-jpa/src/main/java/com/ryuqq/crawlinghub/adapter/persistence/jpa/site/`

**특징:**
- JPA 애노테이션 사용
- 외래키는 Long 타입 필드로만 (연관관계 금지)
- Getter만 제공 (Setter 금지)
- 비즈니스 로직 없음
- Builder 패턴 (수동 구현)

**예시:**

```java
package com.ryuqq.crawlinghub.adapter.persistence.jpa.site;

import com.ryuqq.crawlinghub.adapter.persistence.jpa.common.BaseTimeEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "crawl_site")
public class CrawlSiteEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "site_id")
    private Long siteId;

    @Column(name = "site_name", nullable = false, length = 200)
    private String siteName;

    @Column(name = "site_url", nullable = false, length = 500)
    private String siteUrl;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    // JPA 전용 기본 생성자 (protected)
    protected CrawlSiteEntity() {
    }

    // Private 생성자
    private CrawlSiteEntity(Long siteId, String siteName, String siteUrl, Boolean isActive) {
        this.siteId = siteId;
        this.siteName = siteName;
        this.siteUrl = siteUrl;
        this.isActive = isActive;
    }

    // Getter만 (Setter 금지)
    public Long getSiteId() {
        return siteId;
    }

    public String getSiteName() {
        return siteName;
    }

    public String getSiteUrl() {
        return siteUrl;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    // Builder 패턴 (수동 구현)
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long siteId;
        private String siteName;
        private String siteUrl;
        private Boolean isActive;

        public Builder siteId(Long siteId) {
            this.siteId = siteId;
            return this;
        }

        public Builder siteName(String siteName) {
            this.siteName = siteName;
            return this;
        }

        public Builder siteUrl(String siteUrl) {
            this.siteUrl = siteUrl;
            return this;
        }

        public Builder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public CrawlSiteEntity build() {
            return new CrawlSiteEntity(siteId, siteName, siteUrl, isActive);
        }
    }
}
```

### 5.4 Mapper Pattern

**위치:** `adapter/adapter-out-persistence-jpa/src/main/java/com/ryuqq/crawlinghub/adapter/persistence/jpa/site/mapper/`

**책임:**
- Domain Model ↔ JPA Entity 변환
- 비즈니스 로직 없음
- 단순 데이터 변환만 수행

**예시:**

```java
package com.ryuqq.crawlinghub.adapter.persistence.jpa.site.mapper;

import com.ryuqq.crawlinghub.adapter.persistence.jpa.site.CrawlSiteEntity;
import com.ryuqq.crawlinghub.domain.site.CrawlSite;
import org.springframework.stereotype.Component;

@Component
public class SiteEntityMapper {

    /**
     * JPA Entity → Domain Model 변환
     */
    public CrawlSite toDomain(CrawlSiteEntity entity) {
        return CrawlSite.reconstitute(
            entity.getSiteId(),
            entity.getSiteName(),
            entity.getSiteUrl(),
            entity.getIsActive()
        );
    }

    /**
     * Domain Model → JPA Entity 변환
     */
    public CrawlSiteEntity toEntity(CrawlSite domain) {
        return CrawlSiteEntity.builder()
            .siteId(domain.getSiteId())
            .siteName(domain.getSiteName())
            .siteUrl(domain.getSiteUrl())
            .isActive(domain.isActive())
            .build();
    }
}
```

### 5.5 Repository Implementation (Port 구현)

**위치:** `adapter/adapter-out-persistence-jpa/src/main/java/com/ryuqq/crawlinghub/adapter/persistence/jpa/site/`

**구조:**
1. **JpaRepository** (package-private) - Spring Data JPA 인터페이스
2. **PersistenceAdapter** (public) - Port 구현, Mapper 사용

**예시:**

```java
package com.ryuqq.crawlinghub.adapter.persistence.jpa.site;

import com.ryuqq.crawlinghub.adapter.persistence.jpa.site.mapper.SiteEntityMapper;
import com.ryuqq.crawlinghub.application.site.port.out.LoadSitePort;
import com.ryuqq.crawlinghub.application.site.port.out.SaveSitePort;
import com.ryuqq.crawlinghub.domain.site.CrawlSite;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SitePersistenceAdapter implements SaveSitePort, LoadSitePort {

    private final SiteJpaRepository jpaRepository;
    private final SiteEntityMapper mapper;

    public SitePersistenceAdapter(SiteJpaRepository jpaRepository, SiteEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public CrawlSite save(CrawlSite site) {
        CrawlSiteEntity entity = mapper.toEntity(site);
        CrawlSiteEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<CrawlSite> loadById(Long siteId) {
        return jpaRepository.findById(siteId)
            .map(mapper::toDomain);
    }
}
```

```java
package com.ryuqq.crawlinghub.adapter.persistence.jpa.site;

import org.springframework.data.jpa.repository.JpaRepository;

// package-private (외부 노출 금지)
interface SiteJpaRepository extends JpaRepository<CrawlSiteEntity, Long> {
    // QueryDSL 사용 권장
}
```

### 5.6 Complete Flow Example

**사용 흐름:**

```
1. Controller receives CreateSiteRequest
   ↓
2. Convert to CreateSiteCommand
   ↓
3. UseCase (Application Layer) creates Domain Model
   CrawlSite site = CrawlSite.create("Example", "https://example.com");
   ↓
4. UseCase calls Port to save
   saveSitePort.save(site);
   ↓
5. PersistenceAdapter converts Domain → Entity
   CrawlSiteEntity entity = mapper.toEntity(site);
   ↓
6. JPA Repository saves Entity
   jpaRepository.save(entity);
   ↓
7. PersistenceAdapter converts Entity → Domain
   CrawlSite saved = mapper.toDomain(savedEntity);
   ↓
8. Return to UseCase
   ↓
9. Convert to CreateSiteResponse
   ↓
10. Controller returns HTTP response
```

**코드 예시:**

```java
// 1. Controller (Adapter-In)
@RestController
@RequestMapping("/api/v1/sites")
public class SiteController {

    private final CreateSiteUseCase createSiteUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateSiteResponse createSite(@Valid @RequestBody CreateSiteRequest request) {
        // 2. Request → Command
        CreateSiteCommand command = request.toCommand();

        // 3-8. UseCase 실행
        CreateSiteResult result = createSiteUseCase.execute(command);

        // 9. Result → Response
        return CreateSiteResponse.from(result);
    }
}

// 3. UseCase (Application Layer)
@UseCase
@Transactional
public class CreateSiteService implements CreateSiteUseCase {

    private final SaveSitePort saveSitePort;

    @Override
    public CreateSiteResult execute(CreateSiteCommand command) {
        // 3. Domain Model 생성
        CrawlSite site = CrawlSite.create(
            command.siteName(),
            command.siteUrl()
        );

        // 4-7. Port를 통해 저장 (Mapper 내부에서 변환)
        CrawlSite saved = saveSitePort.save(site);

        // 8. Result 반환
        return CreateSiteResult.from(saved);
    }
}

// 5-7. PersistenceAdapter (Adapter-Out)
@Component
public class SitePersistenceAdapter implements SaveSitePort {

    private final SiteJpaRepository jpaRepository;
    private final SiteEntityMapper mapper;

    @Override
    public CrawlSite save(CrawlSite site) {
        // 5. Domain → Entity 변환
        CrawlSiteEntity entity = mapper.toEntity(site);

        // 6. JPA 저장
        CrawlSiteEntity savedEntity = jpaRepository.save(entity);

        // 7. Entity → Domain 변환
        return mapper.toDomain(savedEntity);
    }
}
```

### 5.7 Key Benefits

**분리의 이점:**

1. **프레임워크 독립성**
   - Domain은 JPA 교체 시에도 영향 없음
   - Hibernate → MyBatis 전환 가능

2. **테스트 용이성**
   - Domain 테스트: 순수 JUnit (빠름)
   - Adapter 테스트: Testcontainers (실제 DB)

3. **비즈니스 로직 집중**
   - Domain 개발자는 영속성 신경 쓰지 않음
   - 기술 세부사항과 분리

4. **유연한 매핑**
   - Domain 1개 → Entity N개 가능
   - Entity 1개 → Domain N개 가능
   - 복잡한 변환 로직 Mapper에 캡슐화

---

## 6. Testing Strategy Correction

### 6.1 Overview

Hexagonal Architecture의 레이어별로 적절한 테스트 도구와 전략을 적용합니다.

**레이어별 테스트 커버리지 목표:**
- **Domain**: 90% 이상 (빌드 시 자동 검증)
- **Application**: 80% 이상 (빌드 시 자동 검증)
- **Adapter**: 70% 이상 (빌드 시 자동 검증)

### 6.2 Domain Tests (Pure Unit Tests)

**특징:**
- **도구**: JUnit 5 + AssertJ
- **의존성**: 프레임워크 없음 (순수 Java)
- **속도**: 매우 빠름 (밀리초 단위)
- **격리**: 완전 격리 (외부 시스템 없음)

**테스트 대상:**
- 비즈니스 규칙 및 로직
- 상태 전이 (activate, confirm 등)
- 검증 로직 (validation)
- Domain 예외 처리

**예시:**

```java
package com.ryuqq.crawlinghub.domain.site;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CrawlSite 도메인 모델 테스트")
class CrawlSiteTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTests {

        @Test
        @DisplayName("유효한 정보로 사이트를 생성할 수 있다")
        void create_WithValidInfo_ShouldSucceed() {
            // given
            String siteName = "Example Site";
            String siteUrl = "https://example.com";

            // when
            CrawlSite site = CrawlSite.create(siteName, siteUrl);

            // then
            assertThat(site.getSiteName()).isEqualTo(siteName);
            assertThat(site.getSiteUrl()).isEqualTo(siteUrl);
            assertThat(site.isActive()).isTrue();
            assertThat(site.getSiteId()).isNull();  // 신규 생성 시 ID는 null
        }

        @Test
        @DisplayName("사이트 이름이 null이면 예외 발생")
        void create_WithNullName_ShouldThrowException() {
            // given
            String siteName = null;
            String siteUrl = "https://example.com";

            // when & then
            assertThatThrownBy(() -> CrawlSite.create(siteName, siteUrl))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Site name cannot be null");
        }

        @Test
        @DisplayName("사이트 이름이 빈 문자열이면 예외 발생")
        void create_WithBlankName_ShouldThrowException() {
            // given
            String siteName = "   ";
            String siteUrl = "https://example.com";

            // when & then
            assertThatThrownBy(() -> CrawlSite.create(siteName, siteUrl))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Site name cannot be null or blank");
        }
    }

    @Nested
    @DisplayName("상태 전이 테스트")
    class StateTransitionTests {

        @Test
        @DisplayName("비활성 사이트를 활성화할 수 있다")
        void activate_WhenInactive_ShouldSucceed() {
            // given
            CrawlSite site = CrawlSite.reconstitute(1L, "Example", "https://example.com", false);

            // when
            site.activate();

            // then
            assertThat(site.isActive()).isTrue();
        }

        @Test
        @DisplayName("이미 활성화된 사이트를 다시 활성화하면 예외 발생")
        void activate_WhenAlreadyActive_ShouldThrowException() {
            // given
            CrawlSite site = CrawlSite.reconstitute(1L, "Example", "https://example.com", true);

            // when & then
            assertThatThrownBy(site::activate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already active");
        }

        @Test
        @DisplayName("활성 사이트를 비활성화할 수 있다")
        void deactivate_WhenActive_ShouldSucceed() {
            // given
            CrawlSite site = CrawlSite.reconstitute(1L, "Example", "https://example.com", true);

            // when
            site.deactivate();

            // then
            assertThat(site.isActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("복원 테스트")
    class ReconstituteTests {

        @Test
        @DisplayName("영속성 계층에서 복원할 수 있다")
        void reconstitute_WithAllFields_ShouldSucceed() {
            // given
            Long siteId = 1L;
            String siteName = "Example";
            String siteUrl = "https://example.com";
            boolean isActive = true;

            // when
            CrawlSite site = CrawlSite.reconstitute(siteId, siteName, siteUrl, isActive);

            // then
            assertThat(site.getSiteId()).isEqualTo(siteId);
            assertThat(site.getSiteName()).isEqualTo(siteName);
            assertThat(site.getSiteUrl()).isEqualTo(siteUrl);
            assertThat(site.isActive()).isEqualTo(isActive);
        }
    }
}
```

**실행:**
```bash
./gradlew :domain:test
./gradlew :domain:jacocoTestReport
# 리포트: domain/build/reports/jacoco/test/html/index.html
```

### 6.3 Application Tests (Service Tests with Test Doubles)

**특징:**
- **도구**: JUnit 5 + Test Double (Inner Static Class 권장)
- **의존성**: Port 인터페이스만
- **격리**: Port는 Test Double로 대체
- **Mockito 지양**: 진짜 객체(Test Double) 사용

**테스트 대상:**
- UseCase 로직
- Port 조합 및 오케스트레이션
- 트랜잭션 경계 (검증은 통합 테스트에서)
- 예외 처리 및 변환

**예시:**

```java
package com.ryuqq.crawlinghub.application.site.service;

import com.ryuqq.crawlinghub.application.site.dto.CreateSiteCommand;
import com.ryuqq.crawlinghub.application.site.dto.CreateSiteResult;
import com.ryuqq.crawlinghub.application.site.port.out.SaveSitePort;
import com.ryuqq.crawlinghub.domain.site.CrawlSite;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CreateSiteService 테스트")
class CreateSiteServiceTest {

    private CreateSiteService service;
    private TestSaveSitePort saveSitePort;

    @BeforeEach
    void setUp() {
        saveSitePort = new TestSaveSitePort();
        service = new CreateSiteService(saveSitePort);
    }

    @Test
    @DisplayName("유효한 커맨드로 사이트 생성에 성공한다")
    void execute_WithValidCommand_ShouldSucceed() {
        // given
        CreateSiteCommand command = new CreateSiteCommand("Example Site", "https://example.com");

        // when
        CreateSiteResult result = service.execute(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.siteId()).isEqualTo(1L);  // TestPort가 반환한 ID
        assertThat(result.siteName()).isEqualTo("Example Site");
        assertThat(saveSitePort.getSavedSite()).isNotNull();
        assertThat(saveSitePort.getSavedSite().getSiteName()).isEqualTo("Example Site");
    }

    @Test
    @DisplayName("사이트 이름이 null이면 예외 발생")
    void execute_WithNullName_ShouldThrowException() {
        // given
        CreateSiteCommand command = new CreateSiteCommand(null, "https://example.com");

        // when & then
        assertThatThrownBy(() -> service.execute(command))
            .isInstanceOf(IllegalArgumentException.class);
    }

    // ========================================
    // Test Double (Inner Static Class)
    // ========================================

    static class TestSaveSitePort implements SaveSitePort {
        private CrawlSite savedSite;

        @Override
        public CrawlSite save(CrawlSite site) {
            this.savedSite = site;
            // 저장된 것처럼 ID를 부여하여 반환
            return CrawlSite.reconstitute(
                1L,  // 가짜 ID
                site.getSiteName(),
                site.getSiteUrl(),
                site.isActive()
            );
        }

        CrawlSite getSavedSite() {
            return savedSite;
        }
    }
}
```

**복잡한 경우: 별도 Fixture Class**

```java
// test/java/com/ryuqq/crawlinghub/fixture/SitePortFixtures.java
package com.ryuqq.crawlinghub.fixture;

import com.ryuqq.crawlinghub.application.site.port.out.LoadSitePort;
import com.ryuqq.crawlinghub.application.site.port.out.SaveSitePort;
import com.ryuqq.crawlinghub.domain.site.CrawlSite;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class SitePortFixtures {

    /**
     * 여러 Port를 통합한 In-Memory Test Double
     */
    public static class InMemorySitePort implements LoadSitePort, SaveSitePort {

        private final Map<Long, CrawlSite> storage = new HashMap<>();
        private final AtomicLong idGenerator = new AtomicLong(1);

        @Override
        public CrawlSite save(CrawlSite site) {
            Long id = site.getSiteId();
            if (id == null) {
                // 신규 생성
                id = idGenerator.getAndIncrement();
                CrawlSite withId = CrawlSite.reconstitute(
                    id,
                    site.getSiteName(),
                    site.getSiteUrl(),
                    site.isActive()
                );
                storage.put(id, withId);
                return withId;
            } else {
                // 업데이트
                storage.put(id, site);
                return site;
            }
        }

        @Override
        public Optional<CrawlSite> loadById(Long siteId) {
            return Optional.ofNullable(storage.get(siteId));
        }

        public void clear() {
            storage.clear();
            idGenerator.set(1);
        }

        public int size() {
            return storage.size();
        }
    }
}
```

**실행:**
```bash
./gradlew :application:test
./gradlew :application:jacocoTestReport
```

### 6.4 Adapter Tests (Integration Tests with Testcontainers)

**특징:**
- **도구**: Spring Boot Test + Testcontainers (PostgreSQL)
- **의존성**: 실제 데이터베이스, Spring Context
- **속도**: 느림 (수 초 단위)
- **격리**: 컨테이너 기반 격리

**테스트 대상:**
- JPA Repository 동작
- Mapper 변환 로직
- 데이터베이스 제약 조건
- 트랜잭션 동작

**H2 사용 금지 이유:**
- PostgreSQL과 SQL 방언 차이
- 프로덕션과 다른 환경으로 인한 버그 발생 가능
- Testcontainers로 실제 PostgreSQL 사용 권장

**예시:**

```java
package com.ryuqq.crawlinghub.adapter.persistence.jpa.site;

import com.ryuqq.crawlinghub.domain.site.CrawlSite;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Testcontainers
@DisplayName("SitePersistenceAdapter 통합 테스트")
class SitePersistenceAdapterTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
        .withDatabaseName("test")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");
    }

    @Autowired
    private SitePersistenceAdapter adapter;

    @Test
    @DisplayName("사이트를 저장하고 조회할 수 있다")
    void save_AndLoad_ShouldSucceed() {
        // given
        CrawlSite site = CrawlSite.create("Example Site", "https://example.com");

        // when
        CrawlSite saved = adapter.save(site);

        // then
        assertThat(saved.getSiteId()).isNotNull();

        // when - 조회
        Optional<CrawlSite> loaded = adapter.loadById(saved.getSiteId());

        // then
        assertThat(loaded).isPresent();
        assertThat(loaded.get().getSiteName()).isEqualTo("Example Site");
        assertThat(loaded.get().getSiteUrl()).isEqualTo("https://example.com");
        assertThat(loaded.get().isActive()).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 사이트 조회 시 Optional.empty 반환")
    void loadById_WithNonExistentId_ShouldReturnEmpty() {
        // when
        Optional<CrawlSite> loaded = adapter.loadById(999L);

        // then
        assertThat(loaded).isEmpty();
    }

    @Test
    @DisplayName("사이트 상태 변경 후 저장")
    void save_AfterStateChange_ShouldPersist() {
        // given
        CrawlSite site = CrawlSite.create("Example Site", "https://example.com");
        CrawlSite saved = adapter.save(site);

        // when - 상태 변경
        CrawlSite loaded = adapter.loadById(saved.getSiteId()).orElseThrow();
        loaded.deactivate();
        adapter.save(loaded);

        // then - 변경 확인
        CrawlSite reloaded = adapter.loadById(saved.getSiteId()).orElseThrow();
        assertThat(reloaded.isActive()).isFalse();
    }
}
```

**실행:**
```bash
./gradlew :adapter:adapter-out-persistence-jpa:test
./gradlew :adapter:adapter-out-persistence-jpa:jacocoTestReport
```

### 6.5 E2E Tests (Controller Tests with REST Assured)

**특징:**
- **도구**: REST Assured + Testcontainers
- **의존성**: 전체 애플리케이션, 실제 DB
- **속도**: 매우 느림 (수십 초 단위)
- **격리**: 컨테이너 기반 격리

**테스트 대상:**
- REST API 엔드포인트
- HTTP 요청/응답 형식
- 인증/인가 (향후)
- 전체 플로우 (Controller → Service → Repository)

**예시:**

```java
package com.ryuqq.crawlinghub.adapter.web.site;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@DisplayName("Site API E2E 테스트")
class SiteApiE2ETest {

    @LocalServerPort
    private int port;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
        .withDatabaseName("test")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("POST /api/v1/sites - 사이트 생성 성공")
    void createSite_WithValidRequest_ShouldReturn201() {
        // given
        String requestBody = """
            {
                "siteName": "Example Site",
                "siteUrl": "https://example.com"
            }
            """;

        // when & then
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/api/v1/sites")
        .then()
            .statusCode(201)
            .body("siteId", notNullValue())
            .body("siteName", equalTo("Example Site"))
            .body("siteUrl", equalTo("https://example.com"))
            .body("isActive", equalTo(true));
    }

    @Test
    @DisplayName("POST /api/v1/sites - 사이트 이름이 null이면 400 반환")
    void createSite_WithNullName_ShouldReturn400() {
        // given
        String requestBody = """
            {
                "siteName": null,
                "siteUrl": "https://example.com"
            }
            """;

        // when & then
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/api/v1/sites")
        .then()
            .statusCode(400)
            .body("code", equalTo("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("GET /api/v1/sites/{siteId} - 사이트 조회 성공")
    void getSite_WithExistingId_ShouldReturn200() {
        // given - 먼저 사이트 생성
        String createRequestBody = """
            {
                "siteName": "Example Site",
                "siteUrl": "https://example.com"
            }
            """;

        Long siteId = given()
            .contentType(ContentType.JSON)
            .body(createRequestBody)
        .when()
            .post("/api/v1/sites")
        .then()
            .statusCode(201)
            .extract()
            .path("siteId");

        // when & then - 조회
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/api/v1/sites/" + siteId)
        .then()
            .statusCode(200)
            .body("siteId", equalTo(siteId.intValue()))
            .body("siteName", equalTo("Example Site"));
    }

    @Test
    @DisplayName("GET /api/v1/sites/{siteId} - 존재하지 않는 사이트 조회 시 404 반환")
    void getSite_WithNonExistentId_ShouldReturn404() {
        // when & then
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/api/v1/sites/999")
        .then()
            .statusCode(404)
            .body("code", equalTo("SITE_NOT_FOUND"));
    }
}
```

**실행:**
```bash
./gradlew :adapter:adapter-in-admin-web:test
```

### 6.6 Test Coverage Verification

**자동 검증 (`build.gradle.kts` lines 116-150):**

```kotlin
tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    dependsOn(tasks.named("jacocoTestReport"))

    violationRules {
        rule {
            enabled = true

            limit {
                minimum = when {
                    project.name == "domain" -> "0.90".toBigDecimal()
                    project.name == "application" -> "0.80".toBigDecimal()
                    project.name.startsWith("adapter-") -> "0.70".toBigDecimal()
                    else -> "0.70".toBigDecimal()
                }
            }
        }
    }
}
```

**빌드 시 자동 실행:**
```bash
./gradlew build
# → 각 모듈의 테스트 커버리지 자동 검증
# → 기준 미달 시 빌드 실패
```

**커버리지 리포트 확인:**
```bash
# Domain
open domain/build/reports/jacoco/test/html/index.html

# Application
open application/build/reports/jacoco/test/html/index.html

# Adapter
open adapter/adapter-out-persistence-jpa/build/reports/jacoco/test/html/index.html
```

---

## 7. Jira Task Template

### 7.1 Domain Model Task Template

**제목:** `[Domain] {Aggregate명} 도메인 모델 구현`

**설명:**

{Aggregate명}의 핵심 도메인 모델을 **순수 Java**로 구현합니다.

**구현 범위:**
- {개수}개의 Domain 객체 (Aggregate Root, Entity, Value Object)
- 도메인 로직 및 비즈니스 규칙
- Domain 전용 예외 클래스

**구현 위치:**
- `domain/src/main/java/com/ryuqq/crawlinghub/domain/{aggregate}/`

**구현 규칙:**
1. **순수 Java만 사용** - 모든 프레임워크 의존성 금지
2. **불변성 (Immutability)** - 모든 필드는 `private final` (일부 상태 변경 필드 제외)
3. **정적 팩토리 메서드** 사용
   - `create()`: 신규 도메인 객체 생성
   - `reconstitute()`: 영속성 계층에서 복원 시 사용
4. **비즈니스 로직 포함** - 상태 전이, 검증, 계산은 Domain 객체 내부에 위치
5. **Setter 금지** - 상태 변경은 명시적 메서드로 (`updateStatus()`, `enable()` 등)
6. **Private 생성자** - 외부에서 직접 생성 금지

**금지 사항:**
- ❌ Lombok (`@Data`, `@Builder`, `@Getter`, `@Setter` 등 모든 애노테이션)
- ❌ JPA (`@Entity`, `@Table`, `@Id`, `@GeneratedValue`, `@OneToMany` 등)
- ❌ Spring Framework (`@Component`, `@Service` 등)
- ❌ Public 생성자
- ❌ Setter 메서드

**테스트 커버리지:**
- Domain 레이어: 90% 이상 필수
- 단위 테스트만 작성 (순수 JUnit, 프레임워크 의존성 없음)

**검증:**
- `./gradlew :domain:test` - 단위 테스트 실행
- `./gradlew :domain:test --tests "*HexagonalArchitectureTest"` - 아키텍처 테스트
- `git commit` - Pre-commit hook 자동 검증

**완료 조건 (Definition of Done):**
- [ ] {개수}개 도메인 객체 구현 완료
- [ ] 모든 필드 `private final` (또는 명시적 상태 변경 필드)
- [ ] `create()` 및 `reconstitute()` 정적 팩토리 메서드 구현
- [ ] 비즈니스 로직 및 검증 로직 포함
- [ ] Lombok, JPA, Spring 의존성 없음
- [ ] 단위 테스트 커버리지 90% 이상
- [ ] ArchUnit 아키텍처 테스트 통과
- [ ] Pre-commit hook 검증 통과

---

### 7.2 Persistence Adapter Task Template

**제목:** `[Adapter-Out] {Aggregate명} 영속성 어댑터 구현`

**설명:**

{Aggregate명}의 영속성 계층을 **JPA**로 구현합니다.

**구현 범위:**
- {개수}개의 JPA Entity 클래스
- {개수}개의 JpaRepository 인터페이스
- {개수}개의 Mapper 클래스
- {개수}개의 PersistenceAdapter 클래스 (Port 구현)

**구현 위치:**
- `adapter/adapter-out-persistence-jpa/src/main/java/com/ryuqq/crawlinghub/adapter/persistence/jpa/{aggregate}/`

**구현 규칙:**
1. **JPA Entity**
   - `@Entity`, `@Table` 애노테이션 사용
   - 외래키는 **Long 타입 필드**로만 (연관관계 금지)
   - Getter만 제공 (Setter 금지)
   - Protected 기본 생성자 + Private 생성자
   - Builder 패턴 (수동 구현)

2. **JpaRepository**
   - **package-private** (외부 노출 금지)
   - QueryDSL 사용 권장

3. **Mapper**
   - Domain ↔ Entity 변환
   - 비즈니스 로직 없음

4. **PersistenceAdapter**
   - **public** (Port 구현)
   - Mapper를 통한 변환
   - JPA 예외 → Domain 예외 변환

**금지 사항:**
- ❌ Lombok (`@Data`, `@Builder`, `@Getter`, `@Setter` 등 모든 애노테이션)
- ❌ JPA 연관관계 (`@OneToMany`, `@ManyToOne`, `@OneToOne`, `@ManyToMany`)
- ❌ Entity에 Setter
- ❌ Entity에 비즈니스 로직
- ❌ `@Transactional` (Application 레이어에서만)

**테스트 전략:**
- Testcontainers (PostgreSQL) 사용
- 실제 데이터베이스 통합 테스트
- 커버리지: 70% 이상

**검증:**
- `./gradlew :adapter:adapter-out-persistence-jpa:test`
- `git commit` - Pre-commit hook 자동 검증

**완료 조건 (Definition of Done):**
- [ ] {개수}개 JPA Entity 구현 완료
- [ ] 외래키는 Long 타입, 연관관계 애노테이션 없음
- [ ] {개수}개 Mapper 구현
- [ ] {개수}개 PersistenceAdapter 구현
- [ ] Lombok 미사용
- [ ] Testcontainers 통합 테스트 작성
- [ ] 테스트 커버리지 70% 이상
- [ ] Pre-commit hook 검증 통과

---

### 7.3 Application Service Task Template

**제목:** `[Application] {UseCase명} 유즈케이스 구현`

**설명:**

{UseCase명}의 애플리케이션 서비스를 구현합니다.

**구현 범위:**
- Inbound Port (UseCase 인터페이스)
- Outbound Port (영속성/외부 시스템 인터페이스)
- UseCase Service 구현
- Command/Query/Result DTO

**구현 위치:**
- `application/src/main/java/com/ryuqq/crawlinghub/application/{aggregate}/`

**구현 규칙:**
1. **Port 인터페이스**
   - 클래스 레벨 Javadoc 필수
   - 비즈니스 규칙은 Service에 위치 (Port는 시그니처만)

2. **UseCase Service**
   - `@UseCase` 애노테이션
   - `@Transactional` (쓰기 작업) 또는 `@Transactional(readOnly = true)` (읽기 작업)
   - Port 인터페이스만 의존
   - Domain 객체만 사용

3. **DTO (Command/Query/Result)**
   - Record 타입 사용
   - Compact Constructor에서 검증

**금지 사항:**
- ❌ Adapter 구체 클래스 의존
- ❌ JPA Entity 사용
- ❌ Lombok

**테스트 전략:**
- Test Double (Inner Static Class) 사용
- Mockito 지양
- 커버리지: 80% 이상

**검증:**
- `./gradlew :application:test`
- `git commit` - Pre-commit hook 자동 검증

**완료 조건 (Definition of Done):**
- [ ] Inbound/Outbound Port 인터페이스 정의
- [ ] Port 클래스 레벨 Javadoc 작성
- [ ] UseCase Service 구현
- [ ] `@Transactional` 적절히 적용
- [ ] Command/Query/Result DTO (Record)
- [ ] Test Double 기반 단위 테스트
- [ ] 테스트 커버리지 80% 이상
- [ ] Pre-commit hook 검증 통과

---

### 7.4 Controller Task Template

**제목:** `[Adapter-In] {리소스명} REST API 구현`

**설명:**

{리소스명}의 REST API 엔드포인트를 구현합니다.

**구현 범위:**
- Controller 클래스
- Request/Response DTO (별도 파일)
- 예외 처리 (GlobalExceptionHandler)
- E2E 테스트

**구현 위치:**
- `adapter/adapter-in-admin-web/src/main/java/com/ryuqq/crawlinghub/adapter/web/{aggregate}/`

**구현 규칙:**
1. **Controller**
   - 얇게 유지 (비즈니스 로직 없음)
   - UseCase 인터페이스만 의존
   - Constructor Injection

2. **Request/Response DTO**
   - **별도 파일** (내부 클래스 금지)
   - Record 타입 사용
   - Bean Validation (`@NotNull`, `@Valid` 등)
   - `toCommand()` 메서드 (Request)
   - `from()` 정적 메서드 (Response)

3. **예외 처리**
   - `@RestControllerAdvice`
   - Domain 예외 → HTTP 상태 코드 매핑

**금지 사항:**
- ❌ 내부 클래스 (Request/Response)
- ❌ Domain 객체 직접 반환
- ❌ Repository/Entity 직접 의존
- ❌ Lombok

**테스트 전략:**
- REST Assured + Testcontainers
- E2E 테스트 작성
- 커버리지: 70% 이상

**검증:**
- `./gradlew :adapter:adapter-in-admin-web:test`
- `git commit` - Pre-commit hook 자동 검증

**완료 조건 (Definition of Done):**
- [ ] Controller 구현
- [ ] Request/Response DTO (별도 파일, Record)
- [ ] Bean Validation 적용
- [ ] GlobalExceptionHandler 예외 처리
- [ ] REST Assured E2E 테스트
- [ ] 테스트 커버리지 70% 이상
- [ ] Pre-commit hook 검증 통과

---

## 8. Quick Reference Checklist

### 8.1 Jira Task Validation Checklist

**태스크 생성 시 검증 체크리스트:**

#### Domain 태스크
- [ ] Domain 태스크에 JPA 관련 언급 없음 (`@Entity`, `@Table`, `@Id`, `@GeneratedValue` 등)
- [ ] Domain 태스크에 Spring 관련 언급 없음 (`@Component`, `@Service`, `@Transactional` 등)
- [ ] Lombok 언급 없음 (`@Data`, `@Builder`, `@Getter`, `@Setter` 등)
- [ ] "순수 Java" 또는 "Pure Java" 명시
- [ ] 정적 팩토리 메서드 (`create()`, `reconstitute()`) 언급
- [ ] 불변성 (Immutability) 원칙 언급
- [ ] 테스트 커버리지 90% 명시

#### Application 태스크
- [ ] Port 인터페이스만 의존 명시
- [ ] Domain 객체만 사용 명시
- [ ] `@Transactional` Application 레이어에만 명시
- [ ] Adapter 구체 클래스 의존 금지 명시
- [ ] Test Double 사용 권장 (Mockito 지양)
- [ ] 테스트 커버리지 80% 명시

#### Adapter 태스크
- [ ] JPA Entity는 Adapter 레이어에만 명시
- [ ] 외래키는 Long 타입 필드로만 명시
- [ ] JPA 연관관계 금지 명시 (`@OneToMany`, `@ManyToOne` 등)
- [ ] Mapper를 통한 Domain ↔ Entity 변환 명시
- [ ] Testcontainers (PostgreSQL) 사용 명시
- [ ] H2 사용 금지 명시
- [ ] 테스트 커버리지 70% 명시

#### 공통
- [ ] Lombok 언급 없음 (모든 레이어)
- [ ] Hexagonal Architecture 레이어 책임 명확히 구분
- [ ] 테스트 전략 레이어별로 적절히 명시
- [ ] 완료 조건 (Definition of Done) 포함

### 8.2 Code Review Checklist

**코드 리뷰 시 검증 체크리스트:**

#### Domain 코드
- [ ] 모든 필드 `private final` (또는 명시적 상태 변경 필드)
- [ ] Setter 메서드 없음
- [ ] Public 생성자 없음
- [ ] 정적 팩토리 메서드 (`create()`, `reconstitute()`) 존재
- [ ] 비즈니스 로직이 Domain 객체 내부에 위치
- [ ] Spring, JPA, Lombok import 없음
- [ ] 단위 테스트 커버리지 90% 이상

#### Application 코드
- [ ] `@Transactional` 적절히 적용
- [ ] Port 인터페이스만 의존 (구체 클래스 의존 없음)
- [ ] Domain 객체만 사용 (JPA Entity 사용 없음)
- [ ] Command/Query/Result DTO 정의됨
- [ ] Port 클래스 레벨 Javadoc 작성
- [ ] Test Double 기반 테스트
- [ ] 테스트 커버리지 80% 이상

#### Adapter 코드 (Persistence)
- [ ] JPA 연관관계 애노테이션 없음 (`@OneToMany`, `@ManyToOne` 등)
- [ ] 외래키가 Long 타입 필드
- [ ] Entity에 Setter 없음
- [ ] Entity에 Public 생성자 없음
- [ ] Mapper 클래스 존재
- [ ] `@Transactional` 없음 (Application에서만)
- [ ] Testcontainers 통합 테스트
- [ ] 테스트 커버리지 70% 이상

#### Adapter 코드 (Web)
- [ ] 내부 클래스 없음 (Request/Response 별도 파일)
- [ ] Request/Response Record 타입
- [ ] Bean Validation 적용
- [ ] Domain 객체 직접 반환 없음
- [ ] UseCase (Port)만 의존
- [ ] REST Assured E2E 테스트
- [ ] 테스트 커버리지 70% 이상

#### 공통
- [ ] Lombok 미사용
- [ ] Constructor Injection 사용
- [ ] 순환 의존성 없음
- [ ] 레이어 의존성 방향 준수

### 8.3 Git Pre-Commit Validation

**Pre-commit hook 자동 검증 항목:**

#### Domain 검증 (`hooks/validators/domain-validator.sh`)
```bash
# 1. JPA import 금지
grep -r "import jakarta.persistence" domain/src/main/java/

# 2. Spring import 금지
grep -r "import org.springframework" domain/src/main/java/

# 3. Lombok import 금지
grep -r "import lombok" domain/src/main/java/

# 4. Public 생성자 금지 (예외: record)
grep -A5 "public class" domain/src/main/java/ | grep "public.*("

# 5. Setter 메서드 금지
grep "public void set[A-Z]" domain/src/main/java/
```

#### Application 검증 (`hooks/validators/application-validator.sh`)
```bash
# 1. Adapter 의존성 금지
grep -r "import.*adapter" application/src/main/java/

# 2. JPA Entity 사용 금지
grep -r "import.*Entity" application/src/main/java/
```

#### Adapter 검증 (`hooks/validators/adapter-*-validator.sh`)
```bash
# 1. JPA 연관관계 금지
grep -r "@OneToMany\|@ManyToOne\|@OneToOne\|@ManyToMany" adapter/adapter-out-persistence-jpa/

# 2. Entity Setter 금지
grep -r "public void set[A-Z]" adapter/adapter-out-persistence-jpa/src/main/java/.*Entity.java

# 3. 내부 클래스 금지 (Controller)
grep -A10 "@RestController" adapter/adapter-in-admin-web/ | grep "public static class"
```

#### 공통 검증 (`hooks/validators/common-validator.sh`)
```bash
# 1. Lombok 금지
grep -r "import lombok" */src/main/java/

# 2. Field Injection 금지
grep -r "@Autowired" */src/main/java/ | grep "private.*;"
```

---

## 9. Action Items

### 9.1 Immediate Actions (우선순위 1 - 긴급)

**CRAW-68: Domain Entity Implementation**

**현재 문제:**
- 태스크 설명이 "JPA Entity with Lombok" 구현 지시
- Domain 레이어에 JPA 및 Lombok 사용 요구

**수정 내용:**
1. 태스크 제목 변경
   - Before: "JPA Entity 구현 (21개)"
   - After: "Domain Model 구현 (21개 순수 도메인 객체)"

2. 태스크 설명 전면 수정
   - JPA 관련 내용 모두 제거
   - Lombok 관련 내용 모두 제거
   - "순수 Java" 명시적 강조
   - 정적 팩토리 메서드 패턴 설명 추가
   - 불변성 원칙 강조
   - Domain vs JPA Entity 분리 설명

3. 코드 예시 추가
   - 순수 Java Domain Model 예시
   - Value Object (Record) 예시
   - 잘못된 예시 (❌)와 올바른 예시 (✅) 대비

4. 검증 방법 명시
   - `./gradlew :domain:test`
   - ArchUnit 테스트
   - Pre-commit hook

**담당자:** Jira 관리자
**기한:** 즉시
**우선순위:** 🔴 Critical

---

**CRAW-76: Test Code Writing**

**현재 문제:**
- Domain 테스트에 Spring 사용 지시
- H2 in-memory database 사용 권장
- 레이어별 테스트 전략 혼재

**수정 내용:**
1. 태스크 제목 변경
   - Before: "단위 테스트, 통합 테스트 작성"
   - After: "레이어별 테스트 코드 작성 (Domain 90%, Application 80%, Adapter 70%)"

2. 태스크 설명 재구성
   - 레이어별 테스트 전략 명확히 구분
   - Domain: 순수 JUnit (Spring 의존성 없음)
   - Application: Test Double (Mockito 지양)
   - Adapter: Testcontainers (H2 금지)
   - E2E: REST Assured

3. 코드 예시 추가
   - 각 레이어별 테스트 예시
   - Test Double (Inner Static Class) 패턴
   - Testcontainers 설정 예시

4. 커버리지 목표 명시
   - Domain: 90% 이상
   - Application: 80% 이상
   - Adapter: 70% 이상

**담당자:** Jira 관리자
**기한:** 즉시
**우선순위:** 🟡 Important

---

### 9.2 Preventive Actions (우선순위 2 - 예방)

**1. Jira Task Template 배포**

**내용:**
- Domain, Application, Adapter 레이어별 태스크 템플릿 생성
- Jira 프로젝트에 템플릿 등록
- 신규 태스크 생성 시 템플릿 사용 강제

**담당자:** Jira 관리자
**기한:** 1주일 이내
**우선순위:** 🟢 Prevention

---

**2. Jira Task Validation Checklist 공유**

**내용:**
- 이 문서의 "Quick Reference Checklist" 섹션 공유
- 태스크 생성 시 체크리스트 적용
- 태스크 리뷰 프로세스 도입

**담당자:** 팀 리더, Jira 관리자
**기한:** 1주일 이내
**우선순위:** 🟢 Prevention

---

**3. 신규 개발자 온보딩 자료 업데이트**

**내용:**
- 이 문서를 온보딩 필수 문서로 지정
- Hexagonal Architecture 원칙 교육
- Lombok 금지 정책 강조
- Domain vs JPA Entity 분리 개념 교육

**담당자:** 팀 리더
**기한:** 2주일 이내
**우선순위:** 🟢 Prevention

---

**4. 정기 Jira 태스크 감사**

**내용:**
- 월 1회 기존 태스크 검토
- 이 문서의 체크리스트로 검증
- 위반 태스크 수정

**담당자:** Jira 관리자
**기한:** 매월 첫째 주
**우선순위:** 🟢 Prevention

---

### 9.3 Monitoring Actions (우선순위 3 - 모니터링)

**1. Pre-commit Hook 실패 모니터링**

**내용:**
- Pre-commit hook 실패 로그 수집
- 자주 실패하는 규칙 분석
- 관련 Jira 태스크 검토 및 수정

**담당자:** DevOps 팀
**기한:** 상시
**우선순위:** 🟢 Monitoring

---

**2. ArchUnit 테스트 실패 모니터링**

**내용:**
- CI/CD 파이프라인에서 ArchUnit 테스트 실패 추적
- 실패 원인 분석 (잘못된 코드 vs 잘못된 태스크 설명)
- 태스크 설명 개선

**담당자:** DevOps 팀, 팀 리더
**기한:** 상시
**우선순위:** 🟢 Monitoring

---

## 10. Conclusion

### 10.1 Summary

이 문서는 CrawlingHub 프로젝트의 Jira 태스크와 코딩 표준 간 불일치를 식별하고 수정하는 가이드를 제공합니다.

**핵심 원칙:**
1. **Domain은 순수 Java** - JPA, Spring, Lombok 절대 금지
2. **JPA Entity는 Adapter Layer에만** - Domain과 완전 분리
3. **Lombok 전체 금지** - 모든 레이어에서 순수 Java 사용
4. **레이어별 테스트 전략** - Domain 90%, Application 80%, Adapter 70%

### 10.2 References

**프로젝트 문서:**
- `/Users/sangwon-ryu/crawlinghub/docs/CODING_STANDARDS.md` - 코딩 표준 (87개 규칙)
- `/Users/sangwon-ryu/crawlinghub/build.gradle.kts` - Lombok 금지 정책 (lines 159-182)
- `/Users/sangwon-ryu/crawlinghub/README.md` - 프로젝트 개요

**Git Hooks:**
- `/Users/sangwon-ryu/crawlinghub/hooks/validators/domain-validator.sh`
- `/Users/sangwon-ryu/crawlinghub/hooks/validators/application-validator.sh`
- `/Users/sangwon-ryu/crawlinghub/hooks/validators/adapter-*-validator.sh`

**ArchUnit Tests:**
- `domain/src/test/java/com/ryuqq/crawlinghub/architecture/HexagonalArchitectureTest.java`
- `adapter/adapter-out-persistence-jpa/src/test/java/com/ryuqq/crawlinghub/architecture/PersistenceArchitectureTest.java`

### 10.3 Contact

**질문 및 피드백:**
- Jira 태스크 관련: Jira 관리자
- 아키텍처 관련: 팀 리더
- 기술 문의: 개발팀 Tech Lead

---

**문서 버전:** 1.0
**최종 수정일:** 2025-10-11
**작성자:** Technical Writer (CrawlingHub Project)
