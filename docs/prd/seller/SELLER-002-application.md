# SELLER-002: Seller Application Layer 구현

**Epic**: 머스트잇 셀러 크롤링 스케줄러
**Bounded Context**: Seller
**Layer**: Application
**브랜치**: feature/SELLER-002-application
**Jira URL**: (sync-to-jira 후 추가)

---

## 📝 목적

Seller 바운더리 컨텍스트의 UseCase 및 비즈니스 로직 흐름을 Application Layer에서 구현합니다.

**핵심 기능**:
- 셀러 등록
- 셀러 상태 변경 (ACTIVE ↔ INACTIVE)
- 셀러 조회 (단건, 목록)
- Event-Driven 처리 (SellerDeactivatedEvent)

---

## 🎯 요구사항

### Command UseCase

#### RegisterSellerUseCase

- [ ] **Input**: `RegisterSellerCommand(mustItSellerId, sellerName)`
- [ ] **Output**: `SellerResponse(sellerId, mustItSellerId, sellerName, status)`
- [ ] **Transaction**: Yes
- [ ] **비즈니스 로직**:
  1. Seller Aggregate 생성 (Domain)
  2. 중복 검증 (mustItSellerId, sellerName)
     - PersistencePort를 통해 중복 체크
  3. Seller 저장 (PersistencePort)
  4. 트랜잭션 커밋

#### ChangeSellerStatusUseCase

- [ ] **Input**: `ChangeSellerStatusCommand(sellerId, targetStatus)`
- [ ] **Output**: `SellerResponse`
- [ ] **Transaction**: Yes
- [ ] **비즈니스 로직** (ACTIVE → INACTIVE):
  1. Seller 조회 (존재하지 않으면 `SellerNotFoundException`)
  2. ACTIVE 스케줄 존재 여부 확인
     - SchedulerQueryPort를 통해 확인
     - 있으면 예외: `SellerHasActiveSchedulersException`
  3. `Seller.deactivate()` (Domain 메서드)
  4. `SellerDeactivatedEvent` 발행
  5. 트랜잭션 커밋
  6. Event Handler에서 스케줄 비활성화 처리

- [ ] **비즈니스 로직** (INACTIVE → ACTIVE):
  1. Seller 조회
  2. `Seller.activate()` (Domain 메서드)
  3. 트랜잭션 커밋

### Query UseCase

#### GetSellerUseCase

- [ ] **Input**: `GetSellerQuery(sellerId)`
- [ ] **Output**: `SellerDetailResponse`
  - `sellerId`, `mustItSellerId`, `sellerName`, `status`
  - `activeSchedulerCount`, `totalSchedulerCount`
  - `createdAt`, `updatedAt`
- [ ] **Transaction**: ReadOnly
- [ ] **비즈니스 로직**:
  1. Seller 조회 (PersistencePort)
  2. 스케줄러 카운트 조회 (SchedulerQueryPort)
  3. SellerDetailResponse 조립

#### ListSellersUseCase

- [ ] **Input**: `ListSellersQuery(status?, page, size)`
- [ ] **Output**: `PageResponse<SellerSummaryResponse>`
- [ ] **Transaction**: ReadOnly
- [ ] **필터링**: status (ACTIVE/INACTIVE)
- [ ] **페이징**: Offset-based Pagination

### Event Handler

#### SellerDeactivatedEventHandler

- [ ] **Input**: `SellerDeactivatedEvent`
- [ ] **처리 로직**:
  1. 해당 Seller의 모든 ACTIVE 스케줄 조회 (SchedulerQueryPort)
  2. 각 스케줄에 대해 EventBridge 바운더리 컨텍스트의 `DeactivateSchedulerUseCase` 호출
     - 참고: EventBridge 바운더리 컨텍스트에서 Outbox Event 생성
  3. 비동기 처리 (TransactionSynchronization)

### Port 정의 (Out)

#### SellerCommandPort

- [ ] `Seller save(Seller seller)`
- [ ] `void delete(Long sellerId)`

#### SellerQueryPort

