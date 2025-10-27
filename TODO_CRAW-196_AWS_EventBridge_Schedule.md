# TODO: CRAW-196 - AWS EventBridge 스케줄 자동 등록/수정 구현

**작성일**: 2025-10-27
**작성자**: Development Team
**Jira 이슈**: [CRAW-196](https://ryuqqq.atlassian.net/browse/CRAW-196)
**브랜치**: `feature/CRAW-196-aws-eventbridge-schedule`
**Epic**: CRAW-193 - 머스트잇 사이트 크롤링 MVP

---

## 📋 프로젝트 개요

### 목적
셀러의 크롤링 주기 변경 시 AWS EventBridge 스케줄을 자동으로 등록/수정하는 기능을 **Orchestrator SDK**를 활용하여 구현

### 핵심 아키텍처 패턴
1. **Transactional Outbox Pattern** (Orchestrator SDK 활용)
2. **Domain Event** (SellerCrawlIntervalChangedEvent)
3. **Hexagonal Architecture** (Port & Adapter)
4. **Long FK Strategy** (JPA 관계 어노테이션 금지)

### 기술 스택
- **Orchestrator SDK v0.1.1** (JitPack)
- **AWS SDK for Java 2.x** (EventBridge Client)
- **Spring Events** (@TransactionalEventListener)
- **Resilience4j** (Circuit Breaker, Retry)
- **LocalStack + Testcontainers** (통합 테스트)

---

## 🚨 중요 버그 및 조치 필요 사항

### ⚠️ CRITICAL BUG: JpaOrchestratorStoreAdapter 패키지 Import 오류

**파일**: `adapter-out/persistence-mysql/src/main/java/com/ryuqq/crawlinghub/adapter/out/persistence/outbox/adapter/JpaOrchestratorStoreAdapter.java`

**라인**: 257-260 (reconstructCommand 메서드)

**문제**:
```java
// ❌ WRONG - 존재하지 않는 패키지!
import com.ryuqq.orchestrator.core.command.BizKey;
import com.ryuqq.orchestrator.core.command.IdemKey;

private Command reconstructCommand(SellerCrawlScheduleOutboxEntity outbox) {
    return Command.of(
        Domain.of(outbox.getDomain()),
        EventType.of(outbox.getEventType()),
        com.ryuqq.orchestrator.core.command.BizKey.of(outbox.getBizKey()),    // ❌
        com.ryuqq.orchestrator.core.command.IdemKey.of(outbox.getIdemKey()),  // ❌
        Payload.of(outbox.getPayload())
    );
}
```

**해결 방법**:
```java
// ✅ CORRECT
import com.ryuqq.orchestrator.core.model.BizKey;
import com.ryuqq.orchestrator.core.model.IdemKey;

private Command reconstructCommand(SellerCrawlScheduleOutboxEntity outbox) {
    return Command.of(
        Domain.of(outbox.getDomain()),
        EventType.of(outbox.getEventType()),
        BizKey.of(outbox.getBizKey()),     // ✅
        IdemKey.of(outbox.getIdemKey()),   // ✅
        Payload.of(outbox.getPayload())
    );
}
```

**영향**: 빌드 실패 - 프로젝트 컴파일 불가

**우선순위**: 🔴 최우선 - 즉시 수정 필요

---

## 📊 진행 현황 요약

### 현재 상태 분석

**✅ Phase 1: 의존성 및 모듈 설정 (100% 완료)**
- Orchestrator SDK v0.1.1 의존성 추가 완료 (JitPack)
- adapter-out-aws-eventbridge 모듈 생성 완료
- AWS SDK, Resilience4j 의존성 추가 완료

**✅ Phase 2: Domain Layer 구현 (100% 완료)**
- SellerCrawlScheduleOutbox 도메인 모델 완성
  - CommandInfo, OperationState, WriteAheadState 내부 클래스 분리
  - 정적 팩토리 메서드 (of(), restore()) 적용
- Domain Events는 기존 구현 활용 (추가 작업 불필요)

**✅ Phase 3: Persistence Layer 구현 (95% 완료)**
- SellerCrawlScheduleOutboxJpaRepository 완성
- SellerCrawlScheduleOutboxEntity 완성
- SellerCrawlScheduleOutboxMapper 완성
- JpaOrchestratorStoreAdapter (Store SPI 구현) 완성
- ⚠️ **중요 버그**: JpaOrchestratorStoreAdapter 패키지 import 오류 (즉시 수정 필요)

**✅ Phase 4: Application Layer 구현 (95% 완료)**
- SellerCrawlScheduleOutboxPort 인터페이스 완성
- SellerCrawlScheduleOutboxAdapter 완성
- SellerScheduleOrchestrationService 완성
  - `submit()` API 사용
  - `@Transactional` 제거 (올바른 패턴)
  - OutboxPort 사용
- JacksonConfig (ObjectMapper 빈 등록) 완성

**⚠️ Phase 4R: 아키텍처 리팩토링 (선택사항 - 0%)**
- 현재 아키텍처는 기능적으로 작동하지만 복잡성이 높음
- 단순화 권장: OutboxPort/Adapter/Domain Model 제거, Store SPI 직접 사용
- 상세 계획은 아래 Phase 4R 섹션 참조

**❌ Phase 5: Adapter-out-aws-eventbridge 구현 (0%)**
- EventBridgeExecutor 미구현 (Outcome 반환 로직 필요)
- CronExpressionConverter 미구현
- EventBridgeConfig, Resilience4jConfig 미구현
- **우선순위**: 🔴 Critical - 실제 AWS API 호출 없이는 기능 동작 불가

**❌ Phase 6: Orchestrator 설정 (0%)**
- Orchestrator Bean 생성 미구현
- Finalizer/Reaper 초기화 미구현
- Bus Bean (InMemoryBus) 미구현
- **우선순위**: 🔴 Critical - Orchestrator 실행 불가

**⬜ Phase 7: 테스트 구현 (0%)**

**⬜ Phase 8: Seller 조회 시 스케줄 상태 조회 (0%)**

**⬜ Phase 9: 최종 검증 및 문서화 (0%)**

### 전체 진행률

| Phase | 항목 | 상태 | 완료율 |
|-------|------|------|--------|
| Phase 1 | 의존성 및 모듈 설정 | ✅ | 100% |
| Phase 2 | Domain Layer 구현 | ✅ | 100% |
| Phase 3 | Persistence Layer 구현 | ⚠️ | 95% (버그 1건) |
| Phase 4 | Application Layer 구현 | ✅ | 95% |
| Phase 4R | 아키텍처 리팩토링 (선택) | ⬜ | 0% |
| Phase 5 | EventBridge Executor 구현 | ❌ | 0% |
| Phase 6 | Orchestrator Config 구현 | ❌ | 0% |
| Phase 7 | 테스트 구현 | ⬜ | 0% |
| Phase 8 | 스케줄 상태 조회 | ⬜ | 0% |
| Phase 9 | 최종 검증 및 문서화 | ⬜ | 0% |

**전체 진행률**: 약 50% (Phase 1-4 거의 완료, Phase 5-6 Critical 미구현)

---

## 🎯 작업 체크리스트

### Phase 1: 의존성 및 모듈 설정 ✅ COMPLETED

#### [✅] 1.1 build.gradle 의존성 추가
**파일**: `build.gradle.kts`, `settings.gradle.kts`, `gradle/libs.versions.toml`

**완료 내용**:
```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

// build.gradle.kts
dependencies {
    implementation("com.github.ryu-qqq.Orchestrator:orchestrator-core:v0.1.1")
    implementation("com.github.ryu-qqq.Orchestrator:orchestrator-adapter-runner:v0.1.1")
    testImplementation("com.github.ryu-qqq.Orchestrator:orchestrator-testkit:v0.1.1")

    implementation("software.amazon.awssdk:eventbridge:2.20.+")
    implementation("io.github.resilience4j:resilience4j-spring-boot3:2.1.0")
}
```

**진행 상태**: ✅ 완료
**완료 날짜**: 2025-10-27
**비고**: JitPack 저장소 추가 및 Orchestrator SDK 의존성 성공적으로 추가

---

#### [✅] 1.2 adapter-out-aws-eventbridge 모듈 생성
**위치**: `adapter-out/aws-eventbridge/`

**디렉터리 구조**:
```
adapter-out/aws-eventbridge/
├── build.gradle.kts
└── src/main/java/com/ryuqq/crawlinghub/adapter/out/aws/eventbridge/
    ├── executor/
    ├── converter/
    └── config/
```

**진행 상태**: ✅ 완료
**완료 날짜**: 2025-10-27
**비고**: 모듈 생성 완료, 내부 클래스는 Phase 5에서 구현 예정

---

### Phase 2: Domain Layer 구현 ✅ COMPLETED

#### [✅] 2.1 SellerCrawlScheduleOutbox Domain Model 구현
**파일**: `domain/src/main/java/com/ryuqq/crawlinghub/domain/mustit/seller/outbox/SellerCrawlScheduleOutbox.java`

**완료 내용**:
```java
public class SellerCrawlScheduleOutbox {
    private Long id;
    private String opId;  // Orchestrator OpId (UUID)
    private CommandInfo commandInfo;  // 내부 Record
    private Long sellerId;  // Long FK
    private String payload;  // JSON String
    private OperationState operationState;  // 내부 Enum
    private WriteAheadState walState;  // 내부 Enum
    private String outcomeJson;
    private Integer retryCount;
    private Integer maxRetries;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    // 정적 팩토리 메서드
    public static SellerCrawlScheduleOutbox of(...) { ... }
    public static SellerCrawlScheduleOutbox restore(...) { ... }

    // 비즈니스 메서드
    public void assignOpId(String newOpId) { ... }
    public void writeAhead(String outcome) { ... }
    public void finalize(OperationState finalState) { ... }
}
```

**코딩 컨벤션 준수 여부**:
- [✅] 내부 클래스 분리 (CommandInfo, OperationState, WriteAheadState)
- [✅] 정적 팩토리 메서드 (of(), restore())
- [✅] Long FK 전략 (sellerId)
- [✅] ❌ Lombok 미사용
- [✅] ❌ Getter 체이닝 없음
- [✅] 비즈니스 메서드만 포함

**진행 상태**: ✅ 완료
**완료 날짜**: 2025-10-27
**비고**: 내부 클래스 분리 완료, 정적 팩토리 메서드 적용 완료

---

#### [N/A] 2.2 SellerCrawlIntervalChangedEvent 정의
**진행 상태**: ⬜ N/A (기존 이벤트 시스템 활용)
**비고**: 별도 이벤트 발행 없이 직접 Orchestrator 호출하는 방식으로 구현 예정

---

#### [N/A] 2.3 Seller Aggregate에서 Event 발행 로직 추가
**진행 상태**: ⬜ N/A
**비고**: 별도 이벤트 발행 없이 직접 Orchestrator 호출

---

### Phase 3: Persistence Layer 구현 ⚠️ 95% COMPLETED (버그 1건)

#### [✅] 3.1 SellerCrawlScheduleOutboxJpaRepository 구현
**파일**: `adapter-out/persistence-mysql/src/main/java/com/ryuqq/crawlinghub/adapter/out/persistence/outbox/repository/SellerCrawlScheduleOutboxJpaRepository.java`

**완료 내용**:
```java
public interface SellerCrawlScheduleOutboxJpaRepository extends JpaRepository<SellerCrawlScheduleOutboxEntity, Long> {
    Optional<SellerCrawlScheduleOutboxEntity> findByOpId(String opId);
    Optional<SellerCrawlScheduleOutboxEntity> findByIdemKey(String idemKey);
    List<SellerCrawlScheduleOutboxEntity> findByWalState(WriteAheadState walState, Pageable pageable);
    List<SellerCrawlScheduleOutboxEntity> findByOperationStateAndCreatedAtBefore(
        OperationState operationState, LocalDateTime threshold, Pageable pageable
    );
}
```

**진행 상태**: ✅ 완료
**완료 날짜**: 2025-10-27

---

#### [⚠️] 3.2 JpaOrchestratorStoreAdapter 구현 (Store SPI 구현체)
**파일**: `adapter-out/persistence-mysql/src/main/java/com/ryuqq/crawlinghub/adapter/out/persistence/outbox/adapter/JpaOrchestratorStoreAdapter.java`

**완료 내용**:
```java
@Component
public class JpaOrchestratorStoreAdapter implements Store {

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void writeAhead(OpId opId, Outcome outcome) { ... }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void finalize(OpId opId, OperationState finalState) { ... }

    @Override
    public Outcome getWriteAheadOutcome(OpId opId) { ... }

    @Override
    public List<OpId> scanWA(WriteAheadState state, int limit) { ... }

    @Override
    public List<OpId> scanInProgress(long timeoutMillis, int limit) { ... }

    @Override
    public Envelope getEnvelope(OpId opId) { ... }

    @Override
    public OperationState getState(OpId opId) { ... }
}
```

**⚠️ 중요 버그**:
- **라인 257-260**: `com.ryuqq.orchestrator.core.command.BizKey` → `com.ryuqq.orchestrator.core.model.BizKey`
- **라인 257-260**: `com.ryuqq.orchestrator.core.command.IdemKey` → `com.ryuqq.orchestrator.core.model.IdemKey`

**진행 상태**: ⚠️ 95% 완료 (패키지 import 버그 수정 필요)
**완료 날짜**: 2025-10-27
**비고**: 🔴 즉시 수정 필요 - 빌드 실패 원인

---

#### [✅] 3.3 SellerCrawlScheduleOutboxEntity 구현
**파일**: `adapter-out/persistence-mysql/src/main/java/com/ryuqq/crawlinghub/adapter/out/persistence/outbox/entity/SellerCrawlScheduleOutboxEntity.java`

**완료 내용**:
```java
@Entity
@Table(name = "seller_crawl_schedule_outbox")
public class SellerCrawlScheduleOutboxEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "op_id", nullable = false, unique = true, length = 36)
    private String opId;

    // CommandInfo 필드들 (flattened)
    @Column(name = "domain", nullable = false, length = 50)
    private String domain;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    // ... 기타 필드

    // Nested CommandInfo record
    public record CommandInfo(...) {}

    // Nested enums
    public enum OperationState { ... }
    public enum WriteAheadState { ... }
}
```

**코딩 컨벤션 준수 여부**:
- [✅] Long FK 전략 (sellerId)
- [✅] ❌ JPA 관계 어노테이션 미사용
- [✅] ❌ Setter 미사용 (필드 직접 설정)
- [✅] 내부 Record/Enum 정의

**진행 상태**: ✅ 완료
**완료 날짜**: 2025-10-27

---

#### [✅] 3.4 SellerCrawlScheduleOutboxMapper 구현
**파일**: `adapter-out/persistence-mysql/src/main/java/com/ryuqq/crawlinghub/adapter/out/persistence/outbox/mapper/SellerCrawlScheduleOutboxMapper.java`

**완료 내용**:
```java
@Component
public class SellerCrawlScheduleOutboxMapper {
    public SellerCrawlScheduleOutboxEntity toEntity(SellerCrawlScheduleOutbox domain) { ... }
    public SellerCrawlScheduleOutbox toDomain(SellerCrawlScheduleOutboxEntity entity) { ... }
}
```

**진행 상태**: ✅ 완료
**완료 날짜**: 2025-10-27

---

### Phase 4: Application Layer 구현 ✅ 95% COMPLETED

#### [✅] 4.1 SellerCrawlScheduleOutboxPort 인터페이스 정의
**파일**: `application/src/main/java/com/ryuqq/crawlinghub/application/mustit/seller/port/out/outbox/SellerCrawlScheduleOutboxPort.java`

**완료 내용**:
```java
public interface SellerCrawlScheduleOutboxPort {
    SellerCrawlScheduleOutbox save(SellerCrawlScheduleOutbox outbox);
    SellerCrawlScheduleOutbox findByOpId(String opId);
    SellerCrawlScheduleOutbox findByIdemKey(String idemKey);
    SellerCrawlScheduleOutbox findLatestBySellerId(Long sellerId);
    void updateOpId(Long outboxId, String opId);
}
```

**진행 상태**: ✅ 완료
**완료 날짜**: 2025-10-27

---

#### [✅] 4.2 SellerCrawlScheduleOutboxAdapter 구현
**파일**: `adapter-out/persistence-mysql/src/main/java/com/ryuqq/crawlinghub/adapter/out/persistence/outbox/adapter/SellerCrawlScheduleOutboxAdapter.java`

**완료 내용**:
```java
@Component
public class SellerCrawlScheduleOutboxAdapter implements SellerCrawlScheduleOutboxPort {

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SellerCrawlScheduleOutbox save(SellerCrawlScheduleOutbox outbox) { ... }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateOpId(Long outboxId, String opId) { ... }

    // ... 기타 메서드
}
```

**진행 상태**: ✅ 완료
**완료 날짜**: 2025-10-27

---

#### [✅] 4.3 SellerScheduleOrchestrationService 구현
**파일**: `application/src/main/java/com/ryuqq/crawlinghub/application/mustit/seller/service/SellerScheduleOrchestrationService.java`

**완료 내용**:
```java
@Service
public class SellerScheduleOrchestrationService {
    private final Orchestrator orchestrator;
    private final SellerCrawlScheduleOutboxPort outboxPort;
    private final ObjectMapper objectMapper;

    // ✅ @Transactional 없음 (올바른 패턴!)
    public OpId startScheduleCreation(Long sellerId, String cronExpression) {
        // 1. CommandInfo 생성
        // 2. Outbox Domain Model 생성 및 저장 (PENDING)
        // 3. Orchestrator Command 생성
        // 4. orchestrator.submit() 호출
        // 5. OpId 업데이트
        return opId;
    }
}
```

**코딩 컨벤션 준수 여부**:
- [✅] ❌ `@Transactional` 미사용 (Orchestrator가 트랜잭션 관리)
- [✅] OutboxPort 사용 (헥사고날 아키텍처)
- [✅] `submit()` API 사용 (올바른 Orchestrator 호출 방식)

**진행 상태**: ✅ 완료
**완료 날짜**: 2025-10-27
**비고**: 트랜잭션 경계 올바르게 구현 완료

---

#### [✅] 4.4 JacksonConfig (ObjectMapper 빈 등록)
**파일**: `application/src/main/java/com/ryuqq/crawlinghub/application/config/JacksonConfig.java`

**완료 내용**:
```java
@Configuration
public class JacksonConfig {
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
```

**진행 상태**: ✅ 완료
**완료 날짜**: 2025-10-27
**비고**: Orchestrator SDK가 JSON 직렬화에 필요

---

#### [N/A] 4.5 SellerCrawlScheduleEventHandler 구현
**진행 상태**: ⬜ N/A (Event 기반 방식 대신 직접 호출 방식 채택)
**비고**: 추후 Event 기반으로 전환 시 구현 예정

---

### Phase 4R: 아키텍처 리팩토링 (선택사항) ⬜ 0%

#### 배경 및 현재 문제점

현재 아키텍처는 다음과 같은 중복 레이어를 가지고 있습니다:

**1. Domain Layer**:
- `SellerCrawlScheduleOutbox` (Domain Model)
- `CommandInfo`, `OperationState`, `WriteAheadState` (내부 클래스)

**2. Application Layer**:
- `SellerCrawlScheduleOutboxPort` (인터페이스)
- `SellerCrawlScheduleOutboxAdapter` (구현체)

**3. Persistence Layer**:
- `SellerCrawlScheduleOutboxEntity` (JPA Entity)
- `JpaOrchestratorStoreAdapter` (Store SPI 구현)
- `SellerCrawlScheduleOutboxMapper` (Domain ↔ Entity 변환)

**문제점**:
- 같은 테이블에 대해 **2개의 Adapter** (OutboxAdapter, StoreAdapter)
- 같은 데이터에 대해 **2개의 모델** (Domain Model, Entity)
- 불필요한 변환 레이어 (Mapper)
- 복잡도 증가, 유지보수 어려움

#### 권장 단순화 방안

**Option 1: Orchestrator Store SPI 직접 사용 (권장)**

제거할 항목:
1. `SellerCrawlScheduleOutbox` (Domain Model) → Entity로 통합
2. `CommandInfo`, `OperationState`, `WriteAheadState` → Entity 내부로 이동
3. `SellerCrawlScheduleOutboxPort` (인터페이스) → Store SPI로 대체
4. `SellerCrawlScheduleOutboxAdapter` (구현체) → 제거
5. `SellerCrawlScheduleOutboxMapper` → 제거

변경 후 구조:
```
Persistence Layer:
  - SellerCrawlScheduleOutboxEntity (JPA Entity + CommandInfo/Enums)
  - SellerCrawlScheduleOutboxJpaRepository (JPA Repository)
  - JpaOrchestratorStoreAdapter (Store SPI 구현)

Application Layer:
  - SellerScheduleOrchestrationService
    → Store SPI 직접 주입
    → Entity 직접 사용 (Domain Model 제거)
```

**장점**:
- 레이어 단순화 (5개 파일 제거)
- 변환 오버헤드 제거 (Mapper 불필요)
- 유지보수 간소화
- Store SPI가 이미 모든 필요 기능 제공

**단점**:
- Domain Layer의 순수성 일부 손실
- Application Layer가 Entity에 직접 의존

**Option 2: 현재 구조 유지**

장점:
- Domain Model 순수성 유지
- 헥사고날 아키텍처 원칙 준수

단점:
- 불필요한 복잡도
- 같은 역할을 하는 2개의 Adapter

#### [ ] 4R.1 아키텍처 리팩토링 결정
**작업 내용**: Option 1 또는 Option 2 선택

**진행 상태**: ⬜ TODO
**비고**: 프로젝트 규모와 요구사항에 따라 결정

---

#### [ ] 4R.2 Domain Model 및 Port/Adapter 제거 (Option 1 선택 시)
**파일 제거 대상**:
- `domain/src/main/java/com/ryuqq/crawlinghub/domain/mustit/seller/outbox/SellerCrawlScheduleOutbox.java`
- `application/src/main/java/com/ryuqq/crawlinghub/application/mustit/seller/port/out/outbox/SellerCrawlScheduleOutboxPort.java`
- `adapter-out/persistence-mysql/src/main/java/com/ryuqq/crawlinghub/adapter/out/persistence/outbox/adapter/SellerCrawlScheduleOutboxAdapter.java`
- `adapter-out/persistence-mysql/src/main/java/com/ryuqq/crawlinghub/adapter/out/persistence/outbox/mapper/SellerCrawlScheduleOutboxMapper.java`

**파일 수정 대상**:
- `SellerScheduleOrchestrationService.java`:
  - `SellerCrawlScheduleOutboxPort` → `Store` SPI 직접 주입
  - `SellerCrawlScheduleOutbox` → `SellerCrawlScheduleOutboxEntity` 사용

**진행 상태**: ⬜ TODO (Option 1 선택 시)

---

#### [ ] 4R.3 CommandInfo, OperationState, WriteAheadState Entity로 이동 (Option 1 선택 시)
**파일**: `adapter-out/persistence-mysql/src/main/java/com/ryuqq/crawlinghub/adapter/out/persistence/outbox/entity/SellerCrawlScheduleOutboxEntity.java`

**작업 내용**:
- Domain Layer의 내부 클래스를 Entity 내부로 이동
- 모든 참조 경로 업데이트

**진행 상태**: ⬜ TODO (Option 1 선택 시)

---

### Phase 5: Adapter-out-aws-eventbridge 구현 ❌ 0% (CRITICAL)

#### [ ] 5.1 EventBridgeExecutor 구현
**파일**: `adapter-out/aws-eventbridge/src/main/java/com/ryuqq/crawlinghub/adapter/out/aws/eventbridge/executor/EventBridgeExecutor.java`

**핵심 역할**:
- AWS EventBridge API 호출 (PutRule, PutTargets)
- Orchestrator `Outcome` 반환 (Ok/Retry/Fail)
- Circuit Breaker 적용 (Resilience4j)

**구현 가이드**:
```java
package com.ryuqq.crawlinghub.adapter.out.aws.eventbridge.executor;

import com.ryuqq.orchestrator.core.model.Envelope;
import com.ryuqq.orchestrator.core.model.Outcome;
import com.ryuqq.orchestrator.core.model.Payload;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.*;

import java.time.Duration;

/**
 * AWS EventBridge Executor
 *
 * Orchestrator Envelope을 받아 AWS EventBridge API를 호출하고
 * Outcome을 반환하는 실행 로직
 *
 * @author ryuqqq
 * @since 1.0
 */
@Component
public class EventBridgeExecutor {

    private final EventBridgeClient eventBridgeClient;
    private final CronExpressionConverter cronConverter;

    public EventBridgeExecutor(
        EventBridgeClient eventBridgeClient,
        CronExpressionConverter cronConverter
    ) {
        this.eventBridgeClient = eventBridgeClient;
        this.cronConverter = cronConverter;
    }

    /**
     * EventBridge 스케줄 생성/수정 실행
     *
     * @param envelope Orchestrator Envelope (Command + Metadata)
     * @return Outcome (Ok/Retry/Fail)
     */
    @CircuitBreaker(name = "eventbridge")
    @Retry(name = "eventbridge", maxAttempts = 0)  // Orchestrator가 재시도 관리
    public Outcome execute(Envelope envelope) {
        try {
            // 1. Payload 파싱
            Payload payload = envelope.command().payload();
            ScheduleRequest request = parsePayload(payload);

            // 2. AWS Cron 표현식 변환
            String ruleName = generateRuleName(request.sellerId());
            String awsCronExpression = cronConverter.toAwsCron(request.cronExpression());

            // 3. EventBridge Rule 생성/수정
            PutRuleRequest putRuleRequest = PutRuleRequest.builder()
                .name(ruleName)
                .scheduleExpression(awsCronExpression)
                .state(RuleState.ENABLED)
                .description("Seller crawl schedule for seller " + request.sellerId())
                .build();

            PutRuleResponse putRuleResponse = eventBridgeClient.putRule(putRuleRequest);

            // 4. Target 설정 (Lambda/SNS/SQS 등)
            setTarget(ruleName, request.sellerId());

            // ✅ 성공 → Outcome.Ok
            return new Outcome.Ok(
                putRuleResponse.ruleArn(),  // 성공 메시지
                Payload.of(String.format("{\"ruleName\": \"%s\", \"ruleArn\": \"%s\"}",
                    ruleName, putRuleResponse.ruleArn()))
            );

        } catch (ThrottlingException e) {
            // ✅ 일시적 오류 → Outcome.Retry
            return new Outcome.Retry(
                Duration.ofSeconds(30),
                "EventBridge throttling: " + e.getMessage()
            );

        } catch (ResourceNotFoundException | AccessDeniedException e) {
            // ✅ 영구적 실패 → Outcome.Fail
            return new Outcome.Fail(
                "EventBridge error: " + e.getMessage(),
                e instanceof AccessDeniedException ? 403 : 404
            );

        } catch (Exception e) {
            // ✅ 예상치 못한 오류 → Outcome.Fail
            return new Outcome.Fail(
                "Unexpected error: " + e.getMessage(),
                500
            );
        }
    }

    /**
     * Payload JSON 파싱
     */
    private ScheduleRequest parsePayload(Payload payload) {
        // JSON 파싱 로직 (ObjectMapper 사용)
        // { "sellerId": 123, "cronExpression": "HOURLY-2" }
    }

    /**
     * Rule 이름 생성
     */
    private String generateRuleName(Long sellerId) {
        return "seller-crawl-schedule-" + sellerId;
    }

    /**
     * EventBridge Target 설정
     */
    private void setTarget(String ruleName, Long sellerId) {
        // Lambda/SNS/SQS ARN 설정
        // PutTargetsRequest 생성 및 호출
    }

    /**
     * Payload 파싱용 DTO
     */
    private record ScheduleRequest(Long sellerId, String cronExpression) {}
}
```

**코딩 컨벤션 체크**:
- [ ] `@Component` 어노테이션
- [ ] `@CircuitBreaker`, `@Retry` (Resilience4j)
- [ ] Outcome 반환 (Ok/Retry/Fail)
- [ ] ❌ `@Transactional` 금지 (Adapter Layer)
- [ ] Javadoc (`@author`, `@since`)

**참고 문서**:
- [Circuit Breaker](docs/coding_convention/07-enterprise-patterns/resilience/01_circuit-breaker.md)
- [Retry and Timeout](docs/coding_convention/07-enterprise-patterns/resilience/02_retry-and-timeout.md)

**진행 상태**: ⬜ TODO
**우선순위**: 🔴 Critical
**비고**: 실제 AWS API 호출이 없으면 기능 동작 불가

---

#### [ ] 5.2 CronExpressionConverter 구현
**파일**: `adapter-out/aws-eventbridge/src/main/java/com/ryuqq/crawlinghub/adapter/out/aws/eventbridge/converter/CronExpressionConverter.java`

**핵심 역할**:
- HOURLY/DAILY/WEEKLY → AWS Cron 표현식 변환

**구현 가이드**:
```java
package com.ryuqq.crawlinghub.adapter.out.aws.eventbridge.converter;

import org.springframework.stereotype.Component;

/**
 * Cron Expression Converter
 *
 * HOURLY/DAILY/WEEKLY를 AWS Cron 형식으로 변환
 *
 * @author ryuqqq
 * @since 1.0
 */
@Component
public class CronExpressionConverter {

    /**
     * AWS Cron 표현식 변환
     *
     * 입력 형식:
     * - HOURLY-2: 2시간마다
     * - DAILY-1: 매일
     * - WEEKLY-1: 매주
     *
     * AWS Cron 형식:
     * - HOURLY-2: cron(0 0/2 * * ? *)
     * - DAILY-1: cron(0 0 0/1 * ? *)
     * - WEEKLY-1: cron(0 0 0 ? * MON/1 *)
     *
     * @param cronExpression HOURLY/DAILY/WEEKLY 형식
     * @return AWS Cron 표현식
     */
    public String toAwsCron(String cronExpression) {
        if (cronExpression == null || cronExpression.isBlank()) {
            throw new IllegalArgumentException("Cron expression must not be blank");
        }

        String[] parts = cronExpression.split("-");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid cron expression format: " + cronExpression);
        }

        String type = parts[0];
        int interval = Integer.parseInt(parts[1]);

        return switch (type) {
            case "HOURLY" -> String.format("cron(0 0/%d * * ? *)", interval);
            case "DAILY" -> String.format("cron(0 0 0/%d * ? *)", interval);
            case "WEEKLY" -> String.format("cron(0 0 0 ? * MON/%d *)", interval);
            default -> throw new IllegalArgumentException("Unknown cron type: " + type);
        };
    }

    /**
     * AWS Cron → HOURLY/DAILY/WEEKLY 역변환
     *
     * @param awsCron AWS Cron 표현식
     * @return HOURLY/DAILY/WEEKLY 형식
     */
    public String fromAwsCron(String awsCron) {
        // 역변환 로직 (필요 시 구현)
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
```

**코딩 컨벤션 체크**:
- [ ] `@Component` 어노테이션
- [ ] Stateless (인스턴스 변수 없음)
- [ ] Javadoc
- [ ] ❌ Lombok 미사용

**진행 상태**: ⬜ TODO
**우선순위**: 🔴 Critical

---

#### [ ] 5.3 EventBridgeConfig 구현
**파일**: `adapter-out/aws-eventbridge/src/main/java/com/ryuqq/crawlinghub/adapter/out/aws/eventbridge/config/EventBridgeConfig.java`

**핵심 역할**:
- AWS SDK EventBridgeClient Bean 생성
- AWS 인증 및 리전 설정

**구현 가이드**:
```java
package com.ryuqq.crawlinghub.adapter.out.aws.eventbridge.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

/**
 * AWS EventBridge Configuration
 *
 * @author ryuqqq
 * @since 1.0
 */
@Configuration
public class EventBridgeConfig {

    @Bean
    public EventBridgeClient eventBridgeClient() {
        return EventBridgeClient.builder()
            .region(Region.AP_NORTHEAST_2)  // 서울 리전
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();
    }
}
```

**진행 상태**: ⬜ TODO
**우선순위**: 🔴 Critical

---

#### [ ] 5.4 Resilience4jConfig 구현
**파일**: `adapter-out/aws-eventbridge/src/main/java/com/ryuqq/crawlinghub/adapter/out/aws/eventbridge/config/Resilience4jConfig.java`

**핵심 역할**:
- Circuit Breaker 설정
- Retry 설정 (Orchestrator가 재시도 관리하므로 비활성화)

**구현 가이드**:
```java
package com.ryuqq.crawlinghub.adapter.out.aws.eventbridge.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Resilience4j Configuration
 *
 * @author ryuqqq
 * @since 1.0
 */
@Configuration
public class Resilience4jConfig {

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
            .failureRateThreshold(50)  // 실패율 50% 이상 시 OPEN
            .waitDurationInOpenState(Duration.ofSeconds(30))
            .permittedNumberOfCallsInHalfOpenState(5)
            .slidingWindowSize(10)
            .build();

        return CircuitBreakerRegistry.of(config);
    }
}
```

**진행 상태**: ⬜ TODO
**우선순위**: 🟡 Important

---

### Phase 6: Orchestrator Config 구현 ❌ 0% (CRITICAL)

#### [ ] 6.1 OrchestratorConfig - Orchestrator Bean 생성
**파일**: `application/src/main/java/com/ryuqq/crawlinghub/application/config/OrchestratorConfig.java`

**핵심 역할**:
- Orchestrator Bean 생성
- Finalizer/Reaper 초기화
- Bus Bean 생성

**구현 가이드**:
```java
package com.ryuqq.crawlinghub.application.config;

import com.ryuqq.orchestrator.adapter.runner.InlineFastPathRunner;
import com.ryuqq.orchestrator.adapter.runner.Finalizer;
import com.ryuqq.orchestrator.adapter.runner.Reaper;
import com.ryuqq.orchestrator.application.orchestrator.Orchestrator;
import com.ryuqq.orchestrator.core.spi.Bus;
import com.ryuqq.orchestrator.core.spi.Store;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.concurrent.Executors;

/**
 * Orchestrator Configuration
 *
 * Orchestrator SDK 초기화 및 백그라운드 프로세스 설정
 *
 * @author ryuqqq
 * @since 1.0
 */
@Configuration
public class OrchestratorConfig {

    private final Store store;
    private final Bus bus;
    private Finalizer finalizer;
    private Reaper reaper;

    public OrchestratorConfig(Store store, Bus bus) {
        this.store = store;
        this.bus = bus;
    }

    /**
     * Orchestrator Bean 생성
     */
    @Bean
    public Orchestrator orchestrator() {
        InlineFastPathRunner runner = new InlineFastPathRunner(
            store,
            bus,
            Executors.newVirtualThreadPerTaskExecutor()  // Java 21 Virtual Thread
        );

        return new Orchestrator(store, runner);
    }

    /**
     * Finalizer Bean 생성
     *
     * Write-Ahead Log (WAL) PENDING 상태를 COMPLETED로 완료
     */
    @Bean
    public Finalizer finalizer() {
        this.finalizer = new Finalizer(
            store,
            bus,
            Executors.newVirtualThreadPerTaskExecutor()
        );
        return this.finalizer;
    }

    /**
     * Reaper Bean 생성
     *
     * 장기 실행 작업 감지 및 상태 조정
     */
    @Bean
    public Reaper reaper() {
        this.reaper = new Reaper(
            store,
            Executors.newVirtualThreadPerTaskExecutor()
        );
        return this.reaper;
    }

    /**
     * Bus Bean 생성 (InMemoryBus)
     */
    @Bean
    public Bus bus() {
        // EventBridgeExecutor를 Bus에 등록
        // bus.register(eventBridgeExecutor);
        return bus;
    }

    /**
     * Finalizer 및 Reaper 시작
     */
    @PostConstruct
    public void startBackgroundProcesses() {
        // Finalizer: 10초마다 WAL PENDING 상태 스캔
        finalizer.start(Duration.ofSeconds(10));

        // Reaper: 1분마다 장기 실행 작업 감지
        reaper.start(Duration.ofMinutes(1));
    }
}
```

**코딩 컨벤션 체크**:
- [ ] `@Configuration` 어노테이션
- [ ] `@Bean` 메서드 정의
- [ ] `@PostConstruct` 초기화 메서드
- [ ] Java 21 Virtual Thread 사용
- [ ] Javadoc

**진행 상태**: ⬜ TODO
**우선순위**: 🔴 Critical
**비고**: Orchestrator 실행에 필수

---

### Phase 7: 테스트 구현 ⬜ 0%

#### [ ] 7.1 Orchestrator TestKit 기반 Contract Test
**파일**: `adapter-out/persistence-mysql/src/test/java/com/ryuqq/crawlinghub/adapter/out/persistence/outbox/adapter/JpaOrchestratorStoreAdapterTest.java`

**핵심 역할**:
- Orchestrator TestKit의 7가지 Contract Test 검증
- Atomicity, Idempotency, Recovery, Redelivery, State Transition, Time Budget, Protection

**구현 가이드**:
```java
package com.ryuqq.crawlinghub.adapter.out.persistence.outbox.adapter;

import com.ryuqq.orchestrator.core.spi.Store;
import com.ryuqq.orchestrator.testkit.StoreContractTest;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

/**
 * JPA Orchestrator Store Adapter Contract Test
 *
 * Orchestrator TestKit의 7가지 Contract Test 검증
 *
 * @author ryuqqq
 * @since 1.0
 */
@DataJpaTest
class JpaOrchestratorStoreAdapterTest extends StoreContractTest {

    @Autowired
    private SellerCrawlScheduleOutboxJpaRepository repository;

    private JpaOrchestratorStoreAdapter adapter;

    @BeforeEach
    void setUp() {
        this.adapter = new JpaOrchestratorStoreAdapter(repository, objectMapper);
    }

    @Override
    protected Store createStore() {
        return adapter;
    }
}
```

**진행 상태**: ⬜ TODO

---

#### [ ] 7.2 LocalStack + Testcontainers 기반 통합 테스트
**파일**: `adapter-out/aws-eventbridge/src/test/java/com/ryuqq/crawlinghub/adapter/out/aws/eventbridge/EventBridgeExecutorIntegrationTest.java`

**핵심 역할**:
- LocalStack으로 AWS EventBridge 모킹
- Testcontainers로 격리된 환경 구성
- E2E 시나리오 검증

**진행 상태**: ⬜ TODO

---

#### [ ] 7.3 ArchUnit 테스트 - 의존성 방향 검증
**파일**: `application/src/test/java/com/ryuqq/crawlinghub/architecture/LayerDependencyTest.java`

**검증 항목**:
- Application Layer는 Adapter Layer에 의존하지 않음
- Adapter Layer는 Application Layer의 Port만 의존
- Domain Layer는 프레임워크 독립적

**진행 상태**: ⬜ TODO

---

### Phase 8: Seller 조회 시 스케줄 상태 조회 ⬜ 0%

#### [ ] 8.1 Seller 상세 조회 응답에 스케줄 상태 추가
**파일**: `adapter-in-rest-api/src/main/java/com/ryuqq/crawlinghub/adapter/in/rest/seller/dto/SellerDetailApiResponse.java`

**추가 필드**:
```java
public record SellerDetailApiResponse(
    Long sellerId,
    String sellerName,
    // ... 기존 필드

    // ✅ 스케줄 등록 상태 추가
    ScheduleStatus scheduleStatus,
    String scheduledCronExpression,
    LocalDateTime lastScheduleUpdateAt
) {
    public record ScheduleStatus(
        String status,           // REGISTERED, PENDING, FAILED, NOT_REGISTERED
        String failureReason,
        Integer retryCount
    ) {}
}
```

**진행 상태**: ⬜ TODO

---

### Phase 9: 최종 검증 및 문서화 ⬜ 0%

#### [ ] 9.1 전체 빌드 및 테스트 실행
**명령어**: `./gradlew clean build`

**검증 항목**:
- [ ] 모든 단위 테스트 통과
- [ ] 모든 통합 테스트 통과
- [ ] ArchUnit 테스트 통과
- [ ] Checkstyle 검증 통과
- [ ] Javadoc 검증 통과

**진행 상태**: ⬜ TODO

---

#### [ ] 9.2 PR 생성 및 문서화
**작업 내용**:
- PR 생성 (feature/CRAW-196 → main)
- PR 설명 작성 (변경 사항, 테스트 결과, 스크린샷)
- Gemini 코드 리뷰 요청

**진행 상태**: ⬜ TODO

---

## 📚 참고 코딩 컨벤션 (자주 확인!)

### Domain Layer
- [Domain Package Guide](docs/coding_convention/02-domain-layer/package-guide/01_domain_package_guide.md)
- [Law of Demeter](docs/coding_convention/02-domain-layer/law-of-demeter/01_getter-chaining-prohibition.md)
- [Aggregate Root Design](docs/coding_convention/02-domain-layer/aggregate-design/02_aggregate-root-design.md)

### Application Layer
- [Application Package Guide](docs/coding_convention/03-application-layer/package-guide/01_application_package_guide.md)
- [Transaction Boundaries](docs/coding_convention/03-application-layer/transaction-management/01_transaction-boundaries.md)
- [Spring Proxy Limitations](docs/coding_convention/03-application-layer/transaction-management/02_spring-proxy-limitations.md)
- [UseCase Inner DTO](docs/coding_convention/03-application-layer/assembler-pattern/02_usecase-inner-dto.md)

### Persistence Layer
- [Persistence Package Guide](docs/coding_convention/04-persistence-layer/package-guide/01_persistence_package_guide.md)
- [Long FK Strategy](docs/coding_convention/04-persistence-layer/jpa-entity-design/01_long-fk-strategy.md)
- [Entity Immutability](docs/coding_convention/04-persistence-layer/jpa-entity-design/02_entity-immutability.md)

### Enterprise Patterns
- [Domain Events](docs/coding_convention/07-enterprise-patterns/event-driven/01_domain-events.md)
- [Circuit Breaker](docs/coding_convention/07-enterprise-patterns/resilience/01_circuit-breaker.md)
- [Retry and Timeout](docs/coding_convention/07-enterprise-patterns/resilience/02_retry-and-timeout.md)

---

## 🚨 Zero-Tolerance 규칙 (절대 위반 금지!)

### Domain Layer
- ❌ Lombok 사용 금지
- ❌ Getter 체이닝 금지 (`order.getCustomer().getAddress().getZip()`)
- ❌ Setter 금지
- ❌ 프레임워크 어노테이션 금지 (`@Entity` 제외)

### Application Layer
- ❌ `@Transactional` 내 외부 API 호출 금지
- ❌ Private/Final 메서드에 `@Transactional` 사용 금지
- ❌ Self-invocation (`this.method()`) 금지

### Persistence Layer
- ❌ JPA 관계 어노테이션 금지 (`@OneToMany`, `@ManyToOne`, `@OneToOne`, `@ManyToMany`)
- ❌ Entity에 비즈니스 메서드 추가 금지
- ❌ Adapter에 `@Transactional` 사용 금지

---

## 🎯 다음 즉시 조치 사항

### 1. 🔴 CRITICAL - JpaOrchestratorStoreAdapter 버그 수정 (즉시)
**파일**: `adapter-out/persistence-mysql/src/main/java/com/ryuqq/crawlinghub/adapter/out/persistence/outbox/adapter/JpaOrchestratorStoreAdapter.java`

**라인**: 257-260

**변경 전**:
```java
import com.ryuqq.orchestrator.core.command.BizKey;
import com.ryuqq.orchestrator.core.command.IdemKey;
```

**변경 후**:
```java
import com.ryuqq.orchestrator.core.model.BizKey;
import com.ryuqq.orchestrator.core.model.IdemKey;
```

### 2. 🔴 CRITICAL - Phase 5 EventBridgeExecutor 구현 (최우선)
- EventBridgeExecutor.java
- CronExpressionConverter.java
- EventBridgeConfig.java
- Resilience4jConfig.java

### 3. 🔴 CRITICAL - Phase 6 OrchestratorConfig 구현 (최우선)
- Orchestrator Bean 생성
- Finalizer/Reaper 초기화
- Bus Bean 생성 및 Executor 등록

### 4. ⬜ OPTIONAL - Phase 4R 아키텍처 리팩토링 (선택)
- Option 1 (권장): Store SPI 직접 사용, Domain Model 제거
- Option 2: 현재 구조 유지

---

## 📝 작업 로그

### 2025-10-27
- TODO 파일 생성
- Orchestrator SDK 분석 완료
- 아키텍처 설계 완료
- Phase 1-4 구현 완료 (Domain, Application, Persistence)
- JpaOrchestratorStoreAdapter 패키지 import 버그 발견
- 아키텍처 리팩토링 방안 수립
- TODO 파일 업데이트 (현재 상태 반영)

---

**다음 작업**:
1. JpaOrchestratorStoreAdapter 버그 수정 (즉시)
2. Phase 5 EventBridgeExecutor 구현
3. Phase 6 OrchestratorConfig 구현
4. Phase 4R 아키텍처 리팩토링 결정 (선택사항)
