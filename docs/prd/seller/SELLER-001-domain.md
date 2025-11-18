# SELLER-001: Seller Domain Layer 구현

**Epic**: 머스트잇 셀러 크롤링 스케줄러
**Bounded Context**: Seller
**Layer**: Domain
**브랜치**: feature/SELLER-001-domain
**Jira URL**: (sync-to-jira 후 추가)

---

## 📝 목적

Seller 바운더리 컨텍스트의 핵심 비즈니스 로직을 Domain Layer에서 구현합니다.

**핵심 도메인 개념**:
- 셀러 등록 및 상태 관리
- 셀러 비활성화 시 스케줄러 제약 검증
- Domain Event 발행 (Event-Driven)

---

## 🎯 요구사항

### Aggregate Root: Seller

- [ ] **Seller Aggregate 구현**
  - `sellerId`: Long (PK)
  - `mustItSellerId`: String (머스트잇 노출 ID, Immutable)
  - `sellerName`: String (Immutable)
  - `status`: SellerStatus (Enum: ACTIVE, INACTIVE)
  - `createdAt`, `updatedAt`: LocalDateTime

- [ ] **생성 메서드 패턴 (3종)**
  - `forNew(mustItSellerId, sellerName)`: 새로운 Seller 생성 (등록 시)
  - `of(sellerId, mustItSellerId, sellerName, status)`: 기존 값으로 생성 (조회 후 재구성)
  - `reconstitute(sellerId, mustItSellerId, sellerName, status, createdAt, updatedAt)`: 영속성 계층에서 완전 재구성

### Value Objects

- [ ] **SellerStatus Enum**
  - `ACTIVE`: 활성 상태
  - `INACTIVE`: 비활성 상태

### 비즈니스 규칙

#### 셀러 등록 (Register Seller)

- [ ] **중복 검증**
  - `mustItSellerId` Unique 제약 (중복 시 `DuplicateMustItSellerIdException`)
  - `sellerName` Unique 제약 (중복 시 `DuplicateSellerNameException`)

- [ ] **Immutable 속성**
  - `mustItSellerId` 변경 불가
  - `sellerName` 변경 불가

- [ ] **초기 상태**
  - 등록 시 기본 상태: `ACTIVE`

#### 셀러 상태 변경 (Change Status)

- [ ] **ACTIVE → INACTIVE 전환**
  - 전제 조건: 해당 셀러의 모든 스케줄이 INACTIVE 상태여야 함
  - ACTIVE 스케줄이 1개라도 있으면 예외 발생: `SellerHasActiveSchedulersException`

- [ ] **Domain Event 발행**
  - 비활성화 시 `SellerDeactivatedEvent` 발행
  - Event 속성: `sellerId`, `occurredAt`

- [ ] **INACTIVE → ACTIVE 재활성화**
  - 재활성화 가능
  - 스케줄은 수동 개별 활성화 필요 (자동 활성화 X)

### Domain Events

- [ ] **SellerDeactivatedEvent**
  ```java
  public record SellerDeactivatedEvent(
      Long sellerId,
      LocalDateTime occurredAt
  ) {}
  ```

### Domain Exceptions

- [ ] **DuplicateMustItSellerIdException**
  - 메시지: "이미 등록된 머스트잇 셀러 ID입니다."

- [ ] **DuplicateSellerNameException**
  - 메시지: "이미 등록된 셀러 이름입니다."

- [ ] **SellerHasActiveSchedulersException**
  - 메시지: "활성 상태의 스케줄러가 존재하여 셀러를 비활성화할 수 없습니다."

- [ ] **SellerNotFoundException**
  - 메시지: "존재하지 않는 셀러입니다."

---

## ⚠️ 제약사항

### Zero-Tolerance 규칙

- [ ] **Lombok 금지**
  - Pure Java 또는 Record 사용
  - Getter/Setter 수동 구현 또는 Record 활용

- [ ] **Law of Demeter 준수**
  - Getter 체이닝 금지
  - 예시:
    - ✅ `seller.getStatus()`
    - ❌ `seller.getSchedulers().getStatus()` (JPA 관계 어노테이션 자체가 금지)