- [ ] `Optional<Seller> findById(Long sellerId)`
- [ ] `Optional<Seller> findByMustItSellerId(String mustItSellerId)`
- [ ] `Optional<Seller> findBySellerName(String sellerName)`
- [ ] `boolean existsByMustItSellerId(String mustItSellerId)`
- [ ] `boolean existsBySellerName(String sellerName)`
- [ ] `Page<Seller> findAllByStatus(SellerStatus status, Pageable pageable)`

#### SchedulerQueryPort (EventBridge 바운더리 컨텍스트)

- [ ] `int countActiveSchedulersBySellerId(Long sellerId)`
- [ ] `int countTotalSchedulersBySellerId(Long sellerId)`
- [ ] `List<Scheduler> findActiveSchedulersBySellerId(Long sellerId)`

### Command/Query DTO

#### Command DTO

- [ ] **RegisterSellerCommand**
  ```java
  public record RegisterSellerCommand(
      String mustItSellerId,
      String sellerName
  ) {}
  ```

- [ ] **ChangeSellerStatusCommand**
  ```java
  public record ChangeSellerStatusCommand(
      Long sellerId,
      SellerStatus targetStatus
  ) {}
  ```

#### Query DTO

- [ ] **GetSellerQuery**
  ```java
  public record GetSellerQuery(Long sellerId) {}
  ```

- [ ] **ListSellersQuery**
  ```java
  public record ListSellersQuery(
      SellerStatus status,
      int page,
      int size
  ) {}
  ```

#### Response DTO

- [ ] **SellerResponse**
  ```java
  public record SellerResponse(
      Long sellerId,
      String mustItSellerId,
      String sellerName,
      SellerStatus status,
      LocalDateTime createdAt
  ) {}
  ```

- [ ] **SellerDetailResponse**
  ```java
  public record SellerDetailResponse(
      Long sellerId,
      String mustItSellerId,
      String sellerName,
      SellerStatus status,
      Integer activeSchedulerCount,
      Integer totalSchedulerCount,
      LocalDateTime createdAt,
      LocalDateTime updatedAt
  ) {}
  ```

- [ ] **SellerSummaryResponse**
  ```java
  public record SellerSummaryResponse(
      Long sellerId,
      String mustItSellerId,
      String sellerName,
      SellerStatus status,
      Integer totalSchedulerCount
  ) {}
  ```

---

## ⚠️ 제약사항

### Zero-Tolerance 규칙

- [ ] **Command/Query 분리 (CQRS)**
  - Command UseCase: 상태 변경 (Transaction 필수)
  - Query UseCase: 조회만 (ReadOnly Transaction)

- [ ] **Transaction 경계 엄격 관리**
  - UseCase 단위로 Transaction 설정
  - `@Transactional` 내 외부 API 호출 절대 금지
  - Event Handler는 별도 Transaction

- [ ] **Spring Proxy 제약사항 준수 (중요!)**
  - ⚠️ **다음 경우 `@Transactional`이 작동하지 않습니다:**
    - Private 메서드에 `@Transactional` 적용 (무시됨)
    - Final 클래스/메서드에 `@Transactional` 적용 (Proxy 생성 불가)
    - 같은 클래스 내부 호출 (`this.method()`) - Proxy 우회
  - ✅ **해결책**:
    - Transaction이 필요한 메서드는 **public**으로 선언
    - 클래스/메서드를 **final로 선언하지 않음**
    - 같은 클래스 내부 호출이 필요하면 **별도 Component로 분리**

- [ ] **Port 의존성 역전**
  - Application Layer는 Port 인터페이스만 의존
  - 구현체는 Adapter Layer에서 제공

- [ ] **Assembler 패턴 사용**
  - Domain ↔ DTO 변환은 Assembler에서 처리
  - UseCase는 비즈니스 로직에만 집중

### 테스트 규칙

- [ ] **ArchUnit 테스트 필수**
  - Application Layer는 Domain Layer만 의존
  - Adapter Layer 의존 금지
  - Port 인터페이스만 사용

- [ ] **TestFixture 사용 필수**
  - Command/Query DTO 생성 시 TestFixture 사용

- [ ] **Mock Port 사용**
  - Unit Test 시 Port는 Mock 객체 사용
  - 예시: `@Mock SellerCommandPort`, `@Mock SellerQueryPort`

- [ ] **테스트 커버리지 > 80%**

---

## ✅ 완료 조건

- [ ] Command UseCase 구현 완료
  - RegisterSellerUseCase
  - ChangeSellerStatusUseCase

- [ ] Query UseCase 구현 완료
  - GetSellerUseCase
  - ListSellersUseCase

- [ ] Event Handler 구현 완료
  - SellerDeactivatedEventHandler

- [ ] Port 인터페이스 정의 완료
  - SellerCommandPort
  - SellerQueryPort
  - SchedulerQueryPort (EventBridge 바운더리 컨텍스트)

- [ ] Command/Query DTO 구현 완료

- [ ] Assembler 구현 완료
  - SellerAssembler (Domain ↔ DTO 변환)

- [ ] Application Unit Test 완료
  - UseCase 테스트 (Mock Port)
  - Event Handler 테스트

- [ ] ArchUnit 테스트 완료
  - Application Layer 의존성 검증

- [ ] Zero-Tolerance 규칙 준수 확인

- [ ] 코드 리뷰 승인

- [ ] PR 머지 완료

---

## 📁 패키지 구조 예시

```
application/
└─ seller/                        # Bounded Context
   ├─ assembler/
   │  └─ SellerAssembler.java
   ├─ dto/
   │  ├─ command/
   │  │   ├─ RegisterSellerCommand.java
   │  │   └─ ChangeSellerStatusCommand.java
   │  ├─ query/
   │  │   ├─ GetSellerQuery.java
   │  │   └─ ListSellersQuery.java
   │  └─ response/
   │      ├─ SellerResponse.java
   │      ├─ SellerDetailResponse.java
   │      └─ SellerSummaryResponse.java
   ├─ facade/
   │  └─ SellerFacade.java         # 여러 UseCase 조합 (선택적)
   ├─ manager/
   │  └─ SellerTransactionManager.java  # 단일 Port 트랜잭션 처리
   ├─ port/
   │  ├─ in/
   │  │   ├─ command/
   │  │   │   ├─ RegisterSellerUseCase.java
   │  │   │   └─ ChangeSellerStatusUseCase.java
   │  │   └─ query/
   │  │       ├─ GetSellerUseCase.java
   │  │       └─ ListSellersUseCase.java
   │  └─ out/
   │      ├─ command/
   │      │   └─ SellerCommandPort.java
   │      └─ query/
   │          ├─ SellerQueryPort.java
   │          └─ SchedulerQueryPort.java
   ├─ service/
   │  ├─ command/
   │  │   ├─ RegisterSellerService.java
   │  │   └─ ChangeSellerStatusService.java
   │  └─ query/
   │      ├─ GetSellerService.java
   │      └─ ListSellersService.java
   └─ listener/
      └─ SellerDeactivatedEventHandler.java
```

**패키지 분리 원칙**:
- `service/command/`: 상태 변경 UseCase 구현체
- `service/query/`: 조회 UseCase 구현체 (ReadOnly)
- `facade/`: 여러 Transaction Manager 조합 (복잡한 흐름)
- `manager/`: 단일 Port 트랜잭션 처리 (단순 흐름)

---

## 🔗 관련 문서

- **PRD**: `/Users/sangwon-ryu/crawlinghub/docs/prd/mustit-seller-crawler-scheduler.md`
- **Plan**: `docs/prd/seller/plans/SELLER-002-application-plan.md` (create-plan 후 생성)
- **Jira**: (sync-to-jira 후 추가)
- **코딩 규칙**: `docs/coding_convention/03-application-layer/`
- **선행 Task**: SELLER-001 (Domain Layer)

---

## 📋 다음 단계

1. `/create-plan SELLER-002` - TDD Plan 생성
2. `/kb/application/go` - Application Layer TDD 시작