- [ ] **Long FK 전략**
  - JPA 관계 어노테이션 금지 (`@OneToMany`, `@ManyToOne` 사용 금지)
  - 외래 키는 Long 타입으로만 관리

- [ ] **Tell Don't Ask 패턴**
  - Getter로 상태 확인 후 외부에서 로직 수행 금지
  - 예시:
    - ❌ `if (seller.getStatus() == ACTIVE) { seller.setStatus(INACTIVE); }`
    - ✅ `seller.deactivate()`

- [ ] **캡슐화 철저**
  - 내부 상태 보호, 외부에서 판단·계산 금지
  - 도메인이 스스로 결정하도록 설계
  - 예시:
    - ❌ 외부에서 "활성 스케줄 개수 확인 후 비활성화 결정"
    - ✅ `seller.canDeactivate(activeSchedulerCount)` 내부에서 판단

- [ ] **불변성 우선**
  - 상태 변경은 명시적 비즈니스 메서드로만 (`deactivate()`, `activate()`)
  - Setter 절대 금지
  - Immutable 필드는 생성 시에만 할당 (mustItSellerId, sellerName)

### 테스트 규칙

- [ ] **ArchUnit 테스트 필수**
  - Domain Layer는 다른 레이어 의존 금지
  - Domain 패키지는 순수 비즈니스 로직만 포함

- [ ] **TestFixture 사용 필수**
  - Seller 테스트 데이터 생성 시 TestFixture 패턴 사용
  - 예시: `SellerFixture.createActive()`, `SellerFixture.createInactive()`

- [ ] **테스트 커버리지 > 80%**
  - 비즈니스 로직 핵심 케이스 모두 테스트

---

## ✅ 완료 조건

- [ ] Seller Aggregate 구현 완료
  - 셀러 등록 (register)
  - 상태 변경 (deactivate, activate)
  - Domain Event 발행

- [ ] Value Object 구현 완료
  - SellerStatus Enum

- [ ] Domain Exception 구현 완료
  - DuplicateMustItSellerIdException
  - DuplicateSellerNameException
  - SellerHasActiveSchedulersException
  - SellerNotFoundException

- [ ] Domain Unit Test 완료
  - 셀러 등록 테스트 (중복 검증)
  - 상태 변경 테스트 (ACTIVE ↔ INACTIVE)
  - Domain Event 발행 테스트
  - TestFixture 패턴 적용

- [ ] ArchUnit 테스트 완료
  - Domain Layer 의존성 검증

- [ ] Zero-Tolerance 규칙 준수 확인
  - Lombok 미사용
  - Law of Demeter 준수
  - Long FK 전략 준수

- [ ] 코드 리뷰 승인

- [ ] PR 머지 완료

---

## 📁 패키지 구조 예시

```
domain/
└─ seller/                        # Bounded Context
   ├─ aggregate/
   │  └─ seller/                  # Aggregate Root 이름 (소문자)
   │     └─ Seller.java           # Aggregate Root
   ├─ vo/
   │  └─ SellerStatus.java        # Value Object (Enum)
   ├─ event/
   │  └─ SellerDeactivatedEvent.java
   └─ exception/
      ├─ DuplicateMustItSellerIdException.java
      ├─ DuplicateSellerNameException.java
      ├─ SellerHasActiveSchedulersException.java
      └─ SellerNotFoundException.java
```

---

## 🔗 관련 문서

- **PRD**: `/Users/sangwon-ryu/crawlinghub/docs/prd/mustit-seller-crawler-scheduler.md`
- **Plan**: `docs/prd/seller/plans/SELLER-001-domain-plan.md` (create-plan 후 생성)
- **Jira**: (sync-to-jira 후 추가)
- **코딩 규칙**: `docs/coding_convention/02-domain-layer/`

---

## 📋 다음 단계

1. `/create-plan SELLER-001` - TDD Plan 생성
2. `/kb/domain/go` - Domain Layer TDD 시작
